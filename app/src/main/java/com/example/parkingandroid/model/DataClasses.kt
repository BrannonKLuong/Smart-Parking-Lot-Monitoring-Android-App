package com.example.parkingandroid.model

// Data class representing the data structure used within the app (e.g., for UI)
data class ParkingSpotData (
    val id: String, // Matches the type from the API (ParkingSpotDto)
    val isFree: Boolean
)

// Data class representing the response structure for parking spots received from the API
data class SpotsResponse(
    val spots: List<ParkingSpotDto>
)

// Data class representing a single parking spot as received directly from the API
data class ParkingSpotDto(
    val id: String,
    val is_available: Boolean // Corresponds to the 'is_available' field in your JSON
)

// Data class for the payload sent when registering an FCM token
data class TokenPayload(
    val token: String,
    val platform: String // e.g., "android"
)
