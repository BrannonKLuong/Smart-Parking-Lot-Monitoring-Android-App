package com.example.parkingandroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
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
import org.json.JSONArray // Import JSONArray
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // Declare adapter as the specific type if possible, or cast later
    private lateinit var adapter: SpotsAdapter

    // OkHttp client for WebSocket
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    // WebSocket Listener to handle events and messages
    private val webSocketListener = object : WebSocketListener() {
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
            // You might want to implement reconnection logic here
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
            // You might want to implement reconnection logic here
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

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        binding.webView.loadUrl("http://10.0.2.2:8000/") // Assuming dashboard is at the root

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    registerToken(task.result)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to get FCM token: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("FCM Token Error", "Failed to get token", task.exception)
                }
            }

        startWebSocket()
    }

    /** Handles incoming WebSocket messages and updates the UI. */
    private fun handleWebSocketMessage(message: String) {
        try {
            val json = JSONObject(message)
            val messageType = json.optString("type") // Get the message type

            when (messageType) {
                "spot_status_update" -> {
                    val spotId = json.optString("spot_id")
                    val status = json.optString("status")

                    if (spotId.isNotEmpty()) {
                        // Explicitly cast adapter to ListAdapter to access currentList
                        val currentList = (adapter as ListAdapter<ParkingSpotData, *>).currentList.toMutableList()
                        val index = currentList.indexOfFirst { it.id == spotId }

                        if (index != -1) {
                            val updatedSpot = currentList[index].copy(isFree = status != "occupied")
                            currentList[index] = updatedSpot

                            lifecycleScope.launch(Dispatchers.Main) {
                                adapter.submitList(currentList)
                                Log.d("WebSocket", "Updated spot ID: $spotId with status: $status") // Added log for successful update
                            }
                        } else {
                            Log.w("WebSocket", "Received status update for unknown spot ID: $spotId")
                        }
                    } else {
                        Log.w("WebSocket", "Received spot status update message with no spot_id: $message")
                    }
                }
                "config_update" -> {
                    val spotsArray = json.optJSONArray("spots")
                    if (spotsArray != null) {
                        val updatedSpots = mutableListOf<ParkingSpotData>()
                        for (i in 0 until spotsArray.length()) {
                            val spotJson = spotsArray.getJSONObject(i)
                            val spotId = spotJson.optString("id")
                            val isAvailable = spotJson.optBoolean("is_available", true) // Default to true if not present

                            if (spotId.isNotEmpty()) {
                                updatedSpots.add(ParkingSpotData(id = spotId, isFree = isAvailable))
                            }
                        }

                        lifecycleScope.launch(Dispatchers.Main) {
                            adapter.submitList(updatedSpots)
                            Log.d("WebSocket", "Received and applied config update. Total spots: ${updatedSpots.size}") // Log config update
                        }
                    } else {
                        Log.w("WebSocket", "Received config update message with no 'spots' array: $message")
                    }
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

    /** Starts the WebSocket connection. */
    private fun startWebSocket() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8000/ws")
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        Log.d("WebSocket", "Attempting to connect to ws://10.0.2.2:8000/ws")
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
                    val errorBody = response.errorBody()?.string()
                    Log.e("FCM", "Failed to register token: ${response.code()} - $errorBody")
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
            } catch (e: Exception) {
                Log.e("FCM", "Error during token registration", e)
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
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot connect to server. Check your network or backend.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("API", "IO exception fetching spots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error fetching spots: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("API", "An unexpected error occurred fetching spots", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "An unexpected error occurred while fetching spots.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Activity destroyed")
    }
}
