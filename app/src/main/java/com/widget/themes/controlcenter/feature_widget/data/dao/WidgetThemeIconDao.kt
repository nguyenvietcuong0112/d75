package com.widget.themes.controlcenter.feature_widget.data.dao

import androidx.room.*
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeIcon

@Dao
interface WidgetThemeIconDao {
    @Query("SELECT * FROM widget_theme_icons")
    fun getAllIcons(): List<WidgetThemeIcon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIcons(icons: List<WidgetThemeIcon>)

    @Update
    fun updateIcon(icon: WidgetThemeIcon)
}
