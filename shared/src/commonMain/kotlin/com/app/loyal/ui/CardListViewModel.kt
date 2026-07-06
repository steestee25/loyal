package com.app.loyal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.loyal.data.LoyaltyCardRepository
import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardListViewModel(
    private val repository: LoyaltyCardRepository
) : ViewModel() {

    val cards: StateFlow<List<LoyaltyCard>> = repository.observeCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCard(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
