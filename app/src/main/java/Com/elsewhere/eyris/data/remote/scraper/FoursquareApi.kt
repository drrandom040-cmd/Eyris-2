package Com.elsewhere.eyris.data.remote.scraper

import Com.elsewhere.eyris.domain.models.Lead
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import Com.elsewhere.eyris.BuildConfig

class FoursquareApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(location: String, category: String): List<Lead> {
        return try {
            val apiKey = BuildConfig.FSQ_API_KEY
            if (apiKey.isEmpty()) return emptyList()

            val response: HttpResponse = client.get("https://api.foursquare.com/v3/places/search") {
                header("Authorization", apiKey)
                parameter("near", location)
                parameter("query", category)
                parameter("fields", "fsq_id,name,location,rating,stats,website")
            }
            
            val json = JSONObject(response.bodyAsText())
            val results = json.getJSONArray("results")
            val leads = mutableListOf<Lead>()
            
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val loc = item.optJSONObject("location")
                val formattedAddress = loc?.optString("formatted_address", "") ?: ""
                val stats = item.optJSONObject("stats")
                
                leads.add(Lead(
                    leadId = item.optString("fsq_id", UUID.randomUUID().toString()),
                    businessName = item.optString("name", "Unknown"),
                    category = category,
                    address = formattedAddress,
                    rating = item.optDouble("rating", 0.0) / 2.0, // Foursquare is 0-10, we use 0-5
                    reviewCount = stats?.optInt("total_ratings", 0) ?: 0,
                    hasWebsite = item.has("website"),
                    searchQuery = "$category in $location"
                ))
            }
            leads
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
