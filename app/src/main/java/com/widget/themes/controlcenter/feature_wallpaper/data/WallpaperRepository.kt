package com.widget.themes.controlcenter.feature_wallpaper.data

import android.content.Context
import android.util.Log
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.PagedData
import com.widget.themes.controlcenter.core.network.WallpaperDto
import com.widget.themes.controlcenter.feature_wallpaper.WallpaperCategory
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class WallpaperRepository(private val context: Context) {

    private val TAG = "WallpaperRepository"
    private val themeRepository = ServiceLocator.getThemeRepository(context)
    private var cachedCategories: List<WallpaperCategory>? = null

    suspend fun getCategories(): List<WallpaperCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val response = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "wallpaper")
            )
            val list = response?.data
            if (!list.isNullOrEmpty()) {
                val cats = list.map { cat ->
                    val displayName = cat.name.ifEmpty { cat.id }.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                    WallpaperCategory(cat.id.lowercase(), displayName)
                }
                val finalCats = listOf(WallpaperCategory("all", "All", true)) + cats
                Log.i(TAG, "Loaded ${finalCats.size} wallpaper categories from CMS: ${finalCats.map { it.name }}")
                cachedCategories = finalCats
                return@withContext finalCats
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories wallpaper error: ${e.message}")
        }

        // Try getting categories from general categories endpoint
        try {
            val allResponse = ApiClient.get<ApiResponse<List<CategoryDto>>>("api/v1/categories")
            val list = allResponse?.data?.filter { it.type == "wallpaper" || it.type == "theme" }
            if (!list.isNullOrEmpty()) {
                val cats = list.map { cat ->
                    val displayName = cat.name.ifEmpty { cat.id }.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                    WallpaperCategory(cat.id.lowercase(), displayName)
                }.distinctBy { it.id }
                val finalCats = listOf(WallpaperCategory("all", "All", true)) + cats
                Log.i(TAG, "Loaded ${finalCats.size} fallback categories for wallpapers: ${finalCats.map { it.name }}")
                cachedCategories = finalCats
                return@withContext finalCats
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories all error: ${e.message}")
        }

        // Fallback: extract from themes
        try {
            val decorateCategories = themeRepository.getCategories()
            val cats = decorateCategories.map { it.category }.distinct().filter { it.isNotEmpty() }.map { catName ->
                val displayName = catName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                WallpaperCategory(catName.lowercase(), displayName)
            }
            val finalCats = listOf(WallpaperCategory("all", "All", true)) + cats
            Log.i(TAG, "Extracted ${finalCats.size} categories from local themes for wallpapers: ${finalCats.map { it.name }}")
            cachedCategories = finalCats
            finalCats
        } catch (e: Exception) {
            listOf(WallpaperCategory("all", "All", true))
        }
    }

    suspend fun getWallpapers(
        categoryId: String? = null,
        page: Int = 1,
        pageSize: Int = 30,
        sortBy: String = "trending"
    ): List<WidgetThemeWallpaper> = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf(
                "page" to page.toString(),
                "page_size" to pageSize.toString(),
                "sort_by" to sortBy
            )
            if (!categoryId.isNullOrEmpty() && !categoryId.equals("all", ignoreCase = true)) {
                params["category_id"] = categoryId.lowercase()
            }

            val response = ApiClient.get<ApiResponse<PagedData<WallpaperDto>>>("api/v1/wallpapers", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                val list = items.map { dto ->
                    val finalUrl = dto.wallpaperUrl ?: dto.previewUrl ?: dto.thumbUrl ?: ""
                    val cat = if (dto.categories.isNotEmpty()) {
                        dto.categories.joinToString(",") { it.lowercase().trim() }
                    } else {
                        dto.category.lowercase().trim()
                    }
                    WidgetThemeWallpaper(
                        id = dto.id.ifEmpty { "wp_${System.currentTimeMillis()}" },
                        themeId = dto.themePath.ifEmpty { dto.id },
                        name = dto.name,
                        order = 0,
                        folder = finalUrl,
                        imageBg = "wallpaper",
                        category = cat,
                        isFree = dto.isFree,
                        isNew = dto.isNew
                    )
                }
                Log.i(TAG, "Loaded ${list.size} wallpapers for category '${categoryId ?: "all"}' from CMS")
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWallpapers error: ${e.message}")
        }

        // Fallback: extract from themes
        try {
            val decorateCategories = themeRepository.getCategories()
            val list = decorateCategories.flatMap { cat ->
                cat.themes.map { t ->
                    WidgetThemeWallpaper(
                        id = "wp_${t.themePath.replace("/", "_")}",
                        themeId = t.themePath,
                        name = t.themeName,
                        order = t.order,
                        folder = t.wallpaperUrl ?: t.themePath,
                        imageBg = "bg_wallpaper",
                        category = cat.category.lowercase(),
                        isFree = t.isFree,
                        isNew = t.isNew
                    )
                }
            }
            if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
                list
            } else {
                list.filter { it.category.contains(categoryId.lowercase()) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
