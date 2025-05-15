package com.example.parkingandroid.model

data class ParkingSpotData (
    val id: String,
    val isFree: Boolean
)

data class SpotsResponse(
    val spots: List<ParkingSpotDto>
)


data class ParkingSpotDto(
    val id: String,
    val is_available: Boolean
)

data class TokenPayload(
    val token: String,
    val platform: String // e.g., "android"
)
