package com.elsewhere.eyris.data.remote.providers.google

import com.elsewhere.eyris.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://google-map-places.p.rapidapi.com"
private const val RAPID_API_HOST = "google-map-places.p.rapidapi.com"

class GooglePlacesProvider {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun searchBusinesses(
        query: String
    ): List<RawBusinessLead> = coroutineScope {

        val searchResponse: TextSearchResponse = client.get(
            "$BASE_URL/maps/api/place/textsearch/json"
        ) {
            header("x-rapidapi-key", BuildConfig.RAPID_API_KEY)
            header("x-rapidapi-host", RAPID_API_HOST)

            parameter("query", query)
            parameter("language", "en")
            parameter("region", "en")
        }.body()

        searchResponse.results
            .take(20)
            .map {
                async {
                    getPlaceDetails(it.placeId)
                }
            }
            .awaitAll()
            .filterNotNull()
            .filter {
                it.website == null
            }
    }

    private suspend fun getPlaceDetails(
        placeId: String
    ): RawBusinessLead? {

        return try {

            val response: PlaceDetailsResponse = client.get(
                "$BASE_URL/maps/api/place/details/json"
            ) {
                header("x-rapidapi-key", BuildConfig.RAPID_API_KEY)
                header("x-rapidapi-host", RAPID_API_HOST)

                parameter("place_id", placeId)

                parameter(
                    "fields",
                    "name,formatted_address,formatted_phone_number,geometry,website,rating,user_ratings_total,opening_hours,photos,types"
                )
            }.body()

            val place = response.result ?: return null

            RawBusinessLead(
                source = "GOOGLE",
                sourceId = placeId,
                name = place.name.orEmpty(),
                category = place.types?.firstOrNull(),
                latitude = place.geometry?.location?.lat,
                longitude = place.geometry?.location?.lng,
                address = place.formattedAddress,
                phone = place.formattedPhoneNumber,
                website = place.website,
                rating = place.rating,
                reviewCount = place.userRatingsTotal,
                openingHours = place.openingHours?.weekdayText,
                imageUrl = place.photos
                    ?.firstOrNull()
                    ?.photoReference
                    ?.let {
                        "$BASE_URL/maps/api/place/photo?photo_reference=$it"
                    }
            )

        } catch (e: Exception) {
            null
        }
    }
}
