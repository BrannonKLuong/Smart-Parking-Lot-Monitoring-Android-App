package com.example.parkingandroid.network

import com.example.parkingandroid.model.SpotsResponse // Corrected import
import com.example.parkingandroid.model.TokenPayload // Import TokenPayload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/spots") // Corrected endpoint path to match backend
    suspend fun getSpots(): Response<SpotsResponse>

    // Method to register the FCM token
    @POST("/api/register_token") // Corrected endpoint path to match backend (added /api and changed hyphen to underscore)
    suspend fun registerToken(@Body tokenPayload: TokenPayload): Response<ResponseBody> // <-- Adjust return type if your server sends a specific response body
}
