package com.widget.themes.controlcenter.core.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable

object IconComposerHelper {

    /**
     * Composites an app's original icon onto the default theme frame plate.
     *
     * @param bgFrame The background frame plate (e.g. bg_icon_default.png)
     * @param appIconDrawable The original icon drawable of the app
     * @param targetSize The dimension of the output square bitmap (e.g. 192x192)
     * @param scaleRatio The scale of the inner icon relative to the frame (e.g. 0.67 = 67%)
     */
    fun composeThemedIcon(
        bgFrame: Bitmap,
        appIconDrawable: Drawable,
        targetSize: Int = 192,
        scaleRatio: Float = 0.67f
    ): Bitmap {
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 1. Draw background frame scaled to targetSize preserving its transparent corners
        val bgRect = Rect(0, 0, targetSize, targetSize)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bgFrame, null, bgRect, paint)

        // 2. Render inner app icon centered with natural proportions to fit the frame plate
        val innerSize = (targetSize * scaleRatio).toInt().coerceAtLeast(1)
        val offset = (targetSize - innerSize) / 2

        val innerBitmap = drawableToRoundedBitmap(appIconDrawable, innerSize, cornerRadiusRatio = 0.22f)
        canvas.drawBitmap(innerBitmap, offset.toFloat(), offset.toFloat(), paint)
        innerBitmap.recycle()

        return result
    }

    /**
     * Prepares a bitmap for adding as a home screen shortcut.
     * If the source icon already has transparent corners (e.g. 3D cutout icon or pre-shaped theme frame plate),
     * it is preserved exactly at 100% full size.
     * If it is a completely opaque square image (e.g. photo or raw image), corners are smoothly rounded.
     */
    fun prepareShortcutBitmap(source: Bitmap, cornerRadiusDp: Float = 16f, density: Float = 1f): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return source

        val cornerInset = (Math.min(width, height) * 0.03f).toInt().coerceIn(0, 8)
        val topLeftAlpha = android.graphics.Color.alpha(source.getPixel(cornerInset, cornerInset))
        val topRightAlpha = android.graphics.Color.alpha(source.getPixel(width - 1 - cornerInset, cornerInset))
        val bottomLeftAlpha = android.graphics.Color.alpha(source.getPixel(cornerInset, height - 1 - cornerInset))
        val bottomRightAlpha = android.graphics.Color.alpha(source.getPixel(width - 1 - cornerInset, height - 1 - cornerInset))

        val hasTransparentCorners = (topLeftAlpha < 100 || topRightAlpha < 100 || bottomLeftAlpha < 100 || bottomRightAlpha < 100)

        return if (hasTransparentCorners) {
            source
        } else {
            val pixels = (cornerRadiusDp * density).toInt().coerceAtLeast(1)
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
            val rect = Rect(0, 0, width, height)
            val rectF = RectF(rect)
            canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(source, rect, rect, paint)
            output
        }
    }

    /**
     * Compatibility helper: preserves full icon fidelity without injecting artificial colored plates.
     */
    fun createAdaptiveIconBitmap(source: Bitmap, targetSize: Int = 192, safeScale: Float = 1.0f): Bitmap {
        return prepareShortcutBitmap(source)
    }

    /**
     * Samples multiple perimeter and inset points to robustly detect the background plate color
     * even if illustrations (bunny ears, camera flash, flowers) touch the top or corners.
     */
    private fun detectDominantBackgroundColor(source: Bitmap): Int? {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return null

        val samplePoints = mutableListOf<Int>()
        val stepX = (width / 10).coerceAtLeast(1)
        val stepY = (height / 10).coerceAtLeast(1)
        val insetX = (width * 0.04f).toInt().coerceIn(1, 16)
        val insetY = (height * 0.04f).toInt().coerceIn(1, 16)

        // Outer perimeter samples
        for (x in insetX until (width - insetX) step stepX) {
            samplePoints.add(source.getPixel(x, insetY))
            samplePoints.add(source.getPixel(x, height - 1 - insetY))
        }
        for (y in insetY until (height - insetY) step stepY) {
            samplePoints.add(source.getPixel(insetX, y))
            samplePoints.add(source.getPixel(width - 1 - insetX, y))
        }

        // Deep insets (15% from borders) to capture background of pre-rounded squircle plates
        val deepInsetX = (width * 0.15f).toInt().coerceIn(1, width - 1)
        val deepInsetY = (height * 0.15f).toInt().coerceIn(1, height - 1)
        for (x in deepInsetX until (width - deepInsetX) step (stepX * 2).coerceAtLeast(1)) {
            samplePoints.add(source.getPixel(x, deepInsetY))
            samplePoints.add(source.getPixel(x, height - 1 - deepInsetY))
        }
        for (y in deepInsetY until (height - deepInsetY) step (stepY * 2).coerceAtLeast(1)) {
            samplePoints.add(source.getPixel(deepInsetX, y))
            samplePoints.add(source.getPixel(width - 1 - deepInsetX, y))
        }

        // Filter out transparent / semi-transparent pixels
        val opaqueSamples = samplePoints.filter { android.graphics.Color.alpha(it) > 180 }
        if (opaqueSamples.size < 8) {
            // Predominantly transparent icon (cutout / glyph)
            return null
        }

        fun colorDist(c1: Int, c2: Int): Int {
            val dr = Math.abs(android.graphics.Color.red(c1) - android.graphics.Color.red(c2))
            val dg = Math.abs(android.graphics.Color.green(c1) - android.graphics.Color.green(c2))
            val db = Math.abs(android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2))
            return dr + dg + db
        }

        // Find the color with the highest density (cluster)
        var bestColor = opaqueSamples[0]
        var maxClusterSize = 0
        val clusterThreshold = 55

        for (candidate in opaqueSamples) {
            var clusterSize = 0
            for (other in opaqueSamples) {
                if (colorDist(candidate, other) <= clusterThreshold) {
                    clusterSize++
                }
            }
            if (clusterSize > maxClusterSize) {
                maxClusterSize = clusterSize
                bestColor = candidate
            }
        }

        // If dominant cluster represents at least 30% of opaque perimeter samples, it's the plate background!
        if (maxClusterSize >= (opaqueSamples.size * 0.30f).toInt()) {
            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            var count = 0
            for (other in opaqueSamples) {
                if (colorDist(bestColor, other) <= clusterThreshold) {
                    sumR += android.graphics.Color.red(other)
                    sumG += android.graphics.Color.green(other)
                    sumB += android.graphics.Color.blue(other)
                    count++
                }
            }
            if (count > 0) {
                return android.graphics.Color.rgb(
                    (sumR / count).toInt(),
                    (sumG / count).toInt(),
                    (sumB / count).toInt()
                )
            }
            return bestColor
        }

        return null
    }

    /**
     * Detects if the source icon has a vertical gradient plate background.
     */
    private fun detectVerticalGradient(source: Bitmap): Pair<Int, Int>? {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return null

        val insetY = (height * 0.08f).toInt().coerceIn(1, 16)
        val deepInsetY = height - 1 - insetY
        val stepX = (width / 6).coerceAtLeast(1)

        val topSamples = mutableListOf<Int>()
        val bottomSamples = mutableListOf<Int>()

        for (x in (width * 0.15f).toInt() until (width * 0.85f).toInt() step stepX) {
            val topP = source.getPixel(x, insetY)
            val botP = source.getPixel(x, deepInsetY)
            if (android.graphics.Color.alpha(topP) > 180) topSamples.add(topP)
            if (android.graphics.Color.alpha(botP) > 180) bottomSamples.add(botP)
        }

        if (topSamples.size < 3 || bottomSamples.size < 3) return null

        fun avgColor(colors: List<Int>): Int {
            val r = colors.map { android.graphics.Color.red(it) }.average().toInt()
            val g = colors.map { android.graphics.Color.green(it) }.average().toInt()
            val b = colors.map { android.graphics.Color.blue(it) }.average().toInt()
            return android.graphics.Color.rgb(r, g, b)
        }

        val topAvg = avgColor(topSamples)
        val bottomAvg = avgColor(bottomSamples)

        val dr = Math.abs(android.graphics.Color.red(topAvg) - android.graphics.Color.red(bottomAvg))
        val dg = Math.abs(android.graphics.Color.green(topAvg) - android.graphics.Color.green(bottomAvg))
        val db = Math.abs(android.graphics.Color.blue(topAvg) - android.graphics.Color.blue(bottomAvg))

        return if (dr + dg + db > 35) {
            Pair(topAvg, bottomAvg)
        } else {
            null
        }
    }

    /**
     * Converts a Drawable into a square Bitmap with smoothly rounded corners.
     */
    fun drawableToRoundedBitmap(drawable: Drawable, size: Int, cornerRadiusRatio: Float = 0.20f): Bitmap {
        val srcBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(srcBitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)

        val cornerRadius = size * cornerRadiusRatio
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val outCanvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())

        outCanvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        outCanvas.drawBitmap(srcBitmap, 0f, 0f, paint)

        srcBitmap.recycle()
        return output
    }
}
