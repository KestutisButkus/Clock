package com.example.widget

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.widget.Toast
import com.example.MainActivity
import com.example.R

object ClockAppIntentHelper {

    private val knownClockPackages = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.oneplus.deskclock",
        "com.coloros.alarmclock",
        "com.miui.clock",
        "com.huawei.deskclock",
        "com.asus.deskclock"
    )

    fun resolveBestClockIntent(context: Context): Intent? {
        val pm = context.packageManager
        val candidates = buildList {
            add(Intent(AlarmClock.ACTION_SHOW_ALARMS))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(Intent(AlarmClock.ACTION_SHOW_TIMERS))
            }
            add(Intent(AlarmClock.ACTION_SET_ALARM))
            knownClockPackages.forEach { pkg ->
                pm.getLaunchIntentForPackage(pkg)?.let { add(it) }
            }
        }

        for (candidate in candidates) {
            candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.resolveActivity(candidate, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.resolveActivity(candidate, 0)
            }
            if (resolved != null && resolved.activityInfo?.packageName != context.packageName) {
                return candidate
            }
        }
        return null
    }

    fun launchSystemClockSafely(context: Context): Boolean {
        val clockIntent = resolveBestClockIntent(context)
        if (clockIntent != null) {
            val launched = runCatching {
                context.startActivity(clockIntent)
                true
            }.getOrElse { error ->
                if (error is ActivityNotFoundException || error is SecurityException) {
                    false
                } else {
                    false
                }
            }
            if (launched) return true
        }

        Toast.makeText(
            context,
            context.getString(R.string.clock_app_unavailable),
            Toast.LENGTH_SHORT
        ).show()

        val fallbackIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        runCatching { context.startActivity(fallbackIntent) }
        return false
    }
}
