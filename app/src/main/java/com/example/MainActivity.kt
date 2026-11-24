package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.WidgetPreferencesRepository
import com.example.ui.MainScreen
import com.example.ui.theme.ChronoClockTheme
import com.example.widget.ClockWidgetProvider

class MainActivity : ComponentActivity() {

    private var activeWidgetIds by mutableStateOf(intArrayOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshWidgets()

        setContent {
            ChronoClockTheme(darkTheme = true) {
                val revision by WidgetPreferencesRepository.revisionFlow.collectAsState()
                MainScreen(
                    activeWidgetIds = activeWidgetIds,
                    revision = revision,
                    onRefreshWidgets = { refreshWidgets() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshWidgets()
    }

    private fun refreshWidgets() {
        activeWidgetIds = ClockWidgetProvider.getActiveWidgetIds(this)
    }
}
