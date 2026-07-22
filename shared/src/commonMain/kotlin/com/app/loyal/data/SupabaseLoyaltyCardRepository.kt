package com.app.loyal.data

import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 *
 * Ogni scrittura viene applicata subito in locale e messa in [outbox]; la coda
 * viene rigiocata prima di ogni lettura dal server, così una modifica fatta
 * offline non viene mai sovrascritta dai dati remoti. Nessun metodo lancia
 * eccezioni per problemi di rete: un errore accende [syncFailed].
 */
class SupabaseLoyaltyCardRepository(
    private val supabase: SupabaseClient,
    private val userId: String,
    private val cache: LoyaltyCardCache,
    private val outbox: PendingOperationQueue
) : LoyaltyCardRepository {

    private val table = supabase.from("loyalty_cards")

    // Partiamo dalle tessere su disco: all'avvio sono già a schermo, senza attese.
    private val cards = MutableStateFlow(cache.load())

    private val syncFailedFlow = MutableStateFlow(false)
    override val syncFailed: StateFlow<Boolean> = syncFailedFlow

    private val pendingChangesFlow = MutableStateFlow(outbox.size)
    override val pendingChanges: StateFlow<Int> = pendingChangesFlow

    // Serializza gli accessi: la coda non è thread-safe e due sincronizzazioni
    // in parallelo rigiocherebbero le stesse operazioni.
    private val mutex = Mutex()

    override fun observeCards(): StateFlow<List<LoyaltyCard>> = cards

    /** Svuota la coda e riallinea la copia locale. Senza rete tiene la cache. */
    suspend fun refresh() {
        mutex.withLock { synchronize() }
    }

    override suspend fun add(card: LoyaltyCard) {
        enqueue(PendingOperation.Add(card.toCached())) { it + card }
    }

    override suspend fun update(card: LoyaltyCard) {
        enqueue(PendingOperation.Update(card.toCached())) { cards ->
            cards.map { if (it.id == card.id) card else it }
        }
    }

    override suspend fun delete(id: String) {
        enqueue(PendingOperation.Delete(id)) { cards -> cards.filterNot { it.id == id } }
    }

    override suspend fun recordView(id: String) {
        val viewedAt = Clock.System.now()
        val newCount = (cards.value.firstOrNull { it.id == id }?.usageCount ?: 0) + 1
        val operation = PendingOperation.RecordView(
            cardId = id,
            usageCount = newCount,
            lastViewedAt = viewedAt.toString()
        )
        enqueue(operation) { cards ->
            cards.map {
                if (it.id == id) it.copy(usageCount = newCount, lastViewedAt = viewedAt) else it
            }
        }
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        enqueue(PendingOperation.SetFavorite(id, favorite)) { cards ->
            cards.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
        }
    }

    /**
     * Applica [transform] alla copia locale, accoda [operation] e prova subito a
     * sincronizzare. La UI è già aggiornata quando questa funzione ritorna.
     */
    private suspend fun enqueue(
        operation: PendingOperation,
        transform: (List<LoyaltyCard>) -> List<LoyaltyCard>
    ) {
        mutex.withLock {
            setLocal(transform(cards.value))
            outbox.enqueue(operation)
            pendingChangesFlow.value = outbox.size
            synchronize()
        }
    }

    /** Esito dello svuotamento della coda. */
    private enum class FlushResult {
        /** Coda svuotata, tutto accettato dal server. */
        Drained,

        /** Coda svuotata ma il server ha rifiutato qualche operazione. */
        Rejected,

        /** Interrotta per mancanza di rete: restano operazioni in coda. */
        Offline
    }

    /** Da chiamare già dentro [mutex]: svuota la coda e poi rilegge dal server. */
    private suspend fun synchronize() {
        val flush = flushPending()
        if (flush == FlushResult.Offline) {
            syncFailedFlow.value = true
            return
        }
        // Anche dopo un rifiuto rileggiamo: così lo stato locale torna ad
        // allinearsi al server invece di restare con una modifica mai accettata.
        runCatching { table.select().decodeList<LoyaltyCardRow>().map { it.toDomain() } }
            .onSuccess { remote ->
                setLocal(remote)
                syncFailedFlow.value = flush == FlushResult.Rejected
            }
            .onFailure { syncFailedFlow.value = true }
    }

    /** Rigioca la coda in ordine, fermandosi al primo errore di rete. */
    private suspend fun flushPending(): FlushResult {
        var result = FlushResult.Drained
        while (true) {
            val operation = outbox.first() ?: break
            val outcome = runCatching { execute(operation) }
            when {
                outcome.isSuccess -> outbox.removeFirst()

                // Il server ha risposto con un errore: rigiocare l'operazione
                // darebbe lo stesso esito e bloccherebbe la coda per sempre.
                // La scartiamo e proseguiamo, segnalando il problema.
                outcome.exceptionOrNull() is RestException -> {
                    outbox.removeFirst()
                    result = FlushResult.Rejected
                }

                // Errore di rete: riproveremo più tardi, nell'ordine originale.
                else -> {
                    pendingChangesFlow.value = outbox.size
                    return FlushResult.Offline
                }
            }
        }
        pendingChangesFlow.value = outbox.size
        return result
    }

    private suspend fun execute(operation: PendingOperation) {
        when (operation) {
            is PendingOperation.Add ->
                table.insert(operation.card.toDomain().toRow(userId))

            is PendingOperation.Update ->
                table.upsert(operation.card.toDomain().toRow(userId))

            is PendingOperation.Delete ->
                table.delete {
                    filter { eq("id", operation.cardId) }
                }

            is PendingOperation.SetFavorite ->
                table.update(LoyaltyCardFavoriteUpdate(isFavorite = operation.favorite)) {
                    filter { eq("id", operation.cardId) }
                }

            is PendingOperation.RecordView ->
                table.update(
                    LoyaltyCardUsageUpdate(
                        usageCount = operation.usageCount,
                        lastViewedAt = operation.lastViewedAt
                    )
                ) {
                    filter { eq("id", operation.cardId) }
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
}
