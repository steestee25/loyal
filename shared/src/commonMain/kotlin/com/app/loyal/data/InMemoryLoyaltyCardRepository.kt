package com.app.loyal.data


import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemoryLoyaltyCardRepository : LoyaltyCardRepository {

    private val cards = MutableStateFlow<List<LoyaltyCard>>(emptyList())

    override fun observeCards(): StateFlow<List<LoyaltyCard>> = cards

    override suspend fun add(card: LoyaltyCard) {
        cards.value = cards.value + card
    }

    override suspend fun update(card: LoyaltyCard) {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
    }

    override suspend fun delete(id: String) {
        cards.value = cards.value.filterNot { it.id == id }
    }
}
