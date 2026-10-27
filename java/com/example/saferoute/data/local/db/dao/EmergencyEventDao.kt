package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.EmergencyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyEventDao {
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EmergencyEventEntity>>

    @Query("SELECT * FROM emergency_events WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getActiveEmergency(): EmergencyEventEntity?

    @Query("SELECT * FROM emergency_events WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    fun observeActiveEmergency(): Flow<EmergencyEventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEventEntity): Long

    @Update
    suspend fun updateEvent(event: EmergencyEventEntity)

    @Query("UPDATE emergency_events SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE emergency_events SET audioPath = :audioPath, photoPath = :photoPath WHERE id = :id")
    suspend fun updateMediaPaths(id: Long, audioPath: String?, photoPath: String?)
}
