package com.example.widget

import android.app.Activity
import android.os.Bundle

/**
 * Lightweight translucent trampoline Activity invoked by widget click PendingIntent.
 * Using PendingIntent.getActivity avoids Android 12+ Background Activity Launch restrictions
 * while allowing multi-step fallback resolution across OEM Clock apps.
 */
class ClockLaunchTrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClockAppIntentHelper.launchSystemClockSafely(this)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}
