package com.example.saferoute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.saferoute.MainActivity
import com.example.saferoute.data.local.datastore.UserPreferencesRepository
import com.example.saferoute.data.local.db.AppDatabase
import com.example.saferoute.data.local.db.entity.EmergencySessionEntity
import com.example.saferoute.data.repository.ContactsRepositoryImpl
import com.example.saferoute.domain.sos.DispatchResult
import com.example.saferoute.domain.sos.SmsDispatchEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveEmergencyState(
    val isActive: Boolean = false,
    val sessionId: String = "",
    val triggerSource: String = "MANUAL_BUTTON",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val batteryLevel: Int = 85,
    val elapsedSeconds: Int = 0,
    val contactsNotifiedCount: Int = 0,
    val smsStatusMessage: String = "Dispatching alerts...",
    val evidenceStatus: String = "RECORDING", // RECORDING, SAVED, FAILED
    val isDemoMode: Boolean = true
)

class SOSService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var currentSessionId: String = ""

    companion object {
        const val CHANNEL_ID = "saferoute_emergency_channel"
        const val NOTIFICATION_ID = 9999

        const val ACTION_TRIGGER_SOS = "com.example.saferoute.action.TRIGGER_SOS"
        const val ACTION_CANCEL_SOS = "com.example.saferoute.action.CANCEL_SOS"
        const val EXTRA_TRIGGER_SOURCE = "extra_trigger_source"

        private val _emergencyState = MutableStateFlow(ActiveEmergencyState())
        val emergencyState: StateFlow<ActiveEmergencyState> = _emergencyState.asStateFlow()

        var isEmergencyActive: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL_SOS -> {
                cancelEmergency()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TRIGGER_SOS -> {
                val source = intent.getStringExtra(EXTRA_TRIGGER_SOURCE) ?: "MANUAL_BUTTON"
                triggerEmergency(source)
            }
            else -> {
                if (isEmergencyActive) {
                    startForegroundNotification()
                }
            }
        }
        return START_STICKY
    }

    private fun triggerEmergency(triggerSource: String) {
        isEmergencyActive = true
        currentSessionId = "sos_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"

        startForegroundNotification()

        // 1. Obtain current GPS location
        val loc = LocationService.currentLocation.value
        val lat = loc.latitude
        val lng = loc.longitude

        // 2. Start Location tracking service
        val locIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(locIntent)
        } else {
            startService(locIntent)
        }

        // 3. Start Evidence Recording (~10s audio + photo)
        val eviIntent = Intent(this, EvidenceRecordingService::class.java).apply {
            action = EvidenceRecordingService.ACTION_START_RECORDING
            putExtra(EvidenceRecordingService.EXTRA_SESSION_ID, currentSessionId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(eviIntent)
        } else {
            startService(eviIntent)
        }

        // Initial State
        _emergencyState.value = ActiveEmergencyState(
            isActive = true,
            sessionId = currentSessionId,
            triggerSource = triggerSource,
            latitude = lat,
            longitude = lng,
            batteryLevel = 85,
            elapsedSeconds = 0,
            evidenceStatus = "RECORDING"
        )

        // 4. Timer loop for elapsed time & battery update
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var seconds = 0
            while (isEmergencyActive) {
                delay(1000L)
                seconds++
                val currLoc = LocationService.currentLocation.value
                val eviState = EvidenceRecordingService.evidenceState.value.name
                _emergencyState.value = _emergencyState.value.copy(
                    elapsedSeconds = seconds,
                    latitude = currLoc.latitude,
                    longitude = currLoc.longitude,
                    evidenceStatus = if (eviState == "SAVED") "SAVED" else "RECORDING"
                )
            }
        }

        // 5. Notify trusted contacts via SMS Engine and persist to Room DB
        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val prefs = UserPreferencesRepository(applicationContext)
            val settings = prefs.userSettingsFlow.first()
            val contactsRepo = ContactsRepositoryImpl(db.trustedContactDao())
            val smsEngine = SmsDispatchEngine(applicationContext, contactsRepo)

            val battery = smsEngine.getBatteryPercentage()

            // Insert initial emergency session
            val session = EmergencySessionEntity(
                sessionId = currentSessionId,
                userId = "usr_sr_01",
                startTimestamp = System.currentTimeMillis(),
                status = "ACTIVE",
                triggerSource = triggerSource,
                startLatitude = lat,
                startLongitude = lng,
                lastLatitude = lat,
                lastLongitude = lng,
                batteryLevel = battery,
                smsStatus = if (settings.isDemoModeEnabled) "DEMO_SIMULATED" else "PENDING",
                isSynced = false
            )
            db.emergencySessionDao().insertSession(session)

            // Dispatch SMS (or demo simulation)
            val dispatchResult: DispatchResult = smsEngine.dispatchEmergencySms(
                lat = lat,
                lng = lng,
                userName = settings.userName,
                isDemoMode = settings.isDemoModeEnabled,
                isSimulatedOffline = settings.isOfflineModeSimulated
            )

            val smsStatusDesc = if (dispatchResult.isDemoMode) {
                "DEMO MODE: ${dispatchResult.successCount} SMS simulated"
            } else if (!dispatchResult.isCellularAvailable) {
                "Offline: SMS queued for sync"
            } else {
                "${dispatchResult.successCount} Emergency SMS dispatched"
            }

            // Update session in DB
            db.emergencySessionDao().updateSession(
                session.copy(
                    smsStatus = if (dispatchResult.isDemoMode) "DEMO_SIMULATED" else if (dispatchResult.successCount > 0) "SENT" else "FAILED",
                    batteryLevel = battery
                )
            )

            _emergencyState.value = _emergencyState.value.copy(
                batteryLevel = battery,
                contactsNotifiedCount = dispatchResult.successCount,
                smsStatusMessage = smsStatusDesc,
                isDemoMode = settings.isDemoModeEnabled
            )

            Log.i("SOSService", "SOS session initialized: $currentSessionId. SMS: $smsStatusDesc")
        }
    }

    private fun cancelEmergency() {
        isEmergencyActive = false
        timerJob?.cancel()

        // Stop evidence recording if still running
        val eviIntent = Intent(this, EvidenceRecordingService::class.java).apply {
            action = EvidenceRecordingService.ACTION_STOP_RECORDING
        }
        startService(eviIntent)

        // Update DB session to CANCELLED
        val sessionIdToClose = currentSessionId
        serviceScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val session = db.emergencySessionDao().getSessionById(sessionIdToClose)
                session?.let {
                    val audioPath = EvidenceRecordingService.lastAudioPath.value
                    db.emergencySessionDao().updateSession(
                        it.copy(
                            endTimestamp = System.currentTimeMillis(),
                            status = "CANCELLED",
                            audioEvidencePath = audioPath
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("SOSService", "Error closing emergency session in DB", e)
            }
        }

        _emergencyState.value = ActiveEmergencyState(isActive = false)
        Log.i("SOSService", "Emergency session cancelled by user: $sessionIdToClose")
    }

    private fun startForegroundNotification() {
        val notification = buildEmergencyNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildEmergencyNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(this, SOSService::class.java).apply {
            action = ACTION_CANCEL_SOS
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 EMERGENCY SOS ACTIVE")
            .setContentText("SafeRoute tracking & evidence collection is active.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setColor(0xFFFF3B6B.toInt()) // Vibrant emergency pink/red
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "CANCEL SOS", cancelPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeRoute Emergency SOS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alert channel for active SOS emergencies"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
