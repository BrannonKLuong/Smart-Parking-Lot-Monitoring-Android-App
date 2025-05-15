package com.example.parkingandroid.network

import com.example.parkingandroid.model.SpotsResponse // Corrected import
import com.example.parkingandroid.model.TokenPayload // Import TokenPayload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/spots")
    suspend fun getSpots(): Response<SpotsResponse>

    @POST("/api/register_token")
    suspend fun registerToken(@Body tokenPayload: TokenPayload): Response<ResponseBody>
}
