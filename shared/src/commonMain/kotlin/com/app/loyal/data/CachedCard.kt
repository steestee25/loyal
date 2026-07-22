package com.app.loyal.data

import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Forma serializzabile di una tessera, condivisa da cache offline e coda di
 * operazioni in sospeso. Le date sono stringhe ISO come nelle righe di Supabase,
 * così non dipendiamo dai serializzatori di Instant.
 */
@Serializable
internal data class CachedCard(
    val id: String,
    val brandName: String,
    val domain: String,
    val logoUrl: String? = null,
    val code: String,
    val format: String,
    val colorArgb: Long,
    val label: String? = null,
    val note: String? = null,
    val createdAt: String,
    val usageCount: Int = 0,
    val lastViewedAt: String? = null,
    val isFavorite: Boolean = false
)

internal fun CachedCard.toDomain() = LoyaltyCard(
    id = id,
    brandName = brandName,
    domain = domain,
    logoUrl = logoUrl,
    code = code,
    format = BarcodeFormat.valueOf(format),
    colorArgb = colorArgb,
    label = label,
    note = note,
    createdAt = Instant.parse(createdAt),
    usageCount = usageCount,
    lastViewedAt = lastViewedAt?.let { Instant.parse(it) },
    isFavorite = isFavorite
)

internal fun LoyaltyCard.toCached() = CachedCard(
    id = id,
    brandName = brandName,
    domain = domain,
    logoUrl = logoUrl,
    code = code,
    format = format.name,
    colorArgb = colorArgb,
    label = label,
    note = note,
    createdAt = createdAt.toString(),
    usageCount = usageCount,
    lastViewedAt = lastViewedAt?.toString(),
    isFavorite = isFavorite
)
