package com.example.widget

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.data.WidgetPreferencesRepository

open class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = WidgetPreferencesRepository(context)
        for (appWidgetId in appWidgetIds) {
            repository.deleteConfig(appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        val allProviders: List<Class<out ClockWidgetProvider>> = listOf(
            ClockCompactWidgetProvider::class.java,
            ClockWidgetProvider::class.java,
            ClockWideWidgetProvider::class.java,
            ClockLargeWidgetProvider::class.java
        )

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
            appWidgetId: Int,
            options: Bundle? = appWidgetManager.getAppWidgetOptions(appWidgetId)
        ) {
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
            val repository = WidgetPreferencesRepository(context)
            val config = repository.loadConfig(appWidgetId)
            val remoteViews = ClockRemoteViewsBuilder.build(
                context = context,
                appWidgetId = appWidgetId,
                config = config,
                options = options
            )
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            getActiveWidgetIds(context).forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun getActiveWidgetIds(context: Context): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return allProviders
                .flatMap { cls ->
                    appWidgetManager.getAppWidgetIds(ComponentName(context, cls)).toList()
                }
                .distinct()
                .toIntArray()
        }
    }
}

class ClockCompactWidgetProvider : ClockWidgetProvider()
class ClockWideWidgetProvider : ClockWidgetProvider()
class ClockLargeWidgetProvider : ClockWidgetProvider()
