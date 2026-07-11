package com.app.loyal.data

import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class LoyaltyCardRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("brand_name") val brandName: String,
    val domain: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    val code: String,
    val format: String,
    @SerialName("color_argb") val colorArgb: Long,
    val label: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("usage_count") val usageCount: Int = 0,
    @SerialName("last_viewed_at") val lastViewedAt: String? = null
)

@Serializable
private data class LoyaltyCardUsageUpdate(
    @SerialName("usage_count") val usageCount: Int,
    @SerialName("last_viewed_at") val lastViewedAt: String
)

private fun LoyaltyCardRow.toDomain() = LoyaltyCard(
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
    lastViewedAt = lastViewedAt?.let { Instant.parse(it) }
)

private fun LoyaltyCard.toRow(userId: String) = LoyaltyCardRow(
    id = id,
    userId = userId,
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
    lastViewedAt = lastViewedAt?.toString()
)

class SupabaseLoyaltyCardRepository(
    private val supabase: SupabaseClient,
    private val userId: String
) : LoyaltyCardRepository {

    private val table = supabase.from("loyalty_cards")
    private val cards = MutableStateFlow<List<LoyaltyCard>>(emptyList())

    suspend fun refresh() {
        cards.value = table.select().decodeList<LoyaltyCardRow>().map { it.toDomain() }
    }

    override fun observeCards(): StateFlow<List<LoyaltyCard>> = cards

    override suspend fun add(card: LoyaltyCard) {
        table.insert(card.toRow(userId))
        refresh()
    }

    override suspend fun update(card: LoyaltyCard) {
        table.upsert(card.toRow(userId))
        refresh()
    }

    override suspend fun delete(id: String) {
        table.delete {
            filter { eq("id", id) }
        }
        refresh()
    }

    override suspend fun recordView(id: String) {
        val newCount = (cards.value.firstOrNull { it.id == id }?.usageCount ?: 0) + 1
        table.update(
            LoyaltyCardUsageUpdate(
                usageCount = newCount,
                lastViewedAt = Clock.System.now().toString()
            )
        ) {
            filter { eq("id", id) }
        }
        refresh()
    }
}
