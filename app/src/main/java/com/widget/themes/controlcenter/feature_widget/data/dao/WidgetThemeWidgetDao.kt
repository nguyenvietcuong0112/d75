package com.widget.themes.controlcenter.feature_widget.data.dao

import androidx.room.*
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWidget

@Dao
interface WidgetThemeWidgetDao {
    @Query("SELECT * FROM widget_theme_widgets")
    fun getAllWidgetThemes(): List<WidgetThemeWidget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidgetThemes(themes: List<WidgetThemeWidget>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidgetTheme(theme: WidgetThemeWidget)

    @Query("SELECT * FROM widget_theme_widgets WHERE folder = :folder LIMIT 1")
    fun getWidgetThemeByFolder(folder: String): WidgetThemeWidget?
}
