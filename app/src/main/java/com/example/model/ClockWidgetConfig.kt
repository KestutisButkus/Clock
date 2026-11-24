package com.example.model

enum class TimeFormatMode(val label: String) {
    SYSTEM("System"),
    HOUR_12("12h AM/PM"),
    HOUR_24("24h")
}

enum class DatePlacement(val label: String) {
    AUTO("Auto"),
    BELOW("Below"),
    BESIDE("Beside")
}

enum class ClockFontStyle(val label: String) {
    SANS_SERIF("Sans"),
    CONDENSED("Condensed"),
    MONOSPACE("Mono"),
    DISPLAY("Geometric")
}

enum class ClockFontWeight(val label: String) {
    LIGHT("Light"),
    REGULAR("Regular"),
    BOLD("Bold")
}

enum class ClockShadowStyle(val label: String) {
    NONE("None"),
    SUBTLE("Soft"),
    STRONG("Strong"),
    OUTLINE("Outline")
}

enum class WidgetBackgroundStyle(val label: String) {
    TRANSPARENT("Clear"),
    FROSTED_DARK("Frost Dark"),
    FROSTED_LIGHT("Frost Light"),
    SOLID_DARK("Midnight")
}

enum class GridPreset(
    val label: String,
    val widthDp: Int,
    val heightDp: Int
) {
    GRID_2X1("2×1", 130, 56),
    GRID_3X1("3×1", 205, 56),
    GRID_4X1("4×1", 280, 62),
    GRID_5X1("5×1", 350, 64),
    GRID_4X2("4×2", 280, 128),
    GRID_5X2("5×2", 350, 138)
}

enum class WidgetSizeBucket {
    COMPACT,
    WIDE,
    LARGE
}

data class ClockWidgetConfig(
    val appWidgetId: Int,
    val timeFormatMode: TimeFormatMode = TimeFormatMode.SYSTEM,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val datePlacement: DatePlacement = DatePlacement.AUTO,
    val showNextAlarm: Boolean = true,
    val fontStyle: ClockFontStyle = ClockFontStyle.SANS_SERIF,
    val fontWeight: ClockFontWeight = ClockFontWeight.BOLD,
    val textColorArgb: Int = 0xFFFFFFFF.toInt(),
    val accentColorArgb: Int = 0xFF38BDF8.toInt(),
    val shadowStyle: ClockShadowStyle = ClockShadowStyle.SUBTLE,
    val backgroundStyle: WidgetBackgroundStyle = WidgetBackgroundStyle.FROSTED_DARK
)

object WidgetColorPalette {
    val textPresets: List<Pair<String, Int>> = listOf(
        "Pure White" to 0xFFFFFFFF.toInt(),
        "Warm Ivory" to 0xFFFDF4E3.toInt(),
        "Sky Mist" to 0xFFE0F2FE.toInt(),
        "Obsidian" to 0xFF0F172A.toInt(),
        "Amber Glow" to 0xFFFDE68A.toInt(),
        "Mint Ice" to 0xFFA7F3D0.toInt()
    )

    val accentPresets: List<Pair<String, Int>> = listOf(
        "Sky Blue" to 0xFF38BDF8.toInt(),
        "Warm Amber" to 0xFFF59E0B.toInt(),
        "Emerald" to 0xFF34D399.toInt(),
        "Coral" to 0xFFFB7185.toInt(),
        "Lavender" to 0xFFA78BFA.toInt(),
        "Slate" to 0xFFCBD5E1.toInt()
    )
}
