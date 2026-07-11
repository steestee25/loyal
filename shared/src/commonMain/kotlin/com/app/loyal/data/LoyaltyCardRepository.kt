package com.app.loyal.data

import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.flow.Flow

interface LoyaltyCardRepository {
    fun observeCards(): Flow<List<LoyaltyCard>>
    suspend fun add(card: LoyaltyCard)
    suspend fun update(card: LoyaltyCard)
    suspend fun delete(id: String)
    suspend fun recordView(id: String)
}