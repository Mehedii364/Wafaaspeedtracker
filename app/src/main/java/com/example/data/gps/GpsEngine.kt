package com.example.data.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GpsEngine(
    private val context: Context,
    private val tripStateMachine: TripStateMachine,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _isGpsEnabled = MutableStateFlow(false)
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _isLocationUpdatesActive = MutableStateFlow(false)
    val isLocationUpdatesActive: StateFlow<Boolean> = _isLocationUpdatesActive.asStateFlow()

    init {
        checkGpsHardwareEnabled()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location = result.lastLocation ?: return
            scope.launch {
                tripStateMachine.onGpsLocation(
                    lat = location.latitude,
                    lon = location.longitude,
                    speedMps = if (location.hasSpeed()) location.speed else 0f,
                    bearing = if (location.hasBearing()) location.bearing else 0f,
                    alt = if (location.hasAltitude()) location.altitude else 0.0,
                    acc = if (location.hasAccuracy()) location.accuracy else 50f,
                    time = location.time
                )
            }
        }
    }

    fun checkGpsHardwareEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val enabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        _isGpsEnabled.value = enabled
        return enabled
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (_isLocationUpdatesActive.value) return
        checkGpsHardwareEnabled()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(0f)
            .build()

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _isLocationUpdatesActive.value = true
        } catch (e: SecurityException) {
            _isLocationUpdatesActive.value = false
        }
    }

    fun stopLocationUpdates() {
        if (!_isLocationUpdatesActive.value) return
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
        _isLocationUpdatesActive.value = false
    }
}
