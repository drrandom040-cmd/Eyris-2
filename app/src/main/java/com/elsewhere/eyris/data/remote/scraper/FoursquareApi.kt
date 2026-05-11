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
    suspend fun search(location: String, category: String): List<Lead> {
        return try {
            val apiKey = BuildConfig.FSQ_API_KEY
            if (apiKey.isBlank()) {
                Log.w("FoursquareApi", "API Key is missing")
                return emptyList()
            }

            val response: HttpResponse = client.get("https://api.foursquare.com/v3/places/search") {
                header("Authorization", apiKey)
                parameter("near", location)
                parameter("query", category)
                parameter("fields", "fsq_id,name,location,rating,stats,website")
                timeout {
                    requestTimeoutMillis = 10000
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
                
                val fsqRating = item.optDouble("rating", 0.0)

                leads.add(Lead(
                    leadId = item.optString("fsq_id", UUID.randomUUID().toString()),
                    businessName = item.optString("name", "Unknown"),
                    category = category,
                    address = formattedAddress,
                    rating = if (fsqRating > 0) fsqRating / 2.0 else 0.0,
                    reviewCount = stats?.optInt("total_ratings", 0) ?: 0,
                    hasWebsite = !item.optString("website", "").isNullOrBlank(),
                    searchQuery = "$category in $location"
                ))
            }
            leads
        } catch (e: Exception) {
            Log.e("FoursquareApi", "Error searching Foursquare", e)
            emptyList()
        }
    }
}
