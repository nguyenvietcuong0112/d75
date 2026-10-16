package com.widget.themes.controlcenter.feature_widget

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.widget.themes.controlcenter.databinding.FragmentDownloadWallpaperBinding
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.feature_theme.DownloadThemeWallpaperBottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWallpaperFragment : Fragment() {

    private var _binding: FragmentDownloadWallpaperBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: ThemeItem
    private var wallpaper: WidgetThemeWallpaper? = null
    private var isInitiallyDownloaded: Boolean = false

    companion object {
        fun newInstance(theme: ThemeItem, isDownloaded: Boolean = false): DownloadWallpaperFragment {
            return DownloadWallpaperFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                    putBoolean("is_downloaded", isDownloaded)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(DownloadThemeViewModel::class.java)
        isInitiallyDownloaded = arguments?.getBoolean("is_downloaded", false) ?: false
        val rawTheme = arguments?.getSerializable("theme") as? ThemeItem
            ?: throw IllegalArgumentException("Theme required")
        theme = if (rawTheme.path.isNotBlank()) {
            rawTheme
        } else {
            val safePath = when {
                rawTheme.name.equals("Dark Moon", ignoreCase = true) -> "assets_collection/theme/theme_1"
                rawTheme.name.equals("Matcha Latte", ignoreCase = true) -> "assets_collection/theme/theme_2"
                rawTheme.name.equals("Dreamy Bear", ignoreCase = true) -> "assets_collection/theme/theme_3"
                rawTheme.name.equals("Trending", ignoreCase = true) -> "assets_theme/category/Trending/theme_1"
                else -> "assets_collection/theme/theme_1"
            }
            rawTheme.copy(path = safePath)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val initiallyDownloaded = isInitiallyDownloaded || (activity?.intent?.getBooleanExtra("is_downloaded", false) == true)
        if (initiallyDownloaded) {
            isInitiallyDownloaded = true
            updateButtonState(isDownloaded = true)
        } else {
            updateButtonState(isDownloaded = false)
        }

        val effectivePath = if (theme.path.isNotBlank()) theme.path else "assets_collection/theme/theme_1"
        val initialWallpaper = theme.backgroundPath?.takeIf { it.isNotBlank() }
            ?: theme.getBackgroundUrl().takeIf { it.isNotBlank() && !it.contains("bg_preview") }
            ?: effectivePath

        wallpaper = WidgetThemeWallpaper(
            id = "wp_${theme.id.ifEmpty { "default" }}",
            themeId = theme.id.ifEmpty { "default" },
            name = theme.name,
            order = 1,
            folder = initialWallpaper,
            imageBg = "bg_wallpaper"
        )
        setupUI()

        // Background tasks: verify Room DB download status & load remote details
        lifecycleScope.launch {
            if (!isInitiallyDownloaded) {
                val isDownloadedInDb = withContext(Dispatchers.IO) {
                    try {
                        val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(requireContext().applicationContext)
                        repo.isThemeDownloaded(theme.id, theme.path, theme.name)
                    } catch (e: Exception) {
                        false
                    }
                }
                if (isDownloadedInDb) {
                    isInitiallyDownloaded = true
                    withContext(Dispatchers.Main) {
                        updateButtonState(isDownloaded = true)
                        wallpaper?.let { preloadWallpaperBitmap(it) }
                    }
                }
            }

            try {
                val themeRepo = ServiceLocator.getThemeRepository(requireContext())
                val detail = withContext(Dispatchers.IO) {
                    try {
                        themeRepo.getThemeDetail(effectivePath)
                    } catch (e: Exception) {
                        null
                    }
                }
                val remoteWpUrl = detail?.wallpaperUrl?.takeIf { it.isNotBlank() }
                if (!remoteWpUrl.isNullOrBlank() && remoteWpUrl != wallpaper?.folder) {
                    wallpaper = wallpaper?.copy(folder = remoteWpUrl)
                    downloadedBitmap = null
                    withContext(Dispatchers.Main) {
                        loadPreviewImage()
                        wallpaper?.let { preloadWallpaperBitmap(it) }
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private var downloadedBitmap: Bitmap? = null

    private fun loadPreviewImage() {
        val wp = wallpaper ?: return
        val wallpaperUri = wp.getOnlineImageUri(requireContext())
        val previewUri = wp.getOnlinePreviewUri(requireContext())
        val fallbackAsset = android.net.Uri.parse("file:///android_asset/assets_collection/theme/theme_1/wallpapers/bg_wallpaper.png")

        Glide.with(this)
            .load(wallpaperUri)
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(
                Glide.with(this)
                    .load(previewUri)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(
                        Glide.with(this)
                            .load(fallbackAsset)
                            .placeholder(R.drawable.bg_default_placeholder)
                    )
            )
            .into(binding.ivPreview)
    }

    private fun preloadWallpaperBitmap(wp: WidgetThemeWallpaper) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val downloadUri = wp.getOnlineImageUri(requireContext()).toString().ifEmpty {
                    "file:///android_asset/assets_collection/theme/theme_1/wallpapers/bg_wallpaper.png"
                }
                val bmp = Glide.with(requireContext().applicationContext)
                    .asBitmap()
                    .load(downloadUri)
                    .submit()
                    .get()
                if (bmp != null) {
                    withContext(Dispatchers.Main) {
                        downloadedBitmap = bmp
                        updateButtonState(isDownloaded = true)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun setupUI() {
        val wp = wallpaper ?: return
        binding.pbCreate.visibility = View.GONE

        // Load preview
        loadPreviewImage()

        // Setup Download / Set Wallpaper action button
        val initiallyDownloaded = isInitiallyDownloaded || (activity?.intent?.getBooleanExtra("is_downloaded", false) == true)
        if (initiallyDownloaded) {
            updateButtonState(isDownloaded = true)
            preloadWallpaperBitmap(wp)
        } else {
            updateButtonState(isDownloaded = downloadedBitmap != null)
        }

        binding.btnAction.setOnClickListener {
            val bmp = downloadedBitmap
            if (bmp != null) {
                showSetWallpaperSheet(bmp)
            } else if (isInitiallyDownloaded || (activity?.intent?.getBooleanExtra("is_downloaded", false) == true)) {
                binding.pbCreate.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    val downloadUri = wp.getOnlineImageUri(requireContext()).toString().ifEmpty {
                        "file:///android_asset/assets_collection/theme/theme_1/wallpapers/bg_wallpaper.png"
                    }
                    val loadedBmp = try {
                        Glide.with(requireContext().applicationContext)
                            .asBitmap()
                            .load(downloadUri)
                            .submit()
                            .get()
                    } catch (e: Exception) {
                        null
                    }
                    withContext(Dispatchers.Main) {
                        _binding?.pbCreate?.visibility = View.GONE
                        if (!isAdded || isStateSaved) return@withContext
                        if (loadedBmp != null) {
                            downloadedBitmap = loadedBmp
                            showSetWallpaperSheet(loadedBmp)
                        } else {
                            downloadWallpaper(wp)
                        }
                    }
                }
            } else {
                downloadWallpaper(wp)
            }
        }
    }

    private fun updateButtonState(isDownloaded: Boolean) {
        val currentBinding = _binding ?: return
        if (isDownloaded) {
            currentBinding.tvAction.text = getString(R.string.set_wallpaper)
            currentBinding.btnAction.setBackgroundResource(R.drawable.bg_btn_theme_set_wallpaper)
            currentBinding.tvAction.setTextColor(android.graphics.Color.WHITE)
        } else {
            currentBinding.tvAction.text = getString(R.string.download)
            currentBinding.btnAction.setBackgroundResource(R.drawable.bg_btn_theme_download_outline)
            val ctx = context
            if (ctx != null) {
                currentBinding.tvAction.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.brand_primary))
            }
        }
    }

    private fun downloadWallpaper(wp: WidgetThemeWallpaper) {
        val ctx = context ?: return
        val downloadDialog = DownloadDialogFragment()
        val downloadUri = wp.getOnlineImageUri(ctx).toString().ifEmpty {
            "file:///android_asset/assets_collection/theme/theme_1/wallpapers/bg_wallpaper.png"
        }
        downloadDialog.setParams(downloadUri, object : DownloadDialogFragment.DownloadCallback {
            override fun onDownloadComplete(bitmap: Bitmap) {
                downloadedBitmap = bitmap
                updateButtonState(isDownloaded = true)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val currentContext = context?.applicationContext ?: return@launch
                        val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(currentContext)
                        val effectivePath = if (theme.path.isNotBlank()) theme.path else "assets_collection/theme/theme_1"
                        val previewUrl = theme.previewPath?.takeIf { it.isNotBlank() }
                            ?: theme.thumbPath?.takeIf { it.isNotBlank() }
                            ?: effectivePath

                        val wpId = wallpaper?.id?.ifEmpty { "wp_${theme.id.ifEmpty { effectivePath.replace('/', '_') }}" }
                            ?: "wp_${theme.id.ifEmpty { effectivePath.replace('/', '_') }}"
                        val wpName = wallpaper?.name?.takeIf { it.isNotBlank() } ?: theme.name
                        val wpTarget = wallpaper?.folder?.takeIf { it.isNotBlank() }
                            ?: wallpaper?.getOnlineImageUri(currentContext).toString().takeIf { it.isNotBlank() }
                            ?: effectivePath
                        val wpPreview = wallpaper?.getOnlinePreviewUri(currentContext)?.toString()?.takeIf { it.isNotBlank() }
                            ?: previewUrl

                        repo.markAsDownloaded(
                            id = wpId,
                            name = wpName,
                            category = "Wallpaper",
                            targetPath = wpTarget,
                            previewPath = wpPreview,
                            rawType = "collection_wallpaper"
                        )
                        repo.markAsDownloaded(
                            id = theme.id.ifEmpty { "theme_${effectivePath.replace('/', '_')}" },
                            name = theme.name,
                            category = "Theme",
                            targetPath = effectivePath,
                            previewPath = previewUrl,
                            rawType = "collection_theme"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onDownloadFailed() {
                if (isAdded && context != null) {
                    Toast.makeText(context, getString(R.string.toast_failed_download_wallpaper), Toast.LENGTH_SHORT).show()
                }
            }
        })
        try {
            val fm = parentFragmentManager
            if (!fm.isStateSaved) {
                downloadDialog.show(fm, "download")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSetWallpaperSheet(bitmap: Bitmap) {
        val sheet = DownloadThemeWallpaperBottomSheet()
        sheet.setCallback(object : DownloadThemeWallpaperBottomSheet.Callback {
            override fun onApply(flag: Int) {
                applyWallpaper(bitmap, flag)
            }
        })
        try {
            val fm = childFragmentManager
            if (!fm.isStateSaved) {
                sheet.show(fm, "set_wallpaper")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyWallpaper(bitmap: Bitmap, flag: Int) {
        _binding?.pbCreate?.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val ctx = context ?: return@launch
                val wm = WallpaperManager.getInstance(ctx)
                if (flag == 0) {
                    // System and Lock
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } else {
                    wm.setBitmap(bitmap, null, true, flag)
                }
                val currentContext = context?.applicationContext ?: return@launch
                val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(currentContext)
                val effectivePath = if (theme.path.isNotBlank()) theme.path else "assets_collection/theme/theme_1"
                val previewUrl = theme.previewPath?.takeIf { it.isNotBlank() }
                    ?: theme.thumbPath?.takeIf { it.isNotBlank() }
                    ?: effectivePath

                val wpId = wallpaper?.id?.ifEmpty { "wp_${theme.id.ifEmpty { effectivePath.replace('/', '_') }}" }
                    ?: "wp_${theme.id.ifEmpty { effectivePath.replace('/', '_') }}"
                val wpName = wallpaper?.name?.takeIf { it.isNotBlank() } ?: theme.name
                val wpTarget = wallpaper?.folder?.takeIf { it.isNotBlank() }
                    ?: wallpaper?.getOnlineImageUri(currentContext).toString().takeIf { it.isNotBlank() }
                    ?: effectivePath
                val wpPreview = wallpaper?.getOnlinePreviewUri(currentContext)?.toString()?.takeIf { it.isNotBlank() }
                    ?: previewUrl

                repo.markAsDownloaded(
                    id = wpId,
                    name = wpName,
                    category = "Wallpaper",
                    targetPath = wpTarget,
                    previewPath = wpPreview,
                    rawType = "collection_wallpaper"
                )
                repo.markAsDownloaded(
                    id = theme.id.ifEmpty { "theme_${effectivePath.replace('/', '_')}" },
                    name = theme.name,
                    category = "Theme",
                    targetPath = effectivePath,
                    previewPath = previewUrl,
                    rawType = "collection_theme"
                )
                withContext(Dispatchers.Main) {
                    _binding?.pbCreate?.visibility = View.GONE
                    if (isAdded && !isStateSaved) {
                        try {
                            val fm = parentFragmentManager
                            if (!fm.isStateSaved) {
                                SetupSucceedDialogFragment().show(fm, "success")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _binding?.pbCreate?.visibility = View.GONE
                    if (isAdded && context != null) {
                        Toast.makeText(context, getString(R.string.toast_failed_apply_wallpaper), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
