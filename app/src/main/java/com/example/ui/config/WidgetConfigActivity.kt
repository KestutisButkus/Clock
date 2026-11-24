package com.example.ui.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.WidgetPreferencesRepository
import com.example.model.ClockWidgetConfig
import com.example.ui.theme.ChronoClockTheme
import com.example.widget.AlarmRepository
import com.example.widget.ClockWidgetProvider

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val repository = WidgetPreferencesRepository(this)
        val initialConfig = repository.loadConfig(appWidgetId)

        setContent {
            ChronoClockTheme(darkTheme = true) {
                var config by remember { mutableStateOf(initialConfig) }
                val nextAlarmInfo = remember(config.timeFormatMode) {
                    AlarmRepository.getNextAlarm(this, config.timeFormatMode)
                }

                WidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    config = config,
                    realNextAlarm = nextAlarmInfo?.formattedTime,
                    onConfigChange = { config = it },
                    onCancel = { finish() },
                    onSave = {
                        saveAndFinish(repository, config)
                    }
                )
            }
        }
    }

    private fun saveAndFinish(
        repository: WidgetPreferencesRepository,
        config: ClockWidgetConfig
    ) {
        repository.saveConfig(config)
        ClockWidgetProvider.updateWidget(
            context = this,
            appWidgetId = appWidgetId
        )
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
