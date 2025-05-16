package com.example.parkingandroid.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://10.0.2.2:8000/")
        .baseUrl("https://5kww6ef6ra.us-east-2.awsapprunner.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}
