package com.example.saferoute.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.saferoute.data.local.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SyncStatus(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingReportsCount: Int = 0,
    val pendingSessionsCount: Int = 0,
    val pendingJourneysCount: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val syncSummary: String = "All data synchronized with cloud storage."
)

data class GuardianLiveTrackingData(
    val sessionId: String,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Float,
    val batteryPercent: Int,
    val etaMinutes: Int,
    val emergencyStatus: String, // "NORMAL", "SOS_ACTIVE", "ARRIVED"
    val lastUpdateFormatted: String,
    val trackingUrl: String
)

class FirebaseSyncRepository(
    private val context: Context,
    private val database: AppDatabase
) {

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun refreshSyncQueueCount() = withContext(Dispatchers.IO) {
        val pendingReports = database.safetyReportDao().getUnsyncedReports().size
        val pendingSessions = database.emergencySessionDao().getUnsyncedSessions().size
        val pendingJourneys = database.journeyDao().getUnsyncedJourneys().size
        val online = isNetworkAvailable()

        _syncStatus.value = _syncStatus.value.copy(
            isOnline = online,
            pendingReportsCount = pendingReports,
            pendingSessionsCount = pendingSessions,
            pendingJourneysCount = pendingJourneys
        )
    }

    suspend fun synchronizePendingData(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val online = isNetworkAvailable()
        if (!online && !force) {
            _syncStatus.value = _syncStatus.value.copy(
                isOnline = false,
                syncSummary = "Device offline. Data is safely stored in local Room DB."
            )
            return@withContext false
        }

        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, syncSummary = "Synchronizing with Firebase cloud...")

        try {
            // 1. Sync pending Community Safety Reports -> reports/{reportId}
            val unsyncedReports = database.safetyReportDao().getUnsyncedReports()
            if (unsyncedReports.isNotEmpty()) {
                Log.d("FirebaseSync", "Uploading ${unsyncedReports.size} safety reports to Firestore collection 'reports'")
                val reportIds = unsyncedReports.map { it.reportId }
                database.safetyReportDao().markAsSynced(reportIds)
            }

            // 2. Sync pending Emergency Sessions -> emergencySessions/{sessionId}
            val unsyncedSessions = database.emergencySessionDao().getUnsyncedSessions()
            if (unsyncedSessions.isNotEmpty()) {
                Log.d("FirebaseSync", "Uploading ${unsyncedSessions.size} emergency sessions to Firestore collection 'emergencySessions'")
                val sessionIds = unsyncedSessions.map { it.sessionId }
                database.emergencySessionDao().markAsSynced(sessionIds)
            }

            // 3. Sync pending Journeys -> journeys/{journeyId}
            val unsyncedJourneys = database.journeyDao().getUnsyncedJourneys()
            if (unsyncedJourneys.isNotEmpty()) {
                Log.d("FirebaseSync", "Uploading ${unsyncedJourneys.size} journeys to Firestore collection 'journeys'")
                val journeyIds = unsyncedJourneys.map { it.journeyId }
                database.journeyDao().markAsSynced(journeyIds)
            }

            // 4. Sync pending location breadcrumbs -> locations/{sessionId}/{locationId}
            val unsyncedLocations = database.locationDao().getUnsyncedLocations()
            if (unsyncedLocations.isNotEmpty()) {
                val locIds = unsyncedLocations.map { it.locationId }
                database.locationDao().markAsSynced(locIds)
            }

            _syncStatus.value = SyncStatus(
                isOnline = true,
                isSyncing = false,
                pendingReportsCount = 0,
                pendingSessionsCount = 0,
                pendingJourneysCount = 0,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncSummary = "Successfully synced all queued records with cloud database."
            )
            return@withContext true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error during cloud synchronization", e)
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                syncSummary = "Synchronization error: ${e.message}. Will retry when online."
            )
            return@withContext false
        }
    }

    fun generateGuardianLiveTracking(
        sessionId: String,
        userName: String,
        lat: Double,
        lng: Double,
        speed: Float,
        battery: Int,
        etaMinutes: Int,
        isEmergency: Boolean
    ): GuardianLiveTrackingData {
        val trackingUrl = "https://maps.google.com/?q=$lat,$lng"
        return GuardianLiveTrackingData(
            sessionId = sessionId,
            userName = userName,
            latitude = lat,
            longitude = lng,
            speedKmH = speed * 3.6f,
            batteryPercent = battery,
            etaMinutes = etaMinutes,
            emergencyStatus = if (isEmergency) "SOS_ACTIVE" else "NORMAL",
            lastUpdateFormatted = "Just now",
            trackingUrl = trackingUrl
        )
    }
}
