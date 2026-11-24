package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ClockFontStyle
import com.example.model.ClockFontWeight
import com.example.model.ClockShadowStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
import com.example.model.TimeFormatMode
import com.example.model.WidgetBackgroundStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WidgetPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun loadConfig(appWidgetId: Int): ClockWidgetConfig {
        val prefix = keyPrefix(appWidgetId)
        val defaults = ClockWidgetConfig(appWidgetId = appWidgetId)
        return ClockWidgetConfig(
            appWidgetId = appWidgetId,
            timeFormatMode = enumOrDefault(
                prefs.getString("${prefix}time_format", null),
                defaults.timeFormatMode
            ),
            showSeconds = prefs.getBoolean("${prefix}show_seconds", defaults.showSeconds),
            showDate = prefs.getBoolean("${prefix}show_date", defaults.showDate),
            datePlacement = enumOrDefault(
                prefs.getString("${prefix}date_placement", null),
                defaults.datePlacement
            ),
            showNextAlarm = prefs.getBoolean("${prefix}show_alarm", defaults.showNextAlarm),
            fontStyle = enumOrDefault(
                prefs.getString("${prefix}font_style", null),
                defaults.fontStyle
            ),
            fontWeight = enumOrDefault(
                prefs.getString("${prefix}font_weight", null),
                defaults.fontWeight
            ),
            textColorArgb = prefs.getInt("${prefix}text_color", defaults.textColorArgb),
            accentColorArgb = prefs.getInt("${prefix}accent_color", defaults.accentColorArgb),
            shadowStyle = enumOrDefault(
                prefs.getString("${prefix}shadow_style", null),
                defaults.shadowStyle
            ),
            backgroundStyle = enumOrDefault(
                prefs.getString("${prefix}bg_style", null),
                defaults.backgroundStyle
            )
        )
    }

    fun saveConfig(config: ClockWidgetConfig) {
        val prefix = keyPrefix(config.appWidgetId)
        prefs.edit()
            .putString("${prefix}time_format", config.timeFormatMode.name)
            .putBoolean("${prefix}show_seconds", config.showSeconds)
            .putBoolean("${prefix}show_date", config.showDate)
            .putString("${prefix}date_placement", config.datePlacement.name)
            .putBoolean("${prefix}show_alarm", config.showNextAlarm)
            .putString("${prefix}font_style", config.fontStyle.name)
            .putString("${prefix}font_weight", config.fontWeight.name)
            .putInt("${prefix}text_color", config.textColorArgb)
            .putInt("${prefix}accent_color", config.accentColorArgb)
            .putString("${prefix}shadow_style", config.shadowStyle.name)
            .putString("${prefix}bg_style", config.backgroundStyle.name)
            .apply()
        notifyUpdated()
    }

    fun deleteConfig(appWidgetId: Int) {
        val prefix = keyPrefix(appWidgetId)
        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith(prefix) }
            .forEach { editor.remove(it) }
        editor.apply()
        notifyUpdated()
    }

    fun hasConfig(appWidgetId: Int): Boolean {
        return prefs.contains("${keyPrefix(appWidgetId)}time_format")
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T {
        if (value == null) return default
        return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
    }

    private fun keyPrefix(appWidgetId: Int): String = "widget_${appWidgetId}_"

    companion object {
        private const val PREFS_NAME = "chrono_clock_widget_prefs"
        private val _revisionFlow = MutableStateFlow(0L)
        val revisionFlow: StateFlow<Long> = _revisionFlow.asStateFlow()

        private fun notifyUpdated() {
            _revisionFlow.value = _revisionFlow.value + 1L
        }
    }
}
