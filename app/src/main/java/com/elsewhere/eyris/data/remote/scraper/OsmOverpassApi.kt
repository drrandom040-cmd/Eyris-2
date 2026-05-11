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
    suspend fun search(lat: Double, lon: Double, category: String, radius: Int = 5000): List<Lead> {
        return try {
            // Map category to multiple possible OSM keys for higher data quality
            val tags = when (category.lowercase()) {
                "restaurant" -> listOf("amenity" to "restaurant")
                "cafe" -> listOf("amenity" to "cafe")
                "bar" -> listOf("amenity" to "bar", "amenity" to "pub")
                "gym" -> listOf("leisure" to "fitness_centre", "leisure" to "gym")
                "plumber" -> listOf("craft" to "plumber")
                "electrician" -> listOf("craft" to "electrician")
                else -> listOf("shop" to category.lowercase(), "amenity" to category.lowercase())
            }
            
            val queryUnion = tags.joinToString("\n") { (key, value) ->
                """
                node["$key"="$value"](around:$radius, $lat, $lon);
                way["$key"="$value"](around:$radius, $lat, $lon);
                relation["$key"="$value"](around:$radius, $lat, $lon);
                """.trimIndent()
            }

            val fullQuery = """
                [out:json][timeout:25];
                (
                $queryUnion
                );
                out center body;
            """.trimIndent()
            
            val response: HttpResponse = client.post("https://overpass-api.de/api/interpreter") {
                setBody(fullQuery)
                header("User-Agent", "Eyris/1.0")
                timeout {
                    requestTimeoutMillis = 20000
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
                val tagsObj = element.optJSONObject("tags") ?: continue
                val name = tagsObj.optString("name", "")
                if (name.isBlank()) continue

                val street = tagsObj.optString("addr:street", "")
                val houseNumber = tagsObj.optString("addr:housenumber", "")
                val city = tagsObj.optString("addr:city", "")
                val addr = formatAddress(houseNumber, street, city)
                
                val centerLat = if (element.has("lat")) element.getDouble("lat") else element.optJSONObject("center")?.optDouble("lat", 0.0) ?: 0.0
                val centerLon = if (element.has("lon")) element.getDouble("lon") else element.optJSONObject("center")?.optDouble("lon", 0.0) ?: 0.0

                results.add(Lead(
                    leadId = "osm_${element.optLong("id")}",
                    businessName = name,
                    category = category,
                    address = addr,
                    lat = centerLat,
                    lng = centerLon,
                    rating = 0.0,
                    reviewCount = 0,
                    hasWebsite = tagsObj.has("website") || tagsObj.has("contact:website"),
                    searchQuery = category
                ))
            }
            results
        } catch (e: Exception) {
            Log.e("OsmOverpassApi", "Error searching OSM", e)
            emptyList()
        }
    }

    private fun formatAddress(houseNumber: String, street: String, city: String): String {
        val parts = mutableListOf<String>()
        if (houseNumber.isNotBlank() || street.isNotBlank()) {
            parts.add("${houseNumber.trim()} ${street.trim()}".trim())
        }
        if (city.isNotBlank()) {
            parts.add(city.trim())
        }
        return if (parts.isEmpty()) "OpenStreetMap Data" else parts.joinToString(", ")
    }
}
