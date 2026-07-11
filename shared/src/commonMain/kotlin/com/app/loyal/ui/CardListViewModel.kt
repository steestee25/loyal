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

    fun setSortOrder(sortOrder: CardSortOrder) {
        sortOrderFlow.value = sortOrder
    }

    fun recordView(id: String) {
        viewModelScope.launch {
            repository.recordView(id)
        }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
