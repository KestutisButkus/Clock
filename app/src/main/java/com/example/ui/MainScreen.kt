package com.example.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.WidgetPreferencesRepository
import com.example.model.ClockFontStyle
import com.example.model.ClockWidgetConfig
import com.example.model.GridPreset
import com.example.model.TimeFormatMode
import com.example.ui.components.ClockWidgetPreview
import com.example.ui.config.WidgetConfigActivity
import com.example.widget.AlarmRepository
import com.example.widget.ClockAppIntentHelper
import com.example.widget.ClockWidgetProvider

@Composable
fun MainScreen(
    activeWidgetIds: IntArray,
    revision: Long,
    onRefreshWidgets: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { WidgetPreferencesRepository(context) }

    var sandboxGrid by remember { mutableStateOf(GridPreset.GRID_4X1) }
    var sandboxConfig by remember { mutableStateOf(ClockWidgetConfig(appWidgetId = 0)) }
    var simulateNoAlarm by remember { mutableStateOf(false) }

    val realAlarm = remember(revision, sandboxConfig.timeFormatMode) {
        AlarmRepository.getNextAlarm(context, sandboxConfig.timeFormatMode)
    }

    val sandboxAlarmText = when {
        simulateNoAlarm -> null
        realAlarm != null -> realAlarm.formattedTime
        else -> "07:30"
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Showcase Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_widget_showcase),
                        contentDescription = stringResource(id = R.string.cd_widget_showcase),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33090E1A),
                                        Color(0xE6090E1A)
                                    )
                                )
                            )
                            .padding(18.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Adaptive Home Screen Clock & Alarm Widget",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                // Primary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val manager = AppWidgetManager.getInstance(context)
                                val provider = ComponentName(context, ClockWidgetProvider::class.java)
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
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("pin_widget_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_home),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Widget")
                    }

                    FilledTonalButton(
                        onClick = { ClockAppIntentHelper.launchSystemClockSafely(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("open_system_clock_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open_in_new),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("System Clock")
                    }
                }

                // Interactive Grid & Edge-Case Sandbox
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_grid_view),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Interactive Grid & Edge-Case Tester",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        ClockWidgetPreview(
                            config = sandboxConfig,
                            gridPreset = sandboxGrid,
                            nextAlarmText = sandboxAlarmText,
                            onWidgetClick = { ClockAppIntentHelper.launchSystemClockSafely(context) },
                            modifier = Modifier.testTag("sandbox_widget_preview")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GridPreset.entries.forEach { preset ->
                                FilterChip(
                                    selected = sandboxGrid == preset,
                                    onClick = { sandboxGrid = preset },
                                    label = { Text(preset.label) },
                                    modifier = Modifier.testTag("sandbox_grid_${preset.name}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeFormatMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = sandboxConfig.timeFormatMode == mode,
                                    onClick = {
                                        sandboxConfig = sandboxConfig.copy(timeFormatMode = mode)
                                    },
                                    label = { Text(mode.label) },
                                    modifier = Modifier.testTag("sandbox_format_${mode.name}")
                                )
                            }
                            ClockFontStyle.entries.forEach { style ->
                                FilterChip(
                                    selected = sandboxConfig.fontStyle == style,
                                    onClick = {
                                        sandboxConfig = sandboxConfig.copy(fontStyle = style)
                                    },
                                    label = { Text(style.label) },
                                    modifier = Modifier.testTag("sandbox_font_${style.name}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seconds (${if (sandboxConfig.showSeconds) "1s Live" else "Minute-only"})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = sandboxConfig.showSeconds,
                                onCheckedChange = {
                                    sandboxConfig = sandboxConfig.copy(showSeconds = it)
                                },
                                modifier = Modifier.testTag("sandbox_seconds_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulate No Active Alarm (Hide Field)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = simulateNoAlarm,
                                onCheckedChange = { simulateNoAlarm = it },
                                modifier = Modifier.testTag("sandbox_no_alarm_switch")
                            )
                        }
                    }
                }

                // Active Home Screen Widgets List
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_widgets),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Placed Home Screen Widgets",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = "${activeWidgetIds.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (activeWidgetIds.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_alarm_clock),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "No Chrono Clock widgets placed yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "Tap \"Add Widget\" above or long-press your home screen to place one. Each placed widget stores independent typography, seconds, date, and color settings by appWidgetId.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            activeWidgetIds.forEach { widgetId ->
                                val itemConfig = remember(widgetId, revision) {
                                    repository.loadConfig(widgetId)
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Widget #$widgetId",
                                                style = MaterialTheme.typography.titleMedium
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
                                            Text("Configure")
                                        }
                                    }
                                    ClockWidgetPreview(
                                        config = itemConfig,
                                        gridPreset = GridPreset.GRID_4X1,
                                        nextAlarmText = if (itemConfig.showNextAlarm) {
                                            realAlarm?.formattedTime ?: "07:30"
                                        } else {
                                            null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
