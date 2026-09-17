package com.widget.themes.controlcenter.feature_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import com.widget.themes.controlcenter.core.di.ServiceLocator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WidgetRenderHelper {

    fun getTargetPixelSize(widgetSize: WidgetSize, widgetType: String = ""): android.util.Size {
        return when (widgetSize) {
            WidgetSize.SMALL -> android.util.Size(500, 500)      // 2x2 Square (500x500 for all 2x2 widgets)
            WidgetSize.MEDIUM -> android.util.Size(1030, 500)    // 4x2 (height = 500, equal height to 2x2)
            WidgetSize.LARGE -> android.util.Size(1000, 1000)   // 4x4
        }
    }

    fun getLayoutForWidget(widgetSize: WidgetSize, widgetType: String): Int {
        val raw = widgetType.lowercase()
        return when (widgetSize) {
            WidgetSize.MEDIUM -> {
                when {
                    raw.contains("checklist") -> R.layout.widget_layout_checklist_4x2
                    raw.contains("countdown") -> R.layout.widget_layout_countdown_4x2
                    else -> R.layout.widget_layout_4x2
                }
            }
            WidgetSize.SMALL -> {
                when {
                    raw.contains("weather") -> R.layout.widget_layout_weather_2x2
                    raw.contains("mini_calendar") -> R.layout.widget_layout_mini_calendar_2x2
                    raw.contains("calendar") -> R.layout.widget_layout_calendar_2x2
                    raw.contains("sleep") -> R.layout.widget_layout_sleep_2x2
                    raw.contains("note") || raw.contains("quote") -> R.layout.widget_layout_note_2x2
                    else -> R.layout.widget_layout_2x2
                }
            }
            WidgetSize.LARGE -> R.layout.widget_layout_4x4
        }
    }

    fun getSnapshotImage(
        context: Context,
        layoutId: Int,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        preloadedBackground: Bitmap? = null
    ): Bitmap? {
        val actualLayoutId = if (layoutId == R.layout.widget_layout_2x2 || layoutId == R.layout.widget_layout_4x2) {
            getLayoutForWidget(widgetSize, widgetItem.widgetType)
        } else {
            layoutId
        }
        val view = LayoutInflater.from(context).inflate(actualLayoutId, null, false) ?: return null
        val targetSize = getTargetPixelSize(widgetSize, widgetItem.widgetType)

        bindDataToView(context, view, widgetSize, widgetItem, widgetId, preloadedBackground)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(targetSize.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(targetSize.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, targetSize.width, targetSize.height)

        val bitmap = Bitmap.createBitmap(targetSize.width, targetSize.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun resolveBackgroundBitmap(
        context: Context,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        preloadedBackground: Bitmap? = null
    ): Bitmap? {
        var bgBmp: Bitmap? = preloadedBackground
        val config = if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)
        } else {
            null
        }

        if (bgBmp == null) {
            if (config != null && config.bgType == "IMAGE" && config.imageUri != null) {
                try {
                    val file = File(Uri.parse(config.imageUri).path ?: "")
                    if (file.exists()) {
                        bgBmp = BitmapFactory.decodeFile(file.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (bgBmp == null) {
            try {
                val sizeStr = when (widgetSize) {
                    WidgetSize.SMALL -> "2x2"
                    WidgetSize.MEDIUM -> "4x2"
                    WidgetSize.LARGE -> "4x4"
                }
                val altSizeStr = when (widgetSize) {
                    WidgetSize.SMALL -> "small"
                    WidgetSize.MEDIUM -> "medium"
                    WidgetSize.LARGE -> "large"
                }
                val cleanId = widgetItem.id.replace('/', '_').replace('\\', '_')
                val cleanFolder = widgetItem.themeFolder.replace('/', '_').replace('\\', '_')
                val fileNames = mutableListOf<String>()
                for (s in listOf(sizeStr, altSizeStr)) {
                    fileNames.add("widget_bg_${cleanId}_${widgetItem.widgetType}_$s.png")
                    fileNames.add("widget_bg_${cleanId.removePrefix("widget_")}_${widgetItem.widgetType}_$s.png")
                    fileNames.add("widget_bg_${cleanFolder}_${widgetItem.widgetType}_$s.png")
                    fileNames.add("widget_bg_${cleanFolder.removePrefix("widget_")}_${widgetItem.widgetType}_$s.png")
                }
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    fileNames.add("widget_bg_${widgetId}.png")
                }
                for (fn in fileNames) {
                    val file = context.getFileStreamPath(fn)
                    if (file.exists()) {
                        bgBmp = BitmapFactory.decodeFile(file.absolutePath)
                        if (bgBmp != null) break
                    }
                }

                // Fallback Tier 1: Search for any file belonging to this theme (cleanId or cleanFolder)
                if (bgBmp == null) {
                    val allFiles = context.fileList() ?: emptyArray()
                    val themeMatch = allFiles.firstOrNull { fn ->
                        fn.startsWith("widget_bg_") && 
                        (fn.contains(cleanId) || fn.contains(cleanFolder) || 
                         fn.contains(cleanId.removePrefix("widget_")) || fn.contains(cleanFolder.removePrefix("widget_")))
                    }
                    if (themeMatch != null) {
                        val f = context.getFileStreamPath(themeMatch)
                        if (f != null && f.exists()) {
                            bgBmp = BitmapFactory.decodeFile(f.absolutePath)
                        }
                    }
                }

                // Fallback Tier 2: Search for the most recently saved widget_bg file on device
                if (bgBmp == null) {
                    val allFiles = context.fileList() ?: emptyArray()
                    val anyWidgetBg = allFiles.filter { it.startsWith("widget_bg_") }
                        .sortedByDescending { context.getFileStreamPath(it)?.lastModified() ?: 0L }
                        .firstOrNull()
                    if (anyWidgetBg != null) {
                        val f = context.getFileStreamPath(anyWidgetBg)
                        if (f != null && f.exists()) {
                            bgBmp = BitmapFactory.decodeFile(f.absolutePath)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (bgBmp == null) {
            try {
                val folder = widgetItem.themeFolder
                    .removePrefix("file:///android_asset/")
                    .removePrefix("file://android_asset/")
                    .removePrefix("android_asset/")
                    .removePrefix("/")
                val directName = when {
                    widgetItem.widgetType.lowercase().contains("checklist") -> "checklist_4x2"
                    widgetItem.widgetType.lowercase().contains("countdown") -> "countdown_4x2"
                    widgetItem.widgetType.lowercase().contains("mini_calendar") -> "mini_calendar_2x2"
                    widgetItem.widgetType.lowercase().contains("calendar") -> "calendar_2x2"
                    widgetItem.widgetType.lowercase().contains("sleep") -> "sleep_2x2"
                    widgetItem.widgetType.lowercase().contains("note") -> "note_2x2"
                    widgetItem.widgetType.lowercase().contains("weather") -> if (widgetSize == WidgetSize.MEDIUM) "weather_4x2" else "weather_2x2"
                    widgetItem.widgetType.lowercase().contains("clock") -> if (widgetSize == WidgetSize.MEDIUM) "clock_4x2" else "clock_2x2"
                    else -> if (widgetSize == WidgetSize.MEDIUM) "clock_4x2" else "clock_2x2"
                }

                val isMedium = widgetSize == WidgetSize.MEDIUM
                val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"

                val candidatePaths = listOf(
                    // 1. Clean background files
                    "$folder/widgets/$bgFileName",
                    "$folder/$bgFileName",
                    "assets_collection/$folder/widgets/$bgFileName",
                    "assets_collection/theme/$folder/widgets/$bgFileName",
                    "assets_collection/widget/$folder/$bgFileName",
                    "assets_theme/$folder/widgets/$bgFileName",
                    "assets_theme/category/$folder/widgets/$bgFileName",
                    "assets_theme/category/Animal/$folder/widgets/$bgFileName",
                    "assets_theme/category/Kawaii/$folder/widgets/$bgFileName",
                    "assets_theme/category/Trending/$folder/widgets/$bgFileName",
                    // 2. Direct name fallbacks
                    "$folder/widgets/$directName.png",
                    "assets_collection/$folder/widgets/$directName.png",
                    "assets_theme/$folder/widgets/$directName.png",
                    "assets_theme/category/$folder/widgets/$directName.png",
                    "assets_theme/category/Animal/$folder/widgets/$directName.png",
                    // 3. Fallback defaults
                    "assets_collection/theme/theme_1/widgets/$bgFileName",
                    "assets_collection/widget/widget_1/$bgFileName",
                    "assets_theme/category/Animal/theme_1/widgets/$bgFileName"
                )
                for (cand in candidatePaths) {
                    try {
                        context.assets.open(cand).use { stream ->
                            bgBmp = BitmapFactory.decodeStream(stream)
                        }
                        if (bgBmp != null) break
                    } catch (e: Exception) {
                        // Try next
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return bgBmp
    }

    fun getBackgroundOnlyBitmap(
        context: Context,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        preloadedBackground: Bitmap? = null
    ): Bitmap? {
        val targetSize = getTargetPixelSize(widgetSize, widgetItem.widgetType)
        val rawBg = resolveBackgroundBitmap(context, widgetSize, widgetItem, widgetId, preloadedBackground)

        val bitmap = Bitmap.createBitmap(targetSize.width, targetSize.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (rawBg != null) {
            val scale = maxOf(
                targetSize.width.toFloat() / rawBg.width,
                targetSize.height.toFloat() / rawBg.height
            )
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(
                    (targetSize.width - rawBg.width * scale) / 2f,
                    (targetSize.height - rawBg.height * scale) / 2f
                )
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            canvas.drawBitmap(rawBg, matrix, paint)
        } else {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_widget_default_gradient)
            if (drawable != null) {
                drawable.setBounds(0, 0, targetSize.width, targetSize.height)
                drawable.draw(canvas)
            } else {
                canvas.drawColor(0xFF232433.toInt())
            }
        }

        // Return full-bleed bitmap without artificial corner clipping.
        // The Android Launcher automatically applies the system widget corner radius to the host container.
        return bitmap
    }

    private fun bindDataToView(
        context: Context,
        view: View,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int,
        preloadedBackground: Bitmap? = null
    ) {
        val config = if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)
        } else {
            null
        }

        // 1. Populate Background
        val ivBackground = view.findViewById<ImageView>(R.id.ivBackground)
        if (ivBackground != null) {
            val bgBmp = resolveBackgroundBitmap(context, widgetSize, widgetItem, widgetId, preloadedBackground)
            if (bgBmp != null) {
                ivBackground.setImageBitmap(bgBmp)
            } else {
                try {
                    ivBackground.setImageResource(R.drawable.bg_widget_default_gradient)
                } catch (e: Exception) {
                    ivBackground.setBackgroundColor(0xFF232433.toInt())
                }
            }
        }

        // 2. Populate components based on size & widgetType
        val now = Date()
        val cal = Calendar.getInstance()
        val rawType = widgetItem.widgetType.lowercase()

        when (widgetSize) {
            WidgetSize.SMALL -> {
                when {
                    rawType.contains("weather") -> {
                        val weather = WeatherHelper.getWeatherData(context)
                        view.findViewById<TextView>(R.id.tvWeatherTemp)?.text = weather.temp
                        view.findViewById<TextView>(R.id.tvWeatherCondition)?.text = weather.condition
                        view.findViewById<TextView>(R.id.tvWeatherCity)?.text = weather.city
                        view.findViewById<TextView>(R.id.tvWeatherRange)?.text = weather.range
                        view.findViewById<ImageView>(R.id.ivWeatherIcon)?.setImageResource(R.drawable.ic_style_weather)
                    }
                    rawType.contains("mini_calendar") -> {
                        view.findViewById<TextView>(R.id.tvMiniDayOfWeek)?.text = SimpleDateFormat("EEE", Locale.ENGLISH).format(now)
                        view.findViewById<TextView>(R.id.tvMiniDayOfMonth)?.text = SimpleDateFormat("d", Locale.ENGLISH).format(now)
                        view.findViewById<TextView>(R.id.tvMiniMonth)?.text = SimpleDateFormat("MMM", Locale.ENGLISH).format(now)
                    }
                    rawType.contains("calendar") -> {
                        val ivCalendarContent = view.findViewById<ImageView>(R.id.ivCalendarContent)
                        if (ivCalendarContent != null) {
                            val targetSize = getTargetPixelSize(widgetSize, widgetItem.widgetType)
                            val calBmp = renderCalendarBitmap(context, targetSize.width, targetSize.height)
                            ivCalendarContent.setImageBitmap(calBmp)
                        }
                    }
                    rawType.contains("sleep") -> {
                        view.findViewById<TextView>(R.id.tvSleepTitle)?.text = "Good night"
                        view.findViewById<TextView>(R.id.tvSleepSub)?.text = "Sleep well 💖"
                    }
                    rawType.contains("note") || rawType.contains("quote") -> {
                        val note = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getNoteData(context, widgetId)
                        view.findViewById<TextView>(R.id.tvNoteText)?.text = note
                    }
                    else -> {
                        // Analog Clock 2x2
                        val ivClock = view.findViewById<ImageView>(R.id.ivClock)
                        if (ivClock != null) {
                            try {
                                val targetSize = getTargetPixelSize(widgetSize)
                                val clockBmp = Bitmap.createBitmap(targetSize.width, targetSize.height, Bitmap.Config.ARGB_8888)
                                val clockCanvas = Canvas(clockBmp)

                                // 1. Draw Dial (Centered) with Black Color
                                val dial = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_dial)
                                if (dial != null) {
                                    val dialScale = (targetSize.width * 0.85f) / dial.width.toFloat()
                                    val matrix = Matrix().apply {
                                        postScale(dialScale, dialScale)
                                        postTranslate(
                                            (targetSize.width - dial.width * dialScale) / 2f,
                                            (targetSize.height - dial.height * dialScale) / 2f
                                        )
                                    }
                                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        isFilterBitmap = true
                                        colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                                    }
                                    clockCanvas.drawBitmap(dial, matrix, paint)
                                }

                                // 2. Calculate Angles
                                val hours = cal.get(Calendar.HOUR)
                                val minutes = cal.get(Calendar.MINUTE)
                                val seconds = cal.get(Calendar.SECOND)

                                val hourAngle = (hours + minutes / 60f) * 30f
                                val minuteAngle = (minutes + seconds / 60f) * 6f
                                val secondAngle = seconds * 6f

                                // 3. Draw Hour Hand (Black)
                                val hourHand = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_hours)
                                drawRotatedHand(clockCanvas, hourHand, hourAngle, targetSize.width, Color.BLACK)

                                // 4. Draw Minute Hand (Black)
                                val minuteHand = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_minutes)
                                drawRotatedHand(clockCanvas, minuteHand, minuteAngle, targetSize.width, Color.BLACK)

                                // 5. Draw Second Hand (Red - original asset color)
                                val secondHand = BitmapFactory.decodeResource(context.resources, R.drawable.widget_clock_1_seconds)
                                drawRotatedHand(clockCanvas, secondHand, secondAngle, targetSize.width, null)

                                ivClock.visibility = View.VISIBLE
                                ivClock.setImageBitmap(clockBmp)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            WidgetSize.MEDIUM -> {
                when {
                    rawType.contains("checklist") -> {
                        val tasks = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getChecklistData(context, widgetId)
                        val textViews = listOf(
                            view.findViewById<TextView>(R.id.tvCheck1),
                            view.findViewById<TextView>(R.id.tvCheck2),
                            view.findViewById<TextView>(R.id.tvCheck3),
                            view.findViewById<TextView>(R.id.tvCheck4)
                        )
                        val checkIcons = listOf(
                            view.findViewById<ImageView>(R.id.ivCheck1),
                            view.findViewById<ImageView>(R.id.ivCheck2),
                            view.findViewById<ImageView>(R.id.ivCheck3),
                            view.findViewById<ImageView>(R.id.ivCheck4)
                        )
                        for (i in 0 until 4) {
                            val task = tasks.getOrNull(i)
                            if (task != null) {
                                textViews.getOrNull(i)?.text = task.first
                                checkIcons.getOrNull(i)?.setImageResource(
                                    if (task.second) R.drawable.ic_widget_check_square else R.drawable.ic_widget_uncheck_square
                                )
                            }
                        }
                    }
                    rawType.contains("countdown") -> {
                        val (title, targetDateStr) = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getCountdownData(context, widgetId)
                        view.findViewById<TextView>(R.id.tvCountdownTitle)?.text = title

                        try {
                            val targetDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(targetDateStr)
                            val diffMs = (targetDate?.time ?: now.time) - now.time
                            val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            view.findViewById<TextView>(R.id.tvCountdownDays)?.text = days.toString()
                            if (targetDate != null) {
                                view.findViewById<TextView>(R.id.tvCountdownTargetDate)?.text = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(targetDate)
                            }
                        } catch (e: Exception) {
                            view.findViewById<TextView>(R.id.tvCountdownDays)?.text = "39"
                            view.findViewById<TextView>(R.id.tvCountdownTargetDate)?.text = "Jun 4, 2027"
                        }
                    }
                    else -> {
                        // Digital Clock 4x2
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.ENGLISH)
                        view.findViewById<TextView>(R.id.tvTime)?.text = timeFormat.format(now)
                        view.findViewById<TextView>(R.id.tvDate)?.text = dateFormat.format(now)
                        view.findViewById<View>(R.id.layoutWeather)?.visibility = View.GONE
                    }
                }
            }
            WidgetSize.LARGE -> {
                val ivCalendar = view.findViewById<ImageView>(R.id.ivCalendar)
                ivCalendar?.visibility = View.GONE
            }
        }
    }

    fun renderCalendarBitmap(context: Context, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_MONTH)
        val todayMonth = now.get(Calendar.MONTH)
        val todayYear = now.get(Calendar.YEAR)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val monthTitle = monthFormat.format(now.time).uppercase()

        // 1. Month Title Paint (Moved up for better spacing)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFF3366.toInt()
            textSize = width * 0.052f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val titleY = height * 0.11f
        canvas.drawText(monthTitle, width / 2f, titleY, titlePaint)

        // 2. Day of Week Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF888888.toInt()
            textSize = width * 0.042f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val daysHeader = listOf("M", "T", "W", "T", "F", "S", "S")
        val marginX = width * 0.105f
        val gridWidth = width - 2 * marginX
        val colWidth = gridWidth / 7f
        val headerY = height * 0.225f

        for (c in 0 until 7) {
            val cx = marginX + c * colWidth + colWidth / 2f
            canvas.drawText(daysHeader[c], cx, headerY, headerPaint)
        }

        // 3. Calendar Grid calculation
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, todayYear)
            set(Calendar.MONTH, todayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = (firstDayOfWeek + 5) % 7
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val rowStartY = height * 0.325f
        val rowHeight = height * 0.106f

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF222222.toInt()
            textSize = width * 0.046f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val todayBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFF3366.toInt()
            style = Paint.Style.FILL
        }

        val todayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = width * 0.046f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val circleRadius = colWidth * 0.38f

        var currentDay = 1
        var dayIndex = offset

        while (currentDay <= maxDays) {
            val col = dayIndex % 7
            val row = dayIndex / 7
            val cx = marginX + col * colWidth + colWidth / 2f
            val cy = rowStartY + row * rowHeight

            if (currentDay == today) {
                val baseline = cy - (todayTextPaint.descent() + todayTextPaint.ascent()) / 2f
                canvas.drawCircle(cx, cy, circleRadius, todayBgPaint)
                canvas.drawText(currentDay.toString(), cx, baseline, todayTextPaint)
            } else {
                val baseline = cy - (datePaint.descent() + datePaint.ascent()) / 2f
                canvas.drawText(currentDay.toString(), cx, baseline, datePaint)
            }

            currentDay++
            dayIndex++
        }

        return bitmap
    }

    private fun drawRotatedHand(canvas: Canvas, handBmp: Bitmap?, angle: Float, size: Int, tintColor: Int? = Color.BLACK) {
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

    private fun roundBitmapCorners(src: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
            shader = android.graphics.BitmapShader(
                src, 
                android.graphics.Shader.TileMode.CLAMP, 
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        
        // Use a tiny 0.5px inset to make sure anti-aliasing edges are extremely smooth
        val rectF = RectF(0.5f, 0.5f, src.width.toFloat() - 0.5f, src.height.toFloat() - 0.5f)
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return output
    }
}
