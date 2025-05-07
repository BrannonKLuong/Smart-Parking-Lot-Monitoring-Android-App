package com.example.parkingandroid
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.Intent


class MainActivity : ComponentActivity() {

    private val viewModel: ParkingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        // 5B: register the “spot freed” callback
        viewModel.setOnSpotFreeListener { spot ->
            Log.i("NotifyTest", "Spot ${spot.id} freed and favorite—sending notification")
            sendSpotFreeNotification(spot)
        }

        setContent {
            val spots by viewModel.spots.collectAsState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spots) { spot ->
                    SpotItem(
                        spot,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onToggleOccupied = { viewModel.toggleOccupied(it) }
                    )
                }
            }
        }
    }

    // 5A: create the notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Spot Alerts"
            val description = "Notifications for your favorite parking spots"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                enableLights(true)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.i("NotifyTest", "Created notification channel $CHANNEL_ID with importance $importance")
        }
    }

    // 5C: fire a notification when a favorite spot frees up
    private val CHANNEL_ID = "spot_alerts"

    private fun sendSpotFreeNotification(spot: ParkingSpotData) {
        // 1) Check notification permission on Android 13+
//        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
//        } else {
//            true
//        }
//        Log.i("NotifyTest", "hasNotificationPermission=$hasPermission")
//        if (!hasPermission) {
//            Log.w("NotifyTest", "POST_NOTIFICATIONS permission not granted; skipping notification.")
//            return
//        }

        // 2) Build the notification
        // 1. Build an intent back to the app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star_filled)          // must be a valid drawable
            .setContentTitle("Spot ${spot.id} Available!")
            .setContentText("Your favorite spot is now free.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)     // high priority for heads-up
            .setAutoCancel(true)                              // dismiss on tap
            .setContentIntent(pendingIntent)                  // open app when tapped
            .build()

        // 3. Dispatch it
        NotificationManagerCompat.from(this)
            .notify(spot.id.hashCode(), notification)
        Log.i("NotifyTest", "Notification dispatched for spot ${spot.id}")
    }
}

@Composable
fun SpotItem(
    spot: ParkingSpotData,
    onToggleFavorite: (ParkingSpotData) -> Unit,
    onToggleOccupied: (ParkingSpotData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (spot.occupied) Color.Red else Color.Green)
                .clickable { onToggleOccupied(spot) }  // tap to simulate freeing
        )
        IconButton(onClick = { onToggleFavorite(spot) }) {
            Icon(
                painter = painterResource(
                    if (spot.favorite) R.drawable.ic_star_filled
                    else R.drawable.ic_star_outline
                ),
                contentDescription = "Toggle Favorite"
            )
        }
    }
}
