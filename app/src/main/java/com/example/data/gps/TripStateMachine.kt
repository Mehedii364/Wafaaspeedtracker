package com.example.data.gps

import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.SpeedEventEntity
import com.example.data.db.entities.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.WarningLevel
import com.example.data.model.WarningType
import com.example.data.repository.AlertRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TripStateMachine(
    private val tripRepository: TripRepository,
    private val vehicleRepository: VehicleRepository,
    private val alertRepository: AlertRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val mutex = Mutex()

    private val _status = MutableStateFlow(TripStatus.IDLE)
    val status: StateFlow<TripStatus> = _status.asStateFlow()

    private val _activeMetrics = MutableStateFlow(ActiveTripMetrics())
    val activeMetrics: StateFlow<ActiveTripMetrics> = _activeMetrics.asStateFlow()

    private val _liveGps = MutableStateFlow(LiveGpsData())
    val liveGps: StateFlow<LiveGpsData> = _liveGps.asStateFlow()

    private val _speedWarningEvent = MutableSharedFlow<Double>()
    val speedWarningEvent: SharedFlow<Double> = _speedWarningEvent.asSharedFlow()

    private var activeTripId: Long = 0L
    private var activeVehicleId: Long? = null
    private var tripStartTime: Long = 0L
    private var totalDistanceMeters: Double = 0.0
    private var maxSpeedKmh: Double = 0.0
    private var speedSumKmh: Double = 0.0
    private var speedSampleCount: Long = 0L
    private var movingSeconds: Long = 0L
    private var stoppedSeconds: Long = 0L
    private var highSpeedCount: Int = 0
    private var currentSpeedLimitKmh: Double = 60.0

    private var lastRecordedPoint: LocationPointEntity? = null
    private var lastGpsUpdateTime: Long = 0L
    private var tickerJob: Job? = null
    private var isOverSpeedingCurrently: Boolean = false
    private var overSpeedStartTime: Long = 0L

    init {
        startTimerTicker()
    }

    private fun startTimerTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                mutex.withLock {
                    val currentStatus = _status.value
                    val now = System.currentTimeMillis()

                    // Check GPS signal staleness
                    if (_liveGps.value.isAvailable && (now - lastGpsUpdateTime > GpsFilter.STALE_LOCATION_THRESHOLD_MS)) {
                        _liveGps.value = _liveGps.value.copy(isSignalLost = true)
                    }

                    if (currentStatus == TripStatus.TRACKING) {
                        val isMovingNow = _liveGps.value.speedKmh >= GpsFilter.STOPPED_SPEED_THRESHOLD_KMH
                        if (isMovingNow) {
                            movingSeconds++
                        } else {
                            stoppedSeconds++
                        }

                        val duration = (now - tripStartTime) / 1000L
                        val avgSpeed = if (movingSeconds > 0) {
                            (totalDistanceMeters / movingSeconds) * 3.6
                        } else 0.0

                        _activeMetrics.value = _activeMetrics.value.copy(
                            durationSeconds = duration,
                            movingTimeSeconds = movingSeconds,
                            stoppedTimeSeconds = stoppedSeconds,
                            avgSpeedKmh = avgSpeed
                        )
                    }
                }
            }
        }
    }

    /**
     * Strict check: handles incoming GPS fix.
     * When IDLE: only live GPS telemetry is updated. ZERO recording or trip accumulation.
     */
    suspend fun onGpsLocation(
        lat: Double,
        lon: Double,
        speedMps: Float,
        bearing: Float,
        alt: Double,
        acc: Float,
        time: Long
    ) = mutex.withLock {
        lastGpsUpdateTime = System.currentTimeMillis()
        val speedKmh = (speedMps * 3.6).coerceAtLeast(0.0)
        val isAccuracyPoor = !GpsFilter.isAccuracyAcceptable(acc)
        val isMoving = speedKmh >= GpsFilter.STOPPED_SPEED_THRESHOLD_KMH

        // Update live GPS state for dashboard viewing
        _liveGps.value = LiveGpsData(
            latitude = lat,
            longitude = lon,
            speedKmh = speedKmh,
            bearing = bearing,
            altitude = alt,
            accuracy = acc,
            timestamp = time,
            isAvailable = true,
            isSignalLost = false,
            isAccuracyPoor = isAccuracyPoor,
            isMoving = isMoving
        )

        // STRICT RECORDING RULE: NO START = NO RECORDING!
        if (_status.value != TripStatus.TRACKING) {
            return@withLock
        }

        // If here, trip is actively TRACKING
        // Sanity & Anomaly filters
        if (!GpsFilter.isSpeedReasonable(speedKmh)) {
            alertRepository.logAlert(
                type = WarningType.DATA_ANOMALY,
                level = WarningLevel.WARNING,
                message = "Filtered anomalous speed reading: ${speedKmh.toInt()} km/h",
                tripId = activeTripId
            )
            return@withLock
        }

        // Check jump anomaly
        lastRecordedPoint?.let { prev ->
            if (GpsFilter.isJumpAnomaly(prev.latitude, prev.longitude, prev.timestamp, lat, lon, time)) {
                alertRepository.logAlert(
                    type = WarningType.DATA_ANOMALY,
                    level = WarningLevel.WARNING,
                    message = "Unfeasible GPS teleport jump detected and filtered",
                    tripId = activeTripId
                )
                return@withLock
            }
        }

        // Calculate distance increment if not stationary drift and accuracy is decent
        var distanceDelta = 0.0
        lastRecordedPoint?.let { prev ->
            if (!isAccuracyPoor && isMoving) {
                val d = GpsFilter.calculateDistanceMeters(prev.latitude, prev.longitude, lat, lon)
                // Filter out small jitter < 1.5m
                if (d >= 1.5) {
                    distanceDelta = d
                    totalDistanceMeters += d
                }
            }
        }

        // Update speed records
        if (speedKmh > maxSpeedKmh) {
            maxSpeedKmh = speedKmh
        }
        if (isMoving) {
            speedSumKmh += speedKmh
            speedSampleCount++
        }

        // High-speed limit monitoring
        val isOverSpeed = speedKmh > currentSpeedLimitKmh
        if (isOverSpeed) {
            if (!isOverSpeedingCurrently) {
                isOverSpeedingCurrently = true
                overSpeedStartTime = time
                highSpeedCount++
                _speedWarningEvent.emit(speedKmh)
                alertRepository.logAlert(
                    type = WarningType.SPEED_LIMIT,
                    level = WarningLevel.CRITICAL,
                    message = "Speed limit of ${currentSpeedLimitKmh.toInt()} km/h exceeded: ${speedKmh.toInt()} km/h",
                    tripId = activeTripId
                )
            }
        } else {
            if (isOverSpeedingCurrently) {
                // Event finished, save high speed duration
                val durationSec = ((time - overSpeedStartTime) / 1000L).coerceAtLeast(1L)
                tripRepository.insertSpeedEvent(
                    SpeedEventEntity(
                        tripId = activeTripId,
                        timestamp = overSpeedStartTime,
                        latitude = lat,
                        longitude = lon,
                        speedKmh = maxSpeedKmh,
                        speedLimitKmh = currentSpeedLimitKmh,
                        durationSeconds = durationSec
                    )
                )
                isOverSpeedingCurrently = false
            }
        }

        // Store location point in database
        val pointEntity = LocationPointEntity(
            tripId = activeTripId,
            latitude = lat,
            longitude = lon,
            speedKmh = speedKmh,
            bearing = bearing,
            altitude = alt,
            accuracy = acc,
            timestamp = time,
            isStopPoint = !isMoving
        )
        tripRepository.insertLocationPoint(pointEntity)
        lastRecordedPoint = pointEntity

        // Update metrics state flow
        val currentPoints = _activeMetrics.value.recentPoints.takeLast(100).toMutableList()
        currentPoints.add(pointEntity)

        val duration = ((time - tripStartTime) / 1000L).coerceAtLeast(0L)
        val avgSpeed = if (movingSeconds > 0) (totalDistanceMeters / movingSeconds) * 3.6 else 0.0

        _activeMetrics.value = _activeMetrics.value.copy(
            distanceMeters = totalDistanceMeters,
            durationSeconds = duration,
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = maxSpeedKmh,
            avgSpeedKmh = avgSpeed,
            isOverSpeed = isOverSpeed,
            highSpeedEventsCount = highSpeedCount,
            recordedPointsCount = _activeMetrics.value.recordedPointsCount + 1,
            recentPoints = currentPoints
        )
    }

    /**
     * Explicit START TRIP action.
     */
    suspend fun startTrip(vehicleId: Long?, speedLimitKmh: Double = 60.0): Long = mutex.withLock {
        if (_status.value == TripStatus.TRACKING) {
            return activeTripId
        }

        activeVehicleId = vehicleId
        currentSpeedLimitKmh = speedLimitKmh
        tripStartTime = System.currentTimeMillis()
        totalDistanceMeters = 0.0
        maxSpeedKmh = 0.0
        speedSumKmh = 0.0
        speedSampleCount = 0L
        movingSeconds = 0L
        stoppedSeconds = 0L
        highSpeedCount = 0
        lastRecordedPoint = null
        isOverSpeedingCurrently = false

        val initialGps = _liveGps.value

        val newTrip = TripEntity(
            vehicleId = vehicleId,
            startTime = tripStartTime,
            endTime = 0L,
            distanceMeters = 0.0,
            durationSeconds = 0L,
            movingTimeSeconds = 0L,
            stoppedTimeSeconds = 0L,
            maxSpeedKmh = 0.0,
            avgSpeedKmh = 0.0,
            status = TripStatus.TRACKING,
            startLatitude = initialGps.latitude,
            startLongitude = initialGps.longitude,
            speedLimitKmh = speedLimitKmh
        )

        activeTripId = tripRepository.insertTrip(newTrip)
        _status.value = TripStatus.TRACKING

        _activeMetrics.value = ActiveTripMetrics(
            tripId = activeTripId,
            vehicleId = vehicleId,
            status = TripStatus.TRACKING,
            startTime = tripStartTime,
            speedLimitKmh = speedLimitKmh,
            recentPoints = emptyList()
        )

        return activeTripId
    }

    /**
     * Pause active trip.
     */
    suspend fun pauseTrip(): Unit = mutex.withLock {
        if (_status.value != TripStatus.TRACKING) return@withLock
        _status.value = TripStatus.PAUSED
        _activeMetrics.value = _activeMetrics.value.copy(status = TripStatus.PAUSED)

        tripRepository.getTripById(activeTripId)?.let { trip ->
            tripRepository.updateTrip(
                trip.copy(
                    status = TripStatus.PAUSED,
                    distanceMeters = totalDistanceMeters,
                    durationSeconds = _activeMetrics.value.durationSeconds,
                    movingTimeSeconds = movingSeconds,
                    stoppedTimeSeconds = stoppedSeconds,
                    maxSpeedKmh = maxSpeedKmh,
                    avgSpeedKmh = _activeMetrics.value.avgSpeedKmh
                )
            )
        }
    }

    /**
     * Resume paused trip.
     */
    suspend fun resumeTrip(): Unit = mutex.withLock {
        if (_status.value != TripStatus.PAUSED) return@withLock
        _status.value = TripStatus.TRACKING
        _activeMetrics.value = _activeMetrics.value.copy(status = TripStatus.TRACKING)

        tripRepository.getTripById(activeTripId)?.let { trip ->
            tripRepository.updateTrip(trip.copy(status = TripStatus.TRACKING))
        }
    }

    /**
     * Stop and save trip permanently into Trip History.
     */
    suspend fun stopTripAndSave(): Long = mutex.withLock {
        if (_status.value != TripStatus.TRACKING && _status.value != TripStatus.PAUSED) {
            return@withLock 0L
        }

        val endTime = System.currentTimeMillis()
        val duration = ((endTime - tripStartTime) / 1000L).coerceAtLeast(0L)
        val avgSpeed = if (movingSeconds > 0) (totalDistanceMeters / movingSeconds) * 3.6 else 0.0
        val lastGps = _liveGps.value

        val savedTripId = activeTripId

        tripRepository.getTripById(savedTripId)?.let { existing ->
            val updated = existing.copy(
                endTime = endTime,
                distanceMeters = totalDistanceMeters,
                durationSeconds = duration,
                movingTimeSeconds = movingSeconds,
                stoppedTimeSeconds = stoppedSeconds,
                maxSpeedKmh = maxSpeedKmh,
                avgSpeedKmh = avgSpeed,
                status = TripStatus.COMPLETED,
                endLatitude = lastGps.latitude,
                endLongitude = lastGps.longitude,
                highSpeedEventsCount = highSpeedCount
            )
            tripRepository.updateTrip(updated)
        }

        // Update vehicle odometer if attached
        activeVehicleId?.let { vId ->
            val addedKm = totalDistanceMeters / 1000.0
            if (addedKm > 0.0) {
                val vehicle = vehicleRepository.getVehicleById(vId)
                vehicle?.let {
                    vehicleRepository.updateVehicle(it.copy(odometerKm = it.odometerKm + addedKm))
                }
            }
        }

        // Reset state machine to IDLE
        _status.value = TripStatus.IDLE
        _activeMetrics.value = ActiveTripMetrics()
        activeTripId = 0L
        activeVehicleId = null
        lastRecordedPoint = null

        return@withLock savedTripId
    }

    /**
     * Discard active trip (clean up partial data).
     */
    suspend fun discardTrip(): Unit = mutex.withLock {
        if (_status.value == TripStatus.IDLE) return@withLock
        val tripIdToDelete = activeTripId
        if (tripIdToDelete > 0) {
            tripRepository.deleteTripById(tripIdToDelete)
        }
        _status.value = TripStatus.IDLE
        _activeMetrics.value = ActiveTripMetrics()
        activeTripId = 0L
        activeVehicleId = null
        lastRecordedPoint = null
    }

    /**
     * Safe crash recovery on app start.
     */
    suspend fun recoverActiveTripIfAny(): Unit = mutex.withLock {
        if (_status.value != TripStatus.IDLE) return@withLock
        val active = tripRepository.getActiveTrip() ?: return@withLock
        activeTripId = active.id
        activeVehicleId = active.vehicleId
        tripStartTime = active.startTime
        totalDistanceMeters = active.distanceMeters
        maxSpeedKmh = active.maxSpeedKmh
        movingSeconds = active.movingTimeSeconds
        stoppedSeconds = active.stoppedTimeSeconds
        highSpeedCount = active.highSpeedEventsCount
        currentSpeedLimitKmh = active.speedLimitKmh

        // Place recovered trip in PAUSED state so user has full control
        _status.value = TripStatus.PAUSED
        _activeMetrics.value = ActiveTripMetrics(
            tripId = active.id,
            vehicleId = active.vehicleId,
            status = TripStatus.PAUSED,
            startTime = active.startTime,
            distanceMeters = active.distanceMeters,
            durationSeconds = active.durationSeconds,
            movingTimeSeconds = active.movingTimeSeconds,
            stoppedTimeSeconds = active.stoppedTimeSeconds,
            maxSpeedKmh = active.maxSpeedKmh,
            avgSpeedKmh = active.avgSpeedKmh,
            speedLimitKmh = active.speedLimitKmh,
            highSpeedEventsCount = active.highSpeedEventsCount
        )
    }
}
