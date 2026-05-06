package com.elsewhere.eyris.data.remote.scraper

import com.elsewhere.eyris.domain.models.Lead
import io.ktor.client.*
import javax.inject.Inject

class OsmOverpassApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(location: String, category: String): List<Lead> {
        // Implementation for OpenStreetMap Overpass API
        // Endpoint: overpass-api.de/api/interpreter
        return emptyList()
    }
}
