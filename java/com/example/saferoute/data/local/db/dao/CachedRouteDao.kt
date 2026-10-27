package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saferoute.data.local.db.entity.CachedRouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedRouteDao {
    @Query("SELECT * FROM cached_routes ORDER BY safetyScore DESC")
    fun getAllRoutes(): Flow<List<CachedRouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: CachedRouteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<CachedRouteEntity>)

    @Query("DELETE FROM cached_routes")
    suspend fun clearRoutes()
}
