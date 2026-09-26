package com.example.data.repository

import com.example.data.db.dao.FuelDao
import com.example.data.db.entities.FuelRecordEntity
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val fuelDao: FuelDao) {
    val allRecords: Flow<List<FuelRecordEntity>> = fuelDao.getAllFuelRecords()
    val totalCost: Flow<Double?> = fuelDao.getTotalFuelCost()
    val totalLiters: Flow<Double?> = fuelDao.getTotalFuelLiters()

    fun getRecordsForVehicle(vehicleId: Long): Flow<List<FuelRecordEntity>> =
        fuelDao.getFuelRecordsForVehicle(vehicleId)

    suspend fun insertRecord(record: FuelRecordEntity): Long = fuelDao.insertFuelRecord(record)

    suspend fun updateRecord(record: FuelRecordEntity) = fuelDao.updateFuelRecord(record)

    suspend fun deleteRecord(record: FuelRecordEntity) = fuelDao.deleteFuelRecord(record)

    suspend fun deleteRecordById(id: Long) = fuelDao.deleteFuelRecordById(id)
}
