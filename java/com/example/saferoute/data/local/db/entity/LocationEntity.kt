package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_breadcrumbs")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val locationId: Long = 0,
    val sessionId: String, // Can be journeyId or emergency sessionId
    val latitude: Double,
    val longitude: Double,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int = 100,
    val isSynced: Boolean = false
)
