package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import com.elsewhere.eyris.domain.models.Lead
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import com.elsewhere.eyris.BuildConfig
import io.ktor.client.plugins.*

class FoursquareApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(lat: Double, lon: Double, category: String, radius: Int = 5000): List<Lead> {
        return try {
            val apiKey = BuildConfig.FSQ_API_KEY
            if (apiKey.isBlank()) {
                Log.w("FoursquareApi", "API Key is missing")
                return emptyList()
            }

            // Using lat/lon for accurate location bias
            val response: HttpResponse = client.get("https://api.foursquare.com/v3/places/search") {
                header("Authorization", apiKey)
                parameter("ll", "$lat,$lon")
                parameter("radius", radius)
                parameter("query", category)
                // Explicitly request stats and website to ensure data quality
                parameter("fields", "fsq_id,name,location,rating,stats,website,social_media")
                timeout {
                    requestTimeoutMillis = 15000
                }
            }

            if (!response.status.isSuccess()) {
                Log.e("FoursquareApi", "API returned error: ${response.status}")
                return emptyList()
            }
            
            val json = JSONObject(response.bodyAsText())
            val results = json.optJSONArray("results") ?: return emptyList()
            val leads = mutableListOf<Lead>()
            
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val loc = item.optJSONObject("location")
                val formattedAddress = loc?.optString("formatted_address", "") ?: ""
                val stats = item.optJSONObject("stats")
                
                // Normalizing 10-point Foursquare rating to 5-star system
                val fsqRating = item.optDouble("rating", -1.0)
                val normalizedRating = if (fsqRating >= 0) fsqRating / 2.0 else 0.0

                val social = item.optJSONObject("social_media")

                leads.add(Lead(
                    leadId = "fsq_${item.optString("fsq_id", UUID.randomUUID().toString())}",
                    businessName = item.optString("name", "Unknown"),
                    category = category,
                    address = formattedAddress,
                    rating = normalizedRating,
                    reviewCount = stats?.optInt("total_ratings", 0) ?: 0,
                    hasWebsite = !item.optString("website", "").isNullOrBlank(),
                    websiteUrl = item.optString("website").takeIf { it.isNotBlank() },
                    instagram = social?.optString("instagram")?.takeIf { it.isNotBlank() },
                    facebook = social?.optString("facebook")?.takeIf { it.isNotBlank() },
                    searchQuery = category
                ))
            }
            leads
        } catch (e: Exception) {
            Log.e("FoursquareApi", "Error searching Foursquare", e)
            emptyList()
        }
    }
}
