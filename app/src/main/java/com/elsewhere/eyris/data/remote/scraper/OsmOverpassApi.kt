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
import io.ktor.client.plugins.*

class OsmOverpassApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(location: String, category: String): List<Lead> {
        return try {
            // Note: In production, geocoding would be used to get lat/lon for the location string.
            // For now, we use London as a fallback if geocoding is not integrated.
            val lat = 51.5074
            val lon = -0.1278
            
            // Map category to OSM tags (e.g., 'cafe' -> node["amenity"="cafe"])
            val osmCategory = when (category.lowercase()) {
                "restaurant" -> "restaurant"
                "cafe" -> "cafe"
                "bar" -> "bar"
                else -> "shop"
            }

            val query = """
                [out:json][timeout:25];
                node["$osmCategory"](around:5000, $lat, $lon);
                out body;
            """.trimIndent()
            
            val response: HttpResponse = client.post("https://overpass-api.de/api/interpreter") {
                setBody(query)
                timeout {
                    requestTimeoutMillis = 15000
                }
            }
            
            if (!response.status.isSuccess()) {
                Log.e("OsmOverpassApi", "OSM API returned error: ${response.status}")
                return emptyList()
            }

            val responseText = response.bodyAsText()
            val json = JSONObject(responseText)
            val elements = json.optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<Lead>()
            
            for (i in 0 until elements.length()) {
                val element = elements.optJSONObject(i) ?: continue
                val tags = element.optJSONObject("tags") ?: continue
                val name = tags.optString("name", "")
                if (name.isBlank()) continue

                val street = tags.optString("addr:street", "")
                val houseNumber = tags.optString("addr:housenumber", "")
                val city = tags.optString("addr:city", "")
                val addr = if (street.isNotEmpty()) "$houseNumber $street, $city".trim() else "OpenStreetMap Data"
                
                results.add(Lead(
                    leadId = element.optLong("id").toString(),
                    businessName = name,
                    category = category,
                    address = addr,
                    lat = element.optDouble("lat", 0.0),
                    lng = element.optDouble("lon", 0.0),
                    rating = 0.0,
                    reviewCount = 0,
                    hasWebsite = tags.has("website") || tags.has("contact:website"),
                    searchQuery = "$category in $location"
                ))
            }
            results
        } catch (e: Exception) {
            Log.e("OsmOverpassApi", "Error searching OSM", e)
            emptyList()
        }
    }
}
