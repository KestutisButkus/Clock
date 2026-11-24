package com.example.widget

import com.example.model.ClockFontStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
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
        config: ClockWidgetConfig
    ): WidgetLayoutSpec {
        val safeWidth = widthDp.coerceAtLeast(100)
        val safeHeight = heightDp.coerceAtLeast(40)

        val bucket = when {
            safeHeight >= 95 -> WidgetSizeBucket.LARGE
            safeWidth < 175 -> WidgetSizeBucket.COMPACT
            else -> WidgetSizeBucket.WIDE
        }

        val hasMetadata = config.showDate || config.showNextAlarm

        val effectiveDatePlacement = when {
            !hasMetadata -> DatePlacement.BELOW
            bucket == WidgetSizeBucket.COMPACT || bucket == WidgetSizeBucket.LARGE -> DatePlacement.BELOW
            config.datePlacement == DatePlacement.BESIDE -> {
                if (safeWidth >= 230) DatePlacement.BESIDE else DatePlacement.BELOW
            }
            config.datePlacement == DatePlacement.BELOW -> DatePlacement.BELOW
            else -> {
                val besideMinWidth = if (config.showSeconds) 285 else 240
                if (safeWidth >= besideMinWidth) DatePlacement.BESIDE else DatePlacement.BELOW
            }
        }

        // Strict <= 5% padding on each edge so digits maximize available widget bounds
        val vPad = floor(safeHeight * 0.045f).toInt().coerceIn(1, 6)
        val hPad = floor(safeWidth * 0.03f).toInt().coerceIn(3, 10)

        val innerHeight = (safeHeight - vPad * 2).toFloat().coerceAtLeast(32f)
        val innerWidth = (safeWidth - hPad * 2).toFloat().coerceAtLeast(88f)

        // Meta row height only reduces vertical digit budget when stacked below the clock
        val metaSp = when (bucket) {
            WidgetSizeBucket.COMPACT -> (safeHeight * 0.19f).coerceIn(10f, 13f)
            WidgetSizeBucket.WIDE -> (safeHeight * 0.21f).coerceIn(11f, 15f)
            WidgetSizeBucket.LARGE -> (safeHeight * 0.135f).coerceIn(13f, 18f)
        }
        val alarmSp = (metaSp * 0.92f).coerceIn(9.5f, 16f)

        val metaStackedHeightDp = if (!hasMetadata ||
            (bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BESIDE)
        ) {
            0f
        } else {
            // Single compact meta line + minimal 2dp gap
            (metaSp * 1.15f) + 2f
        }

        val availableDigitHeightDp = (innerHeight - metaStackedHeightDp).coerceAtLeast(22f)

        // Cap height of digits in Roboto/Inter/Outfit is ~72% of font em-box (sp).
        // Dividing by 0.76 makes visible digits fill ~90-95% of availableDigitHeightDp.
        val maxTimeSpByHeight = availableDigitHeightDp / 0.76f

        val availableDigitWidthDp = when {
            bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BESIDE && hasMetadata -> {
                val reservedMetaWidth = if (config.showDate) 82f else 64f
                (innerWidth - reservedMetaWidth - 8f).coerceAtLeast(innerWidth * 0.58f)
            }
            else -> innerWidth
        }

        val ampmReserveFactor = if (config.timeFormatMode != com.example.model.TimeFormatMode.HOUR_24) 0.88f else 1.0f
        val effectiveCharUnits = if (config.showSeconds) 4.45f else 2.85f
        val fontWidthFactor = when (config.fontStyle) {
            ClockFontStyle.MONOSPACE -> 1.14f
            ClockFontStyle.CONDENSED -> 0.86f
            ClockFontStyle.SANS_SERIF -> 1.0f
            ClockFontStyle.DISPLAY -> 1.02f
        }

        val maxTimeSpByWidth = (availableDigitWidthDp * ampmReserveFactor) / (effectiveCharUnits * fontWidthFactor)

        val finalTimeSp = minOf(maxTimeSpByHeight, maxTimeSpByWidth).coerceIn(20f, 128f)
        val ampmSp = (finalTimeSp * 0.28f).coerceIn(9.5f, 22f)

        val useExtendedDate = (bucket == WidgetSizeBucket.LARGE && safeWidth >= 250) ||
            (bucket == WidgetSizeBucket.WIDE && effectiveDatePlacement == DatePlacement.BELOW && safeWidth >= 260)

        return WidgetLayoutSpec(
            bucket = bucket,
            effectiveDatePlacement = effectiveDatePlacement,
            timeTextSizeSp = finalTimeSp,
            ampmTextSizeSp = ampmSp,
            metaTextSizeSp = metaSp,
            alarmTextSizeSp = alarmSp,
            alarmIconSizeDp = (alarmSp * 1.05f).roundToInt().coerceIn(11, 17),
            horizontalPaddingDp = hPad,
            verticalPaddingDp = vPad,
            useExtendedDateFormat = useExtendedDate
        )
    }
}
