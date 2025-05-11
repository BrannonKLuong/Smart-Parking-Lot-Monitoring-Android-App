package com.example.parkingandroid.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        // Use 10.0.2.2 for Android emulator → your development machine
        .baseUrl("http://10.0.2.2:8000/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}
