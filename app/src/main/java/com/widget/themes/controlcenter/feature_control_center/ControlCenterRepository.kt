package com.widget.themes.controlcenter.feature_control_center

import android.content.Context
import android.util.Log
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.ControlCenterDto
import com.widget.themes.controlcenter.core.network.PagedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class ControlCenterRepository(private val context: Context) {

    private val TAG = "ControlCenterRepository"
    private var cachedCategories: List<ControlCategory>? = null

    suspend fun getCategories(): List<ControlCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }

        // 1. Try remote API
        try {
            val catResponse = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "control_center")
            )
            val apiCategories = catResponse?.data

            val remoteCategoriesList = mutableListOf<ControlCategory>()

            if (!apiCategories.isNullOrEmpty()) {
                for (cat in apiCategories) {
                    val itemsResponse = ApiClient.get<ApiResponse<PagedData<ControlCenterDto>>>(
                        "api/v1/control-center",
                        mapOf("category_id" to cat.id, "page" to "1", "page_size" to "50")
                    )
                    val items = itemsResponse?.data?.items ?: emptyList()
                    val themes = items.map { dto ->
                        ControlTheme(
                            key = dto.key.ifEmpty { dto.id },
                            name = dto.name,
                            slug = dto.slug.ifEmpty { dto.id },
                            category = cat.name.ifEmpty { cat.id },
                            categorySlug = cat.id,
                            folderPath = dto.folderPath ?: "",
                            thumbPath = dto.displayThumb ?: "",
                            previewPath = dto.displayPreview ?: "",
                            isHot = dto.isHot,
                            isNew = dto.isNew,
                            downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.key.ifEmpty { dto.id })
                        )
                    }
                    if (themes.isNotEmpty()) {
                        remoteCategoriesList.add(
                            ControlCategory(
                                slug = cat.id,
                                name = cat.name.ifEmpty { cat.id },
                                themes = themes
                            )
                        )
                    }
                }
            } else {
                // Try fetching all control center items directly
                val itemsResponse = ApiClient.get<ApiResponse<PagedData<ControlCenterDto>>>(
                    "api/v1/control-center",
                    mapOf("page" to "1", "page_size" to "50")
                )
                val items = itemsResponse?.data?.items ?: emptyList()
                if (items.isNotEmpty()) {
                    val themes = items.map { dto ->
                        ControlTheme(
                            key = dto.key.ifEmpty { dto.id },
                            name = dto.name,
                            slug = dto.slug.ifEmpty { dto.id },
                            category = if (dto.categories.isNotEmpty()) dto.categories.first() else dto.category.ifEmpty { "Trending" },
                            categorySlug = if (dto.categories.isNotEmpty()) dto.categories.first().lowercase() else dto.category.lowercase().ifEmpty { "trending" },
                            folderPath = dto.folderPath ?: "",
                            thumbPath = dto.displayThumb ?: "",
                            previewPath = dto.displayPreview ?: "",
                            isHot = dto.isHot,
                            isNew = dto.isNew,
                            downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.key.ifEmpty { dto.id })
                        )
                    }
                    remoteCategoriesList.add(
                        ControlCategory(
                            slug = "trending",
                            name = "Trending",
                            themes = themes
                        )
                    )
                }
            }

            if (remoteCategoriesList.isNotEmpty()) {
                Log.i(TAG, "Loaded ${remoteCategoriesList.size} control center categories from CMS: ${remoteCategoriesList.map { "${it.name} (${it.themes.size} items)" }}")
                cachedCategories = remoteCategoriesList
                return@withContext remoteCategoriesList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading remote CC categories: ${e.message}")
        }

        // 2. Fallback: Local Asset data scanning
        val assetManager = context.assets
        val rootPath = "${ResourceConfig.CONTROL_CENTER}/control_themes"
        val categoryDirs = try {
            assetManager.list(rootPath) ?: emptyArray()
        } catch (e: Exception) {
            emptyArray()
        }

        val categoriesList = mutableListOf<ControlCategory>()

        for (catSlug in categoryDirs) {
            val catPath = "$rootPath/$catSlug"
            val themeDirs = try {
                assetManager.list(catPath) ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }

            val themes = mutableListOf<ControlTheme>()
            for (themeSlug in themeDirs) {
                val themeFolder = "$catPath/$themeSlug"
                var name = themeSlug.replace("_", " ").replaceFirstChar { it.uppercase() }
                var categoryName = catSlug.replaceFirstChar { it.uppercase() }
                var key = themeSlug
                var isHot = false
                var isNew = false
                var downloads = 0L

                // Try reading metadata.json
                try {
                    val metaStream = assetManager.open("$themeFolder/metadata.json")
                    val jsonStr = metaStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)
                    name = json.optString("name", name)
                    categoryName = json.optString("category", categoryName)
                    key = json.optString("key", key)
                    isHot = json.optBoolean("isHot", false)
                    isNew = json.optBoolean("isNew", false)
                    downloads = json.optLong("downloads", 0L)
                } catch (e: Exception) {
                    // Ignore, fallback to defaults
                }

                val thumbFile = "file:///android_asset/$themeFolder/thumb.webp"
                val previewFile = "file:///android_asset/$themeFolder/preview.jpg"

                themes.add(
                    ControlTheme(
                        key = key,
                        name = name,
                        slug = themeSlug,
                        category = categoryName,
                        categorySlug = catSlug,
                        folderPath = themeFolder,
                        thumbPath = thumbFile,
                        previewPath = previewFile,
                        isHot = isHot,
                        isNew = isNew,
                        downloads = downloads
                    )
                )
            }

            if (themes.isNotEmpty()) {
                val catName = themes.firstOrNull()?.category ?: catSlug.replaceFirstChar { it.uppercase() }
                categoriesList.add(
                    ControlCategory(
                        slug = catSlug,
                        name = catName,
                        themes = themes
                    )
                )
            }
        }

        cachedCategories = categoriesList
        categoriesList
    }
}
