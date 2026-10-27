package com.example.saferoute.domain.model

data class TrustedContact(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val isEnabled: Boolean = true,
    val sendSms: Boolean = true,
    val sendLocation: Boolean = true
)
