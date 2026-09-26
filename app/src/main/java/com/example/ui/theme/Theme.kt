package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = CyanNeon,
  onPrimary = Color(0xFF00363D),
  primaryContainer = DarkNavySurfaceVariant,
  onPrimaryContainer = CyanNeon,
  secondary = AmberSpeed,
  onSecondary = Color(0xFF432C00),
  secondaryContainer = Color(0xFF5A3D05),
  onSecondaryContainer = Color(0xFFFFDEA3),
  tertiary = MintSuccess,
  onTertiary = Color(0xFF003828),
  background = DarkNavyBackground,
  onBackground = Color(0xFFE2E8F0),
  surface = DarkNavySurface,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = DarkNavySurfaceVariant,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = DarkNavyOutline,
  error = DangerRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFCBEBFF),
  onPrimaryContainer = Color(0xFF001F29),
  secondary = AmberSpeed,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFE0B2),
  onSecondaryContainer = Color(0xFF4E2600),
  tertiary = MintSuccess,
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = Color(0xFF0F172A),
  surface = LightSurface,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = Color(0xFF475569),
  outline = LightOutline,
  error = DangerRed,
  onError = Color.White
)

@Composable
fun WafaTheme(
  darkTheme: Boolean = true, // Default to sleek cockpit dark theme
  dynamicColor: Boolean = false, // Keep high-contrast cockpit styling
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

