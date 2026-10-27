package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saferoute.data.local.db.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_breadcrumbs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLocationsForSessionFlow(sessionId: String): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location_breadcrumbs WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocationFlow(sessionId: String): Flow<LocationEntity?>

    @Query("SELECT * FROM location_breadcrumbs WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLocation(sessionId: String): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM location_breadcrumbs WHERE isSynced = 0")
    suspend fun getUnsyncedLocations(): List<LocationEntity>

    @Query("UPDATE location_breadcrumbs SET isSynced = 1 WHERE locationId IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}
