package com.example.saferoute.domain.model

data class EmergencyEvent(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerType: TriggerType,
    val status: EmergencyStatus = EmergencyStatus.ACTIVE,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val audioPath: String? = null,
    val photoPath: String? = null,
    val contactsNotifiedCount: Int = 0,
    val notes: String = ""
)

enum class TriggerType {
    MANUAL_SOS,
    SHAKE_GESTURE,
    VOICE_TRIGGER,
    ROUTE_DEVIATION,
    UNEXPECTED_STOP
}

enum class EmergencyStatus {
    ACTIVE,
    DISPATCHED,
    RESOLVED,
    CANCELLED
}
