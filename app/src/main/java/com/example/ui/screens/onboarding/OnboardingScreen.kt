package com.example.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.WafaApplication
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val settingsRepo = app.settingsRepository

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.wafa_hero_banner),
                contentDescription = "Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Wafa Speed Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Live Speed. Smart Tracking. Every Journey.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanNeon
            )
        }

        // 4 Core Guarantees
        OnboardingItem(
            icon = WafaIcons.Speedometer,
            title = "Strict Start Requirement",
            description = "No start = no recording. Trips are strictly recorded only after you press START TRIP. Zero hidden background recording."
        )

        OnboardingItem(
            icon = WafaIcons.Compass,
            title = "Real-time Telemetry & Map",
            description = "High-accuracy live speed, bearing, altitude, speed limit monitoring, and real-time route tracing on dark vector maps."
        )

        OnboardingItem(
            icon = WafaIcons.Security,
            title = "Privacy-First Local Storage",
            description = "All trips, route points, and vehicle telemetry are stored locally in Room database on your device. Complete export & delete control."
        )

        OnboardingItem(
            icon = WafaIcons.Vehicle,
            title = "Multi-Vehicle Garage & Logs",
            description = "Track fuel refuels, maintenance schedules, and speed profiles across cars, bikes, and trucks."
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                settingsRepo.setOnboardingCompleted()
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
        ) {
            Text(
                text = "GET STARTED",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun OnboardingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
