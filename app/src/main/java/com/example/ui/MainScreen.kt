package com.example.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.WidgetPreferencesRepository
import com.example.model.ClockWidgetConfig
import com.example.model.GridPreset
import com.example.model.TimeFormatMode
import com.example.ui.components.ClockWidgetPreview
import com.example.ui.config.WidgetConfigActivity
import com.example.widget.AlarmRepository
import com.example.widget.ClockAppIntentHelper
import com.example.widget.ClockCompactWidgetProvider
import com.example.widget.ClockLargeWidgetProvider
import com.example.widget.ClockWideWidgetProvider
import com.example.widget.ClockWidgetProvider

@Composable
fun MainScreen(
    activeWidgetIds: IntArray,
    revision: Long,
    onRefreshWidgets: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { WidgetPreferencesRepository(context) }

    var previewGrid by remember { mutableStateOf(GridPreset.GRID_4X1) }
    val realAlarm = remember(revision) {
        AlarmRepository.getNextAlarm(context, TimeFormatMode.SYSTEM)
    }

    fun requestPin(providerClass: Class<out ClockWidgetProvider>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, providerClass)
            if (manager.isRequestPinAppWidgetSupported) {
                manager.requestPinAppWidget(provider, null, null)
                Toast.makeText(
                    context,
                    context.getString(R.string.pin_widget_requested),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.pin_widget_unsupported),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.pin_widget_unsupported),
                Toast.LENGTH_LONG
            ).show()
        }
        onRefreshWidgets()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clean Header (no background image)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = "Home screen clock widget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { ClockAppIntentHelper.launchSystemClockSafely(context) },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("open_system_clock_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open_in_new),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock")
                    }
                }

                // Clean Preview
                ClockWidgetPreview(
                    config = ClockWidgetConfig(appWidgetId = 0),
                    gridPreset = previewGrid,
                    nextAlarmText = realAlarm?.formattedTime ?: "07:30",
                    onWidgetClick = { ClockAppIntentHelper.launchSystemClockSafely(context) },
                    modifier = Modifier.testTag("sandbox_widget_preview")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        GridPreset.GRID_2X1,
                        GridPreset.GRID_4X1,
                        GridPreset.GRID_5X1,
                        GridPreset.GRID_4X2,
                        GridPreset.GRID_TABLET
                    ).forEach { preset ->
                        FilterChip(
                            selected = previewGrid == preset,
                            onClick = { previewGrid = preset },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("sandbox_grid_${preset.name}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Quick Add Widget Presets
                Text(
                    text = "Add Widget to Home Screen",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { requestPin(ClockCompactWidgetProvider::class.java) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pin_2x1_button")
                    ) { Text("2×1") }

                    Button(
                        onClick = { requestPin(ClockWidgetProvider::class.java) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pin_4x1_button")
                    ) { Text("4×1") }

                    Button(
                        onClick = { requestPin(ClockWideWidgetProvider::class.java) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pin_5x1_button")
                    ) { Text("5×1") }

                    Button(
                        onClick = { requestPin(ClockLargeWidgetProvider::class.java) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pin_4x2_button")
                    ) { Text("4×2") }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Placed Widgets List
                Text(
                    text = "Placed Widgets (${activeWidgetIds.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                if (activeWidgetIds.isEmpty()) {
                    Text(
                        text = "Tap a size above or long-press your home screen to place a widget. Each widget keeps independent settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activeWidgetIds.forEach { widgetId ->
                        val itemConfig = remember(widgetId, revision) {
                            repository.loadConfig(widgetId)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Widget #$widgetId",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${itemConfig.fontStyle.label} • ${itemConfig.timeFormatMode.label} • ${if (itemConfig.showSeconds) "Seconds ON" else "Minute-only"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.testTag("edit_widget_$widgetId")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}
