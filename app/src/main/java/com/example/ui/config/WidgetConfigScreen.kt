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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
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
    val previewAlarmText = if (config.showNextAlarm) realNextAlarm ?: "07:30" else null

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Widget #$appWidgetId",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("config_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_widget_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", style = MaterialTheme.typography.titleMedium)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClockWidgetPreview(
                    config = config,
                    gridPreset = previewGrid,
                    nextAlarmText = previewAlarmText,
                    modifier = Modifier.testTag("config_live_preview")
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
                        GridPreset.GRID_4X2,
                        GridPreset.GRID_TABLET
                    ).forEach { preset ->
                        FilterChip(
                            selected = previewGrid == preset,
                            onClick = { previewGrid = preset },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("grid_chip_${preset.name}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Time & Info
                SectionLabel("Time & Info")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeFormatMode.entries.forEach { mode ->
                        FilterChip(
                            selected = config.timeFormatMode == mode,
                            onClick = { onConfigChange(config.copy(timeFormatMode = mode)) },
                            label = { Text(mode.label) },
                            modifier = Modifier.testTag("time_format_${mode.name}")
                        )
                    }
                }

                SimpleSwitchRow(
                    title = "Seconds",
                    checked = config.showSeconds,
                    testTag = "switch_show_seconds",
                    onCheckedChange = { onConfigChange(config.copy(showSeconds = it)) }
                )

                SimpleSwitchRow(
                    title = "Date & Weekday",
                    checked = config.showDate,
                    testTag = "switch_show_date",
                    onCheckedChange = { onConfigChange(config.copy(showDate = it)) }
                )

                if (config.showDate) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePlacement.entries.forEach { placement ->
                            FilterChip(
                                selected = config.datePlacement == placement,
                                onClick = { onConfigChange(config.copy(datePlacement = placement)) },
                                label = { Text(placement.label) },
                                modifier = Modifier.testTag("date_placement_${placement.name}")
                            )
                        }
                    }
                }

                SimpleSwitchRow(
                    title = if (realNextAlarm != null) "Next Alarm ($realNextAlarm)" else "Next Alarm",
                    checked = config.showNextAlarm,
                    testTag = "switch_show_alarm",
                    onCheckedChange = { onConfigChange(config.copy(showNextAlarm = it)) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Typography & Effects
                SectionLabel("Typography & Contrast")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClockFontWeight.entries.forEach { weight ->
                        FilterChip(
                            selected = config.fontWeight == weight,
                            onClick = { onConfigChange(config.copy(fontWeight = weight)) },
                            label = { Text(weight.label) },
                            modifier = Modifier.testTag("font_weight_${weight.name}")
                        )
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClockShadowStyle.entries.forEach { shadow ->
                        FilterChip(
                            selected = config.shadowStyle == shadow,
                            onClick = { onConfigChange(config.copy(shadowStyle = shadow)) },
                            label = { Text(shadow.label) },
                            modifier = Modifier.testTag("shadow_style_${shadow.name}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Colors & Backdrop
                SectionLabel("Colors & Surface")
                ColorSwatchRow(
                    presets = WidgetColorPalette.textPresets,
                    selectedArgb = config.textColorArgb,
                    tagPrefix = "text_color",
                    onSelect = { onConfigChange(config.copy(textColorArgb = it)) }
                )
                ColorSwatchRow(
                    presets = WidgetColorPalette.accentPresets,
                    selectedArgb = config.accentColorArgb,
                    tagPrefix = "accent_color",
                    onSelect = { onConfigChange(config.copy(accentColorArgb = it)) }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WidgetBackgroundStyle.entries.forEach { bg ->
                        FilterChip(
                            selected = config.backgroundStyle == bg,
                            onClick = { onConfigChange(config.copy(backgroundStyle = bg)) },
                            label = { Text(bg.label) },
                            modifier = Modifier.testTag("bg_style_${bg.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SimpleSwitchRow(
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        presets.forEach { (name, argb) ->
            val selected = argb == selectedArgb
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(
                        width = if (selected) 2.5.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
