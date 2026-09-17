package com.widget.themes.controlcenter.feature_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.widget.RemoteViews
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import java.util.Calendar

object WidgetClockTicker {

    private var appContext: Context? = null
    private var tickerThread: HandlerThread? = null
    private var tickerHandler: Handler? = null
    @Volatile
    private var isRunning = false
    @Volatile
    private var isScreenOn = true

    // Cached base bitmaps per widgetId: Background + Dial markings
    private val baseBitmapCache = HashMap<Int, Bitmap>()
    private val themeBindingCache = HashMap<Int, String>()

    // Reusable frame bitmap to eliminate allocations on each tick
    private var frameBitmap: Bitmap? = null
    private var frameCanvas: Canvas? = null

    // Cached hand bitmaps
    private var hourHandBmp: Bitmap? = null
    private var minuteHandBmp: Bitmap? = null
    private var secondHandBmp: Bitmap? = null

    private val tickRunnable = object : Runnable {
        override fun run() {
            val ctx = appContext ?: return
            if (!isRunning || !isScreenOn) return

            val pm = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm != null && !pm.isInteractive) {
                isScreenOn = false
                isRunning = false
                return
            }

            try {
                tick(ctx)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (isRunning && isScreenOn) {
                val now = System.currentTimeMillis()
                val delayToNextSecond = (1000L - (now % 1000L)).coerceIn(50L, 1000L)
                tickerHandler?.postDelayed(this, delayToNextSecond)
            }
        }
    }

    @Synchronized
    fun start(context: Context) {
        appContext = context.applicationContext
        isScreenOn = true

        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val c2x2 = ComponentName(context, Widget2x2Provider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(c2x2)
        if (widgetIds == null || widgetIds.isEmpty()) {
            return
        }

        if (tickerThread == null || !tickerThread!!.isAlive) {
            tickerThread = HandlerThread("WidgetClockTickerThread").apply { start() }
            tickerHandler = Handler(tickerThread!!.looper)
        }

        if (!isRunning) {
            isRunning = true
            tickerHandler?.removeCallbacks(tickRunnable)
            tickerHandler?.post(tickRunnable)
        }
    }

    @Synchronized
    fun stop() {
        isRunning = false
        tickerHandler?.removeCallbacks(tickRunnable)
    }

    fun onScreenOff() {
        isScreenOn = false
        stop()
    }

    fun onScreenOn(context: Context) {
        isScreenOn = true
        start(context)
    }

    @Synchronized
    fun invalidateWidget(widgetId: Int) {
        baseBitmapCache.remove(widgetId)?.recycle()
        themeBindingCache.remove(widgetId)
    }

    @Synchronized
    fun clearCache() {
        for ((_, bmp) in baseBitmapCache) {
            if (!bmp.isRecycled) {
                bmp.recycle()
            }
        }
        baseBitmapCache.clear()
        themeBindingCache.clear()
        frameBitmap?.recycle()
        frameBitmap = null
        frameCanvas = null
    }

    private fun loadHands(context: Context) {
        if (hourHandBmp == null) {
            hourHandBmp = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_hours)
        }
        if (minuteHandBmp == null) {
            minuteHandBmp = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_minutes)
        }
        if (secondHandBmp == null) {
            secondHandBmp = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_seconds)
        }
    }

    private fun getOrCreateBaseBitmap(context: Context, widgetId: Int): Bitmap? {
        val (themeId, themeFolder, widgetType) = BaseWidgetProvider.resolveWidgetBinding(context, widgetId, "clock", "2x2")
        val cacheKey = "$themeId|$themeFolder|$widgetType"
        if (themeBindingCache[widgetId] != cacheKey) {
            baseBitmapCache[widgetId]?.recycle()
            baseBitmapCache.remove(widgetId)
            themeBindingCache[widgetId] = cacheKey
        }

        val cached = baseBitmapCache[widgetId]
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        val targetSize = 500
        val baseBmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBmp)

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "2x2",
            isFree = true,
            isFavorite = false
        )

        // 1. Draw Background
        val bgBmp = WidgetRenderHelper.resolveBackgroundBitmap(context, WidgetSize.SMALL, widgetItem, widgetId)
        if (bgBmp != null) {
            val scale = maxOf(targetSize.toFloat() / bgBmp.width, targetSize.toFloat() / bgBmp.height)
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(
                    (targetSize - bgBmp.width * scale) / 2f,
                    (targetSize - bgBmp.height * scale) / 2f
                )
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            canvas.drawBitmap(bgBmp, matrix, paint)
        } else {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_widget_default_gradient)
            if (drawable != null) {
                drawable.setBounds(0, 0, targetSize, targetSize)
                drawable.draw(canvas)
            } else {
                canvas.drawColor(0xFF232433.toInt())
            }
        }

        // 2. Draw Dial (Centered, 0.85f scale, Black tint)
        val dial = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_dial)
        if (dial != null) {
            val dialScale = (targetSize * 0.85f) / dial.width.toFloat()
            val matrix = Matrix().apply {
                postScale(dialScale, dialScale)
                postTranslate(
                    (targetSize - dial.width * dialScale) / 2f,
                    (targetSize - dial.height * dialScale) / 2f
                )
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            }
            canvas.drawBitmap(dial, matrix, paint)
        }

        baseBitmapCache[widgetId] = baseBmp
        return baseBmp
    }

    private fun drawRotatedHand(canvas: Canvas, handBmp: Bitmap?, angle: Float, size: Int, tintColor: Int?) {
        if (handBmp == null) return
        val scale = (size * 0.85f) / handBmp.height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val dx = (size / 2f) - (handBmp.width / 2f * scale)
        val dy = (size / 2f) - (handBmp.height / 2f * scale)
        matrix.postTranslate(dx, dy)
        matrix.postRotate(angle, size / 2f, size / 2f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            if (tintColor != null) {
                colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            }
        }
        canvas.drawBitmap(handBmp, matrix, paint)
    }

    private fun tick(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val c2x2 = ComponentName(context, Widget2x2Provider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(c2x2)
        if (widgetIds == null || widgetIds.isEmpty()) {
            stop()
            return
        }

        loadHands(context)

        val cal = Calendar.getInstance()
        val hours = cal.get(Calendar.HOUR)
        val minutes = cal.get(Calendar.MINUTE)
        val seconds = cal.get(Calendar.SECOND)

        val hourAngle = (hours + minutes / 60f) * 30f
        val minuteAngle = (minutes + seconds / 60f) * 6f
        val secondAngle = seconds * 6f

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

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

        for (widgetId in widgetIds) {
            val (themeId, themeFolder, widgetType) = BaseWidgetProvider.resolveWidgetBinding(context, widgetId, "clock", "2x2")
            val rawType = widgetType.lowercase()
            val isAnalogClock = !rawType.contains("weather") &&
                    !rawType.contains("calendar") &&
                    !rawType.contains("sleep") &&
                    !rawType.contains("note") &&
                    !rawType.contains("quote")

            if (!isAnalogClock) continue

            val baseBmp = getOrCreateBaseBitmap(context, widgetId) ?: continue

            // Ensure reusable frameBitmap exists
            if (frameBitmap == null || frameBitmap?.isRecycled == true) {
                frameBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
                frameCanvas = Canvas(frameBitmap!!)
            }

            val canvas = frameCanvas ?: continue

            // 1. Draw base (background + dial)
            canvas.drawBitmap(baseBmp, 0f, 0f, basePaint)

            // 2. Draw Hour Hand (Black)
            drawRotatedHand(canvas, hourHandBmp, hourAngle, 500, Color.BLACK)

            // 3. Draw Minute Hand (Black)
            drawRotatedHand(canvas, minuteHandBmp, minuteAngle, 500, Color.BLACK)

            // 4. Draw Second Hand (Red - original color)
            drawRotatedHand(canvas, secondHandBmp, secondAngle, 500, null)

            val views = RemoteViews(context.packageName, R.layout.widget_container)
            views.setImageViewBitmap(R.id.ivWidget, frameBitmap)

            val pendingIntent = PendingIntent.getActivity(context, widgetId, intent, flags)
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
            views.setOnClickPendingIntent(R.id.ivWidget, pendingIntent)

            try {
                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}