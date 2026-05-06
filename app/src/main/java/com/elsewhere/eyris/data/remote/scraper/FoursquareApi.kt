package com.elsewhere.eyris.data.remote.scraper

import com.elsewhere.eyris.domain.models.Lead
import io.ktor.client.*
import javax.inject.Inject

class FoursquareApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun search(location: String, category: String): List<Lead> {
        // Implementation for Foursquare /v3/places/search
        return emptyList()
    }
}
