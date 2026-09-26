package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.TripEntity
import com.example.data.model.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getAllCompletedTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    suspend fun getAllCompletedTripsList(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE status IN ('TRACKING', 'PAUSED') ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    fun getTripByIdFlow(id: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE vehicleId = :vehicleId AND status = 'COMPLETED' ORDER BY startTime DESC")
    fun getTripsByVehicle(vehicleId: Long): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: Long)

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    @Query("SELECT COUNT(*) FROM trips WHERE status = 'COMPLETED'")
    fun getCompletedTripCount(): Flow<Int>

    @Query("SELECT SUM(distanceMeters) FROM trips WHERE status = 'COMPLETED'")
    fun getTotalDistanceMeters(): Flow<Double?>

    @Query("SELECT SUM(durationSeconds) FROM trips WHERE status = 'COMPLETED'")
    fun getTotalDurationSeconds(): Flow<Long?>

    @Query("SELECT MAX(maxSpeedKmh) FROM trips WHERE status = 'COMPLETED'")
    fun getMaxSpeedRecorded(): Flow<Double?>

    @Query("SELECT AVG(avgSpeedKmh) FROM trips WHERE status = 'COMPLETED' AND avgSpeedKmh > 0")
    fun getOverallAvgSpeed(): Flow<Double?>

    @Query("""
        SELECT * FROM trips 
        WHERE status = 'COMPLETED' 
        AND (notes LIKE '%' || :query || '%' 
             OR startAddress LIKE '%' || :query || '%' 
             OR endAddress LIKE '%' || :query || '%')
        ORDER BY startTime DESC
    """)
    fun searchTrips(query: String): Flow<List<TripEntity>>
}
