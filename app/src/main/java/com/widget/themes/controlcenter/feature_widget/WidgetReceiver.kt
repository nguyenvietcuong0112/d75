package com.widget.themes.controlcenter.feature_widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.EventBus
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.feature_widget.event.WidgetAddSucceedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetReceiver : BroadcastReceiver() {

    companion object {
        var widgetItem: WidgetItem? = null
        var isMineOrCustom: Boolean = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras ?: return
        val appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        
        if (appWidgetId != 0) {
            context?.let { ctx ->
                Toast.makeText(ctx, ctx.getString(R.string.toast_widget_added), Toast.LENGTH_SHORT).show()

                val item = widgetItem
                val prefs = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getPrefs(
                    ctx, com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.PREFS_WIDGET
                )
                val themeId = extras.getString("theme_id") ?: item?.id ?: prefs.getString("pending_pin_theme_id", null) ?: prefs.getString("last_selected_theme_id", "1") ?: "1"
                val themeFolder = extras.getString("theme_folder") ?: item?.themeFolder ?: prefs.getString("pending_pin_theme_folder", null) ?: prefs.getString("last_selected_theme_folder", "theme_1") ?: "theme_1"
                val widgetType = extras.getString("widget_type") ?: item?.widgetType ?: prefs.getString("pending_pin_widget_type", null) ?: prefs.getString("last_selected_widget_type", "weather") ?: "weather"
                val rawSize = extras.getString("size") ?: item?.size ?: prefs.getString("pending_pin_size", null) ?: "4x2"
                val size = when (rawSize.lowercase()) {
                    "small", "2x2" -> "2x2"
                    "medium", "4x2" -> "4x2"
                    "large", "4x4" -> "4x4"
                    else -> rawSize
                }
                
                val cleanId = themeId.replace('/', '_').replace('\\', '_')
                val cleanFolder = themeFolder.replace('/', '_').replace('\\', '_')
                val candidateFiles = listOf(
                    "widget_bg_${cleanId}_${widgetType}_$size.png",
                    "widget_bg_${cleanId.removePrefix("widget_")}_${widgetType}_$size.png",
                    "widget_bg_${cleanFolder}_${widgetType}_$size.png",
                    "widget_bg_${cleanFolder.removePrefix("widget_")}_${widgetType}_$size.png"
                )
                var file = candidateFiles.map { ctx.getFileStreamPath(it) }.firstOrNull { it != null && it.exists() }
                if (file == null) {
                    val allFiles = ctx.fileList() ?: emptyArray()
                    val match = allFiles.firstOrNull { it.startsWith("widget_bg_") && (it.contains(cleanId) || it.contains(cleanFolder)) }
                    if (match != null) {
                        file = ctx.getFileStreamPath(match)
                    }
                }

                val imageUriStr = if (file != null && file.exists()) Uri.fromFile(file).toString() else null

                val config = WidgetConfig(
                    widgetId = appWidgetId,
                    bgType = if (imageUriStr != null) "IMAGE" else "COLOR",
                    solidColor = 0xFF232433.toInt(),
                    imageUri = imageUriStr,
                    textColor = android.graphics.Color.WHITE,
                    fontStyle = "normal",
                    gradientStartColor = 0,
                    gradientEndColor = 0
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getPrefs(ctx, com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.PREFS_WIDGET).edit()
                            .putString("widget_type_$appWidgetId", widgetType)
                            .putString("theme_folder_$appWidgetId", themeFolder)
                            .putString("theme_id_$appWidgetId", themeId)
                            .putString("widget_size_$appWidgetId", size)
                            .commit()

                        ServiceLocator.getWidgetConfigDao(ctx).saveConfig(config)
                        val appWidgetManager = AppWidgetManager.getInstance(ctx)

                        when {
                            size == "4x2" || size.lowercase() == "medium" -> {
                                Widget4x2Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                            }
                            size == "4x4" || size.lowercase() == "large" -> {
                                Widget4x4Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                            }
                            else -> {
                                Widget2x2Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                            }
                        }
                        BaseWidgetProvider.schedulePeriodicUpdate(ctx)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            EventBus.getDefault().post(WidgetAddSucceedEvent(appWidgetId))
        }
    }
}
