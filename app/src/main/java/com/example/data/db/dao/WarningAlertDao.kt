package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entities.WarningAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarningAlertDao {
    @Query("SELECT * FROM warning_alerts ORDER BY timestamp DESC LIMIT 100")
    fun getAllAlerts(): Flow<List<WarningAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WarningAlertEntity): Long

    @Query("DELETE FROM warning_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)

    @Query("DELETE FROM warning_alerts")
    suspend fun clearAllAlerts()
}
