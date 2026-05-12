package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import com.elsewhere.eyris.domain.models.BusinessLead
import com.elsewhere.eyris.domain.models.SourceType
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
    suspend fun search(lat: Double, lon: Double, category: String, radius: Int = 5000): List<BusinessLead> {
        return try {
            val apiKey = BuildConfig.FSQ_API_KEY
            if (apiKey.isBlank()) return emptyList()

            val response: HttpResponse = client.get("https://api.foursquare.com/v3/places/search") {
                header("Authorization", apiKey)
                parameter("ll", "$lat,$lon")
                parameter("radius", radius)
                parameter("query", category)
                parameter("fields", "fsq_id,name,location,rating,stats,website,social_media")
                timeout {
                    requestTimeoutMillis = 15000
                }
            }

            if (!response.status.isSuccess()) {
                Log.e("FoursquareApi", "Error: ${response.status}")
                return emptyList()
            }
            
            val json = JSONObject(response.bodyAsText())
            val results = json.optJSONArray("results") ?: return emptyList()
            val leads = mutableListOf<BusinessLead>()
            
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val loc = item.optJSONObject("location")
                val address = loc?.optString("formatted_address", null)
                val stats = item.optJSONObject("stats")
                val social = item.optJSONObject("social_media")

                val rawRating = item.optDouble("rating", -1.0)
                val normalizedRating = if (rawRating >= 0) rawRating / 2.0 else null

                leads.add(BusinessLead(
                    id = "fsq_${item.optString("fsq_id", UUID.randomUUID().toString())}",
                    source = SourceType.FOURSQUARE,
                    name = item.optString("name", "Unknown"),
                    category = category,
                    latitude = null, // LL is used for search, but individual result coords not mapped yet
                    longitude = null,
                    address = address,
                    city = loc?.optString("locality"),
                    country = loc?.optString("country"),
                    phone = null,
                    website = item.optString("website").takeIf { it.isNotBlank() },
                    rating = normalizedRating,
                    reviewCount = stats?.optInt("total_ratings"),
                    imageUrl = null,
                    instagram = social?.optString("instagram")?.takeIf { it.isNotBlank() },
                    facebook = social?.optString("facebook")?.takeIf { it.isNotBlank() },
                    confidence = 0.8
                ))
            }
            leads
        } catch (e: Exception) {
            Log.e("FoursquareApi", "Error searching Foursquare", e)
            emptyList()
        }
    }
}
