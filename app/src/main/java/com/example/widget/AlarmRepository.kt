package com.example.widget

import android.app.AlarmManager
import android.content.Context
import android.text.format.DateFormat
import com.example.model.TimeFormatMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NextAlarmInfo(
    val triggerTimeMillis: Long,
    val formattedTime: String
)

object AlarmRepository {

    fun getNextAlarm(
        context: Context,
        timeFormatMode: TimeFormatMode,
        locale: Locale = Locale.getDefault()
    ): NextAlarmInfo? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null
        val nextAlarm = runCatching { alarmManager.nextAlarmClock }.getOrNull() ?: return null
        val triggerMillis = nextAlarm.triggerTime
        if (triggerMillis <= 0L) return null

        val use24Hour = when (timeFormatMode) {
            TimeFormatMode.SYSTEM -> DateFormat.is24HourFormat(context)
            TimeFormatMode.HOUR_12 -> false
            TimeFormatMode.HOUR_24 -> true
        }

        val pattern = if (use24Hour) "EEE HH:mm" else "EEE h:mm a"
        val formatted = SimpleDateFormat(pattern, locale).format(Date(triggerMillis))
        return NextAlarmInfo(
            triggerTimeMillis = triggerMillis,
            formattedTime = formatted
        )
    }
}
