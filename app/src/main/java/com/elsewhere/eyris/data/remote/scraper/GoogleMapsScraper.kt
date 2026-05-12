package com.elsewhere.eyris.data.remote.scraper

import android.util.Log
import com.elsewhere.eyris.domain.models.BusinessLead
import com.elsewhere.eyris.domain.models.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject

class GoogleMapsScraper @Inject constructor() {

    private val mobileUserAgent = "Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"

    suspend fun search(location: String, category: String): List<BusinessLead> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<BusinessLead>()
            try {
                val query = "$category in $location"
                val url = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}&tbm=lcl"
                
                val connection = Jsoup.connect(url)
                    .userAgent(mobileUserAgent)
                    .timeout(15000)
                    .header("Accept-Language", "en-US,en;q=0.9")

                val doc = connection.get()

                // Enhanced block and captcha detection
                val bodyText = doc.body().text()
                if (doc.title().contains("Service Unavailable") ||
                    doc.location().contains("/sorry/") ||
                    bodyText.contains("unusual traffic from your computer network") ||
                    bodyText.contains("To continue, please type the characters below")) {
                    Log.w("GoogleMapsScraper", "Google search blocked: ${doc.title()}")
                    return@withContext emptyList()
                }

                // Select parent result cards only
                val elements = doc.select("div.VkpSyc")
                elements.forEach { element ->
                    val name = element.select("div.rllt__details span.OSrXXb").firstOrNull()?.text() ?: ""
                    val address = element.select("div.rllt__details div:nth-child(3)").firstOrNull()?.text() ?: ""

                    val ratingStr = element.select("span.Ym9Kbc").firstOrNull()?.text()?.replace(",", ".") ?: ""
                    val rating = ratingStr.toDoubleOrNull()

                    val reviewCountStr = element.select("span.RDA5Wb").firstOrNull()?.text()?.filter { it.isDigit() } ?: ""
                    val reviewCount = reviewCountStr.toIntOrNull()
                    
                    val website = element.select("a.yY6Kbe").firstOrNull()?.attr("href")
                    
                    if (name.isNotBlank()) {
                        results.add(BusinessLead(
                            id = "google_${UUID.nameUUIDFromBytes(name.toByteArray())}",
                            source = SourceType.GOOGLE,
                            name = name,
                            category = category,
                            latitude = null,
                            longitude = null,
                            address = address,
                            city = null,
                            country = null,
                            phone = null,
                            website = website,
                            rating = rating,
                            reviewCount = reviewCount,
                            imageUrl = null,
                            instagram = null, // Purged fake data
                            facebook = null,  // Purged fake data
                            confidence = 0.5
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleMapsScraper", "Error scraping Google", e)
            }
            results
        }
    }
}
