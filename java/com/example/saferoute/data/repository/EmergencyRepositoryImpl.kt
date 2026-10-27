package com.example.saferoute.data.repository

import com.example.saferoute.data.local.db.dao.EmergencyEventDao
import com.example.saferoute.data.local.db.entity.EmergencyEventEntity
import com.example.saferoute.domain.model.EmergencyEvent
import com.example.saferoute.domain.model.EmergencyStatus
import com.example.saferoute.domain.model.TriggerType
import com.example.saferoute.domain.repository.EmergencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmergencyRepositoryImpl(
    private val dao: EmergencyEventDao
) : EmergencyRepository {

    override fun getAllEvents(): Flow<List<EmergencyEvent>> {
        return dao.getAllEvents().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun observeActiveEmergency(): Flow<EmergencyEvent?> {
        return dao.observeActiveEmergency().map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun triggerEmergency(
        triggerType: TriggerType,
        lat: Double,
        lng: Double,
        address: String
    ): Long {
        val entity = EmergencyEventEntity(
            triggerType = triggerType.name,
            status = EmergencyStatus.ACTIVE.name,
            latitude = lat,
            longitude = lng,
            address = address,
            timestamp = System.currentTimeMillis()
        )
        return dao.insertEvent(entity)
    }

    override suspend fun resolveEmergency(eventId: Long) {
        dao.updateStatus(eventId, EmergencyStatus.RESOLVED.name)
    }

    override suspend fun cancelEmergency(eventId: Long) {
        dao.updateStatus(eventId, EmergencyStatus.CANCELLED.name)
    }

    override suspend fun updateMediaPaths(eventId: Long, audioPath: String?, photoPath: String?) {
        dao.updateMediaPaths(eventId, audioPath, photoPath)
    }

    private fun EmergencyEventEntity.toDomainModel() = EmergencyEvent(
        id = id,
        timestamp = timestamp,
        triggerType = runCatching { TriggerType.valueOf(triggerType) }.getOrDefault(TriggerType.MANUAL_SOS),
        status = runCatching { EmergencyStatus.valueOf(status) }.getOrDefault(EmergencyStatus.ACTIVE),
        latitude = latitude,
        longitude = longitude,
        address = address,
        audioPath = audioPath,
        photoPath = photoPath,
        contactsNotifiedCount = contactsNotifiedCount,
        notes = notes
    )
}
