package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import com.elsewhere.eyris.domain.models.BusinessLead
import com.elsewhere.eyris.domain.models.SourceType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import javax.inject.Inject
import io.ktor.client.plugins.*

class OsmOverpassApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(lat: Double, lon: Double, category: String, radius: Int = 5000): List<BusinessLead> {
        return try {
            val tag = mapCategoryToOsm(category)
            
            val query = """
                [out:json][timeout:25];
                (
                  node["${tag.first}"="${tag.second}"](around:$radius, $lat, $lon);
                  way["${tag.first}"="${tag.second}"](around:$radius, $lat, $lon);
                  relation["${tag.first}"="${tag.second}"](around:$radius, $lat, $lon);
                );
                out center body;
            """.trimIndent()
            
            val response: HttpResponse = client.post("https://overpass-api.de/api/interpreter") {
                setBody(query)
                header("User-Agent", "Eyris/1.0")
                timeout {
                    requestTimeoutMillis = 20000
                }
            }

            if (!response.status.isSuccess()) {
                Log.e("OsmOverpassApi", "Error: ${response.status}")
                return emptyList()
            }

            val json = JSONObject(response.bodyAsText())
            val elements = json.optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<BusinessLead>()
            
            for (i in 0 until elements.length()) {
                val element = elements.optJSONObject(i) ?: continue
                val tags = element.optJSONObject("tags") ?: continue
                val name = tags.optString("name", "")
                if (name.isBlank()) continue
                
                val centerLat = if (element.has("lat")) element.getDouble("lat") else element.optJSONObject("center")?.optDouble("lat")
                val centerLon = if (element.has("lon")) element.getDouble("lon") else element.optJSONObject("center")?.optDouble("lon")

                val street = tags.optString("addr:street")
                val houseNumber = tags.optString("addr:housenumber")
                val city = tags.optString("addr:city")
                val formattedAddress = formatAddress(houseNumber, street, city)

                results.add(BusinessLead(
                    id = "osm_${element.optLong("id")}",
                    source = SourceType.OSM,
                    name = name,
                    category = category,
                    latitude = centerLat,
                    longitude = centerLon,
                    address = formattedAddress,
                    city = city.takeIf { it.isNotBlank() },
                    country = tags.optString("addr:country").takeIf { it.isNotBlank() },
                    phone = tags.optString("phone").takeIf { it.isNotBlank() },
                    website = (tags.optString("website") ?: tags.optString("contact:website")).takeIf { !it.isNullOrBlank() },
                    rating = null,
                    reviewCount = null,
                    imageUrl = null,
                    instagram = null,
                    facebook = null,
                    confidence = 0.7
                ))
            }
            results
        } catch (e: Exception) {
            Log.e("OsmOverpassApi", "Error searching OSM", e)
            emptyList()
        }
    }

    private fun formatAddress(houseNumber: String, street: String, city: String): String? {
        val parts = mutableListOf<String>()
        if (houseNumber.isNotBlank() || street.isNotBlank()) {
            parts.add("${houseNumber.trim()} ${street.trim()}".trim())
        }
        if (city.isNotBlank()) {
            parts.add(city.trim())
        }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    private fun mapCategoryToOsm(category: String): Pair<String, String> {
        return when (category.lowercase()) {
            "restaurant" -> "amenity" to "restaurant"
            "cafe" -> "amenity" to "cafe"
            "bar" -> "amenity" to "bar"
            "gym" -> "leisure" to "fitness_centre"
            "plumber" -> "craft" to "plumber"
            "electrician" -> "craft" to "electrician"
            else -> "shop" to category.lowercase()
        }
    }
}
