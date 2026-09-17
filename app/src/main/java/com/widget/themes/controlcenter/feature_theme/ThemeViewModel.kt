package com.widget.themes.controlcenter.feature_theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.di.ServiceLocator
import kotlinx.coroutines.launch

data class CategoryTag(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ServiceLocator.getThemeRepository(application)

    private val _categories = MutableLiveData<List<CategoryTag>>()
    val categories: LiveData<List<CategoryTag>> = _categories

    private val _themes = MutableLiveData<List<ThemeItem>>()
    val themes: LiveData<List<ThemeItem>> = _themes

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    var selectedCategoryId: String = "trending"
    private var allLoadedThemes = listOf<ThemeItem>()

    init {
        loadCoins()
        loadCategoriesAndThemes()
    }

    fun loadCoins() {
        _coins.value = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getUserCoins(getApplication())
    }

    fun addCoins(amount: Int) {
        val updated = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.addCoins(getApplication(), amount)
        _coins.value = updated
    }

    private fun loadCategoriesAndThemes() {
        viewModelScope.launch {
            // 1. Load Categories dynamically from Repository (Remote CMS API)
            val decorateCategories = themeRepository.getCategories()

            if (selectedCategoryId.isEmpty() || decorateCategories.none { it.category.equals(selectedCategoryId, ignoreCase = true) }) {
                selectedCategoryId = decorateCategories.firstOrNull()?.category ?: ""
            }

            val categoryTags = decorateCategories.map { decorCat ->
                CategoryTag(
                    id = decorCat.category,
                    name = decorCat.name,
                    isSelected = decorCat.category.equals(selectedCategoryId, ignoreCase = true)
                )
            }
            _categories.value = categoryTags

            // 2. Load Themes from Repository
            allLoadedThemes = themeRepository.getThemes()
            filterThemes()
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        
        // Update selected state in categories list
        val updatedCategories = _categories.value?.map {
            it.copy(isSelected = it.id.equals(categoryId, ignoreCase = true))
        } ?: emptyList()
        _categories.value = updatedCategories

        filterThemes()
    }

    fun loadRandomThemes() {
        viewModelScope.launch {
            allLoadedThemes = allLoadedThemes.shuffled()
            filterThemes()
        }
    }

    private fun filterThemes() {
        if (selectedCategoryId.isEmpty() || selectedCategoryId.equals("all", ignoreCase = true)) {
            _themes.value = allLoadedThemes
            return
        }
        val filtered = allLoadedThemes.filter { theme ->
            val themeCats = theme.categoryId.split(",").map { it.trim().lowercase() }
            themeCats.contains(selectedCategoryId.lowercase()) ||
            themeCats.any { it.contains(selectedCategoryId.lowercase()) } ||
            (selectedCategoryId.equals("trending", ignoreCase = true) && theme.isHot)
        }
        _themes.value = filtered
    }
}
