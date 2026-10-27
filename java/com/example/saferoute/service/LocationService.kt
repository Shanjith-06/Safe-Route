package com.example.saferoute.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.saferoute.MainActivity
import com.example.saferoute.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationData(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val speed: Float = 0f,
    val accuracy: Float = 5f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        const val CHANNEL_ID = "saferoute_location_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.saferoute.action.START_LOCATION"
        const val ACTION_STOP = "com.example.saferoute.action.STOP_LOCATION"

        private val _currentLocation = MutableStateFlow(LocationData())
        val currentLocation: StateFlow<LocationData> = _currentLocation.asStateFlow()

        var isServiceRunning: Boolean = false
            private set

        fun updateMockLocation(lat: Double, lng: Double, speed: Float = 0f) {
            _currentLocation.value = LocationData(
                latitude = lat,
                longitude = lng,
                speed = speed,
                accuracy = 3.0f,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    _currentLocation.value = LocationData(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        speed = loc.speed,
                        accuracy = loc.accuracy,
                        altitude = loc.altitude,
                        timestamp = loc.time
                    )
                    updateNotification(loc)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification("SafeRoute Tracking Active", "Monitoring your safety location..."))
                startTracking()
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        isServiceRunning = true
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission not granted or fallback
        }
    }

    private fun stopTracking() {
        isServiceRunning = false
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    private fun updateNotification(location: Location) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val text = "Current GPS: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"
        notificationManager.notify(NOTIFICATION_ID, buildNotification("SafeRoute Live Tracking", text))
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeRoute Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent location tracking during journey or emergency"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
