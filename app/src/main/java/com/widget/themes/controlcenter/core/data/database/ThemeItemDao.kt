package com.widget.themes.controlcenter.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.widget.themes.controlcenter.core.data.entity.ThemeItem

@Dao
interface ThemeItemDao {
    @Query("SELECT * FROM app_themes")
    fun getAllThemes(): List<ThemeItem>

    @Query("SELECT * FROM app_themes WHERE id = :id LIMIT 1")
    fun getThemeById(id: String): ThemeItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTheme(theme: ThemeItem): Long

    @Delete
    fun deleteTheme(theme: ThemeItem)

    @Query("DELETE FROM app_themes WHERE id = :id")
    fun deleteThemeById(id: String)
}
