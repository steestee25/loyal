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
    val note: String? = null,
    val createdAt: Instant
)