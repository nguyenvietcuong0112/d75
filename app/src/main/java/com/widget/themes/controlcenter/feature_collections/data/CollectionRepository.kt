package com.widget.themes.controlcenter.feature_collections.data

import android.content.Context
import com.widget.themes.controlcenter.core.data.FileUtils
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.ControlCenterDto
import com.widget.themes.controlcenter.core.network.IconPackDto
import com.widget.themes.controlcenter.core.network.PagedData
import com.widget.themes.controlcenter.core.network.ThemeItemDto
import com.widget.themes.controlcenter.core.network.WidgetDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CollectionRepository(private val context: Context) {

    private val downloadedDao by lazy { ServiceLocator.getDownloadedCollectionDao(context) }
    private val tabItemsCache = mutableMapOf<String, List<CollectionItem>>()
    private var cachedCollections: Map<String, List<CollectionItem>>? = null

    suspend fun getDiscoveryItems(category: String, forceRefresh: Boolean = false): List<CollectionItem> = withContext(Dispatchers.IO) {
        val key = when (category.trim().lowercase()) {
            "theme", "themes" -> "theme"
            "icons", "icon" -> "icons"
            "control center", "control", "control_center" -> "control_center"
            "widget", "widgets" -> "widget"
            "wallpaper", "wallpapers" -> "wallpaper"
            else -> category.trim().lowercase()
        }

        var list = tabItemsCache[key]
        if (list.isNullOrEmpty() || forceRefresh) {
            list = when (key) {
                "theme" -> fetchTop12Themes()
                "icons" -> fetchTop12Icons()
                "control_center" -> fetchTop12ControlCenter()
                "widget" -> fetchTop12Widgets()
                "wallpaper" -> fetchAllWallpapers()
                else -> emptyList()
            }
            if (list.isNotEmpty()) {
                tabItemsCache[key] = list
            }
        }

        val result = if (key == "widget" || key == "wallpaper") list else list.shuffled()
        android.util.Log.d("CollectionRepository", "getDiscoveryItems for key='$key': returning ${result.size} items (shuffled=${key != "widget" && key != "wallpaper"})")
        result
    }

    private suspend fun fetchTop12Themes(): List<CollectionItem> {
        try {
            val response = ApiClient.get<ApiResponse<PagedData<ThemeItemDto>>>(
                "api/v1/themes",
                mapOf("page" to "1", "page_size" to "12")
            )
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                return items.take(12).map { dto ->
                    CollectionItem(
                        id = dto.id.ifEmpty { "theme_${dto.themePath}" },
                        name = dto.displayName,
                        category = "Theme",
                        targetPath = dto.themePath.ifEmpty { dto.id },
                        previewPath = dto.displayPreview ?: "",
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.id.ifEmpty { dto.themePath }),
                        rawType = "collection_theme",
                        extra = dto.wallpaper?.wallpaperUrl?.takeIf { it.isNotBlank() } ?: dto.wallpaper?.thumbUrl?.takeIf { it.isNotBlank() }
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "fetchTop12Themes error: ${e.message}")
        }
        try {
            val themeRepo = ServiceLocator.getThemeRepository(context)
            val categories = themeRepo.getCategories()
            val allThemes = categories.flatMap { it.themes }
            if (allThemes.isNotEmpty()) {
                return allThemes.take(12).map { t ->
                    CollectionItem(
                        id = "theme_${t.themePath.replace('/', '_')}",
                        name = t.themeName,
                        category = "Theme",
                        targetPath = t.themePath,
                        previewPath = t.previewUrl ?: "file:///android_asset/${t.themePath.removePrefix("/")}/bg_preview.png",
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(t.downloads, t.themePath),
                        rawType = "collection_theme",
                        extra = t.wallpaperUrl ?: "file:///android_asset/${t.themePath.removePrefix("/")}/wallpapers/bg_wallpaper.png"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "Local themes fallback error: ${e.message}")
        }
        return emptyList()
    }

    private suspend fun fetchTop12Icons(): List<CollectionItem> {
        try {
            val response = ApiClient.get<ApiResponse<PagedData<IconPackDto>>>(
                "api/v1/icons",
                mapOf("page" to "1", "page_size" to "12")
            )
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                return items.take(12).map { dto ->
                    val preview = dto.backgroundIconUrl ?: dto.previewUrl ?: dto.thumbUrl ?: dto.themePath.ifEmpty { dto.id }
                    CollectionItem(
                        id = dto.id.ifEmpty { "icon_${dto.themePath}" },
                        name = dto.displayName,
                        category = "Icons",
                        targetPath = dto.themePath.ifEmpty { dto.id },
                        previewPath = preview,
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.id.ifEmpty { dto.themePath }),
                        rawType = "collection_icon"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "fetchTop12Icons error: ${e.message}")
        }
        try {
            val iconRepo = com.widget.themes.controlcenter.feature_icon.data.IconRepository(context)
            val packs = iconRepo.getIconPacks(page = 1, pageSize = 12)
            if (packs.isNotEmpty()) {
                return packs.take(12).map { pack ->
                    CollectionItem(
                        id = pack.id,
                        name = pack.name,
                        category = "Icons",
                        targetPath = pack.folder,
                        previewPath = pack.folder,
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(0, pack.id),
                        rawType = "collection_icon"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "Local icons fallback error: ${e.message}")
        }
        return emptyList()
    }

    private suspend fun fetchTop12ControlCenter(): List<CollectionItem> {
        try {
            val response = ApiClient.get<ApiResponse<PagedData<ControlCenterDto>>>(
                "api/v1/control-center",
                mapOf("page" to "1", "page_size" to "12")
            )
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                return items.take(12).map { dto ->
                    CollectionItem(
                        id = dto.key.ifEmpty { dto.id },
                        name = dto.name,
                        category = "Control center",
                        targetPath = dto.folderPath ?: dto.id,
                        previewPath = dto.displayPreview ?: dto.displayThumb ?: "",
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.key.ifEmpty { dto.id }),
                        rawType = "collection_control"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "fetchTop12ControlCenter error: ${e.message}")
        }
        try {
            val ccRepo = com.widget.themes.controlcenter.feature_control_center.ControlCenterRepository(context)
            val categories = ccRepo.getCategories()
            val allControls = categories.flatMap { it.themes }
            if (allControls.isNotEmpty()) {
                return allControls.take(12).map { c ->
                    CollectionItem(
                        id = c.key,
                        name = c.name,
                        category = "Control center",
                        targetPath = c.folderPath,
                        previewPath = c.previewPath.ifEmpty { c.thumbPath },
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(c.downloads, c.key.ifEmpty { c.name }),
                        rawType = "collection_control"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "Local control fallback error: ${e.message}")
        }
        return emptyList()
    }

    private suspend fun fetchTop12Widgets(): List<CollectionItem> {
        try {
            val response = ApiClient.get<ApiResponse<PagedData<WidgetDto>>>(
                "api/v1/widgets",
                mapOf("page" to "1", "page_size" to "12", "sort_by" to "trending")
            )
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                return items.take(12).map { dto ->
                    CollectionItem(
                        id = dto.id,
                        name = dto.name,
                        category = "Widget",
                        targetPath = dto.themePath.ifEmpty { dto.id },
                        previewPath = dto.displayUrl ?: "",
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(dto.downloads, dto.id),
                        rawType = dto.rawType,
                        extra = dto.size
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "fetchTop12Widgets error: ${e.message}")
        }
        try {
            val localJson = FileUtils.loadJsonFromAsset(context, "assets_collection/collections.json")
            val fallbackMap = parseCollectionsJson(localJson)
            val widgets = fallbackMap["widget"]
            if (!widgets.isNullOrEmpty()) {
                return widgets.take(12)
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "Local widgets fallback error: ${e.message}")
        }
        return emptyList()
    }

    private suspend fun fetchAllWallpapers(): List<CollectionItem> {
        try {
            val wpRepo = com.widget.themes.controlcenter.feature_wallpaper.data.WallpaperRepository(context)
            val allList = mutableListOf<com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper>()
            var page = 1
            while (page <= 20) {
                val batch = wpRepo.getWallpapers(page = page, pageSize = 50)
                if (batch.isEmpty()) break
                allList.addAll(batch)
                if (batch.size < 50) break
                page++
            }
            if (allList.isNotEmpty()) {
                return allList.distinctBy { it.id }.map { wp ->
                    val wpUrl = wp.folder.takeIf { it.startsWith("http") }
                        ?: wp.getOnlineImageUri(context).toString()
                    CollectionItem(
                        id = wp.id,
                        name = wp.name,
                        category = "Wallpaper",
                        targetPath = wpUrl,
                        previewPath = wpUrl,
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(0, wp.id),
                        rawType = "collection_wallpaper"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CollectionRepository", "fetchAllWallpapers error: ${e.message}")
        }
        return emptyList()
    }

    suspend fun getAllDiscoveryCollections(): Map<String, List<CollectionItem>> = withContext(Dispatchers.IO) {
        loadCollectionsFromApi()
    }

    fun getDownloadedItemsFlow(category: String): Flow<List<DownloadedCollectionItem>> {
        val normalizedCategory = normalizeCategoryName(category)
        return downloadedDao.getDownloadedByCategoryFlow(normalizedCategory)
    }

    suspend fun getDownloadedItems(category: String): List<DownloadedCollectionItem> = withContext(Dispatchers.IO) {
        val normalizedCategory = normalizeCategoryName(category)
        downloadedDao.getDownloadedByCategory(normalizedCategory)
    }

    suspend fun getAllDownloadedItems(): List<DownloadedCollectionItem> = withContext(Dispatchers.IO) {
        downloadedDao.getAllDownloaded()
    }

    suspend fun markAsDownloaded(
        id: String,
        name: String,
        category: String,
        targetPath: String,
        previewPath: String,
        downloads: Long = 7654321,
        rawType: String = "",
        extra: String? = null
    ) = withContext(Dispatchers.IO) {
        val normalizedCategory = normalizeCategoryName(category)
        val categoryPrefix = "${normalizedCategory.lowercase()}_"
        val uniqueId = if (id.startsWith(categoryPrefix)) id else "$categoryPrefix$id"
        val item = DownloadedCollectionItem(
            id = uniqueId,
            name = name,
            category = normalizedCategory,
            targetPath = targetPath,
            previewPath = previewPath,
            downloads = downloads,
            rawType = rawType,
            extra = extra,
            appliedTimestamp = System.currentTimeMillis()
        )
        downloadedDao.insertDownloaded(item)
    }

    suspend fun isThemeDownloaded(id: String, path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanId = id.removePrefix("theme_").trim()
            val prefixedId = "theme_$cleanId"
            downloadedDao.countDownloaded(cleanId, prefixedId, path.trim(), name.trim()) > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun normalizeCategoryName(category: String): String {
        return when (category.trim().lowercase()) {
            "theme" -> "Theme"
            "icons", "icon" -> "Icons"
            "control center", "control" -> "Control center"
            "widget", "widgets" -> "Widget"
            "wallpaper", "wallpapers" -> "Wallpaper"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    private fun loadCollectionsFromApi(): Map<String, List<CollectionItem>> {
        cachedCollections?.let { return it }

        // 1. Primary data source: Fetch directly from server API (https://api.1teps.com/themes/api/v1/collections?param1=1&param2=1)
        val apiBase = com.widget.themes.controlcenter.core.data.ResourceConfig.API_BASE_URL
        if (apiBase.isNotEmpty()) {
            try {
                val cleanBase = apiBase.trimEnd('/')
                val endpoint = if (cleanBase.endsWith("/api/v1") || cleanBase.endsWith("/api")) {
                    "$cleanBase/collections?param1=1&param2=1"
                } else {
                    "$cleanBase/api/v1/collections?param1=1&param2=1"
                }
                val url = java.net.URL(endpoint)
                val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "ThemeAppAndroid")
                }
                if (conn.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val remoteMap = parseCollectionsJson(jsonStr)
                    if (remoteMap.isNotEmpty()) {
                        val normalizedMap = mutableMapOf<String, List<CollectionItem>>()
                        for ((catKey, remoteItems) in remoteMap) {
                            val isWp = catKey == "wallpaper"
                            normalizedMap[catKey] = remoteItems.map { item ->
                                var preview = item.previewPath
                                var target = item.targetPath.ifEmpty { preview }
                                if (isWp || item.category.contains("wallpaper", ignoreCase = true)) {
                                    if (preview.contains("bg_preview.png")) preview = preview.replace("bg_preview.png", "bg_wallpaper.png")
                                    if (preview.contains("preview.png")) preview = preview.replace("preview.png", "wallpaper.png")
                                    if (target.contains("bg_preview.png")) target = target.replace("bg_preview.png", "bg_wallpaper.png")
                                    if (target.contains("preview.png")) target = target.replace("preview.png", "wallpaper.png")
                                }
                                item.copy(previewPath = preview, targetPath = target, rawType = item.rawType.ifEmpty { "collection_$catKey" })
                            }
                        }
                        cachedCollections = normalizedMap
                        return normalizedMap
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CollectionRepository", "Failed to fetch remote collections: ${e.message}")
            }
        }

        // 2. Offline fallback ONLY if network is unavailable or server is unreachable
        val localJson = FileUtils.loadJsonFromAsset(context, "assets_collection/collections.json")
        val fallbackMap = parseCollectionsJson(localJson)
        cachedCollections = fallbackMap
        return fallbackMap
    }

    private fun parseCollectionsJson(jsonStr: String): MutableMap<String, List<CollectionItem>> {
        val resultMap = mutableMapOf<String, List<CollectionItem>>()
        if (jsonStr.isBlank()) return resultMap
        try {
            val rootObj = JSONObject(jsonStr)
            val root = if (rootObj.has("data") && rootObj.get("data") is JSONObject) rootObj.getJSONObject("data") else rootObj

            if (rootObj.has("data") && rootObj.get("data") is org.json.JSONArray) {
                val dataArr = rootObj.getJSONArray("data")
                for (i in 0 until dataArr.length()) {
                    val obj = dataArr.getJSONObject(i)
                    val catKey = when (obj.optString("category").lowercase()) {
                        "theme" -> "theme"
                        "icons", "icon" -> "icons"
                        "control center", "control", "control_center" -> "control_center"
                        "widget", "widgets" -> "widget"
                        "wallpaper", "wallpapers" -> "wallpaper"
                        else -> obj.optString("category").lowercase()
                    }
                    val item = CollectionItem(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        targetPath = obj.optString("targetPath"),
                        previewPath = obj.optString("previewPath"),
                        downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(obj.optLong("downloads", 0), obj.optString("id")),
                        rawType = obj.optString("rawType", ""),
                        extra = if (obj.has("extra")) obj.optString("extra") else null
                    )
                    val currentList = resultMap.getOrPut(catKey) { mutableListOf() } as MutableList<CollectionItem>
                    currentList.add(item)
                }
            }

            val it = root.keys()
            while (it.hasNext()) {
                val key = it.next()
                val array = root.optJSONArray(key) ?: continue
                val normalizedKey = when (key.lowercase()) {
                    "theme", "themes", "theme bundle" -> "theme"
                    "icons", "icon" -> "icons"
                    "control_center", "control center", "control" -> "control_center"
                    "widget", "widgets" -> "widget"
                    "wallpaper", "wallpapers" -> "wallpaper"
                    else -> key.lowercase()
                }
                val items = mutableListOf<CollectionItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        CollectionItem(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            category = obj.optString("category"),
                            targetPath = obj.optString("targetPath"),
                            previewPath = obj.optString("previewPath"),
                            downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(obj.optLong("downloads", 0), obj.optString("id")),
                            rawType = obj.optString("rawType", ""),
                            extra = if (obj.has("extra")) obj.optString("extra") else null
                        )
                    )
                }
                if (items.isNotEmpty()) {
                    val existing = resultMap.getOrPut(normalizedKey) { mutableListOf() } as MutableList<CollectionItem>
                    existing.addAll(items)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultMap
    }

    companion object {
        val TABS = listOf("Theme", "Icons", "Control center", "Widget", "Wallpaper")

        fun getLocalizedCategoryName(context: Context, categoryName: String): String {
            val key = categoryName.lowercase().replace(" ", "_").trim()
            return when {
                key == "all" -> context.getString(com.widget.themes.controlcenter.R.string.all)
                key.contains("theme") -> context.getString(com.widget.themes.controlcenter.R.string.tab_theme)
                key.contains("icon") -> context.getString(com.widget.themes.controlcenter.R.string.tab_icons)
                key.contains("control") -> context.getString(com.widget.themes.controlcenter.R.string.tab_control)
                key.contains("widget") -> context.getString(com.widget.themes.controlcenter.R.string.tab_widget)
                key.contains("wallpaper") -> context.getString(com.widget.themes.controlcenter.R.string.wallpaper)
                else -> categoryName
            }
        }
    }
}
