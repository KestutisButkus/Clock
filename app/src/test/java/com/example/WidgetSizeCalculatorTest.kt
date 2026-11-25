package com.example

import com.example.model.ClockFontStyle
import com.example.model.ClockWidgetConfig
import com.example.model.DatePlacement
import com.example.model.GridPreset
import com.example.model.WidgetSizeBucket
import com.example.widget.WidgetSizeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetSizeCalculatorTest {

    @Test
    fun `grid presets map to expected responsive buckets`() {
        val base = ClockWidgetConfig(appWidgetId = 1)

        assertEquals(
            WidgetSizeBucket.COMPACT,
            WidgetSizeCalculator.calculate(GridPreset.GRID_2X1.widthDp, GridPreset.GRID_2X1.heightDp, base).bucket
        )
        assertEquals(
            WidgetSizeBucket.WIDE,
            WidgetSizeCalculator.calculate(GridPreset.GRID_3X1.widthDp, GridPreset.GRID_3X1.heightDp, base).bucket
        )
        assertEquals(
            WidgetSizeBucket.WIDE,
            WidgetSizeCalculator.calculate(GridPreset.GRID_4X1.widthDp, GridPreset.GRID_4X1.heightDp, base).bucket
        )
        assertEquals(
            WidgetSizeBucket.WIDE,
            WidgetSizeCalculator.calculate(GridPreset.GRID_5X1.widthDp, GridPreset.GRID_5X1.heightDp, base).bucket
        )
        assertEquals(
            WidgetSizeBucket.LARGE,
            WidgetSizeCalculator.calculate(GridPreset.GRID_4X2.widthDp, GridPreset.GRID_4X2.heightDp, base).bucket
        )
        assertEquals(
            WidgetSizeBucket.LARGE,
            WidgetSizeCalculator.calculate(GridPreset.GRID_5X2.widthDp, GridPreset.GRID_5X2.heightDp, base).bucket
        )
    }

    @Test
    fun `enabling seconds reduces time text size to prevent clipping`() {
        GridPreset.entries.forEach { preset ->
            val minuteOnly = ClockWidgetConfig(appWidgetId = 1, showSeconds = false)
            val withSeconds = ClockWidgetConfig(appWidgetId = 1, showSeconds = true)

            val minuteSpec = WidgetSizeCalculator.calculate(preset.widthDp, preset.heightDp, minuteOnly)
            val secondsSpec = WidgetSizeCalculator.calculate(preset.widthDp, preset.heightDp, withSeconds)

            assertTrue(
                "Expected smaller time size with seconds on ${preset.label}",
                secondsSpec.timeTextSizeSp < minuteSpec.timeTextSizeSp
            )
        }
    }

    @Test
    fun `auto date placement chooses below on 3x1 and beside on 4x1 without seconds`() {
        val config = ClockWidgetConfig(appWidgetId = 2, showSeconds = false, datePlacement = DatePlacement.AUTO)

        val spec3x1 = WidgetSizeCalculator.calculate(GridPreset.GRID_3X1.widthDp, GridPreset.GRID_3X1.heightDp, config)
        val spec4x1 = WidgetSizeCalculator.calculate(GridPreset.GRID_4X1.widthDp, GridPreset.GRID_4X1.heightDp, config)

        assertEquals(DatePlacement.BELOW, spec3x1.effectiveDatePlacement)
        assertEquals(DatePlacement.BESIDE, spec4x1.effectiveDatePlacement)
    }

    @Test
    fun `monospace scales slightly smaller than condensed to avoid overflow`() {
        val mono = ClockWidgetConfig(appWidgetId = 3, fontStyle = ClockFontStyle.MONOSPACE, showSeconds = true)
        val condensed = ClockWidgetConfig(appWidgetId = 3, fontStyle = ClockFontStyle.CONDENSED, showSeconds = true)

        val monoSpec = WidgetSizeCalculator.calculate(280, 128, mono)
        val condensedSpec = WidgetSizeCalculator.calculate(280, 128, condensed)

        assertTrue(monoSpec.timeTextSizeSp < condensedSpec.timeTextSizeSp)
    }

    @Test
    fun `vertical padding stays at or below 5 percent across all grid presets`() {
        val config = ClockWidgetConfig(appWidgetId = 4)
        GridPreset.entries.forEach { preset ->
            val spec = WidgetSizeCalculator.calculate(preset.widthDp, preset.heightDp, config)
            assertTrue(
                "Expected <= 5% vertical padding on ${preset.label}, was ${spec.verticalPaddingDp}dp / ${preset.heightDp}dp",
                spec.verticalPaddingDp <= preset.heightDp * 0.05f
            )
        }
    }

    @Test
    fun `tablet dimensions use TABLET bucket and scale digits above 120sp`() {
        val config = ClockWidgetConfig(appWidgetId = 5, showSeconds = false)
        val spec = WidgetSizeCalculator.calculate(580, 185, config, isSystem24Hour = true)
        assertEquals(WidgetSizeBucket.TABLET, spec.bucket)
        assertTrue("Expected large tablet digits, got ${spec.timeTextSizeSp}", spec.timeTextSizeSp >= 120f)
    }
}
