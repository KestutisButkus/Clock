package com.example.widget

import com.example.model.ClockFontStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
import com.example.model.TimeFormatMode
import com.example.model.WidgetSizeBucket
import kotlin.math.floor
import kotlin.math.roundToInt

data class WidgetLayoutSpec(
    val bucket: WidgetSizeBucket,
    val effectiveDatePlacement: DatePlacement,
    val timeTextSizeSp: Float,
    val ampmTextSizeSp: Float,
    val metaTextSizeSp: Float,
    val alarmTextSizeSp: Float,
    val alarmIconSizeDp: Int,
    val horizontalPaddingDp: Int,
    val verticalPaddingDp: Int,
    val useExtendedDateFormat: Boolean
)

object WidgetSizeCalculator {

    fun calculate(
        widthDp: Int,
        heightDp: Int,
        config: ClockWidgetConfig,
        isSystem24Hour: Boolean = true
    ): WidgetLayoutSpec {
        val safeWidth = widthDp.coerceAtLeast(100)
        val safeHeight = heightDp.coerceAtLeast(40)

        val bucket = when {
            safeWidth >= 440 && safeHeight >= 130 -> WidgetSizeBucket.TABLET
            safeHeight >= 95 -> WidgetSizeBucket.LARGE
            safeWidth < 175 -> WidgetSizeBucket.COMPACT
            else -> WidgetSizeBucket.WIDE
        }

        val hasMetadata = config.showDate || config.showNextAlarm

        val effectiveDatePlacement = when {
            !hasMetadata -> DatePlacement.BELOW
            bucket == WidgetSizeBucket.COMPACT ||
                bucket == WidgetSizeBucket.LARGE ||
                bucket == WidgetSizeBucket.TABLET -> DatePlacement.BELOW
            config.datePlacement == DatePlacement.BESIDE -> {
                if (safeWidth >= 230) DatePlacement.BESIDE else DatePlacement.BELOW
            }
            config.datePlacement == DatePlacement.BELOW -> DatePlacement.BELOW
            else -> {
                val besideMinWidth = if (config.showSeconds) 285 else 240
                if (safeWidth >= besideMinWidth) DatePlacement.BESIDE else DatePlacement.BELOW
            }
        }

        // Strict <= 5% padding on each edge so digits maximize available widget bounds on phones & tablets
        val vPad = floor(safeHeight * 0.04f).toInt().coerceIn(1, 8)
        val hPad = floor(safeWidth * 0.028f).toInt().coerceIn(3, 14)

        val innerHeight = (safeHeight - vPad * 2).toFloat().coerceAtLeast(32f)
        val innerWidth = (safeWidth - hPad * 2).toFloat().coerceAtLeast(88f)

        val metaSp = when (bucket) {
            WidgetSizeBucket.COMPACT -> (safeHeight * 0.19f).coerceIn(10f, 13f)
            WidgetSizeBucket.WIDE -> (safeHeight * 0.21f).coerceIn(11f, 16f)
            WidgetSizeBucket.LARGE -> (safeHeight * 0.135f).coerceIn(13f, 20f)
            WidgetSizeBucket.TABLET -> (safeHeight * 0.125f).coerceIn(16f, 26f)
        }
        val alarmSp = (metaSp * 0.92f).coerceIn(9.5f, 23f)

        val metaStackedHeightDp = if (!hasMetadata ||
            (bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BESIDE)
        ) {
            0f
        } else {
            (metaSp * 1.15f) + 3f
        }

        val availableDigitHeightDp = (innerHeight - metaStackedHeightDp).coerceAtLeast(22f)
        val maxTimeSpByHeight = availableDigitHeightDp / 0.76f

        val availableDigitWidthDp = when {
            bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BESIDE && hasMetadata -> {
                val reservedMetaWidth = if (config.showDate) (safeWidth * 0.29f).coerceIn(82f, 150f) else 64f
                (innerWidth - reservedMetaWidth - 8f).coerceAtLeast(innerWidth * 0.58f)
            }
            else -> innerWidth
        }

        val is24Hour = when (config.timeFormatMode) {
            TimeFormatMode.HOUR_24 -> true
            TimeFormatMode.HOUR_12 -> false
            TimeFormatMode.SYSTEM -> isSystem24Hour
        }
        val ampmReserveFactor = if (!is24Hour) 0.88f else 1.0f
        val effectiveCharUnits = if (config.showSeconds) 4.45f else 2.85f
        val fontWidthFactor = when (config.fontStyle) {
            ClockFontStyle.MONOSPACE -> 1.14f
            ClockFontStyle.CONDENSED -> 0.86f
            ClockFontStyle.SANS_SERIF -> 1.0f
            ClockFontStyle.DISPLAY -> 1.02f
        }

        val maxTimeSpByWidth = (availableDigitWidthDp * ampmReserveFactor) / (effectiveCharUnits * fontWidthFactor)

        val finalTimeSp = minOf(maxTimeSpByHeight, maxTimeSpByWidth).coerceIn(20f, 220f)
        val ampmSp = (finalTimeSp * 0.27f).coerceIn(9.5f, 38f)

        val useExtendedDate = ((bucket == WidgetSizeBucket.LARGE || bucket == WidgetSizeBucket.TABLET) && safeWidth >= 250) ||
            (bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BELOW && safeWidth >= 260)

        return WidgetLayoutSpec(
            bucket = bucket,
            effectiveDatePlacement = effectiveDatePlacement,
            timeTextSizeSp = finalTimeSp,
            ampmTextSizeSp = ampmSp,
            metaTextSizeSp = metaSp,
            alarmTextSizeSp = alarmSp,
            alarmIconSizeDp = (alarmSp * 1.05f).roundToInt().coerceIn(11, 24),
            horizontalPaddingDp = hPad,
            verticalPaddingDp = vPad,
            useExtendedDateFormat = useExtendedDate
        )
    }
}
