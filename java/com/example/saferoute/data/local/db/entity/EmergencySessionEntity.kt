package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_sessions")
data class EmergencySessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String = "user_default",
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val status: String = "ACTIVE", // ACTIVE, CANCELLED, RESOLVED
    val triggerSource: String = "MANUAL_BUTTON", // MANUAL_BUTTON, SHAKE_GESTURE, VOICE_KEYWORD, DEVIATION_ALERT, UNEXPECTED_STOP
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val lastLatitude: Double = 0.0,
    val lastLongitude: Double = 0.0,
    val batteryLevel: Int = 100,
    val smsStatus: String = "NOT_SENT", // NOT_SENT, SENT, FAILED, DEMO_SIMULATED
    val audioEvidencePath: String? = null,
    val photoEvidencePath: String? = null,
    val isSynced: Boolean = false
)
