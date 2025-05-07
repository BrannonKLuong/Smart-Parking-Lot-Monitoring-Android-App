package com.example.parkingandroid

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParkingViewModel : ViewModel() {
    private val _spots = MutableStateFlow(
        listOf(
            ParkingSpotData("1", occupied = false, lastChanged = "11:00", favorite = false),
            ParkingSpotData("2", occupied = true,  lastChanged = "10:55", favorite = false),
            ParkingSpotData("3", occupied = false, lastChanged = "10:50", favorite = false),
            ParkingSpotData("4", occupied = true,  lastChanged = "10:45", favorite = false),
        )
    )
    val spots = _spots.asStateFlow()

    private var onSpotFree: ((ParkingSpotData) -> Unit)? = null
    fun setOnSpotFreeListener(listener: (ParkingSpotData) -> Unit) {
        onSpotFree = listener
    }

    /** Toggle favorite */
    fun toggleFavorite(spot: ParkingSpotData) {
        _spots.value = _spots.value.map {
            if (it.id == spot.id) it.copy(favorite = !it.favorite) else it
        }
    }

    /** Simulate a spot freeing up */
    fun toggleOccupied(spot: ParkingSpotData) {
        val old = _spots.value.find { it.id == spot.id } ?: return
        val updated = old.copy(occupied = !old.occupied)
        _spots.value = _spots.value.map { if (it.id == spot.id) updated else it }
        // Fire notification only when going from occupied→free
        if (old.favorite && old.occupied && !updated.occupied) {
            onSpotFree?.invoke(updated)
        }
    }
}
