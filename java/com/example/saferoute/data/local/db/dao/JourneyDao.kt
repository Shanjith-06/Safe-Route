package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.JourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journeys ORDER BY startTime DESC")
    fun getAllJourneysFlow(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE journeyId = :id LIMIT 1")
    suspend fun getJourneyById(id: String): JourneyEntity?

    @Query("SELECT * FROM journeys WHERE status = 'IN_PROGRESS' ORDER BY startTime DESC LIMIT 1")
    fun getActiveJourneyFlow(): Flow<JourneyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity)

    @Update
    suspend fun updateJourney(journey: JourneyEntity)

    @Query("SELECT * FROM journeys WHERE isSynced = 0")
    suspend fun getUnsyncedJourneys(): List<JourneyEntity>

    @Query("UPDATE journeys SET isSynced = 1 WHERE journeyId IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
