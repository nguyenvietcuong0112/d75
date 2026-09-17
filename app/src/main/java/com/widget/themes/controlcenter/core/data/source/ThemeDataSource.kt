package com.widget.themes.controlcenter.core.data.source

import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.widget.themes.controlcenter.core.data.entity.ThemeItem

/**
 * Interface abstracting data loading (Categories, Themes, Decorates).
 * Supports seamless switching between Local JSON Asset and Remote REST API.
 */
interface ThemeDataSource {
    suspend fun getDecorateCategories(): List<DecorateCategory>
    suspend fun getThemesByCategory(categoryId: String): List<ThemeItem>
    suspend fun getAllThemes(): List<ThemeItem>
    suspend fun getThemeDetail(themePath: String): DecorateThemeItem?
}
