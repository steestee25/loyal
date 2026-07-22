package com.app.loyal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.loyal.data.AppPreferences
import com.app.loyal.data.LoyaltyCardRepository
import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

enum class CardSortOrder {
    Alphabetical,
    MostUsed,
    RecentlyViewed
}

/** Massimo di tessere preferite: la home ne mostra 2 per riga su 2 righe. */
const val MAX_FAVORITE_CARDS = 4

class CardListViewModel(
    private val repository: LoyaltyCardRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val storedCards = repository.observeCards()

    // L'ordinamento riparte da quello scelto l'ultima volta.
    private val sortOrderFlow = MutableStateFlow(preferences.sortOrder)
    val sortOrder: StateFlow<CardSortOrder> = sortOrderFlow

    val syncFailed: StateFlow<Boolean> = repository.syncFailed

    val cards: StateFlow<List<LoyaltyCard>> = combine(
        storedCards,
        sortOrderFlow
    ) { cards, sortOrder ->
        sorted(cards, sortOrder)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        // Le tessere in cache sono già disponibili: partiamo da quelle.
        initialValue = sorted(storedCards.value, sortOrderFlow.value)
    )

    /** I preferiti non vanno in cima: hanno la loro sezione nella home. */
    private fun sorted(cards: List<LoyaltyCard>, sortOrder: CardSortOrder) = when (sortOrder) {
        CardSortOrder.Alphabetical -> cards.sortedBy { it.brandName.lowercase() }
        CardSortOrder.MostUsed -> cards.sortedByDescending { it.usageCount }
        CardSortOrder.RecentlyViewed ->
            cards.sortedByDescending { it.lastViewedAt ?: Instant.DISTANT_PAST }
    }

    fun setSortOrder(sortOrder: CardSortOrder) {
        sortOrderFlow.value = sortOrder
        preferences.sortOrder = sortOrder
    }

    fun clearSyncFailed() {
        repository.clearSyncFailed()
    }

    fun recordView(id: String) {
        viewModelScope.launch {
            repository.recordView(id)
        }
    }

    /**
     * Aggiunge o toglie [card] dai preferiti.
     * Ritorna false — senza modificare nulla — se si sta cercando di aggiungere
     * un preferito oltre [MAX_FAVORITE_CARDS]; rimuovere è sempre permesso.
     */
    fun toggleFavorite(card: LoyaltyCard): Boolean {
        val favorites = storedCards.value.count { it.isFavorite }
        if (!card.isFavorite && favorites >= MAX_FAVORITE_CARDS) return false
        viewModelScope.launch {
            repository.setFavorite(card.id, !card.isFavorite)
        }
        return true
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
