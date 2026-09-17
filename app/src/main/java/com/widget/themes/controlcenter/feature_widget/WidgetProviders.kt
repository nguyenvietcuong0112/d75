package com.widget.themes.controlcenter.feature_widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import android.view.View
import com.bumptech.glide.Glide
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

open class BaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        schedulePeriodicUpdate(context)
        WidgetClockTicker.start(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
        WeatherHelper.updateWeather(context)
        WidgetClockTicker.start(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (ids != null && ids.isNotEmpty()) {
                    for (id in ids) {
                        updateWidget(context, appWidgetManager, id)
                    }
                } else {
                    updateAllWidgets(context)
                }
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_PRESENT,
            ACTION_AUTO_UPDATE -> {
                updateAllWidgets(context)
                WeatherHelper.updateWeather(context)
                schedulePeriodicUpdate(context)
            }
        }
    }

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        // Subclasses implement this
    }

    protected fun applyWidgetConfig(context: Context, views: RemoteViews, config: WidgetConfig?, defaultBg: Int, widgetId: Int) {
        if (config == null) {
            views.setInt(R.id.ivBackground, "setBackgroundColor", defaultBg)
            return
        }

        when (config.bgType) {
            "COLOR" -> {
                views.setImageViewBitmap(R.id.ivBackground, null)
                views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
            }
            "GRADIENT" -> {
                try {
                    val width = 400
                    val height = 400
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    val gd = GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(config.gradientStartColor, config.gradientEndColor)
                    )
                    gd.setBounds(0, 0, width, height)
                    gd.cornerRadius = 24f
                    gd.draw(canvas)
                    views.setImageViewBitmap(R.id.ivBackground, bmp)
                } catch (e: Exception) {
                    views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                }
            }
            "IMAGE" -> {
                if (config.imageUri != null) {
                    thread {
                        try {
                            val bmp = Glide.with(context.applicationContext)
                                .asBitmap()
                                .load(Uri.parse(config.imageUri))
                                .submit()
                                .get()
                            views.setImageViewBitmap(R.id.ivBackground, bmp)
                            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
                        }
                    }
                } else {
                    views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                }
            }
        }
    }

    protected fun getTypeface(context: Context, fontStyle: String?): Typeface {
        if (fontStyle.isNullOrEmpty() || fontStyle == "normal") return Typeface.DEFAULT
        return try {
            Typeface.createFromAsset(context.assets, "assets_theme/fonts/$fontStyle")
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
    }
    protected fun attachWidgetClickIntent(context: Context, views: RemoteViews, widgetId: Int) {
        val intent = Intent(context, com.widget.themes.controlcenter.feature_main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_tab", 4)
            putExtra("target_collection_category", "Widget")
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, widgetId, intent, flags)
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
        views.setOnClickPendingIntent(R.id.ivWidget, pendingIntent)
    }

    protected fun safeUpdateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        views: RemoteViews,
        bitmap: Bitmap?
    ) {
        attachWidgetClickIntent(context, views, widgetId)
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidget, bitmap)
        }
        try {
            appWidgetManager.updateAppWidget(widgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
            if (bitmap != null) {
                try {
                    val scaled = Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * 0.65f).toInt().coerceAtLeast(1),
                        (bitmap.height * 0.65f).toInt().coerceAtLeast(1),
                        true
                    )
                    views.setImageViewBitmap(R.id.ivWidget, scaled)
                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val ACTION_AUTO_UPDATE = "com.widget.themes.controlcenter.WIDGET_AUTO_UPDATE"

        fun resolveWidgetBinding(
            context: Context,
            widgetId: Int,
            defaultType: String,
            defaultSize: String
        ): Triple<String, String, String> {
            val prefs = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getPrefs(
                context, com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.PREFS_WIDGET
            )
            var themeFolder = prefs.getString("theme_folder_$widgetId", null)
            var themeId = prefs.getString("theme_id_$widgetId", null)
            var widgetType = prefs.getString("widget_type_$widgetId", null)

            if (themeFolder.isNullOrEmpty()) {
                val pendingTime = prefs.getLong("pending_pin_timestamp", 0L)
                val isRecentPending = System.currentTimeMillis() - pendingTime < 10 * 60 * 1000L
                if (isRecentPending) {
                    themeFolder = prefs.getString("pending_pin_theme_folder", null)
                    themeId = prefs.getString("pending_pin_theme_id", null)
                    widgetType = prefs.getString("pending_pin_widget_type", null)
                }
                if (themeFolder.isNullOrEmpty()) {
                    themeFolder = prefs.getString("last_selected_theme_folder", "theme_1")
                    themeId = prefs.getString("last_selected_theme_id", "1")
                    widgetType = prefs.getString("last_selected_widget_type", defaultType)
                }
                themeFolder = themeFolder ?: "theme_1"
                themeId = themeId ?: "1"
                widgetType = widgetType ?: defaultType

                prefs.edit()
                    .putString("theme_folder_$widgetId", themeFolder)
                    .putString("theme_id_$widgetId", themeId)
                    .putString("widget_type_$widgetId", widgetType)
                    .putString("widget_size_$widgetId", defaultSize)
                    .apply()

                // Save config to Room database in background
                thread {
                    try {
                        val cleanId = themeId.replace('/', '_').replace('\\', '_')
                        val sizeStr = when (defaultSize.lowercase()) {
                            "small", "2x2" -> "2x2"
                            "medium", "4x2" -> "4x2"
                            "large", "4x4" -> "4x4"
                            else -> defaultSize
                        }
                        val fileName = "widget_bg_${cleanId}_${widgetType}_$sizeStr.png"
                        val file = context.getFileStreamPath(fileName)
                        val imageUriStr = if (file.exists()) Uri.fromFile(file).toString() else null

                        val config = WidgetConfig(
                            widgetId = widgetId,
                            bgType = if (imageUriStr != null) "IMAGE" else "COLOR",
                            solidColor = 0xFF1E1E2E.toInt(),
                            imageUri = imageUriStr,
                            textColor = android.graphics.Color.WHITE,
                            fontStyle = "normal",
                            gradientStartColor = 0,
                            gradientEndColor = 0
                        )
                        ServiceLocator.getWidgetConfigDao(context).saveConfig(config)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return Triple(themeId ?: "1", themeFolder ?: "theme_1", widgetType ?: defaultType)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val c2x2 = ComponentName(context, Widget2x2Provider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(c2x2)) {
                Widget2x2Provider().updateWidget(context, appWidgetManager, id)
            }
            val c4x2 = ComponentName(context, Widget4x2Provider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(c4x2)) {
                Widget4x2Provider().updateWidget(context, appWidgetManager, id)
            }
            val c4x4 = ComponentName(context, Widget4x4Provider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(c4x4)) {
                Widget4x4Provider().updateWidget(context, appWidgetManager, id)
            }
        }

        fun schedulePeriodicUpdate(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, Widget2x2Provider::class.java).apply {
                    action = ACTION_AUTO_UPDATE
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(context, 9001, intent, flags)
                val now = System.currentTimeMillis()
                val nextMinute = (now / 60000L + 1) * 60000L

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            alarmManager.canScheduleExactAlarms()
                        } catch (e: Exception) {
                            true
                        }
                    } else {
                        true
                    }

                    if (canExact) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                nextMinute,
                                pendingIntent
                            )
                            return
                        } catch (se: Exception) {
                            // Fallback to setAndAllowWhileIdle below
                        }
                    }
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMinute,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextMinute,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class Widget2x2Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val (themeId, themeFolder, widgetType) = resolveWidgetBinding(context, widgetId, "clock", "2x2")

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "2x2",
            isFree = true,
            isFavorite = false
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_2x2,
            widgetSize = WidgetSize.SMALL,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        safeUpdateAppWidget(context, appWidgetManager, widgetId, views, bitmap)
        WidgetClockTicker.invalidateWidget(widgetId)
        WidgetClockTicker.start(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetClockTicker.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val c2x2 = ComponentName(context, Widget2x2Provider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(c2x2)
        if (ids == null || ids.isEmpty()) {
            WidgetClockTicker.stop()
            WidgetClockTicker.clearCache()
        }
    }
}

class Widget4x2Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val (themeId, themeFolder, widgetType) = resolveWidgetBinding(context, widgetId, "clock_4x2", "4x2")

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "4x2",
            isFree = true,
            isFavorite = false
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_4x2,
            widgetSize = WidgetSize.MEDIUM,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        safeUpdateAppWidget(context, appWidgetManager, widgetId, views, bitmap)
    }
}

class Widget4x4Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val (themeId, themeFolder, widgetType) = resolveWidgetBinding(context, widgetId, "calendar", "4x4")

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "4x4",
            isFree = true,
            isFavorite = false
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_4x4,
            widgetSize = WidgetSize.LARGE,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        safeUpdateAppWidget(context, appWidgetManager, widgetId, views, bitmap)
    }
}
