package com.example.ui.screens.warnings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.ui.components.WarningBanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WarningCenterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val alertRepo = app.alertRepository

    val alerts by alertRepo.allAlerts.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Warning Center",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time safety events & GPS telemetry log",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            if (alerts.isNotEmpty()) {
                Button(
                    onClick = {
                        kotlinx.coroutines.runBlocking { alertRepo.clearAll() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Clear All", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No safety alerts recorded. All systems optimal.",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(alerts, key = { it.id }) { alert ->
                    val timeStr = dateFormat.format(Date(alert.timestamp))
                    WarningBanner(
                        title = "${alert.type.defaultTitle} • $timeStr",
                        message = alert.message,
                        level = alert.level,
                        onDismiss = {
                            kotlinx.coroutines.runBlocking { alertRepo.deleteAlert(alert.id) }
                        }
                    )
                }
            }
        }
    }
}
