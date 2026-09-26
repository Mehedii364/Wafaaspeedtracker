package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entities.SpeedEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedEventDao {
    @Query("SELECT * FROM speed_events WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getEventsForTrip(tripId: Long): Flow<List<SpeedEventEntity>>

    @Query("SELECT * FROM speed_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SpeedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SpeedEventEntity): Long

    @Query("DELETE FROM speed_events WHERE tripId = :tripId")
    suspend fun deleteEventsForTrip(tripId: Long)

    @Query("SELECT COUNT(*) FROM speed_events")
    fun getTotalEventCount(): Flow<Int>
}
