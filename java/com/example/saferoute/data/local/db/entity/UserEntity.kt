package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long = 1,
    val userId: String = "user_default",
    val name: String = "SafeRoute User",
    val phoneNumber: String = "+15550192831",
    val email: String = "user@saferoute.local",
    val emergencyMessage: String = "SAFE ROUTE EMERGENCY ALERT: I may be in danger. Please track my location.",
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
