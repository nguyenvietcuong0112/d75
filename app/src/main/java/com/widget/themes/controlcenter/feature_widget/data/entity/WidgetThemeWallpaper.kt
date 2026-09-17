package com.widget.themes.controlcenter.feature_widget.data.entity

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.util.UUID
import com.widget.themes.controlcenter.core.data.ResourceConfig

@Entity(tableName = "widget_theme_wallpapers")
data class WidgetThemeWallpaper(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val themeId: String,
    val name: String,
    val order: Int,
    val folder: String,
    val imageBg: String,
    val isFree: Boolean = true,
    var isFavorite: Boolean = false,
    val isNew: Boolean = false,
    val category: String = "" // Custom field for easy category horizontal filter
) : java.io.Serializable {

    fun getFile(context: Context): File {
        val config = context.resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600
        val suffix = if (isTablet) "_tablet" else ""
        val fileName = "${imageBg}${suffix}"
        return File(context.filesDir, "images/$folder/wallpapers/$fileName.png")
    }

    fun getOnlinePreviewUri(context: Context): Uri {
        if (folder.startsWith("http://") || folder.startsWith("https://")) {
            return Uri.parse(folder)
        }
        if (folder.endsWith(".png") || folder.endsWith(".jpg") || folder.endsWith(".webp")) {
            return Uri.parse(if (folder.startsWith("file://")) folder else "file:///android_asset/${folder.removePrefix("/")}")
        }
        val themePath = if (folder.startsWith("theme_") || folder.contains("/")) folder else "$folder/$imageBg"
        val themeFolder = ResourceConfig.getThemeFolderByPath(context, themePath)
        return Uri.parse(ResourceConfig.getWallpaperThumbnailUrl(themeFolder, "bg_wallpaper"))
    }

    fun getOnlineImageUri(context: Context): Uri {
        if (folder.startsWith("http://") || folder.startsWith("https://")) {
            return Uri.parse(folder)
        }
        if (folder.endsWith(".png") || folder.endsWith(".jpg") || folder.endsWith(".webp")) {
            return Uri.parse(if (folder.startsWith("file://")) folder else "file:///android_asset/${folder.removePrefix("/")}")
        }
        val themePath = if (folder.startsWith("theme_") || folder.contains("/")) folder else "$folder/$imageBg"
        val themeFolder = ResourceConfig.getThemeFolderByPath(context, themePath)
        return Uri.parse(ResourceConfig.getWallpaperFullUrl(themeFolder, "bg_wallpaper"))
    }
}
