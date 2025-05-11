package com.example.parkingandroid

import com.example.parkingandroid.model.ParkingSpotData
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parkingandroid.adapter.SpotsAdapter
import com.example.parkingandroid.databinding.ActivityMainBinding
import com.example.parkingandroid.model.TokenPayload // Import TokenPayload from model
import com.example.parkingandroid.network.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SpotsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Inflate via ViewBinding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) RecyclerView wiring:
        adapter = SpotsAdapter()
        binding.spotsList.layoutManager = LinearLayoutManager(this)
        binding.spotsList.adapter = adapter

        // 3) Kick off your network fetch:
        fetchAndDisplaySpots()

        // 4) Dashboard in a WebView:
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        // Replace with your actual URL
        binding.webView.loadUrl("http://10.0.2.2:8000/")

        // 5) FCM token → server
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Notice: no 'this@MainActivity.' qualifier needed here
                    registerToken(task.result)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to get FCM token: ${task.exception?.message}",
                        Toast.LENGTH_SHORT // Corrected: Pass duration constant
                    ).show()
                    Log.e("FCM Token Error", "Failed to get token", task.exception)
                }
            }
    }

    /** Sends the FCM registration token to your backend. */
    private fun registerToken(token: String?) {
        if (token == null) {
            Log.w("FCM", "FCM token is null. Not registering.")
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.service.registerToken(
                    TokenPayload(token = token, platform = "android")
                )
                if (response.isSuccessful) {
                    Log.d("FCM", "Token registered successfully.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Token registered!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FCM", "Failed to register token: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to register token. Server error: ${response.code()}",
                            Toast.LENGTH_LONG // Corrected: Pass duration constant
                        ).show()
                    }
                }
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No network connection", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /** Fetches your spots and feeds them into the RecyclerView adapter. */
    private fun fetchAndDisplaySpots() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.service.getSpots()
                if (resp.isSuccessful) {
                    val spots = resp.body()?.spots?.map { dto ->
                        ParkingSpotData(
                            id = dto.id,
                            isFree = dto.is_available
                        )
                    } ?: emptyList()

                    withContext(Dispatchers.Main) {
                        adapter.submitList(spots)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Server error: ${resp.code()}",
                            Toast.LENGTH_LONG // Corrected: Pass duration constant
                        ).show()
                    }
                }
            } catch (e: UnknownHostException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot connect to server. Check your network or backend.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
