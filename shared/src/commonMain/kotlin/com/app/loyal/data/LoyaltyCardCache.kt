package com.app.loyal.data

import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import com.app.loyal.util.KeyValueStore
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Copia locale di una tessera. Le date sono stringhe ISO come nelle righe di
 * Supabase, così non dipendiamo dai serializzatori di Instant.
 */
@Serializable
private data class CachedCard(
    val id: String,
    val brandName: String,
    val domain: String,
    val logoUrl: String? = null,
    val code: String,
    val format: String,
    val colorArgb: Long,
    val label: String? = null,
    val note: String? = null,
    val createdAt: String,
    val usageCount: Int = 0,
    val lastViewedAt: String? = null,
    @SerialName("isFavorite") val isFavorite: Boolean = false
)

private fun CachedCard.toDomain() = LoyaltyCard(
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

private fun LoyaltyCard.toCached() = CachedCard(
    id = id,
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
 * Cache offline delle tessere di un utente. Serve a mostrare i codici a barre
 * anche senza rete — tipicamente in cassa, dove il segnale è pessimo.
 * La chiave include lo user id: su un dispositivo condiviso ognuno vede le sue.
 */
class LoyaltyCardCache(
    private val store: KeyValueStore,
    userId: String
) {
    private val key = "cards_$userId"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Tessere salvate localmente; lista vuota se assenti o illeggibili. */
    fun load(): List<LoyaltyCard> {
        val raw = store.getString(key) ?: return emptyList()
        // Una cache corrotta o di un formato più vecchio non deve impedire
        // l'avvio: la si ignora e verrà riscritta al primo refresh riuscito.
        return runCatching {
            json.decodeFromString<List<CachedCard>>(raw).map { it.toDomain() }
        }.getOrElse { emptyList() }
    }

    fun save(cards: List<LoyaltyCard>) {
        runCatching {
            store.putString(key, json.encodeToString(cards.map { it.toCached() }))
        }
    }

    fun clear() {
        store.remove(key)
    }
}
