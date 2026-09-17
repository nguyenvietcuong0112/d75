package com.widget.themes.controlcenter.feature_widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.widget.themes.controlcenter.core.data.EventBus
import com.widget.themes.controlcenter.core.data.ShortcutEvent

class SingleIconWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val iconId = intent.getStringExtra("icon_id") ?: ""
        val targetPkg = intent.getStringExtra("target_pkg") ?: ""
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && iconId.isNotEmpty()) {
            val prefs = context.getSharedPreferences("single_icon_widgets", Context.MODE_PRIVATE)
            val filePath = prefs.getString("pending_file_$iconId", null)
            if (filePath != null) {
                prefs.edit()
                    .putString("pkg_$appWidgetId", targetPkg)
                    .putString("file_$appWidgetId", filePath)
                    .remove("pending_file_$iconId")
                    .remove("pending_pkg_$iconId")
                    .apply()

                val appWidgetManager = AppWidgetManager.getInstance(context)
                if (appWidgetManager != null) {
                    SingleIconWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }

        // Notify that shortcut/widget was installed so batch processing can advance
        EventBus.getDefault().post(ShortcutEvent())
    }
}
