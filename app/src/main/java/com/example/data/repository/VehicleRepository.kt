package com.example.data.repository

import com.example.data.db.dao.VehicleDao
import com.example.data.db.entities.VehicleEntity
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    suspend fun getVehicleById(id: Long): VehicleEntity? = vehicleDao.getVehicleById(id)

    suspend fun getDefaultVehicle(): VehicleEntity? = vehicleDao.getDefaultVehicle()

    suspend fun insertVehicle(vehicle: VehicleEntity): Long {
        if (vehicle.isDefault) {
            vehicleDao.clearDefaultFlags()
        }
        val id = vehicleDao.insertVehicle(vehicle)
        // If this is the only vehicle, make it default
        if (vehicleDao.getVehicleCount() == 1) {
            vehicleDao.setDefaultVehicle(id)
        }
        return id
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) {
        if (vehicle.isDefault) {
            vehicleDao.clearDefaultFlags()
        }
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun setDefaultVehicle(id: Long) {
        vehicleDao.clearDefaultFlags()
        vehicleDao.setDefaultVehicle(id)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) = vehicleDao.deleteVehicle(vehicle)

    suspend fun deleteVehicleById(id: Long) = vehicleDao.deleteVehicleById(id)

    suspend fun ensureDefaultVehicleExists() {
        if (vehicleDao.getVehicleCount() == 0) {
            vehicleDao.insertVehicle(
                VehicleEntity(
                    name = "Primary Car",
                    type = VehicleType.CAR,
                    registrationNumber = "WAFA-01",
                    odometerKm = 0.0,
                    notes = "Default vehicle for trips",
                    isDefault = true
                )
            )
        }
    }
}
