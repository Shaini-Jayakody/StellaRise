package com.example.stellarise.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.stellarise.MainActivity
import com.example.stellarise.R
import com.example.stellarise.data.DataManager
import java.text.NumberFormat

/**
 * Home-screen widget showing today's habit completion percentage
 * Displays progress as a visual indicator with percentage
 */
class StellariseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is deleted
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dataManager = DataManager(context)
            val completionRate = dataManager.getTodayHabitsCompletionRate()
            val totalHabits = dataManager.getTotalHabitsCount()
            val completedHabits = dataManager.getCompletedHabitsCount()

            val views = RemoteViews(context.packageName, R.layout.widget_stellarise)
            
            // Update progress text
            val percentage = (completionRate * 100).toInt()
            views.setTextViewText(R.id.tvProgressText, "$percentage%")
            views.setTextViewText(R.id.tvHabitsCount, "$completedHabits/$totalHabits habits")
            
            // Update progress bar
            views.setProgressBar(R.id.progressBar, 100, percentage, false)
            
            // Set click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
            
            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}