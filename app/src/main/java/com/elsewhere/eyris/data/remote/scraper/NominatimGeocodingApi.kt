package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NominatimGeocodingApi @Inject constructor(
    private val client: HttpClient
) {
    data class Location(val lat: Double, val lon: Double)

    suspend fun geocode(query: String): Location? {
        return try {
            val response: HttpResponse = client.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", 1)
                header("User-Agent", "Eyris/1.0")
            }

            if (!response.status.isSuccess()) {
                Log.e("NominatimGeocoding", "Geocoding failed: ${response.status}")
                return null
            }

            val json = JSONArray(response.bodyAsText())
            if (json.length() > 0) {
                val result = json.getJSONObject(0)
                Location(
                    lat = result.getDouble("lat"),
                    lon = result.getDouble("lon")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("NominatimGeocoding", "Error during geocoding", e)
            null
        }
    }
}
