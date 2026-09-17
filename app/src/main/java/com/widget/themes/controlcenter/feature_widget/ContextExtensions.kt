package com.widget.themes.controlcenter.feature_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import kotlin.reflect.KClass

fun <V : AppWidgetProvider> Context.addWidget(
    providerClass: KClass<V>, 
    widgetItem: WidgetItem, 
    isMineOrCustom: Boolean = false
) {
    val appWidgetManager = getSystemService(AppWidgetManager::class.java) ?: return
    val componentName = ComponentName(this, providerClass.java)
    val bundle = Bundle()
    
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        val widgetSize = when (widgetItem.size.lowercase()) {
            "2x2", "small" -> WidgetSize.SMALL
            "4x2", "medium" -> WidgetSize.MEDIUM
            "4x4", "large" -> WidgetSize.LARGE
            else -> WidgetSize.SMALL
        }
        val layoutRes = when (widgetSize) {
            WidgetSize.SMALL -> R.layout.widget_layout_2x2
            WidgetSize.MEDIUM -> R.layout.widget_layout_4x2
            WidgetSize.LARGE -> R.layout.widget_layout_4x4
        }
        
        try {
            val previewBitmap = WidgetRenderHelper.getSnapshotImage(
                context = this,
                layoutId = layoutRes,
                widgetSize = widgetSize,
                widgetItem = widgetItem
            )
            if (previewBitmap != null) {
                val maxDim = 320
                val scaledBitmap = if (previewBitmap.width > maxDim || previewBitmap.height > maxDim) {
                    val scale = maxDim.toFloat() / maxOf(previewBitmap.width, previewBitmap.height)
                    val w = (previewBitmap.width * scale).toInt().coerceAtLeast(1)
                    val h = (previewBitmap.height * scale).toInt().coerceAtLeast(1)
                    android.graphics.Bitmap.createScaledBitmap(previewBitmap, w, h, true)
                } else {
                    previewBitmap
                }
                val remoteViews = RemoteViews(packageName, R.layout.widget_container)
                remoteViews.setImageViewBitmap(R.id.ivWidget, scaledBitmap)
                bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val normSize = when (widgetItem.size.lowercase()) {
            "2x2", "small" -> "2x2"
            "4x2", "medium" -> "4x2"
            "4x4", "large" -> "4x4"
            else -> "2x2"
        }

        // Persist pending pin data so if OEM launcher skips WidgetReceiver callback, onUpdate can still bind this theme
        try {
            com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getPrefs(
                this, com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.PREFS_WIDGET
            ).edit()
                .putString("pending_pin_theme_id", widgetItem.id)
                .putString("pending_pin_theme_folder", widgetItem.themeFolder)
                .putString("pending_pin_widget_type", widgetItem.widgetType)
                .putString("pending_pin_size", normSize)
                .putLong("pending_pin_timestamp", System.currentTimeMillis())
                .putString("last_selected_theme_id", widgetItem.id)
                .putString("last_selected_theme_folder", widgetItem.themeFolder)
                .putString("last_selected_widget_type", widgetItem.widgetType)
                .putString("last_selected_size", normSize)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(this, WidgetReceiver::class.java).apply {
            putExtra("theme_id", widgetItem.id)
            putExtra("theme_folder", widgetItem.themeFolder)
            putExtra("widget_type", widgetItem.widgetType)
            putExtra("size", normSize)
            WidgetReceiver.widgetItem = widgetItem
            WidgetReceiver.isMineOrCustom = isMineOrCustom
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        
        appWidgetManager.requestPinAppWidget(componentName, bundle, pendingIntent)
    }
}
