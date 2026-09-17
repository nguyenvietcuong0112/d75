package com.widget.themes.controlcenter.feature_icon.data.dao

import androidx.room.*
import com.widget.themes.controlcenter.feature_widget.data.entity.AppIconItem
import com.widget.themes.controlcenter.feature_icon.data.entity.ThemeIconPack
import java.util.UUID

/**
 * Data Access Object (DAO) for ThemeIconPack and AppIconItem entities
 */
@Dao
interface IconDao {

    @Query("SELECT * FROM theme_icon_packs")
    fun getAllIconPacks(): List<ThemeIconPack>

    @Query("SELECT * FROM theme_icon_packs WHERE themeId = :themeId LIMIT 1")
    fun getIconPackByTheme(themeId: UUID): ThemeIconPack?

    @Query("SELECT * FROM app_icon_items WHERE iconPackId = :iconPackId")
    fun getIconsByPack(iconPackId: UUID): List<AppIconItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIconPack(pack: ThemeIconPack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIconItems(items: List<AppIconItem>)

    @Update
    fun updateIconItem(item: AppIconItem)
}
