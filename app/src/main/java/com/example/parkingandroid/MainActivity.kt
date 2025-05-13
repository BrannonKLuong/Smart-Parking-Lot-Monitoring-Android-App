package com.example.parkingandroid
import java.net.UnknownHostException
import android.Manifest // <-- Import Manifest
import android.content.pm.PackageManager // <-- Import PackageManager
import android.os.Build // <-- Import Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts // <-- Import ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // <-- Import ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.parkingandroid.adapter.SpotsAdapter
import com.example.parkingandroid.databinding.ActivityMainBinding
import com.example.parkingandroid.model.ParkingSpotData
import com.example.parkingandroid.model.TokenPayload
import com.example.parkingandroid.network.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SpotsAdapter

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private var webSocket: WebSocket? = null

    // ---- START: Declare the ActivityResultLauncher for notification permission ----
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted.")
                // Permission is granted. You can now expect notifications.
                // You might want to re-fetch FCM token here if it's critical and might have failed before
                // getAndRegisterFcmToken()
            } else {
                Log.d("MainActivity", "Notification permission denied.")
                Toast.makeText(this, "Notification permission denied. You may miss updates.", Toast.LENGTH_LONG).show()
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings unless requested by the user.
            }
        }
    // ---- END: Declare the ActivityResultLauncher ----

    // WebSocket Listener (your existing code)
    private val webSocketListener = object : WebSocketListener() {
        // ... your existing onOpen, onMessage, etc. ...
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.d("WebSocket", "Connection opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("WebSocket", "Received message: $text")
            handleWebSocketMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d("WebSocket", "Connection closing: $code / $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("WebSocket", "Connection closed: $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.e("WebSocket", "Connection failure", t)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "WebSocket connection failed: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SpotsAdapter()
        binding.spotsList.layoutManager = LinearLayoutManager(this)
        binding.spotsList.adapter = adapter

        fetchAndDisplaySpots()

//        binding.webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//        }
//        // We will change this later for the direct video feed
//        binding.webView.loadUrl("http://10.0.2.2:8000/")

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
             builtInZoomControls = true
             displayZoomControls = false // Hides the +/- zoom buttons if builtInZoomControls is true
        }
        binding.webView.loadUrl("http://10.0.2.2:8000/webcam_feed")




        askNotificationPermission()

        getAndRegisterFcmToken() // Renamed function for clarity
        startWebSocket()
    }

    // ---- START: Function to ask for notification permission ----
    private fun askNotificationPermission() {
        // This is only necessary for API level 33 and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MainActivity", "Notification permission already granted.")
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user why your app needs this
                // permission for a specific feature to behave as expected, and what features are
                // disabled if it's declined. In this UI, include a "cancel" or "no thanks" button
                // that lets the user continue without granting the permission.
                // For now, just log or show a toast, then request.
                Toast.makeText(this, "Notification permission is needed to show parking alerts.", Toast.LENGTH_LONG).show()
                Log.d("MainActivity", "Showing rationale for notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission.
                Log.d("MainActivity", "Requesting notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    // ---- END: Function to ask for notification permission ----

    private fun getAndRegisterFcmToken() { // Renamed for clarity
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM Token", "Token: $token")
                    registerTokenWithBackend(token) // Extracted to a new function
                } else {
                    Toast.makeText(
                        this,
                        "Failed to get FCM token: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("FCM Token Error", "Failed to get token", task.exception)
                }
            }
    }

    private fun registerTokenWithBackend(token: String?) { // New function
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
                    Log.d("FCM", "Token registered successfully with backend.")
                    withContext(Dispatchers.Main) {
                        // Toast.makeText(this@MainActivity, "Token registered!", Toast.LENGTH_SHORT).show() // Optional toast
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FCM", "Failed to register token with backend: ${response.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to register token. Server error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No network connection for token registration", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error during token registration: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error during token registration with backend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "An unexpected error occurred during token registration.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private fun handleWebSocketMessage(message: String) {
        try {
            val json = JSONObject(message)
            val messageType = json.optString("type")

            when (messageType) {
                "spot_status_update" -> {
                    val spotId = json.optString("spot_id")
                    val status = json.optString("status")

                    if (spotId.isNotEmpty()) {
                        val currentList = (adapter as ListAdapter<ParkingSpotData, *>).currentList.toMutableList()
                        val index = currentList.indexOfFirst { it.id == spotId }

                        if (index != -1) {
                            val updatedSpot = currentList[index].copy(isFree = status != "occupied")
                            currentList[index] = updatedSpot

                            lifecycleScope.launch(Dispatchers.Main) {
                                adapter.submitList(currentList.toList()) // Submit a new list
                                Log.d("WebSocket", "Updated spot ID: $spotId with status: $status")
                            }
                        } else {
                            Log.w("WebSocket", "Received status update for unknown spot ID: $spotId. Refetching spots.")
                            fetchAndDisplaySpots() // If spot not found, maybe configuration changed
                        }
                    } else {
                        Log.w("WebSocket", "Received spot status update message with no spot_id: $message")
                    }
                }
                "config_update" -> {
                    Log.d("WebSocket", "Received config update message. Re-fetching spots...")
                    fetchAndDisplaySpots()
                }
                else -> {
                    Log.w("WebSocket", "Received message with unknown type: $messageType")
                }
            }

        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing WebSocket message: $message", e)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Error processing spot update.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startWebSocket() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8000/ws")
            .build()
        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        Log.d("WebSocket", "Attempting to connect to ws://10.0.2.2:8000/ws")
    }


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
                        Log.d("MainActivity", "Fetched and displayed ${spots.size} spots.")
                    }
                } else {
                    val errorBody = resp.errorBody()?.string()
                    Log.e("API", "Failed to fetch spots: ${resp.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to fetch spots. Server error: ${resp.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: UnknownHostException) {
                Log.e("API", "Unknown host exception fetching spots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Cannot connect to server. Check network.", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("API", "IO exception fetching spots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Network error fetching spots: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("API", "An unexpected error occurred fetching spots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching spots.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Activity destroyed")
    }
}