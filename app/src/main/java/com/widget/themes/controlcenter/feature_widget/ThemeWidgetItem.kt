package com.widget.themes.controlcenter.feature_widget

import android.graphics.Bitmap

data class ThemeWidgetItem(
    val id: String,
    val name: String,
    val size: String,
    val providerClass: Class<*>,
    val previewUrl: String,
    val rawType: String = "",
    var isSelected: Boolean = true,
    var cachedSnapshot: Bitmap? = null
)

