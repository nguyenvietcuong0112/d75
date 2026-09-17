package com.widget.themes.controlcenter.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = 200,
    val status: Int = 200,
    val message: String = "",
    val data: T? = null
) {
    val isSuccessful: Boolean get() = code == 200 || status == 200
}

@Serializable
data class PagedData<T>(
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 1,
    val items: List<T> = emptyList()
)

@Serializable
data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val type: String = "theme",
    val thumbUrl: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class ThemeItemDto(
    val id: String = "",
    val name: String = "",
    val themeName: String = "",
    val themePath: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    val previewUrl: String? = null,
    val previewPath: String? = null,
    val thumbUrl: String? = null,
    val thumbPath: String? = null,
    val downloads: Long = 0,
    val views: Long = 0,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val totalIcons: Int = 0,
    val totalWidgets: Int = 0,
    val wallpaper: WallpaperDetailDto? = null,
    val icons: ThemeIconsDetailDto? = null,
    val widgets: List<WidgetDto> = emptyList()
) {
    val displayName: String get() = themeName.ifEmpty { name }
    val displayPreview: String? get() = previewUrl ?: previewPath ?: thumbUrl ?: thumbPath
}

@Serializable
data class ThemeDetailDto(
    val id: String = "",
    val name: String = "",
    val themeName: String = "",
    val themePath: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    val previewUrl: String? = null,
    val previewPath: String? = null,
    val thumbUrl: String? = null,
    val thumbPath: String? = null,
    val downloads: Long = 0,
    val views: Long = 0,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val wallpaper: WallpaperDetailDto? = null,
    val icons: ThemeIconsDetailDto? = null,
    val widgets: List<WidgetDto> = emptyList()
) {
    val displayName: String get() = themeName.ifEmpty { name }
    val displayPreview: String? get() = previewUrl ?: previewPath ?: thumbUrl ?: thumbPath
}

@Serializable
data class WallpaperDetailDto(
    val wallpaperUrl: String? = null,
    val thumbUrl: String? = null
)

@Serializable
data class ThemeIconsDetailDto(
    val themePath: String? = null,
    val themeName: String? = null,
    val backgroundIconUrl: String? = null,
    val backgroundIconDefaultUrl: String? = null,
    val totalIcons: Int = 0,
    val items: List<IconItemDto> = emptyList(),
    val icons: List<IconItemDto> = emptyList()
) {
    val allIcons: List<IconItemDto> get() = items.ifEmpty { icons }
}

typealias IconDetailDto = ThemeIconsDetailDto

@Serializable
data class IconItemDto(
    val name: String = "",
    val appName: String = "",
    val label: String = "",
    val iconUrl: String = "",
    val packageCandidates: List<String> = emptyList()
)

@Serializable
data class WallpaperDto(
    val id: String = "",
    val name: String = "",
    val themePath: String = "",
    val wallpaperUrl: String? = null,
    val thumbUrl: String? = null,
    val previewUrl: String? = null,
    val category: String = "",
    val categories: List<String> = emptyList(),
    val downloads: Long = 0,
    val views: Long = 0,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isFree: Boolean = true
) {
    val displayUrl: String? get() = wallpaperUrl ?: previewUrl ?: thumbUrl
}

@Serializable
data class WidgetDto(
    val id: String = "",
    val name: String = "",
    val widgetUrl: String? = null,
    val previewUrl: String? = null,
    val thumbUrl: String? = null,
    val size: String = "4x2",
    val rawType: String = "clock",
    val themeId: String = "",
    val themeName: String = "",
    val themePath: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    val downloads: Long = 0,
    val views: Long = 0,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val isNew: Boolean = false
) {
    val displayUrl: String? get() = previewUrl ?: widgetUrl ?: thumbUrl
}

@Serializable
data class IconPackDto(
    val id: String = "",
    val name: String = "",
    val themeName: String = "",
    val themePath: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    val backgroundIconUrl: String? = null,
    val backgroundIconDefaultUrl: String? = null,
    val thumbUrl: String? = null,
    val previewUrl: String? = null,
    val totalIcons: Int = 0,
    val downloads: Long = 0,
    val views: Long = 0,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isFree: Boolean = true,
    val previewIcons: List<IconItemDto> = emptyList()
) {
    val displayName: String get() = themeName.ifEmpty { name }
}

@Serializable
data class ControlCenterDto(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val slug: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    val folderPath: String? = null,
    val thumbUrl: String? = null,
    val thumbPath: String? = null,
    val previewUrl: String? = null,
    val previewPath: String? = null,
    val downloads: Long = 0,
    val views: Long = 0,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isFree: Boolean = true
) {
    val displayThumb: String? get() = thumbPath ?: thumbUrl ?: previewPath ?: previewUrl
    val displayPreview: String? get() = previewPath ?: previewUrl ?: thumbPath ?: thumbUrl
}

@Serializable
data class ChargingDto(
    val id: String = "",
    val name: String = "",
    val previewUrl: String? = null,
    val thumbUrl: String? = null,
    val videoUrl: String? = null,
    val videoFoldUrl: String? = null,
    val downloads: Long = 0,
    val views: Long = 0,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isFree: Boolean = true
)

@Serializable
data class CollectionDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val previewPath: String = "",
    val targetPath: String = "",
    val downloads: Long = 0,
    val rawType: String = "",
    val extra: String? = null
)
