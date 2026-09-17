package com.widget.themes.controlcenter.core.data.source

import android.util.Log
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.PagedData
import com.widget.themes.controlcenter.core.network.ThemeDetailDto
import com.widget.themes.controlcenter.core.network.ThemeItemDto
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateThemeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Remote DataSource connecting to REST API Server (https://api.1teps.com/themes).
 */
class ApiThemeDataSource(
    private val baseUrl: String = ""
) : ThemeDataSource {

    private val TAG = "ApiThemeDataSource"
    private var cachedCategories: List<DecorateCategory>? = null

    override suspend fun getDecorateCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val response = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "theme")
            )
            val list = response?.data
            if (!list.isNullOrEmpty()) {
                val mapped = list.map { cat ->
                    DecorateCategory(
                        category = cat.id,
                        name = cat.name.ifEmpty { cat.id }
                    )
                }
                Log.i(TAG, "Successfully loaded ${mapped.size} theme categories from CMS: ${mapped.map { it.name }}")
                cachedCategories = mapped
                return@withContext mapped
            }

            // Fallback: extract unique categories from remote themes
            val remoteThemes = getAllThemes()
            if (remoteThemes.isNotEmpty()) {
                val catNames = remoteThemes.map { it.categoryId }.distinct().filter { it.isNotEmpty() }
                val dynamicCategories = mutableListOf<DecorateCategory>()
                dynamicCategories.add(DecorateCategory(category = "trending", name = "Trending"))
                for (cat in catNames) {
                    if (!cat.equals("trending", ignoreCase = true)) {
                        dynamicCategories.add(
                            DecorateCategory(
                                category = cat,
                                name = cat.replaceFirstChar { it.uppercase() }
                            )
                        )
                    }
                }
                Log.i(TAG, "Extracted ${dynamicCategories.size} categories from themes: ${dynamicCategories.map { it.name }}")
                cachedCategories = dynamicCategories
                return@withContext dynamicCategories
            }
        } catch (e: Exception) {
            Log.e(TAG, "getDecorateCategories error: ${e.message}")
        }
        emptyList()
    }

    override suspend fun getThemesByCategory(categoryId: String): List<ThemeItem> = withContext(Dispatchers.IO) {
        try {
            val params = mapOf(
                "category_id" to categoryId,
                "page" to "1",
                "page_size" to "100"
            )
            val response = ApiClient.get<ApiResponse<PagedData<ThemeItemDto>>>("api/v1/themes", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                val list = items.map { it.toThemeItem() }
                Log.i(TAG, "Loaded ${list.size} themes for category '$categoryId'")
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getThemesByCategory error: ${e.message}")
        }
        emptyList()
    }

    override suspend fun getAllThemes(): List<ThemeItem> = withContext(Dispatchers.IO) {
        try {
            val params = mapOf(
                "page" to "1",
                "page_size" to "100"
            )
            val response = ApiClient.get<ApiResponse<PagedData<ThemeItemDto>>>("api/v1/themes", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                val list = items.map { it.toThemeItem() }
                Log.i(TAG, "Loaded ${list.size} total themes from CMS")
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllThemes error: ${e.message}")
        }
        emptyList()
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        try {
            val cleanId = themePath.substringAfterLast("/").trim()
            val response = ApiClient.get<ApiResponse<ThemeDetailDto>>("api/v1/themes/$cleanId")
            val detail = response?.data
            if (detail != null) {
                Log.i(TAG, "Loaded theme detail for '$cleanId' (Name: ${detail.displayName})")
                return@withContext DecorateThemeItem(
                    themeName = detail.displayName,
                    themePath = if (detail.id.isNotEmpty()) detail.id else themePath,
                    themeType = "custom",
                    isNew = detail.isNew,
                    isPremium = !detail.isFree,
                    isFree = detail.isFree,
                    isHot = detail.isHot,
                    order = 0,
                    downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(detail.downloads, detail.id.ifEmpty { detail.themePath }),
                    previewUrl = detail.displayPreview,
                    wallpaperUrl = detail.wallpaper?.wallpaperUrl ?: detail.wallpaper?.thumbUrl,
                    widgets = detail.widgets
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getThemeDetail error: ${e.message}")
        }
        null
    }

    private fun ThemeItemDto.toThemeItem(): ThemeItem {
        val catId = if (categories.isNotEmpty()) {
            categories.joinToString(",") { it.lowercase().trim() }
        } else {
            category.lowercase().trim()
        }
        val resolvedWallpaper = wallpaper?.wallpaperUrl?.takeIf { it.isNotBlank() }
            ?: wallpaper?.thumbUrl?.takeIf { it.isNotBlank() }
        return ThemeItem(
            id = id.ifEmpty { "theme_$themePath" },
            categoryId = catId,
            name = displayName,
            path = themePath.ifEmpty { id },
            rawType = "default",
            previewPath = displayPreview,
            thumbPath = thumbUrl ?: thumbPath ?: previewUrl ?: previewPath,
            backgroundPath = resolvedWallpaper,
            isPremium = !isFree,
            isFree = isFree,
            isHot = isHot,
            isNew = isNew,
            downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(downloads, id.ifEmpty { themePath }),
            totalIcons = totalIcons,
            totalWidgets = totalWidgets
        )
    }
}
