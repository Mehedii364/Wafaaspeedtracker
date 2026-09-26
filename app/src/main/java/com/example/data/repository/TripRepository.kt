package com.example.data.repository

import com.example.data.db.dao.LocationPointDao
import com.example.data.db.dao.SpeedEventDao
import com.example.data.db.dao.TripDao
import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.SpeedEventEntity
import com.example.data.db.entities.TripEntity
import com.example.data.model.TripStatus
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val locationPointDao: LocationPointDao,
    private val speedEventDao: SpeedEventDao
) {
    val completedTrips: Flow<List<TripEntity>> = tripDao.getAllCompletedTrips()
    val totalTripsCount: Flow<Int> = tripDao.getCompletedTripCount()
    val totalDistanceMeters: Flow<Double?> = tripDao.getTotalDistanceMeters()
    val totalDurationSeconds: Flow<Long?> = tripDao.getTotalDurationSeconds()
    val maxSpeedOverall: Flow<Double?> = tripDao.getMaxSpeedRecorded()
    val avgSpeedOverall: Flow<Double?> = tripDao.getOverallAvgSpeed()

    suspend fun getActiveTrip(): TripEntity? = tripDao.getActiveTrip()

    suspend fun getTripById(id: Long): TripEntity? = tripDao.getTripById(id)

    fun getTripByIdFlow(id: Long): Flow<TripEntity?> = tripDao.getTripByIdFlow(id)

    fun getPointsForTrip(tripId: Long): Flow<List<LocationPointEntity>> =
        locationPointDao.getPointsForTrip(tripId)

    suspend fun getPointsForTripSync(tripId: Long): List<LocationPointEntity> =
        locationPointDao.getPointsForTripSync(tripId)

    fun getSpeedEventsForTrip(tripId: Long): Flow<List<SpeedEventEntity>> =
        speedEventDao.getEventsForTrip(tripId)

    suspend fun insertTrip(trip: TripEntity): Long = tripDao.insertTrip(trip)

    suspend fun updateTrip(trip: TripEntity) = tripDao.updateTrip(trip)

    suspend fun deleteTrip(trip: TripEntity) = tripDao.deleteTrip(trip)

    suspend fun deleteTripById(id: Long) = tripDao.deleteTripById(id)

    suspend fun deleteAllTrips() = tripDao.deleteAllTrips()

    suspend fun insertLocationPoint(point: LocationPointEntity): Long =
        locationPointDao.insertPoint(point)

    suspend fun insertSpeedEvent(event: SpeedEventEntity): Long =
        speedEventDao.insertEvent(event)

    fun searchTrips(query: String): Flow<List<TripEntity>> = tripDao.searchTrips(query)

    suspend fun getAllCompletedTripsList(): List<TripEntity> =
        tripDao.getAllCompletedTripsList()
}
