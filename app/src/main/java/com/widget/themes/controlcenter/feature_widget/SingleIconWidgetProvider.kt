package com.widget.themes.controlcenter.feature_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.widget.themes.controlcenter.R
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SingleIconWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (appWidgetId in appWidgetIds) {
            val filePath = prefs.getString("file_$appWidgetId", null)
            if (filePath != null) {
                try {
                    File(filePath).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            editor.remove("file_$appWidgetId")
            editor.remove("pkg_$appWidgetId")
            editor.remove("label_$appWidgetId")
        }
        editor.apply()
    }

    companion object {
        const val PREFS_NAME = "single_icon_widgets"
        const val ACTION_PINNED = "com.widget.themes.controlcenter.SINGLE_ICON_WIDGET_PINNED"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var filePath = prefs.getString("file_$appWidgetId", null)
            var targetPkg = prefs.getString("pkg_$appWidgetId", null)

            if (filePath == null) {
                val allPending = prefs.all.keys.filter { it.startsWith("pending_file_") }
                val latestPendingKey = allPending.firstOrNull()
                if (latestPendingKey != null) {
                    val iconId = latestPendingKey.removePrefix("pending_file_")
                    filePath = prefs.getString("pending_file_$iconId", null)
                    targetPkg = prefs.getString("pending_pkg_$iconId", null)
                    if (filePath != null) {
                        prefs.edit()
                            .putString("file_$appWidgetId", filePath)
                            .putString("pkg_$appWidgetId", targetPkg)
                            .remove("pending_file_$iconId")
                            .remove("pending_pkg_$iconId")
                            .apply()
                    }
                }
            }

            val views = RemoteViews(context.packageName, R.layout.widget_single_icon)

            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.ivSingleIcon, bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (!targetPkg.isNullOrEmpty()) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPkg)
                    ?: Intent(context, ChangeIconActivity::class.java).apply {
                        putExtra("target_package", targetPkg)
                    }
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getActivity(context, appWidgetId, launchIntent, flags)
                views.setOnClickPendingIntent(R.id.rootSingleIcon, pendingIntent)
                views.setOnClickPendingIntent(R.id.ivSingleIcon, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun pinIconWidget(
            context: Context,
            targetPkg: String,
            bitmap: Bitmap,
            label: String? = null,
            iconId: String = UUID.randomUUID().toString()
        ): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return false
            }

            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false
            if (!appWidgetManager.isRequestPinAppWidgetSupported) {
                return false
            }

            return try {
                val dir = File(context.filesDir, "single_icons")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val safeIconId = iconId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val iconFile = File(dir, "icon_${safeIconId}_${System.currentTimeMillis()}.png")
                FileOutputStream(iconFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("pending_file_$iconId", iconFile.absolutePath)
                    .putString("pending_pkg_$iconId", targetPkg)
                    .putString("pending_label_$iconId", label ?: "")
                    .apply()

                val previewViews = RemoteViews(context.packageName, R.layout.widget_single_icon).apply {
                    setImageViewBitmap(R.id.ivSingleIcon, bitmap)
                }
                val previewBundle = Bundle().apply {
                    putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, previewViews)
                }

                val successIntent = Intent(context, SingleIconWidgetReceiver::class.java).apply {
                    action = ACTION_PINNED
                    putExtra("icon_id", iconId)
                    putExtra("target_pkg", targetPkg)
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val successCallback = PendingIntent.getBroadcast(
                    context,
                    iconId.hashCode(),
                    successIntent,
                    flags
                )

                val providerComponent = ComponentName(context, SingleIconWidgetProvider::class.java)
                appWidgetManager.requestPinAppWidget(providerComponent, previewBundle, successCallback)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
