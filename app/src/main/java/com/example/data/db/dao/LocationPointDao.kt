package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entities.LocationPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationPointDao {
    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getPointsForTrip(tripId: Long): Flow<List<LocationPointEntity>>

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getPointsForTripSync(tripId: Long): List<LocationPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: LocationPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<LocationPointEntity>)

    @Query("DELETE FROM location_points WHERE tripId = :tripId")
    suspend fun deletePointsForTrip(tripId: Long)

    @Query("SELECT COUNT(*) FROM location_points WHERE tripId = :tripId")
    suspend fun getPointCountForTrip(tripId: Long): Int
}
