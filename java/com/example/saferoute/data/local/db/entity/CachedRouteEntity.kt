package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_routes")
data class CachedRouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originName: String,
    val destinationName: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val safetyScore: Double,
    val lightingLevel: String,
    val openFacilitiesCount: Int,
    val riskRating: String,
    val isRecommended: Boolean = true,
    val routeWaypointsJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
