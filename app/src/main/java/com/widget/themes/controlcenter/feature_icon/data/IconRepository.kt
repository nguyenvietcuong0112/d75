package com.widget.themes.controlcenter.feature_icon.data

import android.content.Context
import android.util.Log
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.IconDetailDto
import com.widget.themes.controlcenter.core.network.IconItemDto
import com.widget.themes.controlcenter.core.network.IconPackDto
import com.widget.themes.controlcenter.core.network.PagedData
import com.widget.themes.controlcenter.feature_theme.CategoryTag
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconRepository(private val context: Context) {

    private val TAG = "IconRepository"
    private val themeRepository = ServiceLocator.getThemeRepository(context)
    private var cachedCategories: List<CategoryTag>? = null

    suspend fun getCategories(): List<CategoryTag> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val response = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "icon")
            )
            val list = response?.data
            if (!list.isNullOrEmpty()) {
                val cats = list.map { cat ->
                    CategoryTag(
                        id = cat.id,
                        name = cat.name.ifEmpty { cat.id }
                    )
                }
                val finalCats = listOf(CategoryTag("all", "All", true)) + cats
                Log.i(TAG, "Loaded ${finalCats.size} icon categories from CMS: ${finalCats.map { it.name }}")
                cachedCategories = finalCats
                return@withContext finalCats
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories icon error: ${e.message}")
        }

        // Try general categories
        try {
            val allResponse = ApiClient.get<ApiResponse<List<CategoryDto>>>("api/v1/categories")
            val list = allResponse?.data?.filter { it.type == "icon" || it.type == "theme" }
            if (!list.isNullOrEmpty()) {
                val cats = list.map { cat ->
                    CategoryTag(
                        id = cat.id.lowercase(),
                        name = cat.name.ifEmpty { cat.id }
                    )
                }.distinctBy { it.id }
                val finalCats = listOf(CategoryTag("all", "All", true)) + cats
                Log.i(TAG, "Loaded ${finalCats.size} fallback categories for icons: ${finalCats.map { it.name }}")
                cachedCategories = finalCats
                return@withContext finalCats
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories all error: ${e.message}")
        }

        // Fallback to theme categories
        try {
            val themeCats = themeRepository.getCategories()
            val filtered = themeCats.filter { !it.category.equals("Aesthetic", ignoreCase = true) }
            val cats = filtered.map { cat ->
                CategoryTag(
                    id = cat.category.lowercase(),
                    name = cat.name.ifEmpty { cat.category }
                )
            }.sortedByDescending { it.id == "trending" }
            val finalCats = listOf(CategoryTag("all", "All", true)) + cats
            Log.i(TAG, "Extracted ${finalCats.size} categories from local themes for icons: ${finalCats.map { it.name }}")
            cachedCategories = finalCats
            finalCats
        } catch (e: Exception) {
            listOf(CategoryTag("all", "All", true))
        }
    }

    suspend fun getIconPacks(
        page: Int = 1,
        pageSize: Int = 30
    ): List<WidgetThemeIcon> = withContext(Dispatchers.IO) {
        try {
            val params = mapOf(
                "page" to page.toString(),
                "page_size" to pageSize.toString()
            )
            val response = ApiClient.get<ApiResponse<PagedData<IconPackDto>>>("api/v1/icons", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                val list = items.map { dto ->
                    val preview = dto.backgroundIconUrl ?: dto.previewUrl ?: dto.thumbUrl ?: dto.themePath.ifEmpty { dto.id }
                    WidgetThemeIcon(
                        id = dto.id.ifEmpty { "icon_${System.currentTimeMillis()}" },
                        name = dto.displayName,
                        folder = preview,
                        category = if (dto.categories.isNotEmpty()) dto.categories.first().lowercase() else dto.category.ifEmpty { "all" },
                        isFree = dto.isFree
                    )
                }
                Log.i(TAG, "Loaded ${list.size} icon packs from CMS")
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getIconPacks error: ${e.message}")
        }

        // Fallback: extract from themes
        try {
            val decorateCategories = themeRepository.getCategories()
            val filtered = decorateCategories.filter { !it.category.equals("Aesthetic", ignoreCase = true) }
            val list = filtered.flatMap { cat ->
                cat.themes.map { t ->
                    WidgetThemeIcon(
                        id = "icon_${t.themePath.replace("/", "_")}",
                        name = t.themeName,
                        folder = t.themePath,
                        category = cat.category.lowercase(),
                        isFree = !t.isPremium
                    )
                }
            }
            Log.i(TAG, "Loaded ${list.size} fallback icon packs from local themes")
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIconPackDetail(themeId: String): IconDetailDto? = withContext(Dispatchers.IO) {
        try {
            val raw = themeId.substringAfterLast("/").substringBeforeLast(".").trim()
            val candidateIds = linkedSetOf<String>()
            candidateIds.add(raw)
            if (raw.startsWith("icon_theme_")) candidateIds.add(raw.removePrefix("icon_"))
            if (raw.startsWith("icon_")) candidateIds.add(raw.removePrefix("icon_"))
            if (raw.contains("theme_")) candidateIds.add("theme_" + raw.substringAfterLast("theme_"))
            if (!raw.startsWith("theme_")) candidateIds.add("theme_$raw")

            for (id in candidateIds) {
                if (id.isBlank()) continue
                val response = ApiClient.get<ApiResponse<IconDetailDto>>("api/v1/icons/$id")
                if (response?.data != null) {
                    Log.i(TAG, "Loaded icon pack detail for '$id' (Total icons: ${response.data.allIcons.size})")
                    return@withContext response.data
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getIconPackDetail error: ${e.message}")
        }
        null
    }
}
