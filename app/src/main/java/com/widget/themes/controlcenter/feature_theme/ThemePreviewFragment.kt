package com.widget.themes.controlcenter.feature_theme

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.ui.DownloadThemeButtonView
import com.widget.themes.controlcenter.feature_widget.DownloadThemeActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemePreviewFragment : Fragment() {

    private lateinit var themeId: String
    private var themeName: String = ""
    private var themePath: String = ""
    private var themeType: String = ""
    private var themePreview: String = ""
    private var themeWallpaper: String = ""
    private var theme: ThemeItem? = null

    private var thumbnailImageView: ImageView? = null
    private var imageView: ImageView? = null
    private var llDownload: DownloadThemeButtonView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = arguments?.getString("theme_id") ?: ""
        themeName = arguments?.getString("theme_name") ?: ""
        themePath = arguments?.getString("theme_path") ?: ""
        themeType = arguments?.getString("theme_type") ?: ""
        themePreview = arguments?.getString("theme_preview") ?: ""
        themeWallpaper = arguments?.getString("theme_wallpaper") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thumbnailImageView = view.findViewById(R.id.thumbnailImageView)
        imageView = view.findViewById(R.id.imageView)
        llDownload = view.findViewById(R.id.llDownload)

        // Apply native blur filter to background image on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            thumbnailImageView?.setRenderEffect(
                android.graphics.RenderEffect.createBlurEffect(
                    25f, 25f, android.graphics.Shader.TileMode.CLAMP
                )
            )
        }

        loadThemeDetails()
    }

    private fun loadThemeDetails() {
        lifecycleScope.launch {
            val context = context ?: return@launch
            val themeItem = withContext(Dispatchers.IO) {
                if (themeId.startsWith("default_")) {
                    val path = themeId.substringAfter("default_")
                    val cat = path.substringBefore("/")
                    val name = path.substringAfter("/").replace("-", " ").replaceFirstChar { it.uppercase() }
                    ThemeItem(id = themeId, categoryId = cat.lowercase(), name = name, path = path, rawType = "default")
                } else {
                    val fromDb = ServiceLocator.getThemeDao(context).getThemeById(themeId)
                    if (fromDb != null) {
                        fromDb
                    } else {
                        val path = themePath.ifEmpty { themeId }
                        val name = themeName.ifEmpty { "Theme" }
                        val detail = try {
                            ServiceLocator.getThemeRepository(context).getThemeDetail(path)
                        } catch (e: Exception) {
                            null
                        }
                        val remotePreview = themePreview.takeIf { it.startsWith("http") }
                            ?: detail?.previewUrl
                            ?: detail?.wallpaperUrl
                        val resolvedWp = detail?.wallpaperUrl?.takeIf { it.isNotBlank() }
                            ?: themeWallpaper.takeIf { it.isNotBlank() }
                        ThemeItem(
                            id = themeId,
                            categoryId = "theme",
                            name = detail?.themeName?.takeIf { it.isNotBlank() } ?: name,
                            path = detail?.themePath?.takeIf { it.isNotBlank() } ?: path,
                            rawType = themeType.ifEmpty { "collection_theme" },
                            previewPath = remotePreview,
                            thumbPath = remotePreview,
                            backgroundPath = resolvedWp
                        )
                    }
                }
            }

            theme = themeItem

            themeItem?.let { item ->
                llDownload?.setText("Apply")

                val previewUrl = when {
                    !item.previewPath.isNullOrEmpty() && item.previewPath.startsWith("http") -> item.previewPath
                    !item.thumbPath.isNullOrEmpty() && item.thumbPath.startsWith("http") -> item.thumbPath
                    themePreview.startsWith("http") -> themePreview
                    item.getThumbnailUrl().startsWith("http") -> item.getThumbnailUrl()
                    item.path.startsWith("http://") || item.path.startsWith("https://") -> item.path
                    else -> item.getThumbnailUrl()
                }

                // Load blurred background
                if (thumbnailImageView != null) {
                    Glide.with(context)
                        .load(previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .into(thumbnailImageView!!)
                }

                // Load mockup preview
                if (imageView != null) {
                    Glide.with(context)
                        .load(previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .into(imageView!!)
                }

                llDownload?.setOnClickListener {
                    val resolvedPreview = item.previewPath ?: item.getThumbnailUrl()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(requireContext().applicationContext)
                        repo.markAsDownloaded(
                            id = item.id,
                            name = item.name,
                            category = "Theme",
                            targetPath = item.path,
                            previewPath = resolvedPreview,
                            rawType = "collection_theme"
                        )
                    }

                    val intent = Intent(context, DownloadThemeActivity::class.java).apply {
                        putExtra("theme_item", item)
                        putExtra("theme_id", item.id)
                        putExtra("theme_name", item.name)
                        putExtra("theme_path", item.path)
                        putExtra("theme_type", item.rawType)
                        putExtra("theme_preview", resolvedPreview)
                        putExtra("theme_thumb", resolvedPreview)
                        putExtra("theme_wallpaper", item.backgroundPath ?: item.getBackgroundUrl())
                        putExtra("is_downloaded", true)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        fun newInstance(
            themeId: String,
            themeName: String = "",
            themePath: String = "",
            themeType: String = "",
            themePreview: String = "",
            themeWallpaper: String = ""
        ): ThemePreviewFragment {
            return ThemePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString("theme_id", themeId)
                    putString("theme_name", themeName)
                    putString("theme_path", themePath)
                    putString("theme_type", themeType)
                    putString("theme_preview", themePreview)
                    putString("theme_wallpaper", themeWallpaper)
                }
            }
        }
    }
}
