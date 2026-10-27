package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_reports")
data class SafetyReportEntity(
    @PrimaryKey
    val reportId: String,
    val type: String, // Broken Street Light, Unsafe Area, Suspicious Activity, Police Checkpoint, Safe Zone, Closed Facility, Other
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val severity: Int = 2, // 1: Low, 2: Medium, 3: High
    val photoUri: String? = null,
    val isSynced: Boolean = false,
    val status: String = "ACTIVE"
)
