package com.example.saferoute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.saferoute.MainActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.sqrt

class ShakeDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Configurable shake parameters per specification:
    // Normal walking should not trigger SOS easily.
    private var shakeThreshold = 14.5f // Acceleration magnitude beyond gravity (~9.8)
    private val shakeCountThreshold = 3
    private val shakeWindowMillis = 1500L
    private val cooldownMillis = 3500L

    private var shakeCount = 0
    private var firstShakeTimestamp = 0L
    private var lastTriggerTimestamp = 0L

    companion object {
        const val CHANNEL_ID = "saferoute_shake_channel"
        const val NOTIFICATION_ID = 1003

        const val ACTION_START = "com.example.saferoute.action.START_SHAKE_DETECTION"
        const val ACTION_STOP = "com.example.saferoute.action.STOP_SHAKE_DETECTION"

        private val _shakeTriggerEvents = MutableSharedFlow<Long>(extraBufferCapacity = 1)
        val shakeTriggerEvents: SharedFlow<Long> = _shakeTriggerEvents.asSharedFlow()

        var isRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopShakeListening()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Shake SOS Guard Active", "Deliberate shake gesture monitoring is armed.")
                )
                startShakeListening()
            }
        }
        return START_STICKY
    }

    private fun startShakeListening() {
        isRunning = true
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("ShakeDetectionService", "Accelerometer listener registered for shake detection")
        } ?: Log.w("ShakeDetectionService", "Accelerometer not available on this device")
    }

    private fun stopShakeListening() {
        isRunning = false
        sensorManager.unregisterListener(this)
        Log.d("ShakeDetectionService", "Accelerometer listener stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Net acceleration magnitude subtracting gravity (~9.8 m/s^2)
        val gForce = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
        val now = System.currentTimeMillis()

        if (now - lastTriggerTimestamp < cooldownMillis) {
            return // Cooldown period active
        }

        if (gForce > shakeThreshold) {
            if (shakeCount == 0) {
                firstShakeTimestamp = now
                shakeCount = 1
            } else {
                if (now - firstShakeTimestamp <= shakeWindowMillis) {
                    shakeCount++
                    if (shakeCount >= shakeCountThreshold) {
                        // Deliberate shake sequence verified!
                        lastTriggerTimestamp = now
                        shakeCount = 0
                        Log.i("ShakeDetectionService", "Deliberate Shake SOS Triggered at $now!")
                        _shakeTriggerEvents.tryEmit(now)

                        // Forward directly to SOSService
                        val sosIntent = Intent(applicationContext, SOSService::class.java).apply {
                            action = SOSService.ACTION_TRIGGER_SOS
                            putExtra(SOSService.EXTRA_TRIGGER_SOURCE, "SHAKE_GESTURE")
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(sosIntent)
                        } else {
                            startService(sosIntent)
                        }
                    }
                } else {
                    // Window expired; restart shake count
                    firstShakeTimestamp = now
                    shakeCount = 1
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeRoute Shake Detection",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Background shake sensor listener for rapid emergency activation"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopShakeListening()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
