package com.widget.themes.controlcenter.feature_icon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.widget.themes.controlcenter.feature_icon.data.IconRepository
import com.widget.themes.controlcenter.feature_theme.CategoryTag
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeIcon
import kotlinx.coroutines.launch

class IconViewModel(application: Application) : AndroidViewModel(application) {

    private val iconRepository = IconRepository(application)

    private val _categories = MutableLiveData<List<CategoryTag>>()
    val categories: LiveData<List<CategoryTag>> = _categories

    private val _icons = MutableLiveData<List<WidgetThemeIcon>>()
    val icons: LiveData<List<WidgetThemeIcon>> = _icons

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    private var selectedCategoryId: String = "all"
    private var allLoadedIcons = listOf<WidgetThemeIcon>()

    init {
        loadCoins()
        loadCategoriesAndIcons()
    }

    fun loadCoins() {
        _coins.value = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getUserCoins(getApplication())
    }

    private fun loadCategoriesAndIcons() {
        viewModelScope.launch {
            val cats = iconRepository.getCategories()
            _categories.value = cats.map { it.copy(isSelected = (it.id == selectedCategoryId)) }

            val list = iconRepository.getIconPacks()
            allLoadedIcons = list
            filterIcons()
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        val updated = _categories.value?.map {
            it.copy(isSelected = (it.id == categoryId))
        } ?: emptyList()
        _categories.value = updated
        filterIcons()
    }

    private fun filterIcons() {
        val filtered = if (selectedCategoryId == "all") {
            allLoadedIcons
        } else {
            allLoadedIcons.filter { it.category.equals(selectedCategoryId, ignoreCase = true) }
        }
        _icons.value = filtered
    }
}
