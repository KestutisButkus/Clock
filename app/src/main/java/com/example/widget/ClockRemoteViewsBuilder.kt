package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
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
import java.util.Locale
import kotlin.math.roundToInt

object ClockRemoteViewsBuilder {

    fun build(
        context: Context,
        appWidgetId: Int,
        config: ClockWidgetConfig,
        options: Bundle?
    ): RemoteViews {
        val nextAlarm = if (config.showNextAlarm) {
            AlarmRepository.getNextAlarm(context, config.timeFormatMode)
        } else {
            null
        }
        val clickPendingIntent = createClockLaunchPendingIntent(context, appWidgetId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val sizeMap = GridPreset.entries.associate { preset ->
                SizeF(preset.widthDp.toFloat(), preset.heightDp.toFloat()) to
                    buildForDimensions(
                        context = context,
                        widthDp = preset.widthDp,
                        heightDp = preset.heightDp,
                        config = config,
                        nextAlarm = nextAlarm,
                        clickPendingIntent = clickPendingIntent
                    )
            }
            return RemoteViews(sizeMap)
        }

        val widthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 280)
            ?.takeIf { it > 0 } ?: 280
        val heightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 62)
            ?.takeIf { it > 0 } ?: 62

        return buildForDimensions(
            context = context,
            widthDp = widthDp,
            heightDp = heightDp,
            config = config,
            nextAlarm = nextAlarm,
            clickPendingIntent = clickPendingIntent
        )
    }

    fun buildForDimensions(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        config: ClockWidgetConfig,
        nextAlarm: NextAlarmInfo?,
        clickPendingIntent: PendingIntent
    ): RemoteViews {
        val spec = WidgetSizeCalculator.calculate(widthDp, heightDp, config)
        val layoutRes = when (spec.bucket) {
            WidgetSizeBucket.COMPACT -> R.layout.widget_clock_compact
            WidgetSizeBucket.WIDE -> R.layout.widget_clock_wide
            WidgetSizeBucket.LARGE -> R.layout.widget_clock_large
        }

        val views = RemoteViews(context.packageName, layoutRes)

        val bgRes = when (config.backgroundStyle) {
            WidgetBackgroundStyle.TRANSPARENT -> R.drawable.bg_widget_transparent
            WidgetBackgroundStyle.FROSTED_DARK -> R.drawable.bg_widget_frosted_dark
            WidgetBackgroundStyle.FROSTED_LIGHT -> R.drawable.bg_widget_frosted_light
            WidgetBackgroundStyle.SOLID_DARK -> R.drawable.bg_widget_solid_dark
        }
        views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)

        val hPadPx = dpToPx(context, spec.horizontalPaddingDp)
        val vPadPx = dpToPx(context, spec.verticalPaddingDp)
        views.setViewPadding(R.id.widget_root, hPadPx, vPadPx, hPadPx, vPadPx)
        views.setOnClickPendingIntent(R.id.widget_root, clickPendingIntent)

        // Compose native TextClock layers for font style + weight + shadow/outline
        populateTimeLayers(context, views, spec, config)

        // Configure AM/PM TextClock
        when (config.timeFormatMode) {
            TimeFormatMode.SYSTEM -> {
                views.setViewVisibility(R.id.tc_ampm, View.VISIBLE)
                views.setCharSequence(R.id.tc_ampm, "setFormat12Hour", "a")
                views.setCharSequence(R.id.tc_ampm, "setFormat24Hour", "")
            }
            TimeFormatMode.HOUR_12 -> {
                views.setViewVisibility(R.id.tc_ampm, View.VISIBLE)
                views.setCharSequence(R.id.tc_ampm, "setFormat12Hour", "a")
                views.setCharSequence(R.id.tc_ampm, "setFormat24Hour", "a")
            }
            TimeFormatMode.HOUR_24 -> {
                views.setViewVisibility(R.id.tc_ampm, View.GONE)
            }
        }
        views.setTextViewTextSize(R.id.tc_ampm, TypedValue.COMPLEX_UNIT_SP, spec.ampmTextSizeSp)
        views.setTextColor(R.id.tc_ampm, config.accentColorArgb)

        // Date formatting & metadata placement
        val dateSkeleton = if (spec.useExtendedDateFormat) "EEEEMMMd" else "EEEMMMd"
        val localizedDatePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), dateSkeleton)
        val dateColor = ColorUtils.setAlphaComponent(config.textColorArgb, 224)
        val showAnyMeta = config.showDate || nextAlarm != null

        when (spec.bucket) {
            WidgetSizeBucket.COMPACT, WidgetSizeBucket.LARGE -> {
                views.setViewVisibility(
                    R.id.meta_below_row,
                    if (showAnyMeta) View.VISIBLE else View.GONE
                )
                bindDateClock(
                    views = views,
                    viewId = R.id.tc_date_below,
                    visible = config.showDate,
                    pattern = localizedDatePattern,
                    textSizeSp = spec.metaTextSizeSp,
                    textColor = dateColor
                )
                bindAlarm(
                    views = views,
                    containerId = R.id.alarm_container_below,
                    textId = R.id.tv_alarm_below,
                    iconId = R.id.iv_alarm_below,
                    nextAlarm = nextAlarm,
                    textSizeSp = spec.alarmTextSizeSp,
                    accentColor = config.accentColorArgb
                )
            }
            WidgetSizeBucket.WIDE -> {
                val placeBeside = spec.effectiveDatePlacement == DatePlacement.BESIDE && showAnyMeta
                val placeBelow = spec.effectiveDatePlacement != DatePlacement.BESIDE && showAnyMeta

                views.setViewVisibility(
                    R.id.meta_beside_column,
                    if (placeBeside) View.VISIBLE else View.GONE
                )
                views.setViewVisibility(
                    R.id.meta_below_row,
                    if (placeBelow) View.VISIBLE else View.GONE
                )

                if (placeBeside) {
                    bindDateClock(
                        views = views,
                        viewId = R.id.tc_date_beside,
                        visible = config.showDate,
                        pattern = localizedDatePattern,
                        textSizeSp = spec.metaTextSizeSp,
                        textColor = dateColor
                    )
                    bindAlarm(
                        views = views,
                        containerId = R.id.alarm_container_beside,
                        textId = R.id.tv_alarm_beside,
                        iconId = R.id.iv_alarm_beside,
                        nextAlarm = nextAlarm,
                        textSizeSp = spec.alarmTextSizeSp,
                        accentColor = config.accentColorArgb
                    )
                } else {
                    bindDateClock(
                        views = views,
                        viewId = R.id.tc_date_below,
                        visible = config.showDate,
                        pattern = localizedDatePattern,
                        textSizeSp = spec.metaTextSizeSp,
                        textColor = dateColor
                    )
                    bindAlarm(
                        views = views,
                        containerId = R.id.alarm_container_below,
                        textId = R.id.tv_alarm_below,
                        iconId = R.id.iv_alarm_below,
                        nextAlarm = nextAlarm,
                        textSizeSp = spec.alarmTextSizeSp,
                        accentColor = config.accentColorArgb
                    )
                }
            }
        }

        return views
    }

    private fun populateTimeLayers(
        context: Context,
        parent: RemoteViews,
        spec: WidgetLayoutSpec,
        config: ClockWidgetConfig
    ) {
        parent.removeAllViews(R.id.time_layer_frame)
        val leafLayout = resolveLeafLayout(config.fontStyle, config.fontWeight)
        val dp1 = dpToPx(context, 1)
        val dp2 = dpToPx(context, 2)
        val dp3 = dpToPx(context, 3)

        val pattern12 = if (config.showSeconds) "h:mm:ss" else "h:mm"
        val pattern24 = if (config.showSeconds) "HH:mm:ss" else "HH:mm"
        val (effective12, effective24) = when (config.timeFormatMode) {
            TimeFormatMode.SYSTEM -> pattern12 to pattern24
            TimeFormatMode.HOUR_12 -> pattern12 to pattern12
            TimeFormatMode.HOUR_24 -> pattern24 to pattern24
        }

        fun createLayer(
            color: Int,
            padLeft: Int,
            padTop: Int,
            padRight: Int,
            padBottom: Int
        ): RemoteViews {
            return RemoteViews(context.packageName, leafLayout).apply {
                setTextViewTextSize(R.id.tc_clock_item, TypedValue.COMPLEX_UNIT_SP, spec.timeTextSizeSp)
                setTextColor(R.id.tc_clock_item, color)
                setCharSequence(R.id.tc_clock_item, "setFormat12Hour", effective12)
                setCharSequence(R.id.tc_clock_item, "setFormat24Hour", effective24)
                setViewPadding(R.id.tc_wrapper, padLeft, padTop, padRight, padBottom)
            }
        }

        val effectBaseColor = if (ColorUtils.calculateLuminance(config.textColorArgb) < 0.25) {
            Color.WHITE
        } else {
            Color.BLACK
        }

        when (config.shadowStyle) {
            ClockShadowStyle.NONE -> {
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(config.textColorArgb, 0, 0, 0, 0)
                )
            }
            ClockShadowStyle.SUBTLE -> {
                val shadowColor = ColorUtils.setAlphaComponent(effectBaseColor, 140)
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(shadowColor, dp1, dp2, 0, 0)
                )
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(config.textColorArgb, 0, 0, dp1, dp2)
                )
            }
            ClockShadowStyle.STRONG -> {
                val shadowColor = ColorUtils.setAlphaComponent(effectBaseColor, 215)
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(shadowColor, dp2, dp3, 0, 0)
                )
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(config.textColorArgb, 0, 0, dp2, dp3)
                )
            }
            ClockShadowStyle.OUTLINE -> {
                val outlineColor = ColorUtils.setAlphaComponent(effectBaseColor, 235)
                parent.addView(R.id.time_layer_frame, createLayer(outlineColor, 0, dp1, dp2, dp1))
                parent.addView(R.id.time_layer_frame, createLayer(outlineColor, dp2, dp1, 0, dp1))
                parent.addView(R.id.time_layer_frame, createLayer(outlineColor, dp1, 0, dp1, dp2))
                parent.addView(R.id.time_layer_frame, createLayer(outlineColor, dp1, dp2, dp1, 0))
                parent.addView(
                    R.id.time_layer_frame,
                    createLayer(config.textColorArgb, dp1, dp1, dp1, dp1)
                )
            }
        }
    }

    private fun bindDateClock(
        views: RemoteViews,
        viewId: Int,
        visible: Boolean,
        pattern: String,
        textSizeSp: Float,
        textColor: Int
    ) {
        if (!visible) {
            views.setViewVisibility(viewId, View.GONE)
            return
        }
        views.setViewVisibility(viewId, View.VISIBLE)
        views.setCharSequence(viewId, "setFormat12Hour", pattern)
        views.setCharSequence(viewId, "setFormat24Hour", pattern)
        views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, textSizeSp)
        views.setTextColor(viewId, textColor)
    }

    private fun bindAlarm(
        views: RemoteViews,
        containerId: Int,
        textId: Int,
        iconId: Int,
        nextAlarm: NextAlarmInfo?,
        textSizeSp: Float,
        accentColor: Int
    ) {
        if (nextAlarm == null) {
            views.setViewVisibility(containerId, View.GONE)
            return
        }
        views.setViewVisibility(containerId, View.VISIBLE)
        views.setTextViewText(textId, nextAlarm.formattedTime)
        views.setTextViewTextSize(textId, TypedValue.COMPLEX_UNIT_SP, textSizeSp)
        views.setTextColor(textId, accentColor)
        views.setInt(iconId, "setColorFilter", accentColor)
    }

    @LayoutRes
    private fun resolveLeafLayout(fontStyle: ClockFontStyle, weight: ClockFontWeight): Int {
        return when (fontStyle) {
            ClockFontStyle.SANS_SERIF -> when (weight) {
                ClockFontWeight.LIGHT -> R.layout.tc_sans_light
                ClockFontWeight.REGULAR -> R.layout.tc_sans_regular
                ClockFontWeight.BOLD -> R.layout.tc_sans_bold
            }
            ClockFontStyle.CONDENSED -> when (weight) {
                ClockFontWeight.LIGHT -> R.layout.tc_condensed_light
                ClockFontWeight.REGULAR -> R.layout.tc_condensed_regular
                ClockFontWeight.BOLD -> R.layout.tc_condensed_bold
            }
            ClockFontStyle.MONOSPACE -> when (weight) {
                ClockFontWeight.LIGHT -> R.layout.tc_mono_light
                ClockFontWeight.REGULAR -> R.layout.tc_mono_regular
                ClockFontWeight.BOLD -> R.layout.tc_mono_bold
            }
            ClockFontStyle.DISPLAY -> when (weight) {
                ClockFontWeight.LIGHT -> R.layout.tc_display_light
                ClockFontWeight.REGULAR -> R.layout.tc_display_regular
                ClockFontWeight.BOLD -> R.layout.tc_display_bold
            }
        }
    }

    private fun createClockLaunchPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, ClockLaunchTrampolineActivity::class.java).apply {
            action = "com.example.widget.ACTION_OPEN_CLOCK_$appWidgetId"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).roundToInt()
    }
}
