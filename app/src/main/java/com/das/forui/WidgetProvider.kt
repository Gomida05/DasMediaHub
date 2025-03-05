package com.das.forui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgetProvider: AppWidgetProvider() {

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        appWidgetIds.forEach { appWidgetId ->

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )



            val views: RemoteViews = RemoteViews(
                context.applicationContext.packageName,
                R.layout.loading_appwidget
            ).apply {
                setOnClickPendingIntent(R.id.buttonMe, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)

        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }
}