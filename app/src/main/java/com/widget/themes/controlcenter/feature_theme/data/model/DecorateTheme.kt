package com.widget.themes.controlcenter.feature_theme.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DecorateCategory(
    val category: String,
    val name: String = "",
    val themes: List<DecorateThemeItem> = emptyList()
)

@Serializable
data class DecorateThemeItem(
    val themeName: String,
    val themePath: String,
    val themeType: String = "custom",
    val isNew: Boolean = false,
    val isPremium: Boolean = false,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val order: Int = 0,
    val downloads: Long = 0,
    val previewUrl: String? = null,
    val wallpaperUrl: String? = null,
    val widgets: List<com.widget.themes.controlcenter.core.network.WidgetDto> = emptyList()
)
