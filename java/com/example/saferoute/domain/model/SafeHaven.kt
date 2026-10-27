package com.example.saferoute.domain.model

data class SafeHaven(
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
