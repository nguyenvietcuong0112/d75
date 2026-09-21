package com.widget.themes.controlcenter.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.widget.themes.controlcenter.core.data.ResourceConfig
import java.util.UUID

@Entity(tableName = "app_themes")
data class ThemeItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val name: String,
    val path: String = "",
    val rawType: String = "widget_theme", // "default", "custom", "widget_theme"
    val backgroundPath: String? = null,
    val previewPath: String? = null,
    val thumbPath: String? = null,
    val isPremium: Boolean = false,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val downloads: Long = 0,
    val totalIcons: Int = 0,
    val totalWidgets: Int = 0
) : java.io.Serializable {

    fun getThumbnailUrl(): String {
        if (!thumbPath.isNullOrEmpty()) return thumbPath
        if (!previewPath.isNullOrEmpty()) return previewPath
        return getPreviewUrl()
    }

    fun getPreviewUrl(): String {
        if (!previewPath.isNullOrEmpty()) return previewPath
        if (path.isEmpty()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        if (path.startsWith("category/")) {
            return "file:///android_asset/assets_theme/$path/bg_preview.png"
        }
        return ResourceConfig.getThemePreviewUrl(path)
    }

    fun getBackgroundUrl(): String {
        if (!backgroundPath.isNullOrEmpty()) return backgroundPath
        if (path.isEmpty()) return ""
        if (path.startsWith("category/")) {
            return "file:///android_asset/assets_theme/$path/wallpapers/bg_wallpaper.png"
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            if (path.contains("wallpaper")) return path
            return ""
        }
        return ResourceConfig.getWallpaperFullUrl(path)
    }
}
