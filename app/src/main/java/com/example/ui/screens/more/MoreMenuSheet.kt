package com.example.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    onDismiss: () -> Unit,
    onNavigateToFuel: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToAiMonitor: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWarningCenter: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131B2F),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334155))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "More Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MoreMenuItem(
                icon = WafaIcons.Fuel,
                iconColor = AmberSpeed,
                title = "Fuel Tracker",
                subtitle = "Manage fuel refills, consumption & expenses",
                onClick = { onDismiss(); onNavigateToFuel() }
            )

            MoreMenuItem(
                icon = WafaIcons.Maintenance,
                iconColor = CyanNeon,
                title = "Maintenance Manager",
                subtitle = "Service schedules, parts, oil & odometer logs",
                onClick = { onDismiss(); onNavigateToMaintenance() }
            )

            MoreMenuItem(
                icon = WafaIcons.Compass,
                iconColor = MintSuccess,
                title = "Wafa AI Monitor",
                subtitle = "Intelligent trip summaries & driving advice",
                onClick = { onDismiss(); onNavigateToAiMonitor() }
            )

            MoreMenuItem(
                icon = WafaIcons.Search,
                iconColor = CyanNeon,
                title = "Search Everything",
                subtitle = "Filter trips, vehicles, fuel logs & alerts",
                onClick = { onDismiss(); onNavigateToSearch() }
            )

            MoreMenuItem(
                icon = WafaIcons.Warning,
                iconColor = AmberSpeed,
                title = "Warning Center",
                subtitle = "Review speed warnings & GPS anomalies",
                onClick = { onDismiss(); onNavigateToWarningCenter() }
            )

            MoreMenuItem(
                icon = WafaIcons.Security,
                iconColor = MintSuccess,
                title = "Permission Center",
                subtitle = "Sensor controls & transparent permissions",
                onClick = { onDismiss(); onNavigateToPermissions() }
            )

            MoreMenuItem(
                icon = WafaIcons.Settings,
                iconColor = Color.White,
                title = "Settings",
                subtitle = "Speed limit, units, alerts & AI backend",
                onClick = { onDismiss(); onNavigateToSettings() }
            )

            MoreMenuItem(
                icon = WafaIcons.Info,
                iconColor = CyanNeon,
                title = "About & Developer",
                subtitle = "Md. Mehedi Hasan • Mehedi364 / Wafa Zone",
                onClick = { onDismiss(); onNavigateToAbout() }
            )
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
