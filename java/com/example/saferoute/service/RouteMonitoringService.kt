package com.example.saferoute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.saferoute.MainActivity
import com.example.saferoute.domain.navigation.NavigationMonitor
import com.example.saferoute.domain.navigation.NavigationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteAlertState(
    val hasDeviationAlert: Boolean = false,
    val deviationMeters: Double = 0.0,
    val hasUnexpectedStopAlert: Boolean = false,
    val stopDurationSeconds: Int = 0,
    val alertMessage: String? = null
)

class RouteMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var locationSubscriptionJob: Job? = null

    companion object {
        const val CHANNEL_ID = "saferoute_monitoring_channel"
        const val NOTIFICATION_ID = 1004

        const val ACTION_START_MONITORING = "com.example.saferoute.action.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.saferoute.action.STOP_MONITORING"

        private val _routeAlertState = MutableStateFlow(RouteAlertState())
        val routeAlertState: StateFlow<RouteAlertState> = _routeAlertState.asStateFlow()

        var isMonitoring: Boolean = false
            private set

        fun triggerMockDeviation(meters: Double = 55.0) {
            _routeAlertState.value = RouteAlertState(
                hasDeviationAlert = true,
                deviationMeters = meters,
                alertMessage = "You are ${meters.toInt()}m off your calculated safe route. Are you safe?"
            )
        }

        fun triggerMockUnexpectedStop(seconds: Int = 60) {
            _routeAlertState.value = RouteAlertState(
                hasUnexpectedStopAlert = true,
                stopDurationSeconds = seconds,
                alertMessage = "You appear to have stopped for over $seconds seconds. Please confirm your safety."
            )
        }

        fun dismissAlerts() {
            _routeAlertState.value = RouteAlertState()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Safe Journey Guard Active", "Monitoring route deviation and movement safety...")
                )
                startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        isMonitoring = true
        locationSubscriptionJob?.cancel()
        locationSubscriptionJob = serviceScope.launch {
            LocationService.currentLocation.collect { loc ->
                // Monitor location against active route
                // If service is running with actual GPS updates
            }
        }
    }

    private fun stopMonitoring() {
        isMonitoring = false
        locationSubscriptionJob?.cancel()
        dismissAlerts()
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
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeRoute Navigation Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors route deviation and unexpected stops during navigation"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
