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

    /**
     * true dall'ultimo errore di sincronizzazione fino alla prima riuscita.
     * Resta true per tutta la sessione offline, senza riemettere a ogni
     * tentativo fallito: la UI può mostrare un solo avviso per episodio.
     */
    val syncFailed: StateFlow<Boolean>

    /** Numero di modifiche applicate in locale e non ancora sincronizzate. */
    val pendingChanges: StateFlow<Int>
}
