package com.widget.themes.controlcenter.core.data

import android.content.Context

object ResourceConfig {
    /**
     * Điền Domain máy chủ Backend API tại đây.
     * Ví dụ: const val BASE_URL = "https://api.yourdomain.com"
     * Để chuỗi rỗng "" để chạy chế độ Offline (đọc JSON & ảnh nội bộ).
     */
    const val BASE_URL = "https://api.1teps.com/themes"

    val API_BASE_URL: String
        get() = BASE_URL.trimEnd('/')

    val ASSET_BASE_URL: String
        get() = "file:///android_asset"

    const val CONTROL_CENTER = "assets_control_center"


    private var themePathToFolderMap: Map<String, String>? = null

    @Synchronized
    fun getThemeFolderByPath(context: Context, themePath: String): String {
        if (themePath.startsWith("theme_decorates/")) {
            return themePath.removePrefix("theme_decorates/")
        }
        return themePath
    }

    fun resolveThemeAssetPath(themeFolder: String, subPath: String): String {
        if (themeFolder.isEmpty()) return ""
        if (themeFolder.startsWith("http://") || themeFolder.startsWith("https://") || themeFolder.startsWith("file://")) {
            return themeFolder
        }
        val clean = themeFolder.removePrefix("/")
        val cleanSub = subPath.removePrefix("/")
        return when {
            clean.startsWith("assets_collection/") -> "$ASSET_BASE_URL/$clean/$cleanSub"
            clean.startsWith("assets_theme/") -> "$ASSET_BASE_URL/$clean/$cleanSub"
            clean.startsWith("assets_charging/") -> "$ASSET_BASE_URL/$clean/$cleanSub"
            clean.startsWith("assets_control_center/") -> "$ASSET_BASE_URL/$clean/$cleanSub"
            clean.startsWith("category/") -> "$ASSET_BASE_URL/assets_theme/$clean/$cleanSub"
            clean.startsWith("theme/") || clean.startsWith("widget/") || clean.startsWith("icons/") || clean.startsWith("wallpaper/") || clean.startsWith("control_center/") -> "$ASSET_BASE_URL/assets_collection/$clean/$cleanSub"
            else -> "$ASSET_BASE_URL/assets_theme/category/$clean/$cleanSub"
        }
    }

    /**
     * 2. Theme Previews & Wallpapers
     */
    fun getThemePreviewUrl(themeFolder: String): String {
        return resolveThemeAssetPath(themeFolder, "bg_preview.png")
    }

    fun getWallpaperThumbnailUrl(themeFolder: String, imageName: String = ""): String {
        if (themeFolder.isEmpty()) return ""
        val clean = themeFolder.removePrefix("/")
        if (clean.endsWith(".png") || clean.endsWith(".jpg") || clean.endsWith(".webp")) {
            return "$ASSET_BASE_URL/$clean"
        }
        if (clean.startsWith("assets_collection/wallpaper/")) {
            return "$ASSET_BASE_URL/$clean/wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName.removeSuffix(".png")
        return resolveThemeAssetPath(themeFolder, "wallpapers/$name.png")
    }

    fun getWallpaperFullUrl(themeFolder: String, imageName: String = "bg_wallpaper"): String {
        if (themeFolder.isEmpty()) return ""
        val clean = themeFolder.removePrefix("/")
        if (clean.endsWith(".png") || clean.endsWith(".jpg") || clean.endsWith(".webp")) {
            return "$ASSET_BASE_URL/$clean"
        }
        if (clean.startsWith("assets_collection/wallpaper/")) {
            return "$ASSET_BASE_URL/$clean/wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName.removeSuffix(".png")
        return resolveThemeAssetPath(themeFolder, "wallpapers/$name.png")
    }

    fun getDiyStickerUrl(categoryFolder: String, imageName: String): String {
        if (categoryFolder.isEmpty() || imageName.isEmpty()) return ""
        val cleanCat = categoryFolder.removePrefix("/")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/stickers/$cleanCat/$imageName"
    }

    fun getDiyBackgroundUrl(categoryFolder: String, imageName: String): String {
        if (categoryFolder.isEmpty() || imageName.isEmpty()) return ""
        val cleanCat = categoryFolder.removePrefix("/")
        val cleanImg = if (imageName.endsWith(".png")) imageName else "$imageName.png"
        return "$ASSET_BASE_URL/assets_wallpaper/templates/background/$cleanCat/$cleanImg"
    }

    fun getChargingVideoUrl(animFolder: String, isFold: Boolean = false): String {
        if (animFolder.isEmpty()) return ""
        val clean = animFolder.removePrefix("charging/").removePrefix("assets_charging/charging/").removePrefix("feature_charging/charging/")
        val fileSuffix = if (isFold) "video_fold.mp4" else "video.mp4"
        return "$ASSET_BASE_URL/assets_charging/charging/$clean/$fileSuffix"
    }

    /**
     * 5. App Icons
     */
    fun getIconPackPreviewUrl(themeFolder: String): String {
        return resolveThemeAssetPath(themeFolder, "icons/bg_icon.png")
    }

    fun getIconCategoryPreviewUrl(folder: String): String = getIconPackPreviewUrl(folder)

    fun getSingleIconUrl(themeFolder: String, iconId: String): String {
        if (themeFolder.isEmpty() || iconId.isEmpty()) return ""
        val cleanIconId = iconId.removePrefix("ic_").removeSuffix(".png").lowercase()
        return resolveThemeAssetPath(themeFolder, "icons/ic_$cleanIconId.png")
    }
    /**
     * 6. System Widgets
     */
    fun getWidgetAssetUrl(themeFolder: String, relativePath: String): String {
        return resolveThemeAssetPath(themeFolder, relativePath)
    }

    fun getWidgetPreviewUrl(themeFolder: String, widgetSize: String = "medium"): String {
        val cleanSize = widgetSize.lowercase().removeSuffix(".png")
        val fileName = if (cleanSize == "4x2" || cleanSize == "medium") "clock_4x2.png" else "clock_2x2.png"
        return resolveThemeAssetPath(themeFolder, "widgets/$fileName")
    }

    fun getWidgetPreviewUrl(context: Context, themeFolder: String): String = getThemePreviewUrl(themeFolder)
}
