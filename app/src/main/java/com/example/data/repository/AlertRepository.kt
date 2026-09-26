package com.example.data.repository

import com.example.data.db.dao.WarningAlertDao
import com.example.data.db.entities.WarningAlertEntity
import com.example.data.model.WarningLevel
import com.example.data.model.WarningType
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val warningAlertDao: WarningAlertDao) {
    val allAlerts: Flow<List<WarningAlertEntity>> = warningAlertDao.getAllAlerts()

    suspend fun logAlert(
        type: WarningType,
        level: WarningLevel,
        message: String,
        tripId: Long? = null
    ): Long {
        val alert = WarningAlertEntity(
            tripId = tripId,
            timestamp = System.currentTimeMillis(),
            type = type,
            level = level,
            message = message
        )
        return warningAlertDao.insertAlert(alert)
    }

    suspend fun deleteAlert(id: Long) = warningAlertDao.deleteAlertById(id)

    suspend fun clearAll() = warningAlertDao.clearAllAlerts()
}
