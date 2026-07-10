package com.app.loyal.data

import com.app.loyal.BRANDFETCH_CLIENT_ID
import com.app.loyal.util.averageColorArgb
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class BrandSearchResult(
    val name: String?,
    val domain: String,
    val icon: String? = null
)

class BrandSearchApi(private val client: HttpClient) {
    suspend fun search(query: String): List<BrandSearchResult> {
        if (query.isBlank()) return emptyList()
        return client.get("https://api.brandfetch.io/v2/search/$query") {
            parameter("c", BRANDFETCH_CLIENT_ID)
        }.body()
    }

    /** Scarica il logo e ne ricava il colore medio (ARGB), o null in caso di errore. */
    suspend fun logoColorArgb(logoUrl: String): Long? = try {
        val bytes: ByteArray = client.get(logoUrl).body()
        averageColorArgb(bytes)
    } catch (e: Exception) {
        null
    }
}