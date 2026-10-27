package com.example.saferoute.domain.repository

import com.example.saferoute.domain.model.EmergencyEvent
import com.example.saferoute.domain.model.TriggerType
import kotlinx.coroutines.flow.Flow

interface EmergencyRepository {
    fun getAllEvents(): Flow<List<EmergencyEvent>>
    fun observeActiveEmergency(): Flow<EmergencyEvent?>
    suspend fun triggerEmergency(triggerType: TriggerType, lat: Double, lng: Double, address: String): Long
    suspend fun resolveEmergency(eventId: Long)
    suspend fun cancelEmergency(eventId: Long)
    suspend fun updateMediaPaths(eventId: Long, audioPath: String?, photoPath: String?)
}
