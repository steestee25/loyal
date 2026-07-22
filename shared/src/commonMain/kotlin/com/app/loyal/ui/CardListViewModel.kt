package com.app.loyal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class CardListViewModel(
    private val repository: LoyaltyCardRepository
) : ViewModel() {

    private val sortOrderFlow = MutableStateFlow(CardSortOrder.Alphabetical)
    val sortOrder: StateFlow<CardSortOrder> = sortOrderFlow

    val cards: StateFlow<List<LoyaltyCard>> = combine(
        repository.observeCards(),
        sortOrderFlow
    ) { cards, sortOrder ->
        val comparator = when (sortOrder) {
            CardSortOrder.Alphabetical -> compareBy<LoyaltyCard> { it.brandName.lowercase() }
            CardSortOrder.MostUsed -> compareByDescending { it.usageCount }
            CardSortOrder.RecentlyViewed -> compareByDescending { it.lastViewedAt ?: Instant.DISTANT_PAST }
        }
        // I preferiti restano sempre in cima, ordinati fra loro come il resto della lista.
        cards.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.then(comparator))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSortOrder(sortOrder: CardSortOrder) {
        sortOrderFlow.value = sortOrder
    }

    fun recordView(id: String) {
        viewModelScope.launch {
            repository.recordView(id)
        }
    }

    fun toggleFavorite(card: LoyaltyCard) {
        viewModelScope.launch {
            repository.setFavorite(card.id, !card.isFavorite)
        }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
