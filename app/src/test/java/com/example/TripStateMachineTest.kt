package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.WafaDatabase
import com.example.data.gps.GpsFilter
import com.example.data.gps.TripStateMachine
import com.example.data.model.TripStatus
import com.example.data.repository.AlertRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripStateMachineTest {

    private lateinit var database: WafaDatabase
    private lateinit var tripRepo: TripRepository
    private lateinit var vehicleRepo: VehicleRepository
    private lateinit var alertRepo: AlertRepository
    private lateinit var stateMachine: TripStateMachine

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, WafaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tripRepo = TripRepository(
            database.tripDao(),
            database.locationPointDao(),
            database.speedEventDao()
        )
        vehicleRepo = VehicleRepository(database.vehicleDao())
        alertRepo = AlertRepository(database.warningAlertDao())
        stateMachine = TripStateMachine(tripRepo, vehicleRepo, alertRepo)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `initial state is strictly IDLE and does NOT record GPS points`() = runTest {
        assertEquals(TripStatus.IDLE, stateMachine.status.value)
        assertEquals(0L, stateMachine.activeMetrics.value.tripId)

        // Receive GPS callback while IDLE
        stateMachine.onGpsLocation(
            lat = 23.8103,
            lon = 90.4125,
            speedMps = 15f,
            bearing = 45f,
            alt = 10.0,
            acc = 5f,
            time = System.currentTimeMillis()
        )

        // Verify state is still IDLE
        assertEquals(TripStatus.IDLE, stateMachine.status.value)
        // Verify NO trip created
        val activeTrip = tripRepo.getActiveTrip()
        assertEquals(null, activeTrip)
        val completedTrips = tripRepo.getAllCompletedTripsList()
        assertEquals(0, completedTrips.size)
        // Live GPS should be updated for viewing
        assertTrue(stateMachine.liveGps.value.isAvailable)
        assertEquals(23.8103, stateMachine.liveGps.value.latitude, 0.0001)
    }

    @Test
    fun `full state machine transitions IDLE to START to TRACKING to PAUSE to RESUME to STOP`() = runTest {
        // Start Trip explicitly
        val tripId = stateMachine.startTrip(vehicleId = null, speedLimitKmh = 60.0)
        assertTrue(tripId > 0)
        assertEquals(TripStatus.TRACKING, stateMachine.status.value)

        // GPS location 1
        stateMachine.onGpsLocation(
            lat = 23.8100,
            lon = 90.4100,
            speedMps = 10f, // 36 km/h
            bearing = 0f,
            alt = 5.0,
            acc = 4f,
            time = 1000L
        )

        // GPS location 2 (approx 111 meters north)
        stateMachine.onGpsLocation(
            lat = 23.8110,
            lon = 90.4100,
            speedMps = 12f, // 43.2 km/h
            bearing = 0f,
            alt = 5.0,
            acc = 4f,
            time = 10000L
        )

        val metricsAfterMove = stateMachine.activeMetrics.value
        assertTrue("Distance should be accumulated during tracking", metricsAfterMove.distanceMeters > 50.0)

        // Pause Trip
        stateMachine.pauseTrip()
        assertEquals(TripStatus.PAUSED, stateMachine.status.value)
        val distanceAtPause = stateMachine.activeMetrics.value.distanceMeters

        // GPS while paused should NOT add distance
        stateMachine.onGpsLocation(
            lat = 23.8120,
            lon = 90.4100,
            speedMps = 15f,
            bearing = 0f,
            alt = 5.0,
            acc = 4f,
            time = 15000L
        )
        assertEquals("Distance should NOT accumulate while PAUSED", distanceAtPause, stateMachine.activeMetrics.value.distanceMeters, 0.001)

        // Resume Trip
        stateMachine.resumeTrip()
        assertEquals(TripStatus.TRACKING, stateMachine.status.value)

        // Stop & Save
        val savedId = stateMachine.stopTripAndSave()
        assertEquals(tripId, savedId)
        assertEquals(TripStatus.IDLE, stateMachine.status.value)

        // Verify stored in DB
        val savedTrip = tripRepo.getTripById(savedId)
        assertNotNull(savedTrip)
        assertEquals(TripStatus.COMPLETED, savedTrip?.status)
    }

    @Test
    fun `gps filtering validates anomaly detection`() {
        // Haversine distance sanity check
        val dist = GpsFilter.haversineDistanceMeters(0.0, 0.0, 0.0, 1.0)
        assertTrue("1 degree longitude at equator is ~111km", dist in 111_000.0..112_000.0)

        // Jump anomaly test (impossible speed)
        val isAnomaly = GpsFilter.isJumpAnomaly(
            prevLat = 23.8103,
            prevLon = 90.4125,
            prevTime = 1000L,
            currLat = 28.6139, // Delhi, India (~1400km jump in 1 second)
            currLon = 77.2090,
            currTime = 2000L
        )
        assertTrue(isAnomaly)

        // Feasible movement
        val isFeasible = GpsFilter.isJumpAnomaly(
            prevLat = 23.8100,
            prevLon = 90.4100,
            prevTime = 1000L,
            currLat = 23.8101, // ~11 meters in 1 second = ~40 km/h
            currLon = 90.4100,
            currTime = 2000L
        )
        assertFalse(isFeasible)
    }
}
