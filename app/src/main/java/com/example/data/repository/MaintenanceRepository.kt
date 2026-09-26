package com.example.data.repository

import com.example.data.db.dao.MaintenanceDao
import com.example.data.db.entities.MaintenanceEntity
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val maintenanceDao: MaintenanceDao) {
    val allRecords: Flow<List<MaintenanceEntity>> = maintenanceDao.getAllMaintenanceRecords()
    val totalCost: Flow<Double?> = maintenanceDao.getTotalMaintenanceCost()

    fun getRecordsForVehicle(vehicleId: Long): Flow<List<MaintenanceEntity>> =
        maintenanceDao.getMaintenanceForVehicle(vehicleId)

    suspend fun insertRecord(record: MaintenanceEntity): Long = maintenanceDao.insertMaintenance(record)

    suspend fun updateRecord(record: MaintenanceEntity) = maintenanceDao.updateMaintenance(record)

    suspend fun deleteRecord(record: MaintenanceEntity) = maintenanceDao.deleteMaintenance(record)

    suspend fun deleteRecordById(id: Long) = maintenanceDao.deleteMaintenanceById(id)
}
