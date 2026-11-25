package com.example.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ClockFontStyle
import com.example.model.ClockFontWeight
import com.example.model.ClockShadowStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
import com.example.model.GridPreset
import com.example.model.TimeFormatMode
import com.example.model.WidgetBackgroundStyle
import com.example.model.WidgetSizeBucket
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RobotoCondensedFontFamily
import com.example.widget.WidgetSizeCalculator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockWidgetPreview(
    config: ClockWidgetConfig,
    gridPreset: GridPreset,
    nextAlarmText: String?,
    modifier: Modifier = Modifier,
    onWidgetClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(config.showSeconds) {
        while (true) {
            currentMillis = System.currentTimeMillis()
            val delayMs = if (config.showSeconds) {
                1000L - (currentMillis % 1000L)
            } else {
                60_000L - (currentMillis % 60_000L)
            }
            delay(delayMs.coerceAtLeast(250L))
        }
    }

    val isSystem24Hour = remember(context) { DateFormat.is24HourFormat(context) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidthDp = maxWidth.value.toInt().coerceAtLeast(120)
        val effectiveWidthDp = minOf(gridPreset.widthDp, availableWidthDp)
        val scaleRatio = effectiveWidthDp.toFloat() / gridPreset.widthDp.toFloat()
        val effectiveHeightDp = (gridPreset.heightDp * scaleRatio).toInt().coerceAtLeast(48)

        val spec = remember(effectiveWidthDp, effectiveHeightDp, config, isSystem24Hour) {
            WidgetSizeCalculator.calculate(effectiveWidthDp, effectiveHeightDp, config, isSystem24Hour)
        }

        val use24Hour = when (config.timeFormatMode) {
            TimeFormatMode.SYSTEM -> isSystem24Hour
            TimeFormatMode.HOUR_12 -> false
            TimeFormatMode.HOUR_24 -> true
        }

        val timePattern = when {
            use24Hour && config.showSeconds -> "HH:mm:ss"
            use24Hour -> "HH:mm"
            config.showSeconds -> "h:mm:ss"
            else -> "h:mm"
        }

        val locale = Locale.getDefault()
        val now = Date(currentMillis)
        val timeText = SimpleDateFormat(timePattern, locale).format(now)
        val ampmText = if (!use24Hour) SimpleDateFormat("a", locale).format(now) else ""
        val datePattern = DateFormat.getBestDateTimePattern(
            locale,
            if (spec.useExtendedDateFormat) "EEEEMMMd" else "EEEMMMd"
        )
        val dateText = SimpleDateFormat(datePattern, locale).format(now)

        val textColor = Color(config.textColorArgb)
        val accentColor = Color(config.accentColorArgb)
        val fontFamily = when (config.fontStyle) {
            ClockFontStyle.SANS_SERIF -> InterFontFamily
            ClockFontStyle.CONDENSED -> RobotoCondensedFontFamily
            ClockFontStyle.MONOSPACE -> JetBrainsMonoFontFamily
            ClockFontStyle.DISPLAY -> OutfitFontFamily
        }
        val fontWeight = when (config.fontWeight) {
            ClockFontWeight.LIGHT -> FontWeight.Light
            ClockFontWeight.REGULAR -> FontWeight.Normal
            ClockFontWeight.BOLD -> FontWeight.Bold
        }

        val effectColor = if (textColor.luminance() < 0.25f) Color.White else Color.Black
        val textShadow = when (config.shadowStyle) {
            ClockShadowStyle.NONE -> null
            ClockShadowStyle.SUBTLE -> Shadow(
                color = effectColor.copy(alpha = 0.55f),
                offset = Offset(2f, 4f),
                blurRadius = 6f
            )
            ClockShadowStyle.STRONG -> Shadow(
                color = effectColor.copy(alpha = 0.85f),
                offset = Offset(4f, 6f),
                blurRadius = 10f
            )
            ClockShadowStyle.OUTLINE -> null
        }

        val widgetBgModifier = when (config.backgroundStyle) {
            WidgetBackgroundStyle.TRANSPARENT -> Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            WidgetBackgroundStyle.FROSTED_DARK -> Modifier
                .background(Color(0x660F172A), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
            WidgetBackgroundStyle.FROSTED_LIGHT -> Modifier
                .background(Color(0x80F8FAFC), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x4D0F172A), RoundedCornerShape(18.dp))
            WidgetBackgroundStyle.SOLID_DARK -> Modifier
                .background(Color(0xE60B1120), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(18.dp))
        }

        val displayedAlarm = if (config.showNextAlarm) nextAlarmText else null
        val showAnyMeta = config.showDate || displayedAlarm != null

        Box(
            modifier = Modifier
                .widthIn(max = effectiveWidthDp.dp)
                .fillMaxWidth()
                .height(effectiveHeightDp.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(widgetBgModifier)
                .let { mod ->
                    if (onWidgetClick != null) mod.clickable(onClick = onWidgetClick) else mod
                }
                .padding(
                    horizontal = spec.horizontalPaddingDp.dp,
                    vertical = spec.verticalPaddingDp.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            if (spec.bucket == WidgetSizeBucket.WIDE &&
                spec.effectiveDatePlacement == DatePlacement.BESIDE &&
                showAnyMeta
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PreviewTimeBlock(
                        timeText = timeText,
                        ampmText = ampmText,
                        textSizeSp = spec.timeTextSizeSp,
                        ampmSizeSp = spec.ampmTextSizeSp,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        textColor = textColor,
                        accentColor = accentColor,
                        shadow = textShadow,
                        outline = config.shadowStyle == ClockShadowStyle.OUTLINE,
                        outlineColor = effectColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (config.showDate) {
                            Text(
                                text = dateText,
                                color = textColor.copy(alpha = 0.88f),
                                fontSize = spec.metaTextSizeSp.sp,
                                fontFamily = InterFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (displayedAlarm != null) {
                            if (config.showDate) Spacer(modifier = Modifier.height(2.dp))
                            PreviewAlarmBadge(
                                alarmText = displayedAlarm,
                                textSizeSp = spec.alarmTextSizeSp,
                                iconSizeDp = spec.alarmIconSizeDp,
                                accentColor = accentColor
                            )
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PreviewTimeBlock(
                        timeText = timeText,
                        ampmText = ampmText,
                        textSizeSp = spec.timeTextSizeSp,
                        ampmSizeSp = spec.ampmTextSizeSp,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        textColor = textColor,
                        accentColor = accentColor,
                        shadow = textShadow,
                        outline = config.shadowStyle == ClockShadowStyle.OUTLINE,
                        outlineColor = effectColor
                    )
                    if (showAnyMeta) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (config.showDate) {
                                Text(
                                    text = dateText,
                                    color = textColor.copy(alpha = 0.88f),
                                    fontSize = spec.metaTextSizeSp.sp,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (config.showDate && displayedAlarm != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (displayedAlarm != null) {
                                PreviewAlarmBadge(
                                    alarmText = displayedAlarm,
                                    textSizeSp = spec.alarmTextSizeSp,
                                    iconSizeDp = spec.alarmIconSizeDp,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewTimeBlock(
    timeText: String,
    ampmText: String,
    textSizeSp: Float,
    ampmSizeSp: Float,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    fontWeight: FontWeight,
    textColor: Color,
    accentColor: Color,
    shadow: Shadow?,
    outline: Boolean,
    outlineColor: Color
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box {
            val baseStyle = TextStyle(
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontSize = textSizeSp.sp,
                lineHeight = (textSizeSp * 0.9f).sp,
                fontFeatureSettings = "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
            if (outline) {
                val offsets = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                offsets.forEach { (dx, dy) ->
                    Text(
                        text = timeText,
                        style = baseStyle,
                        color = outlineColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        modifier = Modifier.offset(x = dx.dp, y = dy.dp)
                    )
                }
            }
            Text(
                text = timeText,
                style = baseStyle.copy(shadow = shadow),
                color = textColor,
                maxLines = 1
            )
        }
        if (ampmText.isNotEmpty()) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = ampmText,
                color = accentColor,
                fontSize = ampmSizeSp.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun PreviewAlarmBadge(
    alarmText: String,
    textSizeSp: Float,
    iconSizeDp: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .background(Color(0x26FFFFFF), RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_alarm_clock),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(iconSizeDp.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = alarmText,
            color = accentColor,
            fontSize = textSizeSp.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = InterFontFamily,
            maxLines = 1
        )
    }
}
