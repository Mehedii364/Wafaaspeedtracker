package com.example.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.wafa_hero_banner),
                contentDescription = "Wafa Speed Tracker Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // App Branding
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Wafa Speed Tracker",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Live Speed. Smart Tracking. Every Journey.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanNeon
            )
            Text(
                text = "Version 1.0 • Build 2026",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Developer Info Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DEVELOPER PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "Md. Mehedi Hasan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Mehedi364 · Wafa Zone",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintSuccess
                )

                Text(
                    text = "Role: Web Developer · Software Engineer · Programmer",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Links
                ContactRow(
                    label = "Portfolio",
                    value = "https://mehedii364.github.io/",
                    onClick = { openUrl(context, "https://mehedii364.github.io/") }
                )

                ContactRow(
                    label = "Website",
                    value = "https://www.wafazone.site.je/",
                    onClick = { openUrl(context, "https://www.wafazone.site.je/") }
                )

                ContactRow(
                    label = "GitHub",
                    value = "https://github.com/Mehedi364",
                    onClick = { openUrl(context, "https://github.com/Mehedi364") }
                )

                ContactRow(
                    label = "Facebook",
                    value = "https://www.facebook.com/mehedi3643",
                    onClick = { openUrl(context, "https://www.facebook.com/mehedi3643") }
                )

                ContactRow(
                    label = "Email",
                    value = "useable.me2@gmail.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:useable.me2@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        // Footer
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "Developed by Mehedi364 ❤️",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 12.sp, color = CyanNeon)
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
