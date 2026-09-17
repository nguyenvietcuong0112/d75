package com.widget.themes.controlcenter.feature_collections

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.widget.themes.controlcenter.feature_control_center.ControlCenterPreviewBottomSheet
import com.widget.themes.controlcenter.feature_icon.DownloadIconActivity
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.feature_theme.ThemePreviewActivity
import com.widget.themes.controlcenter.feature_wallpaper.DownloadWallpaperActivity
import com.widget.themes.controlcenter.feature_widget.SelectWidgetBottomSheet
import com.widget.themes.controlcenter.feature_collections.data.CollectionItem

object CollectionNavigator {

    fun navigateToDetail(activity: Activity, item: CollectionItem, isDownloaded: Boolean = false) {
        val performNavigate = {
            val categoryKey = item.category.lowercase()
            val rawTypeKey = item.rawType.lowercase()
            when {
            categoryKey.contains("theme") || rawTypeKey.contains("theme") -> {
                val resolvedPath = when {
                    item.targetPath.isNotBlank() -> item.targetPath
                    item.name.equals("Trending", ignoreCase = true) -> "assets_theme/category/Trending/theme_1"
                    item.name.equals("Dark Moon", ignoreCase = true) -> "assets_collection/theme/theme_1"
                    item.name.equals("Matcha Latte", ignoreCase = true) -> "assets_collection/theme/theme_2"
                    item.name.equals("Dreamy Bear", ignoreCase = true) -> "assets_collection/theme/theme_3"
                    else -> "assets_collection/theme/theme_1"
                }
                val intent = Intent(activity, com.widget.themes.controlcenter.feature_widget.DownloadThemeActivity::class.java).apply {
                    putExtra("theme_id", item.id.ifEmpty { "col_theme" })
                    putExtra("theme_name", item.name)
                    putExtra("theme_path", resolvedPath)
                    putExtra("theme_type", item.rawType.ifEmpty { "collection_theme" })
                    putExtra("theme_preview", item.previewPath)
                    putExtra("theme_wallpaper", item.extra)
                    putExtra("is_downloaded", isDownloaded)
                }
                activity.startActivity(intent)
            }

            categoryKey.contains("icon") -> {
                val resolvedPath = when {
                    item.targetPath.isNotBlank() -> item.targetPath
                    item.name.contains("Dark Moon", ignoreCase = true) -> "assets_collection/icons/icon_1"
                    item.name.contains("Matcha Latte", ignoreCase = true) -> "assets_collection/icons/icon_2"
                    item.name.contains("Dreamy Bear", ignoreCase = true) -> "assets_collection/icons/icon_3"
                    else -> "assets_collection/icons/icon_1"
                }
                val intent = Intent(activity, DownloadIconActivity::class.java).apply {
                    putExtra("theme_id", item.id.ifEmpty { "col_icon" })
                    putExtra("theme_name", item.name)
                    putExtra("theme_path", resolvedPath)
                    putExtra("theme_type", "icon_theme")
                    putExtra("theme_preview", item.previewPath)
                }
                activity.startActivity(intent)
            }

            categoryKey.contains("control") -> {
                com.widget.themes.controlcenter.feature_control_center.ControlCenterPreviewActivity.start(
                    activity,
                    themePath = item.targetPath,
                    themeName = item.name,
                    previewPath = item.previewPath,
                    thumbPath = item.previewPath
                )
            }

            categoryKey.contains("widget") -> {
                val widgetType = item.rawType.ifEmpty { "clock" }
                val size = item.extra ?: if (widgetType == "calendar") "4x2" else "2x2"
                val sheet = SelectWidgetBottomSheet()
                val dummyTheme = ThemeItem(
                    id = "widget_${item.targetPath.replace('/', '_')}",
                    name = item.name,
                    path = item.targetPath,
                    rawType = "widget_theme"
                )
                sheet.setParams(
                    dummyTheme,
                    widgetType,
                    size,
                    item.previewPath,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (activity is FragmentActivity) {
                    sheet.show(activity.supportFragmentManager, "select_widget")
                }
            }

            categoryKey.contains("wallpaper") -> {
                val intent = Intent(activity, DownloadWallpaperActivity::class.java).apply {
                    val fullWallpaperUrl = when {
                        item.targetPath.startsWith("http://") || item.targetPath.startsWith("https://") -> item.targetPath
                        item.previewPath.contains("bg_preview.png") -> item.previewPath.replace("bg_preview.png", "bg_wallpaper.png")
                        item.previewPath.contains("preview.png") -> item.previewPath.replace("preview.png", "wallpaper.png")
                        item.previewPath.startsWith("http://") || item.previewPath.startsWith("https://") -> item.previewPath
                        item.targetPath.isNotEmpty() -> {
                            if (item.targetPath.startsWith("file://")) {
                                item.targetPath
                            } else {
                                "file:///android_asset/${item.targetPath.removePrefix("/")}"
                            }
                        }
                        else -> item.previewPath
                    }
                    putExtra("wallpaper_path", fullWallpaperUrl)
                    putExtra("wallpaper_title", item.name)
                    putExtra("source_type", "collection")
                    putExtra("collection_id", item.id)
                }
                activity.startActivity(intent)
            }
        }
    }
        com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
            activity = activity,
            adUnitId = activity.getString(com.widget.themes.controlcenter.R.string.inter_all),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
            onNext = { performNavigate() }
        )
    }
}
