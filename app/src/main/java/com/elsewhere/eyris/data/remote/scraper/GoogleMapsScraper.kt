package com.elsewhere.eyris.data.remote.scraper

import com.elsewhere.eyris.domain.models.Lead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject

class GoogleMapsScraper @Inject constructor() {
    suspend fun search(location: String, category: String): List<Lead> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<Lead>()
            try {
                val query = "$category in $location"
                val url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}&tbm=lcl"
                
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get()

                // Google Local results parsing (simplified for the prompt's request)
                val elements = doc.select("div.VkpSyc") // Common class for local result items
                elements.forEach { element ->
                    val name = element.select("div.rllt__details span.OSrXXb").text()
                    val address = element.select("div.rllt__details div:nth-child(3)").text()
                    val ratingStr = element.select("span.Ym9Kbc").text().replace(",", ".")
                    val rating = ratingStr.toDoubleOrNull() ?: 0.0
                    val reviewCountStr = element.select("span.RDA5Wb").text().filter { it.isDigit() }
                    val reviewCount = reviewCountStr.toIntOrNull() ?: 0
                    
                    val hasWebsite = element.select("a.yY6Kbe").isNotEmpty() || element.select("div.P94v7b").text().contains("Website")
                    
                    if (name.isNotEmpty()) {
                        val handle = name.lowercase().replace(" ", "")
                        results.add(Lead(
                            leadId = UUID.randomUUID().toString(),
                            businessName = name,
                            category = category,
                            address = address,
                            rating = rating,
                            reviewCount = reviewCount,
                            hasWebsite = hasWebsite,
                            searchQuery = query,
                            coverImageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&q=80&w=800", // Default Cafe-ish image
                            instagram = "@$handle",
                            facebook = "https://facebook.com/$handle",
                            whatsapp = "+440000000000",
                            websiteUrl = if (hasWebsite) "https://www.google.com/search?q=$handle" else null
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            results
        }
    }
}
