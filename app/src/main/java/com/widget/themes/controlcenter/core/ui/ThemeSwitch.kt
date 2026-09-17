package com.widget.themes.controlcenter.core.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import android.widget.Checkable

/**
 * Custom switch component replicating iOS toggle switch style.
 * - Checked track: Pink (#FF5678)
 * - Unchecked track: Smooth light grey (#D8D8DC)
 * - Thumb: Pure white circle with soft drop shadow underneath
 */
class ThemeSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Checkable {

    private var checked = false
    private var progress = 0f // 0f = unchecked (left), 1f = checked (right)
    private var animator: ValueAnimator? = null

    var onCheckedChangeListener: ((ThemeSwitch, Boolean) -> Unit)? = null
    var isUserToggleable: Boolean = false

    var colorChecked: Int = Color.parseColor("#FF5678") // Vibrant Pink
    var colorUnchecked: Int = Color.parseColor("#D8D8DC") // iOS-style light grey
    var colorThumb: Int = Color.WHITE

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val thumbShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        // Soft drop shadow under the thumb
        setShadowLayer(
            5f * resources.displayMetrics.density,
            0f,
            1.8f * resources.displayMetrics.density,
            Color.argb(55, 0, 0, 0)
        )
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val trackRect = RectF()
    private val argbEvaluator = ArgbEvaluator()

    init {
        // Software layer for smooth shadowLayer support
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val desiredWidth = (54 * density).toInt()
        val desiredHeight = (34 * density).toInt()
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        // Leave room for thumb shadow around the track
        val padH = 2f * density
        val padV = 2f * density
        trackRect.set(padH, padV, w.toFloat() - padH, h.toFloat() - padV)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val trackRadius = trackRect.height() / 2f

        // 1. Draw track pill
        val currentTrackColor = argbEvaluator.evaluate(progress, colorUnchecked, colorChecked) as Int
        trackPaint.color = currentTrackColor
        canvas.drawRoundRect(trackRect, trackRadius, trackRadius, trackPaint)

        // 2. Calculate thumb bounds (contained with inset inside track)
        val density = resources.displayMetrics.density
        val inset = 2.5f * density
        val thumbRadius = trackRadius - inset
        val startX = trackRect.left + inset + thumbRadius
        val endX = trackRect.right - inset - thumbRadius
        val thumbCenterX = startX + (endX - startX) * progress
        val thumbCenterY = trackRect.centerY()

        // 3. Draw thumb with shadow
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbShadowPaint)
        thumbPaint.color = colorThumb
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbPaint)
    }

    override fun isChecked(): Boolean = checked

    override fun setChecked(checked: Boolean) {
        setChecked(checked, animate = true)
    }

    fun setChecked(checked: Boolean, animate: Boolean) {
        if (this.checked == checked && progress == (if (checked) 1f else 0f)) return
        this.checked = checked

        animator?.cancel()
        if (animate && isAttachedToWindow) {
            val target = if (checked) 1f else 0f
            animator = ValueAnimator.ofFloat(progress, target).apply {
                duration = 260L
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            progress = if (checked) 1f else 0f
            invalidate()
        }
    }

    override fun toggle() {
        val newState = !checked
        setChecked(newState, animate = true)
        onCheckedChangeListener?.invoke(this, newState)
    }

    override fun performClick(): Boolean {
        if (isUserToggleable) {
            toggle()
        }
        return super.performClick()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return android.widget.Switch::class.java.name
    }

    @Suppress("DEPRECATION")
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.isCheckable = true
        info.isChecked = checked
    }
}
