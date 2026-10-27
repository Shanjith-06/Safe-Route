package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazard_reports")
data class HazardReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val severity: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val status: String = "ACTIVE"
)
