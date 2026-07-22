package com.app.loyal.data

import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.StateFlow

interface LoyaltyCardRepository {
    /**
     * StateFlow e non Flow: il valore corrente serve subito e in modo sincrono,
     * per evitare che all'avvio la home lampeggi sullo stato "nessuna tessera"
     * prima che arrivi la cache.
     */
    fun observeCards(): StateFlow<List<LoyaltyCard>>
    suspend fun add(card: LoyaltyCard)
    suspend fun update(card: LoyaltyCard)
    suspend fun delete(id: String)
    suspend fun recordView(id: String)
    suspend fun setFavorite(id: String, favorite: Boolean)

    /** true quando l'ultima operazione non ha raggiunto il server. */
    val syncFailed: StateFlow<Boolean>

    /** Da chiamare dopo aver mostrato l'avviso di mancata sincronizzazione. */
    fun clearSyncFailed()
}
