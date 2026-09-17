package com.widget.themes.controlcenter.feature_widget.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a Theme bundle (WidgetTheme)
 */
@Entity(tableName = "widget_themes")
data class WidgetTheme(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val folder: String, // Folder name containing theme on CDN (e.g.: "theme_1")
    val order: Int,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false // DIY Custom theme flag
) : java.io.Serializable
