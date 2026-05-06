package com.elsewhere.eyris.data.remote.scraper

import com.elsewhere.eyris.domain.models.Lead
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class OsmOverpassApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(location: String, category: String): List<Lead> {
        return try {
            // Simplified: Use fixed coordinates for London if location is specified
            // In production, you would geocode the 'location' string
            val lat = 51.5074
            val lon = -0.1278
            
            val query = """
                [out:json];
                node["amenity"="$category"](around:5000, $lat, $lon);
                out body;
            """.trimIndent()
            
            val response: HttpResponse = client.post("https://overpass-api.de/api/interpreter") {
                setBody(query)
            }
            
            val json = JSONObject(response.bodyAsText())
            val elements = json.getJSONArray("elements")
            val results = mutableListOf<Lead>()
            
            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject("tags") ?: continue
                val name = tags.optString("name", "Unknown Business")
                val street = tags.optString("addr:street", "")
                val houseNumber = tags.optString("addr:housenumber", "")
                val addr = if (street.isNotEmpty()) "$houseNumber $street".trim() else "OpenStreetMap Data"
                
                results.add(Lead(
                    leadId = UUID.randomUUID().toString(),
                    businessName = name,
                    category = category,
                    address = addr,
                    rating = 0.0,
                    reviewCount = 0,
                    hasWebsite = tags.has("website") || tags.has("contact:website"),
                    searchQuery = "$category in $location"
                ))
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
