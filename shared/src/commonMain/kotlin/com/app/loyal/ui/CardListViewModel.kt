package com.app.loyal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.loyal.data.LoyaltyCardRepository
import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    private val repository: LoyaltyCardRepository
) : ViewModel() {

    private val sortOrderFlow = MutableStateFlow(CardSortOrder.Alphabetical)
    val sortOrder: StateFlow<CardSortOrder> = sortOrderFlow

    val cards: StateFlow<List<LoyaltyCard>> = combine(
        repository.observeCards(),
        sortOrderFlow
    ) { cards, sortOrder ->
        // I preferiti non vengono più messi in cima: hanno la loro sezione nella home.
        when (sortOrder) {
            CardSortOrder.Alphabetical -> cards.sortedBy { it.brandName.lowercase() }
            CardSortOrder.MostUsed -> cards.sortedByDescending { it.usageCount }
            CardSortOrder.RecentlyViewed -> cards.sortedByDescending { it.lastViewedAt ?: Instant.DISTANT_PAST }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Conteggio dei preferiti, tenuto sempre aggiornato ([SharingStarted.Eagerly]):
     * il limite va verificato anche dal dettaglio carta, dove [cards] non ha collector.
     */
    private val favoriteCount: StateFlow<Int> = repository.observeCards()
        .map { cards -> cards.count { it.isFavorite } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    fun setSortOrder(sortOrder: CardSortOrder) {
        sortOrderFlow.value = sortOrder
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
        if (!card.isFavorite && favoriteCount.value >= MAX_FAVORITE_CARDS) return false
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
