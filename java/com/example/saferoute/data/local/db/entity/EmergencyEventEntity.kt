package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerType: String,
    val status: String = "ACTIVE",
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val audioPath: String? = null,
    val photoPath: String? = null,
    val contactsNotifiedCount: Int = 0,
    val notes: String = ""
)
