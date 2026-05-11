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
                    .timeout(10000)
                    .header("Accept-Language", "en-US,en;q=0.9")

                val doc = connection.get()

                // Check for Captcha
                if (doc.title().contains("Service Unavailable") || doc.select("iframe[src*='recaptcha']").isNotEmpty()) {
                    Log.w("GoogleMapsScraper", "Google search blocked by captcha or rate limit")
                    return@withContext emptyList()
                }

                // Google Local results parsing
                val elements = doc.select("div.VkpSyc, div.dbg0pd")
                elements.forEach { element ->
                    val name = element.select("div.rllt__details span.OSrXXb, span.S9ZSDC").firstOrNull()?.text() ?: ""
                    val address = element.select("div.rllt__details div:nth-child(3), span.LrzUub").firstOrNull()?.text() ?: ""

                    val ratingStr = element.select("span.Ym9Kbc, span.Aq14f").firstOrNull()?.text()?.replace(",", ".") ?: ""
                    val rating = ratingStr.toDoubleOrNull() ?: 0.0

                    val reviewCountStr = element.select("span.RDA5Wb, span.ruS8S").firstOrNull()?.text()?.filter { it.isDigit() } ?: ""
                    val reviewCount = reviewCountStr.toIntOrNull() ?: 0
                    
                    val hasWebsite = element.select("a.yY6Kbe, a.m7S7Vb").isNotEmpty() ||
                                     element.text().contains("Website", ignoreCase = true)
                    
                    if (name.isNotBlank()) {
                        val handle = name.lowercase().replace(Regex("[^a-z0-9]"), "")
                        results.add(Lead(
                            leadId = UUID.randomUUID().toString(),
                            businessName = name,
                            category = category,
                            address = address,
                            rating = rating,
                            reviewCount = reviewCount,
                            hasWebsite = hasWebsite,
                            searchQuery = query,
                            coverImageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&q=80&w=800",
                            instagram = if (handle.isNotEmpty()) "@$handle" else null,
                            facebook = if (handle.isNotEmpty()) "https://facebook.com/$handle" else null,
                            whatsapp = null,
                            websiteUrl = null
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleMapsScraper", "Error scraping Google Maps", e)
            }
            results
        }
    }
}
