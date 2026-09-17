package com.widget.themes.controlcenter.core.data.source

import android.content.Context
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.widget.themes.controlcenter.core.data.FileUtils
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Local DataSource loading data directly from JSON file in assets:
 * assets/themes/json/theme_data_decorate.json
 * Uses in-memory caching for performance optimization.
 */
class AssetThemeDataSource(
    private val context: Context,
    private val jsonFilePath: String = "assets_theme/json/theme_data_decorate.json"
) : ThemeDataSource {

    private var cachedCategories: List<DecorateCategory>? = null
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    @Synchronized
    private fun getOrLoadCategoriesSync(): List<DecorateCategory> {
        cachedCategories?.let { return it }
        return try {
            val jsonStr = FileUtils.loadJsonFromAsset(context, jsonFilePath)
            val parsed = json.decodeFromString<List<DecorateCategory>>(jsonStr)
            cachedCategories = parsed
            parsed
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getDecorateCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        getOrLoadCategoriesSync()
    }

    override suspend fun getThemesByCategory(categoryId: String): List<ThemeItem> = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        val matchedCategory = categories.find { it.category.equals(categoryId, ignoreCase = true) }
            ?: return@withContext emptyList()

        matchedCategory.themes.map { item ->
            ThemeItem(
                id = "default_${item.themePath}",
                categoryId = matchedCategory.category.lowercase(),
                name = item.themeName,
                path = item.themePath,
                rawType = "default",
                previewPath = item.previewUrl,
                thumbPath = item.previewUrl,
                backgroundPath = item.wallpaperUrl ?: "file:///android_asset/assets_theme/${item.themePath}/wallpapers/bg_wallpaper.png",
                isPremium = item.isPremium,
                downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(item.downloads, item.themePath)
            )
        }
    }

    override suspend fun getAllThemes(): List<ThemeItem> = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        val result = mutableListOf<ThemeItem>()
        for (cat in categories) {
            val catId = cat.category.lowercase()
            for (item in cat.themes) {
                result.add(
                    ThemeItem(
                        id = "default_${item.themePath}",
                        categoryId = catId,
                        name = item.themeName,
                        path = item.themePath,
                        rawType = "default",
                        previewPath = item.previewUrl,
                        thumbPath = item.previewUrl,
                        backgroundPath = item.wallpaperUrl ?: "file:///android_asset/assets_theme/${item.themePath}/wallpapers/bg_wallpaper.png",
                        isPremium = item.isPremium,
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(item.downloads, item.themePath)
                    )
                )
            }
        }
        result
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        for (cat in categories) {
            val item = cat.themes.find { it.themePath.equals(themePath, ignoreCase = true) }
            if (item != null) return@withContext item
        }
        null
    }
}
