package com.app.loyal.data

import com.app.loyal.model.LoyaltyCard
import com.app.loyal.util.KeyValueStore
import kotlinx.serialization.json.Json

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
