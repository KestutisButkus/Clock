package com.example.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ClockFontStyle
import com.example.model.ClockFontWeight
import com.example.model.ClockShadowStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
import com.example.model.GridPreset
import com.example.model.TimeFormatMode
import com.example.model.WidgetBackgroundStyle
import com.example.model.WidgetColorPalette
import com.example.ui.components.ClockWidgetPreview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    config: ClockWidgetConfig,
    realNextAlarm: String?,
    onConfigChange: (ClockWidgetConfig) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    var previewGrid by remember { mutableStateOf(GridPreset.GRID_4X1) }
    var simulateAlarmInPreview by remember { mutableStateOf(realNextAlarm == null) }

    val previewAlarmText = when {
        !config.showNextAlarm -> null
        realNextAlarm != null -> realNextAlarm
        simulateAlarmInPreview -> "07:30"
        else -> null
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Widget Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Instance #$appWidgetId",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("config_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel configuration"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_widget_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Apply Widget",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Preview
                ClockWidgetPreview(
                    config = config,
                    gridPreset = previewGrid,
                    nextAlarmText = previewAlarmText,
                    modifier = Modifier.testTag("config_live_preview")
                )

                // Grid Size Tester
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GridPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = previewGrid == preset,
                            onClick = { previewGrid = preset },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("grid_chip_${preset.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Time & Battery Section
                ConfigSectionCard(
                    title = "Time & Information",
                    icon = painterResource(id = com.example.R.drawable.ic_schedule)
                ) {
                    Text(
                        text = "Time Format",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeFormatMode.entries.forEach { mode ->
                            FilterChip(
                                selected = config.timeFormatMode == mode,
                                onClick = {
                                    onConfigChange(config.copy(timeFormatMode = mode))
                                },
                                label = { Text(mode.label) },
                                modifier = Modifier.testTag("time_format_${mode.name}")
                            )
                        }
                    }

                    ConfigSwitchRow(
                        title = "Show Seconds",
                        subtitle = if (config.showSeconds) {
                            "Live 1s TextClock ticker (visible screen only)"
                        } else {
                            "Battery-saving mode: minute-only updates"
                        },
                        checked = config.showSeconds,
                        testTag = "switch_show_seconds",
                        onCheckedChange = { onConfigChange(config.copy(showSeconds = it)) }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_battery_bolt),
                            contentDescription = null,
                            tint = if (config.showSeconds) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (config.showSeconds) {
                                "Seconds tick inside Launcher TextClock without background wakeups."
                            } else {
                                "Optimal battery mode active: zero per-second updates."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ConfigSwitchRow(
                        title = "Show Date & Weekday",
                        subtitle = "Adapts placement automatically across grid sizes",
                        checked = config.showDate,
                        testTag = "switch_show_date",
                        onCheckedChange = { onConfigChange(config.copy(showDate = it)) }
                    )

                    if (config.showDate) {
                        Text(
                            text = "Date Placement",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePlacement.entries.forEach { placement ->
                                FilterChip(
                                    selected = config.datePlacement == placement,
                                    onClick = {
                                        onConfigChange(config.copy(datePlacement = placement))
                                    },
                                    label = { Text(placement.label) },
                                    modifier = Modifier.testTag("date_placement_${placement.name}")
                                )
                            }
                        }
                    }

                    ConfigSwitchRow(
                        title = "Show Next Alarm",
                        subtitle = if (realNextAlarm != null) {
                            "Active system alarm: $realNextAlarm"
                        } else {
                            "Auto-hides on home screen when no alarm is set"
                        },
                        checked = config.showNextAlarm,
                        testTag = "switch_show_alarm",
                        onCheckedChange = { onConfigChange(config.copy(showNextAlarm = it)) }
                    )

                    if (config.showNextAlarm && realNextAlarm == null) {
                        ConfigSwitchRow(
                            title = "Preview Simulated Alarm",
                            subtitle = "Test alarm badge spacing when no system alarm is active",
                            checked = simulateAlarmInPreview,
                            testTag = "switch_simulate_alarm",
                            onCheckedChange = { simulateAlarmInPreview = it }
                        )
                    }
                }

                // Typography & Readability Section
                ConfigSectionCard(
                    title = "Typography & Readability",
                    icon = painterResource(id = com.example.R.drawable.ic_font_style)
                ) {
                    Text(
                        text = "Digit Font Style",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClockFontStyle.entries.forEach { style ->
                            FilterChip(
                                selected = config.fontStyle == style,
                                onClick = { onConfigChange(config.copy(fontStyle = style)) },
                                label = { Text(style.label) },
                                modifier = Modifier.testTag("font_style_${style.name}")
                            )
                        }
                    }

                    Text(
                        text = "Font Weight",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClockFontWeight.entries.forEach { weight ->
                            FilterChip(
                                selected = config.fontWeight == weight,
                                onClick = { onConfigChange(config.copy(fontWeight = weight)) },
                                label = { Text(weight.label) },
                                modifier = Modifier.testTag("font_weight_${weight.name}")
                            )
                        }
                    }

                    Text(
                        text = "Contrast Shadow / Outline",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClockShadowStyle.entries.forEach { shadow ->
                            FilterChip(
                                selected = config.shadowStyle == shadow,
                                onClick = { onConfigChange(config.copy(shadowStyle = shadow)) },
                                label = { Text(shadow.label) },
                                modifier = Modifier.testTag("shadow_style_${shadow.name}")
                            )
                        }
                    }
                }

                // Colors & Background Section
                ConfigSectionCard(
                    title = "Colors & Surface",
                    icon = painterResource(id = com.example.R.drawable.ic_tune)
                ) {
                    Text(
                        text = "Digit & Date Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ColorSwatchRow(
                        presets = WidgetColorPalette.textPresets,
                        selectedArgb = config.textColorArgb,
                        tagPrefix = "text_color",
                        onSelect = { onConfigChange(config.copy(textColorArgb = it)) }
                    )

                    Text(
                        text = "Accent Color (AM/PM & Alarm)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ColorSwatchRow(
                        presets = WidgetColorPalette.accentPresets,
                        selectedArgb = config.accentColorArgb,
                        tagPrefix = "accent_color",
                        onSelect = { onConfigChange(config.copy(accentColorArgb = it)) }
                    )

                    Text(
                        text = "Widget Surface Backdrop",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetBackgroundStyle.entries.forEach { bg ->
                            FilterChip(
                                selected = config.backgroundStyle == bg,
                                onClick = { onConfigChange(config.copy(backgroundStyle = bg)) },
                                label = { Text(bg.label) },
                                modifier = Modifier.testTag("bg_style_${bg.name}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    icon: Painter,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
private fun ConfigSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ColorSwatchRow(
    presets: List<Pair<String, Int>>,
    selectedArgb: Int,
    tagPrefix: String,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        presets.forEach { (name, argb) ->
            val selected = argb == selectedArgb
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
                    .clickable { onSelect(argb) }
                    .semantics { contentDescription = name }
                    .testTag("${tagPrefix}_${name.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    val iconTint = if (argb == 0xFF0F172A.toInt()) Color.White else Color(0xFF0F172A)
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected $name",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
