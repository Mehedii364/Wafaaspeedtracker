package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.FuelRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_records ORDER BY timestamp DESC")
    fun getAllFuelRecords(): Flow<List<FuelRecordEntity>>

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getFuelRecordsForVehicle(vehicleId: Long): Flow<List<FuelRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: FuelRecordEntity): Long

    @Update
    suspend fun updateFuelRecord(record: FuelRecordEntity)

    @Delete
    suspend fun deleteFuelRecord(record: FuelRecordEntity)

    @Query("DELETE FROM fuel_records WHERE id = :id")
    suspend fun deleteFuelRecordById(id: Long)

    @Query("SELECT SUM(totalCost) FROM fuel_records")
    fun getTotalFuelCost(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM fuel_records")
    fun getTotalFuelLiters(): Flow<Double?>
}
