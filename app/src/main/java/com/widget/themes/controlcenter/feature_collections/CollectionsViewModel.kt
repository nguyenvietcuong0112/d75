package com.widget.themes.controlcenter.feature_collections

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.widget.themes.controlcenter.feature_collections.data.CollectionItem
import com.widget.themes.controlcenter.feature_collections.data.CollectionRepository
import com.widget.themes.controlcenter.feature_collections.data.CollectionTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CollectionRepository(application)

    private val _tabs = MutableLiveData<List<CollectionTab>>()
    val tabs: LiveData<List<CollectionTab>> = _tabs

    private val _selectedTab = MutableLiveData<CollectionTab>()
    val selectedTab: LiveData<CollectionTab> = _selectedTab

    private val _discoveryItems = MutableLiveData<List<CollectionItem>>()
    val discoveryItems: LiveData<List<CollectionItem>> = _discoveryItems

    private val _downloadedItems = MutableLiveData<List<CollectionItem>>()
    val downloadedItems: LiveData<List<CollectionItem>> = _downloadedItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentTabName: String = "Theme"

    private var pendingTabName: String? = null

    init {
        initTabs()
    }

    private fun initTabs() {
        val tabList = CollectionRepository.TABS.map { name ->
            CollectionTab(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isSelected = (name.equals("Theme", ignoreCase = true))
            )
        }
        _tabs.value = tabList
        val targetName = pendingTabName
        pendingTabName = null
        val targetTab = if (!targetName.isNullOrEmpty()) {
            tabList.firstOrNull { it.name.equals(targetName, ignoreCase = true) || it.id.equals(targetName, ignoreCase = true) }
        } else null

        val chosenTab = targetTab ?: tabList.firstOrNull()
        if (chosenTab != null) {
            selectTab(chosenTab)
        } else {
            loadDataForSelectedTab()
        }
    }

    fun selectTab(tab: CollectionTab) {
        Log.d("CollectionDebug", "ViewModel: selectTab '${tab.name}' (id=${tab.id})")
        currentTabName = tab.name
        val updated = _tabs.value?.map {
            it.copy(isSelected = (it.id == tab.id))
        } ?: emptyList()
        _tabs.value = updated
        _selectedTab.value = updated.firstOrNull { it.isSelected } ?: tab
        loadDataForSelectedTab()
    }

    fun selectTabByName(name: String) {
        val currentTabs = _tabs.value
        if (currentTabs.isNullOrEmpty()) {
            pendingTabName = name
            return
        }
        val tab = currentTabs.firstOrNull { it.name.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true) }
        if (tab != null) {
            selectTab(tab)
        } else {
            pendingTabName = name
        }
    }

    fun loadDataForSelectedTab() {
        _isLoading.value = true
        Log.d("CollectionDebug", "ViewModel: loadDataForSelectedTab starting for '$currentTabName'")
        viewModelScope.launch {
            val discovery = repository.getDiscoveryItems(currentTabName)
            val downloadedEntities = repository.getDownloadedItems(currentTabName)
            val downloaded = downloadedEntities.map { entity ->
                var preview = entity.previewPath
                val isIcon = entity.category.contains("Icon", ignoreCase = true)
                val isTheme = entity.category.contains("Theme", ignoreCase = true)
                val isWidget = entity.category.contains("Widget", ignoreCase = true)
                val isWallpaper = entity.category.contains("Wallpaper", ignoreCase = true)

                if (isIcon) {
                    if (preview.isEmpty() && entity.targetPath.isNotBlank()) {
                        preview = entity.targetPath
                    }
                    if (preview.startsWith("file:///android_asset/") || !preview.startsWith("http")) {
                        if (entity.targetPath.startsWith("http")) {
                            preview = entity.targetPath
                        } else {
                            val cleanId = entity.targetPath.substringAfterLast("/").substringBeforeLast(".").ifEmpty { entity.id.removePrefix("icon_").removePrefix("theme_") }
                            try {
                                val iconRepo = com.widget.themes.controlcenter.feature_icon.data.IconRepository(getApplication())
                                val detail = iconRepo.getIconPackDetail(cleanId) ?: iconRepo.getIconPackDetail("theme_$cleanId")
                                var remote = detail?.backgroundIconUrl?.takeIf { it.startsWith("http") }
                                if (remote == null) {
                                    val packs = iconRepo.getIconPacks()
                                    remote = packs.firstOrNull { it.id.contains(cleanId, ignoreCase = true) || it.name.equals(entity.name, ignoreCase = true) }?.folder
                                        ?: packs.firstOrNull()?.folder
                                }
                                if (remote != null) {
                                    preview = remote
                                    repository.markAsDownloaded(
                                        id = entity.id,
                                        name = entity.name,
                                        category = "Icons",
                                        targetPath = entity.targetPath,
                                        previewPath = remote,
                                        downloads = entity.downloads,
                                        rawType = entity.rawType,
                                        extra = entity.extra
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("CollectionsViewModel", "Icon preview recovery error: ${e.message}")
                            }
                        }
                    }
                } else if (isTheme) {
                    if (preview.isEmpty() && entity.targetPath.isNotBlank()) {
                        preview = entity.targetPath
                    }
                    if (preview.startsWith("file:///android_asset/") || !preview.startsWith("http")) {
                        if (entity.targetPath.startsWith("http")) {
                            preview = entity.targetPath
                        } else {
                            val cleanId = entity.targetPath.substringAfterLast("/").ifEmpty { entity.id.removePrefix("theme_") }
                            try {
                                val detail = com.widget.themes.controlcenter.core.di.ServiceLocator.getThemeRepository(getApplication()).getThemeDetail(cleanId)
                                val remote = detail?.previewUrl?.takeIf { it.startsWith("http") }
                                if (remote != null) {
                                    preview = remote
                                    repository.markAsDownloaded(
                                        id = entity.id,
                                        name = entity.name,
                                        category = "Theme",
                                        targetPath = entity.targetPath,
                                        previewPath = remote,
                                        downloads = entity.downloads,
                                        rawType = entity.rawType,
                                        extra = entity.extra
                                    )
                                }
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    }
                } else if (isWallpaper) {
                    if (preview.contains("bg_preview.png")) {
                        preview = preview.replace("bg_preview.png", "bg_wallpaper.png")
                    } else if (preview.contains("preview.png")) {
                        preview = preview.replace("preview.png", "wallpaper.png")
                    }
                    if (entity.targetPath.startsWith("http") && (preview.startsWith("file://") || !preview.startsWith("http"))) {
                        preview = entity.targetPath
                    }
                } else if (isWidget) {
                    if (entity.targetPath.startsWith("http") && (preview.isEmpty() || !preview.startsWith("http"))) {
                        preview = entity.targetPath
                    }
                    if (!preview.startsWith("http")) {
                        val isMedium = entity.extra == "4x2" || entity.extra == "4x4" || entity.rawType.contains("4x2") || entity.id.contains("4x2")
                        val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"
                        if (preview.contains("clock_") || preview.contains("calendar_") || preview.contains("clocks/") || preview.contains("today/")) {
                            val folder = preview.substringBeforeLast("/")
                            preview = "$folder/$bgFileName"
                        } else if (preview.isEmpty() || !preview.contains(".png")) {
                            val cleanTarget = if (entity.targetPath.isNotBlank()) entity.targetPath.removePrefix("/") else "assets_collection/theme/theme_1"
                            preview = "file:///android_asset/$cleanTarget/widgets/$bgFileName"
                        }
                    }
                }

                val resolvedCategory = when {
                    isTheme -> "Theme"
                    isIcon -> "Icons"
                    isWidget -> "Widget"
                    isWallpaper -> "Wallpaper"
                    else -> entity.category
                }

                val resolvedRawType = when {
                    isTheme -> "collection_theme"
                    isIcon -> "collection_icon"
                    else -> entity.rawType
                }

                CollectionItem(
                    id = entity.id,
                    name = entity.name,
                    category = resolvedCategory,
                    targetPath = entity.targetPath,
                    previewPath = preview,
                    downloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(entity.downloads, entity.id.ifEmpty { entity.name }),
                    rawType = resolvedRawType,
                    extra = entity.extra
                )
            }

            withContext(Dispatchers.Main) {
                Log.d("CollectionDebug", "ViewModel: Posting discoveryItems size=${discovery.size} for '$currentTabName'")
                _discoveryItems.value = discovery
                _downloadedItems.value = downloaded
                _isLoading.value = false
            }
        }
    }
}
