package com.example.parkingandroid

data class ParkingSpotData(
    val id: String,
    val occupied: Boolean,
    val lastChanged: String,
    var favorite: Boolean = false
)