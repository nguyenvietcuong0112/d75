package com.widget.themes.controlcenter.feature_widget.data

import android.content.Context
import android.util.Log
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.PagedData
import com.widget.themes.controlcenter.core.network.WidgetDto
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetRepository(private val context: Context) {

    private val TAG = "WidgetRepository"
    private val themeRepository = ServiceLocator.getThemeRepository(context)
    private var cachedCategories: List<CategoryDto>? = null

    suspend fun getCategories(): List<CategoryDto> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val response = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "widget")
            )
            val list = response?.data
            if (!list.isNullOrEmpty()) {
                Log.i(TAG, "Loaded ${list.size} widget categories from CMS: ${list.map { it.name }}")
                cachedCategories = list
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories widget error: ${e.message}")
        }

        // Fallback: try general categories from API
        try {
            val allResponse = ApiClient.get<ApiResponse<List<CategoryDto>>>("api/v1/categories")
            val list = allResponse?.data?.filter { it.type == "widget" || it.type == "theme" }
            if (!list.isNullOrEmpty()) {
                val distinct = list.distinctBy { it.id.lowercase() }
                Log.i(TAG, "Loaded ${distinct.size} fallback categories for widgets from CMS: ${distinct.map { it.name }}")
                cachedCategories = distinct
                return@withContext distinct
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories all error: ${e.message}")
        }

        // Fallback to theme categories
        try {
            val themeCats = themeRepository.getCategories()
            val mapped = themeCats.mapIndexed { idx, cat ->
                CategoryDto(
                    id = cat.category.lowercase(),
                    name = cat.name.ifEmpty { cat.category },
                    type = "widget",
                    sortOrder = idx
                )
            }
            Log.i(TAG, "Loaded ${mapped.size} local theme categories for widgets: ${mapped.map { it.name }}")
            cachedCategories = mapped
            mapped
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWidgets(
        categoryId: String? = null,
        size: String? = null,
        rawType: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
        sortBy: String = "trending"
    ): List<WidgetDto> = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf(
                "page" to page.toString(),
                "page_size" to pageSize.toString(),
                "sort_by" to sortBy
            )
            if (!categoryId.isNullOrEmpty() && !categoryId.equals("all", ignoreCase = true)) {
                params["category_id"] = categoryId.lowercase()
            }
            if (!size.isNullOrEmpty()) {
                params["size"] = size
            }
            if (!rawType.isNullOrEmpty()) {
                params["raw_type"] = rawType
            }

            val response = ApiClient.get<ApiResponse<PagedData<WidgetDto>>>("api/v1/widgets", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                Log.i(TAG, "Loaded ${items.size} widgets for category '${categoryId ?: "all"}' (types: ${items.map { it.rawType }})")
                return@withContext items
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWidgets error: ${e.message}")
        }

        emptyList()
    }
}
