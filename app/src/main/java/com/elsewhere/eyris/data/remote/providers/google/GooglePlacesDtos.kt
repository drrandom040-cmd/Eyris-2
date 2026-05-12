package com.elsewhere.eyris.data.remote.providers.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TextSearchResponse(
    val results: List<TextSearchResult> = emptyList()
)

@Serializable
data class TextSearchResult(
    @SerialName("place_id")
    val placeId: String
)

@Serializable
data class PlaceDetailsResponse(
    val result: PlaceDetailsResult? = null
)

@Serializable
data class PlaceDetailsResult(
    val name: String? = null,

    @SerialName("formatted_address")
    val formattedAddress: String? = null,

    @SerialName("formatted_phone_number")
    val formattedPhoneNumber: String? = null,

    val website: String? = null,

    val rating: Double? = null,

    @SerialName("user_ratings_total")
    val userRatingsTotal: Int? = null,

    val geometry: GeometryDto? = null,

    @SerialName("opening_hours")
    val openingHours: OpeningHoursDto? = null,

    val photos: List<PhotoDto>? = null,

    val types: List<String>? = null
)

@Serializable
data class GeometryDto(
    val location: LocationDto? = null
)

@Serializable
data class LocationDto(
    val lat: Double? = null,
    val lng: Double? = null
)

@Serializable
data class OpeningHoursDto(
    @SerialName("weekday_text")
    val weekdayText: List<String>? = null
)

@Serializable
data class PhotoDto(
    @SerialName("photo_reference")
    val photoReference: String? = null
)
