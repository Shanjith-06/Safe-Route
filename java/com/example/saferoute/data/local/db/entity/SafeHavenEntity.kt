package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safe_havens")
data class SafeHavenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isVerified: Boolean = true,
    val isOpen24_7: Boolean = true,
    val phoneNumber: String = "",
    val distanceMeters: Double = 0.0
)
