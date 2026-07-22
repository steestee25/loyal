package com.app.loyal.data

import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class LoyaltyCardRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("brand_name") val brandName: String,
    val domain: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    val code: String,
    val format: String,
    @SerialName("color_argb") val colorArgb: Long,
    val label: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("usage_count") val usageCount: Int = 0,
    @SerialName("last_viewed_at") val lastViewedAt: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false
)

@Serializable
private data class LoyaltyCardUsageUpdate(
    @SerialName("usage_count") val usageCount: Int,
    @SerialName("last_viewed_at") val lastViewedAt: String
)

@Serializable
private data class LoyaltyCardFavoriteUpdate(
    @SerialName("is_favorite") val isFavorite: Boolean
)

private fun LoyaltyCardRow.toDomain() = LoyaltyCard(
    id = id,
    brandName = brandName,
    domain = domain,
    logoUrl = logoUrl,
    code = code,
    format = BarcodeFormat.valueOf(format),
    colorArgb = colorArgb,
    label = label,
    note = note,
    createdAt = Instant.parse(createdAt),
    usageCount = usageCount,
    lastViewedAt = lastViewedAt?.let { Instant.parse(it) },
    isFavorite = isFavorite
)

private fun LoyaltyCard.toRow(userId: String) = LoyaltyCardRow(
    id = id,
    userId = userId,
    brandName = brandName,
    domain = domain,
    logoUrl = logoUrl,
    code = code,
    format = format.name,
    colorArgb = colorArgb,
    label = label,
    note = note,
    createdAt = createdAt.toString(),
    usageCount = usageCount,
    lastViewedAt = lastViewedAt?.toString(),
    isFavorite = isFavorite
)

/**
 * Repository offline-first: la UI legge sempre dalla copia locale, che parte
 * dalla [cache] su disco e viene riallineata a Supabase quando la rete c'è.
 * Nessun metodo lancia eccezioni per problemi di rete: un errore accende
 * [syncFailed], così le tessere restano consultabili anche offline.
 */
class SupabaseLoyaltyCardRepository(
    private val supabase: SupabaseClient,
    private val userId: String,
    private val cache: LoyaltyCardCache
) : LoyaltyCardRepository {

    private val table = supabase.from("loyalty_cards")

    // Partiamo dalle tessere su disco: all'avvio sono già a schermo, senza attese.
    private val cards = MutableStateFlow(cache.load())

    private val syncFailedFlow = MutableStateFlow(false)
    override val syncFailed: StateFlow<Boolean> = syncFailedFlow

    override fun observeCards(): StateFlow<List<LoyaltyCard>> = cards

    override fun clearSyncFailed() {
        syncFailedFlow.value = false
    }

    /** Riallinea la copia locale a Supabase. Se la rete manca, tiene la cache. */
    suspend fun refresh() {
        runCatching { table.select().decodeList<LoyaltyCardRow>().map { it.toDomain() } }
            .onSuccess { remote ->
                setLocal(remote)
                syncFailedFlow.value = false
            }
            .onFailure { syncFailedFlow.value = true }
    }

    override suspend fun add(card: LoyaltyCard) {
        setLocal(cards.value + card)
        sync { table.insert(card.toRow(userId)) }
    }

    override suspend fun update(card: LoyaltyCard) {
        setLocal(cards.value.map { if (it.id == card.id) card else it })
        sync { table.upsert(card.toRow(userId)) }
    }

    override suspend fun delete(id: String) {
        setLocal(cards.value.filterNot { it.id == id })
        sync {
            table.delete {
                filter { eq("id", id) }
            }
        }
    }

    override suspend fun recordView(id: String) {
        val viewedAt = Clock.System.now()
        val newCount = (cards.value.firstOrNull { it.id == id }?.usageCount ?: 0) + 1
        setLocal(
            cards.value.map {
                if (it.id == id) it.copy(usageCount = newCount, lastViewedAt = viewedAt) else it
            }
        )
        sync {
            table.update(
                LoyaltyCardUsageUpdate(
                    usageCount = newCount,
                    lastViewedAt = viewedAt.toString()
                )
            ) {
                filter { eq("id", id) }
            }
        }
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        setLocal(cards.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it })
        sync {
            table.update(LoyaltyCardFavoriteUpdate(isFavorite = favorite)) {
                filter { eq("id", id) }
            }
        }
    }

    /**
     * Applica subito la modifica in locale (UI reattiva) e la persiste su disco,
     * così sopravvive anche se l'app viene chiusa prima della sincronizzazione.
     */
    private fun setLocal(updated: List<LoyaltyCard>) {
        cards.value = updated
        cache.save(updated)
    }

    /**
     * Esegue la scrittura remota senza propagare gli errori: la modifica locale
     * è già stata applicata. In caso di esito positivo riallinea dal server.
     */
    private suspend fun sync(write: suspend () -> Unit) {
        runCatching { write() }
            .onSuccess { refresh() }
            .onFailure { syncFailedFlow.value = true }
    }
}
