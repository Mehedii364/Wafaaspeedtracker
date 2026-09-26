package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.WafaNavApp
import com.example.ui.theme.WafaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WafaApplication

        setContent {
            val settings by app.settingsRepository.settings.collectAsStateWithLifecycle()

            // Permission requester
            val permissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (fine || coarse) {
                    app.gpsEngine.startLocationUpdates()
                }
            }

            LaunchedEffect(Unit) {
                val hasFine = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val permissionsToRequest = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                if (!hasFine) {
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    app.gpsEngine.startLocationUpdates()
                }
            }

            WafaTheme(darkTheme = settings.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WafaNavApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as WafaApplication
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            app.gpsEngine.startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val app = application as WafaApplication
        // Only stop GPS engine updates if no active trip is running in background
        if (app.tripStateMachine.status.value == com.example.data.model.TripStatus.IDLE) {
            app.gpsEngine.stopLocationUpdates()
        }
    }
}
