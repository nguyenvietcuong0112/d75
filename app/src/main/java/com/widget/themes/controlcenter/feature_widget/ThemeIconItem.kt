package com.widget.themes.controlcenter.feature_widget

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class ThemeIconItem(
    val id: String,
    val iconName: String,
    val assetPath: String,
    var targetPackageName: String? = null,
    var targetAppName: String? = null,
    var targetAppIcon: Drawable? = null,
    var isSelected: Boolean = true,
    var isUnlocked: Boolean = false,
    var customBitmap: Bitmap? = null,
    var isGenerated: Boolean = false
)
