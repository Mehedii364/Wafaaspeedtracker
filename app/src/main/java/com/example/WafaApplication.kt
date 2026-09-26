package com.example

import android.app.Application
import com.example.data.ai.WafaAiService
import com.example.data.db.WafaDatabase
import com.example.data.gps.GpsEngine
import com.example.data.gps.TripStateMachine
import com.example.data.repository.AlertRepository
import com.example.data.repository.FuelRepository
import com.example.data.repository.MaintenanceRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TripRepository
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WafaApplication : Application() {

    lateinit var database: WafaDatabase
        private set

    lateinit var tripRepository: TripRepository
        private set

    lateinit var vehicleRepository: VehicleRepository
        private set

    lateinit var fuelRepository: FuelRepository
        private set

    lateinit var maintenanceRepository: MaintenanceRepository
        private set

    lateinit var alertRepository: AlertRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var tripStateMachine: TripStateMachine
        private set

    lateinit var gpsEngine: GpsEngine
        private set

    lateinit var aiService: WafaAiService
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        database = WafaDatabase.getInstance(this)
        tripRepository = TripRepository(
            database.tripDao(),
            database.locationPointDao(),
            database.speedEventDao()
        )
        vehicleRepository = VehicleRepository(database.vehicleDao())
        fuelRepository = FuelRepository(database.fuelDao())
        maintenanceRepository = MaintenanceRepository(database.maintenanceDao())
        alertRepository = AlertRepository(database.warningAlertDao())
        settingsRepository = SettingsRepository(this)
        aiService = WafaAiService()

        tripStateMachine = TripStateMachine(
            tripRepository = tripRepository,
            vehicleRepository = vehicleRepository,
            alertRepository = alertRepository,
            scope = applicationScope
        )

        gpsEngine = GpsEngine(
            context = this,
            tripStateMachine = tripStateMachine,
            scope = applicationScope
        )

        applicationScope.launch {
            vehicleRepository.ensureDefaultVehicleExists()
            tripStateMachine.recoverActiveTripIfAny()
        }
    }
}
