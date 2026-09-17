package com.widget.themes.controlcenter.feature_wallpaper

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.widget.themes.controlcenter.feature_wallpaper.data.WallpaperRepository
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WallpaperCategory(
    val id: String,
    val name: String,
    val isAll: Boolean = false
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val wallpaperRepository = WallpaperRepository(application)

    private val _categories = MutableLiveData<List<WallpaperCategory>>()
    val categories: LiveData<List<WallpaperCategory>> = _categories

    val selectedCategory = MutableLiveData<WallpaperCategory>()

    private val _wallpapers = MutableLiveData<List<WidgetThemeWallpaper>>()
    val wallpapers: LiveData<List<WidgetThemeWallpaper>> = _wallpapers

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    private var currentPage = 1
    private var isEndReached = false
    private var isLoading = false
    private val loadedList = mutableListOf<WidgetThemeWallpaper>()

    init {
        loadCoins()
        loadOnlineCategories()
    }

    private fun loadOnlineCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val cats = wallpaperRepository.getCategories()
            withContext(Dispatchers.Main) {
                _categories.value = cats
                val firstCat = cats.firstOrNull() ?: WallpaperCategory("all", "All", true)
                selectedCategory.value = firstCat
                loadWallpapers(loadMore = false)
            }
        }
    }

    fun getNumberOfColumns(context: Context): Int {
        val config = context.resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600
        val isUltraWide = config.screenWidthDp >= 1000
        return when {
            isUltraWide -> 5
            isTablet -> 4
            else -> 3
        }
    }

    fun loadWallpapers(loadMore: Boolean = false) {
        if (isLoading) return
        if (loadMore && isEndReached) return

        if (!loadMore) {
            currentPage = 1
            isEndReached = false
            loadedList.clear()
        } else {
            currentPage++
        }

        isLoading = true
        val catId = selectedCategory.value?.id ?: "all"

        viewModelScope.launch(Dispatchers.IO) {
            val result = wallpaperRepository.getWallpapers(
                categoryId = if (catId == "all") null else catId,
                page = currentPage,
                pageSize = 30
            )

            withContext(Dispatchers.Main) {
                isLoading = false
                if (result.isEmpty()) {
                    isEndReached = true
                } else {
                    loadedList.addAll(result)
                    _wallpapers.value = ArrayList(loadedList)
                }
            }
        }
    }

    fun filterWallpapers(category: String) {
        val catObj = _categories.value?.find {
            it.name.equals(category, ignoreCase = true) || it.id.equals(category, ignoreCase = true)
        } ?: WallpaperCategory(category.lowercase(), category)

        selectedCategory.value = catObj
        loadWallpapers(loadMore = false)
    }

    fun loadCoins() {
        _coins.value = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getUserCoins(getApplication())
    }
}
