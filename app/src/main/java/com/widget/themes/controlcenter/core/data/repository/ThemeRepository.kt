package com.widget.themes.controlcenter.core.data.repository

import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.widget.themes.controlcenter.core.data.entity.ThemeItem

enum class DataSourceMode {
    LOCAL_ASSET,
    REMOTE_API
}

interface ThemeRepository {
    suspend fun getCategories(): List<DecorateCategory>
    suspend fun getThemes(categoryId: String? = null): List<ThemeItem>
    suspend fun getPresetThemes(categoryId: String? = null): List<ThemeItem>
    suspend fun getCustomThemes(): List<ThemeItem>
    suspend fun getThemeDetail(themePath: String): DecorateThemeItem?
    suspend fun saveCustomTheme(theme: ThemeItem)
    suspend fun deleteCustomTheme(theme: ThemeItem)
    fun setDataSourceMode(mode: DataSourceMode)
    fun getDataSourceMode(): DataSourceMode
}
