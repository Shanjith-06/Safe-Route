package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.HazardReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HazardReportDao {
    @Query("SELECT * FROM hazard_reports ORDER BY timestamp DESC")
    fun getAllHazards(): Flow<List<HazardReportEntity>>

    @Query("SELECT * FROM hazard_reports WHERE status = 'ACTIVE' ORDER BY timestamp DESC")
    fun getActiveHazards(): Flow<List<HazardReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: HazardReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazards(hazards: List<HazardReportEntity>)

    @Update
    suspend fun updateHazard(hazard: HazardReportEntity)

    @Query("UPDATE hazard_reports SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("DELETE FROM hazard_reports WHERE id = :id")
    suspend fun deleteHazard(id: Long)
}
