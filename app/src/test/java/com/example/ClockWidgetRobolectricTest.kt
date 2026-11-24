package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.data.WidgetPreferencesRepository
import com.example.model.ClockFontStyle
import com.example.model.ClockShadowStyle
import com.example.model.ClockWidgetConfig
import com.example.model.TimeFormatMode
import com.example.widget.AlarmRepository
import com.example.widget.ClockRemoteViewsBuilder
import com.example.widget.NextAlarmInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClockWidgetRobolectricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `per widget id configs are isolated and deleted cleanly`() {
        val repo = WidgetPreferencesRepository(context)
        val c1 = ClockWidgetConfig(appWidgetId = 101, showSeconds = true, fontStyle = ClockFontStyle.MONOSPACE)
        val c2 = ClockWidgetConfig(appWidgetId = 102, showSeconds = false, fontStyle = ClockFontStyle.CONDENSED)

        repo.saveConfig(c1)
        repo.saveConfig(c2)

        assertTrue(repo.loadConfig(101).showSeconds)
        assertEquals(ClockFontStyle.MONOSPACE, repo.loadConfig(101).fontStyle)

        assertFalse(repo.loadConfig(102).showSeconds)
        assertEquals(ClockFontStyle.CONDENSED, repo.loadConfig(102).fontStyle)

        repo.deleteConfig(101)
        assertFalse(repo.hasConfig(101))
        assertTrue(repo.hasConfig(102))
    }

    @Test
    fun `remote views hides alarm when nextAlarm is null and shows when present`() {
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val config = ClockWidgetConfig(appWidgetId = 200, showNextAlarm = true)

        val noAlarmViews = ClockRemoteViewsBuilder.buildForDimensions(
            context = context,
            widthDp = 280,
            heightDp = 128,
            config = config,
            nextAlarm = null,
            clickPendingIntent = pendingIntent
        )
        val noAlarmRoot = noAlarmViews.apply(context, FrameLayout(context))
        val hiddenAlarmContainer = noAlarmRoot.findViewById<View>(R.id.alarm_container_below)
        assertEquals(View.GONE, hiddenAlarmContainer.visibility)

        val withAlarmViews = ClockRemoteViewsBuilder.buildForDimensions(
            context = context,
            widthDp = 280,
            heightDp = 128,
            config = config,
            nextAlarm = NextAlarmInfo(1_800_000_000_000L, "Tue 07:30"),
            clickPendingIntent = pendingIntent
        )
        val withAlarmRoot = withAlarmViews.apply(context, FrameLayout(context))
        val visibleAlarmContainer = withAlarmRoot.findViewById<View>(R.id.alarm_container_below)
        val alarmText = withAlarmRoot.findViewById<TextView>(R.id.tv_alarm_below)
        assertEquals(View.VISIBLE, visibleAlarmContainer.visibility)
        assertEquals("Tue 07:30", alarmText.text.toString())
    }

    @Test
    fun `remote views sets seconds and 12h or 24h formats properly`() {
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val config12Sec = ClockWidgetConfig(
            appWidgetId = 201,
            timeFormatMode = TimeFormatMode.HOUR_12,
            showSeconds = true,
            shadowStyle = ClockShadowStyle.NONE
        )

        val views12 = ClockRemoteViewsBuilder.buildForDimensions(
            context = context,
            widthDp = 280,
            heightDp = 128,
            config = config12Sec,
            nextAlarm = null,
            clickPendingIntent = pendingIntent
        )
        val root12 = views12.apply(context, FrameLayout(context))
        val tc12 = root12.findViewById<TextClock>(R.id.tc_clock_item)
        val ampm12 = root12.findViewById<TextClock>(R.id.tc_ampm)

        assertEquals("h:mm:ss", tc12.format12Hour.toString())
        assertEquals("h:mm:ss", tc12.format24Hour.toString())
        assertEquals(View.VISIBLE, ampm12.visibility)

        val config24NoSec = ClockWidgetConfig(
            appWidgetId = 202,
            timeFormatMode = TimeFormatMode.HOUR_24,
            showSeconds = false,
            shadowStyle = ClockShadowStyle.OUTLINE
        )
        val views24 = ClockRemoteViewsBuilder.buildForDimensions(
            context = context,
            widthDp = 280,
            heightDp = 128,
            config = config24NoSec,
            nextAlarm = null,
            clickPendingIntent = pendingIntent
        )
        val root24 = views24.apply(context, FrameLayout(context))
        val layerFrame = root24.findViewById<FrameLayout>(R.id.time_layer_frame)
        val tc24 = root24.findViewById<TextClock>(R.id.tc_clock_item)
        val ampm24 = root24.findViewById<TextClock>(R.id.tc_ampm)

        // Outline mode creates 4 outline layers + 1 foreground layer
        assertEquals(5, layerFrame.childCount)
        assertEquals("HH:mm", tc24.format24Hour.toString())
        assertEquals(View.GONE, ampm24.visibility)
    }

    @Test
    fun `alarm repository returns null when no alarm is scheduled and formats when set`() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        assertNull(AlarmRepository.getNextAlarm(context, TimeFormatMode.SYSTEM))

        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val futureMillis = System.currentTimeMillis() + 3_600_000L
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(futureMillis, pendingIntent),
            pendingIntent
        )
        val info = AlarmRepository.getNextAlarm(context, TimeFormatMode.HOUR_24)
        assertEquals(futureMillis, info?.triggerTimeMillis)
    }
}
