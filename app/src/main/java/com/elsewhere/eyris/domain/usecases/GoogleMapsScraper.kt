package com.elsewhere.eyris.data.remote.scraper

import com.elsewhere.eyris.domain.models.Lead
import org.jsoup.Jsoup
import javax.inject.Inject

class GoogleMapsScraper @Inject constructor() {
    suspend fun search(location: String, category: String): List<Lead> {
        // Implementation using Jsoup to parse public Google Maps search pages
        return emptyList()
    }
}
