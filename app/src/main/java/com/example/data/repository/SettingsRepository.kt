package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SpeedUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val speedLimitKmh: Double = 60.0,
    val isVibrationEnabled: Boolean = true,
    val isSoundAlertEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val isAnalogGauge: Boolean = false,
    val aiBackendUrl: String = "",
    val openRouterModel: String = "google/gemini-2.0-flash-001",
    val isAiEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wafa_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val unitStr = prefs.getString("speed_unit", SpeedUnit.KMH.name) ?: SpeedUnit.KMH.name
        val unit = runCatching { SpeedUnit.valueOf(unitStr) }.getOrDefault(SpeedUnit.KMH)
        val limit = prefs.getFloat("speed_limit", 60.0f).toDouble()
        val vibration = prefs.getBoolean("vibration_enabled", true)
        val sound = prefs.getBoolean("sound_enabled", true)
        val dark = prefs.getBoolean("dark_theme", true)
        val analog = prefs.getBoolean("analog_gauge", false)
        val aiUrl = prefs.getString("ai_backend_url", "") ?: ""
        val aiModel = prefs.getString("ai_model", "google/gemini-2.0-flash-001") ?: "google/gemini-2.0-flash-001"
        val aiEnabled = prefs.getBoolean("ai_enabled", true)
        val onboarding = prefs.getBoolean("onboarding_done", false)

        return UserSettings(
            speedUnit = unit,
            speedLimitKmh = limit,
            isVibrationEnabled = vibration,
            isSoundAlertEnabled = sound,
            isDarkTheme = dark,
            isAnalogGauge = analog,
            aiBackendUrl = aiUrl,
            openRouterModel = aiModel,
            isAiEnabled = aiEnabled,
            hasCompletedOnboarding = onboarding
        )
    }

    fun updateSpeedUnit(unit: SpeedUnit) {
        prefs.edit().putString("speed_unit", unit.name).apply()
        _settings.value = _settings.value.copy(speedUnit = unit)
    }

    fun updateSpeedLimit(limitKmh: Double) {
        prefs.edit().putFloat("speed_limit", limitKmh.toFloat()).apply()
        _settings.value = _settings.value.copy(speedLimitKmh = limitKmh)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isVibrationEnabled = enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isSoundAlertEnabled = enabled)
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
    }

    fun setAnalogGauge(isAnalog: Boolean) {
        prefs.edit().putBoolean("analog_gauge", isAnalog).apply()
        _settings.value = _settings.value.copy(isAnalogGauge = isAnalog)
    }

    fun setAiBackendUrl(url: String) {
        prefs.edit().putString("ai_backend_url", url).apply()
        _settings.value = _settings.value.copy(aiBackendUrl = url)
    }

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isAiEnabled = enabled)
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean("onboarding_done", true).apply()
        _settings.value = _settings.value.copy(hasCompletedOnboarding = true)
    }
}
