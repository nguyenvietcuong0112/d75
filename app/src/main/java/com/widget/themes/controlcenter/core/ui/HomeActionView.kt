package com.widget.themes.controlcenter.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.widget.themes.controlcenter.R

class HomeActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class HomeActionType {
        WALLPAPER, THEME, WIDGET
    }

    interface OnHomeActionViewListener {
        fun onSelect(action: HomeActionType)
    }

    private var listener: OnHomeActionViewListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_home_action_layout, this, true)
        
        findViewById<View>(R.id.llWallpaper)?.setOnClickListener {
            listener?.onSelect(HomeActionType.WALLPAPER)
        }
        findViewById<View>(R.id.llTheme)?.setOnClickListener {
            listener?.onSelect(HomeActionType.THEME)
        }
        findViewById<View>(R.id.llWidget)?.setOnClickListener {
            listener?.onSelect(HomeActionType.WIDGET)
        }
    }

    fun setListener(listener: OnHomeActionViewListener) {
        this.listener = listener
    }
}
