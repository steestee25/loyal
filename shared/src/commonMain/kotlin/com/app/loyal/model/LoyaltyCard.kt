package com.app.loyal.model

import kotlinx.datetime.Instant

data class LoyaltyCard(
    val id: String,
    val brandName: String,
    val domain: String,
    val logoUrl: String? = null,
    val code: String,
    val format: BarcodeFormat,
    val colorArgb: Long,
    val label: String? = null,
    val note: String? = null,
    val createdAt: Instant,
    val usageCount: Int = 0,
    val lastViewedAt: Instant? = null,
    val isFavorite: Boolean = false
)