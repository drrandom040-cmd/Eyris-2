package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import com.elsewhere.eyris.domain.models.Lead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject

class GoogleMapsScraper @Inject constructor() {

    private val mobileUserAgent = "Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"

    suspend fun search(location: String, category: String): List<Lead> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<Lead>()
            try {
                val query = "$category in $location"
                val url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}&tbm=lcl"
                
                val connection = Jsoup.connect(url)
                    .userAgent(mobileUserAgent)
                    .timeout(15000)
                    .header("Accept-Language", "en-US,en;q=0.9")

                val doc = connection.get()

                // Robust Captcha and Block Detection
                val pageTitle = doc.title()
                if (pageTitle.contains("Service Unavailable") ||
                    pageTitle.contains("Sorry") ||
                    doc.select("iframe[src*='recaptcha']").isNotEmpty() ||
                    doc.body().text().contains("unusual traffic from your computer network")) {
                    Log.w("GoogleMapsScraper", "Google search blocked: $pageTitle")
                    return@withContext emptyList()
                }

                // Corrected Selector: Only select parent result cards to avoid duplicates
                // div.VkpSyc is the standard container for local pack result cards
                val elements = doc.select("div.VkpSyc")
                if (elements.isEmpty()) {
                    Log.d("GoogleMapsScraper", "No results found for query: $query")
                }

                elements.forEach { element ->
                    // Scope searches strictly within the result card
                    val name = element.select("div.rllt__details span.OSrXXb").firstOrNull()?.text() ?: ""

                    // Address parsing: typically the third div in the details block
                    val address = element.select("div.rllt__details div:nth-child(3)").firstOrNull()?.text() ?: ""

                    val ratingStr = element.select("span.Ym9Kbc").firstOrNull()?.text()?.replace(",", ".") ?: ""
                    val rating = ratingStr.toDoubleOrNull() ?: 0.0

                    val reviewCountStr = element.select("span.RDA5Wb").firstOrNull()?.text()?.filter { it.isDigit() } ?: ""
                    val reviewCount = reviewCountStr.toIntOrNull() ?: 0
                    
                    // Website detection: check for specific icons or text indicators
                    val hasWebsite = element.select("a.yY6Kbe").isNotEmpty() ||
                                     element.text().contains("Website", ignoreCase = true)
                    
                    if (name.isNotBlank()) {
                        results.add(Lead(
                            leadId = "google_${UUID.nameUUIDFromBytes(name.toByteArray())}",
                            businessName = name,
                            category = category,
                            address = address,
                            rating = rating,
                            reviewCount = reviewCount,
                            hasWebsite = hasWebsite,
                            searchQuery = query,
                            coverImageUrl = null, // Removed fabricated placeholder
                            instagram = null,    // Removed fabricated handles
                            facebook = null,     // Removed fabricated handles
                            whatsapp = null,
                            websiteUrl = null
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleMapsScraper", "Error scraping Google Search Local", e)
            }
            results
        }
    }
}
