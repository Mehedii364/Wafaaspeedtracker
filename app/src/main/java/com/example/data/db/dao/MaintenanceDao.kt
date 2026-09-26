package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.MaintenanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records ORDER BY timestamp DESC")
    fun getAllMaintenanceRecords(): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getMaintenanceForVehicle(vehicleId: Long): Flow<List<MaintenanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(record: MaintenanceEntity): Long

    @Update
    suspend fun updateMaintenance(record: MaintenanceEntity)

    @Delete
    suspend fun deleteMaintenance(record: MaintenanceEntity)

    @Query("DELETE FROM maintenance_records WHERE id = :id")
    suspend fun deleteMaintenanceById(id: Long)

    @Query("SELECT SUM(cost) FROM maintenance_records")
    fun getTotalMaintenanceCost(): Flow<Double?>
}
