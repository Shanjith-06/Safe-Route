package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.EmergencySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencySessionDao {
    @Query("SELECT * FROM emergency_sessions ORDER BY startTimestamp DESC")
    fun getAllSessionsFlow(): Flow<List<EmergencySessionEntity>>

    @Query("SELECT * FROM emergency_sessions WHERE status = 'ACTIVE' ORDER BY startTimestamp DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<EmergencySessionEntity?>

    @Query("SELECT * FROM emergency_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: String): EmergencySessionEntity?

    @Query("SELECT * FROM emergency_sessions WHERE status = 'ACTIVE' ORDER BY startTimestamp DESC LIMIT 1")
    suspend fun getActiveSession(): EmergencySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: EmergencySessionEntity)

    @Update
    suspend fun updateSession(session: EmergencySessionEntity)

    @Query("SELECT * FROM emergency_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<EmergencySessionEntity>

    @Query("UPDATE emergency_sessions SET isSynced = 1 WHERE sessionId IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
