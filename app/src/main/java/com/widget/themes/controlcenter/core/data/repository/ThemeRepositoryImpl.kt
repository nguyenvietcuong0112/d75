package com.widget.themes.controlcenter.core.data.repository

import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.widget.themes.controlcenter.core.data.database.ThemeItemDao
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.data.source.ThemeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThemeRepositoryImpl(
    private val assetDataSource: ThemeDataSource,
    private var apiDataSource: ThemeDataSource? = null,
    private val themeDao: ThemeItemDao
) : ThemeRepository {

    private var currentMode: DataSourceMode = DataSourceMode.LOCAL_ASSET

    private val activeDataSource: ThemeDataSource
        get() = if (currentMode == DataSourceMode.REMOTE_API && apiDataSource != null) {
            apiDataSource!!
        } else {
            assetDataSource
        }

    override suspend fun getCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        if (apiDataSource != null) {
            try {
                val remote = apiDataSource!!.getDecorateCategories()
                if (remote.isNotEmpty()) {
                    return@withContext remote
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback to local assets if server fails
        assetDataSource.getDecorateCategories()
    }

    override suspend fun getPresetThemes(categoryId: String?): List<ThemeItem> = withContext(Dispatchers.IO) {
        if (apiDataSource != null) {
            try {
                val remote = if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
                    apiDataSource!!.getAllThemes()
                } else {
                    apiDataSource!!.getThemesByCategory(categoryId)
                }
                if (remote.isNotEmpty()) {
                    return@withContext remote
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback to local assets if server fails
        if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
            assetDataSource.getAllThemes()
        } else {
            assetDataSource.getThemesByCategory(categoryId)
        }
    }

    override suspend fun getThemes(categoryId: String?): List<ThemeItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ThemeItem>()
        // 1. Load preset themes from data source (remote first, local fallback)
        val presets = getPresetThemes(categoryId)
        result.addAll(presets)

        // 2. Load custom themes from local Room database
        try {
            val customList = themeDao.getAllThemes()
            if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
                result.addAll(customList)
            } else {
                result.addAll(customList.filter { it.categoryId.equals(categoryId, ignoreCase = true) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result
    }

    override suspend fun getCustomThemes(): List<ThemeItem> = withContext(Dispatchers.IO) {
        try {
            themeDao.getAllThemes()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        if (apiDataSource != null) {
            try {
                val remote = apiDataSource!!.getThemeDetail(themePath)
                if (remote != null) return@withContext remote
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback to local assets if server fails
        assetDataSource.getThemeDetail(themePath)
    }

    override suspend fun saveCustomTheme(theme: ThemeItem): Unit = withContext(Dispatchers.IO) {
        try {
            themeDao.insertTheme(theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Unit
    }

    override suspend fun deleteCustomTheme(theme: ThemeItem): Unit = withContext(Dispatchers.IO) {
        try {
            themeDao.deleteTheme(theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Unit
    }

    override fun setDataSourceMode(mode: DataSourceMode) {
        currentMode = mode
    }

    override fun getDataSourceMode(): DataSourceMode {
        return currentMode
    }
}
