package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY isDefault DESC, createdAt ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultVehicle(): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE vehicles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultVehicle(id: Long)

    @Query("UPDATE vehicles SET odometerKm = odometerKm + :additionalKm WHERE id = :vehicleId")
    suspend fun addToOdometer(vehicleId: Long, additionalKm: Double)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Long)

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int
}
