package com.widget.themes.controlcenter.feature_collections

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.utils.SystemUtil
import com.widget.themes.controlcenter.databinding.ActivityDownloadedBinding
import com.widget.themes.controlcenter.feature_collections.adapter.CollectionDiscoveryAdapter
import com.widget.themes.controlcenter.feature_collections.adapter.CollectionTabAdapter
import com.widget.themes.controlcenter.feature_collections.data.CollectionItem
import com.widget.themes.controlcenter.feature_collections.data.CollectionRepository
import com.widget.themes.controlcenter.feature_collections.data.CollectionTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadedActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        SystemUtil.setLocale(newBase)
        super.attachBaseContext(newBase)
    }

    private lateinit var binding: ActivityDownloadedBinding
    private lateinit var repository: CollectionRepository
    private lateinit var tabAdapter: CollectionTabAdapter
    private lateinit var contentAdapter: CollectionDiscoveryAdapter

    private var currentCategory: String = "Theme"
    private var tabs: List<CollectionTab> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = CollectionRepository(this)
        currentCategory = intent.getStringExtra(EXTRA_CATEGORY) ?: "Theme"

        initViews()
        setupAdapters()
        setupListeners()
        initTabs()
        observeDownloadedItems()
    }

    private fun initViews() {
        binding.tvTitle.text = getString(R.string.downloaded_title)
    }

    private fun setupAdapters() {
        tabAdapter = CollectionTabAdapter(emptyList()) { selectedTab ->
            selectTab(selectedTab.name)
        }
        binding.rvCategoryTabs.adapter = tabAdapter

        contentAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(this, item, isDownloaded = true)
        }
        val gridLayoutManager = GridLayoutManager(this, 3)
        val spanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val lm = binding.rvDownloadedGrid.layoutManager as? GridLayoutManager ?: return 1
                val spanCount = lm.spanCount
                if (spanCount <= 1) return 1
                val item = contentAdapter.getItem(position)
                val cat = item?.category?.lowercase() ?: ""
                return when {
                    cat.contains("icon") -> 1
                    cat.contains("widget") -> {
                        val is4x2 = item?.extra == "4x2" || item?.extra == "4x4" || item?.id?.contains("4x2") == true || item?.rawType?.contains("4x2") == true || item?.rawType?.contains("checklist") == true || item?.rawType?.contains("countdown") == true
                        if (is4x2) minOf(2, spanCount) else 1
                    }
                    else -> 1
                }
            }
        }
        spanLookup.isSpanIndexCacheEnabled = false
        gridLayoutManager.spanSizeLookup = spanLookup
        binding.rvDownloadedGrid.layoutManager = gridLayoutManager

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        while (binding.rvDownloadedGrid.itemDecorationCount > 0) {
            binding.rvDownloadedGrid.removeItemDecorationAt(0)
        }
        binding.rvDownloadedGrid.addItemDecoration(CollectionsFragment.GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))

        binding.rvDownloadedGrid.adapter = contentAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnExplore.setOnClickListener {
            val targetTab = when {
                currentCategory.contains("theme", ignoreCase = true) -> 0
                currentCategory.contains("icon", ignoreCase = true) -> 1
                currentCategory.contains("control", ignoreCase = true) -> 2
                currentCategory.contains("widget", ignoreCase = true) -> 3
                else -> -1
            }
            if (targetTab != -1) {
                val resultIntent = Intent().apply {
                    putExtra("target_main_tab", targetTab)
                    putExtra(EXTRA_CATEGORY, currentCategory)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                if (isTaskRoot) {
                    val intent = Intent(this, com.widget.themes.controlcenter.feature_main.MainActivity::class.java).apply {
                        putExtra("target_tab", targetTab)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }
                finish()
            } else {
                finish()
            }
        }
    }

    private fun initTabs() {
        tabs = CollectionRepository.TABS.map { name ->
            CollectionTab(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isSelected = name.equals(currentCategory, ignoreCase = true)
            )
        }
        tabAdapter.updateTabs(tabs)
        updateEmptyStateTitle(currentCategory)
        updateLayoutForCategory(currentCategory)
    }

    private fun selectTab(categoryName: String) {
        currentCategory = categoryName
        tabs = tabs.map { it.copy(isSelected = it.name.equals(categoryName, ignoreCase = true)) }
        tabAdapter.updateTabs(tabs)
        updateEmptyStateTitle(categoryName)
        updateLayoutForCategory(categoryName)
        observeDownloadedItems()
    }

    private fun updateLayoutForCategory(categoryName: String) {
        val catLower = categoryName.lowercase()
        val spanCount = when {
            catLower.contains("icon") -> 1
            catLower.contains("widget") -> 2
            else -> 3
        }

        (binding.rvDownloadedGrid.layoutManager as? GridLayoutManager)?.spanCount = spanCount

        while (binding.rvDownloadedGrid.itemDecorationCount > 0) {
            binding.rvDownloadedGrid.removeItemDecorationAt(0)
        }
        val spacingHorizontal = if (spanCount == 1) 0 else resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        binding.rvDownloadedGrid.addItemDecoration(
            CollectionsFragment.GridSpacingItemDecoration(spanCount, spacingHorizontal, spacingVertical, false)
        )
    }

    private fun updateEmptyStateTitle(categoryName: String) {
        val localizedCategory = CollectionRepository.getLocalizedCategoryName(this, categoryName)
        binding.tvEmptySubtitle.text = getString(R.string.downloaded_category_desc, localizedCategory)
    }

    private fun observeDownloadedItems() {
        lifecycleScope.launch {
            repository.getDownloadedItemsFlow(currentCategory).collectLatest { items ->
                if (items.isEmpty()) {
                    binding.rvDownloadedGrid.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    val isWp = currentCategory.contains("wallpaper", ignoreCase = true)
                    binding.btnExplore.visibility = if (isWp) View.GONE else View.VISIBLE
                } else {
                    binding.rvDownloadedGrid.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    val collectionItems = items.map { entity ->
                        var preview = entity.previewPath
                        val isIcon = entity.category.contains("Icon", ignoreCase = true)
                        val isTheme = entity.category.contains("Theme", ignoreCase = true)
                        val isWidget = entity.category.contains("Widget", ignoreCase = true)
                        val isWallpaper = entity.category.contains("Wallpaper", ignoreCase = true)

                        if (isIcon) {
                            if (preview.isEmpty() && entity.targetPath.isNotBlank()) {
                                preview = entity.targetPath
                            }
                            if (preview.startsWith("file:///android_asset/") || !preview.startsWith("http")) {
                                if (entity.targetPath.startsWith("http")) {
                                    preview = entity.targetPath
                                } else {
                                    val cleanId = entity.targetPath.substringAfterLast("/").substringBeforeLast(".").ifEmpty { entity.id.removePrefix("icon_").removePrefix("theme_") }
                                    try {
                                        val iconRepo = com.widget.themes.controlcenter.feature_icon.data.IconRepository(this@DownloadedActivity)
                                        val detail = iconRepo.getIconPackDetail(cleanId) ?: iconRepo.getIconPackDetail("theme_$cleanId")
                                        var remote = detail?.backgroundIconUrl?.takeIf { it.startsWith("http") }
                                        if (remote == null) {
                                            val packs = iconRepo.getIconPacks()
                                            remote = packs.firstOrNull { it.id.contains(cleanId, ignoreCase = true) || it.name.equals(entity.name, ignoreCase = true) }?.folder
                                                ?: packs.firstOrNull()?.folder
                                        }
                                        if (remote != null) {
                                            preview = remote
                                            repository.markAsDownloaded(
                                                id = entity.id,
                                                name = entity.name,
                                                category = "Icons",
                                                targetPath = entity.targetPath,
                                                previewPath = remote,
                                                downloads = entity.downloads,
                                                rawType = entity.rawType,
                                                extra = entity.extra
                                            )
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                            }
                        } else if (isTheme) {
                            if (preview.isEmpty() && entity.targetPath.isNotBlank()) {
                                preview = entity.targetPath
                            }
                            if (preview.startsWith("file:///android_asset/") || !preview.startsWith("http")) {
                                if (entity.targetPath.startsWith("http")) {
                                    preview = entity.targetPath
                                } else {
                                    val cleanId = entity.targetPath.substringAfterLast("/").ifEmpty { entity.id.removePrefix("theme_") }
                                    try {
                                        val detail = com.widget.themes.controlcenter.core.di.ServiceLocator.getThemeRepository(this@DownloadedActivity).getThemeDetail(cleanId)
                                        val remote = detail?.previewUrl?.takeIf { it.startsWith("http") }
                                        if (remote != null) {
                                            preview = remote
                                            repository.markAsDownloaded(
                                                id = entity.id,
                                                name = entity.name,
                                                category = "Theme",
                                                targetPath = entity.targetPath,
                                                previewPath = remote,
                                                downloads = entity.downloads,
                                                rawType = entity.rawType,
                                                extra = entity.extra
                                            )
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                            }
                        } else if (isWallpaper) {
                            if (preview.contains("bg_preview.png")) {
                                preview = preview.replace("bg_preview.png", "bg_wallpaper.png")
                            } else if (preview.contains("preview.png")) {
                                preview = preview.replace("preview.png", "wallpaper.png")
                            }
                            if (entity.targetPath.startsWith("http") && (preview.startsWith("file://") || !preview.startsWith("http"))) {
                                preview = entity.targetPath
                            }
                        } else if (isWidget) {
                            if (entity.targetPath.startsWith("http") && (preview.isEmpty() || !preview.startsWith("http"))) {
                                preview = entity.targetPath
                            }
                            if (!preview.startsWith("http")) {
                                val isMedium = entity.extra == "4x2" || entity.extra == "4x4" || entity.rawType.contains("4x2") || entity.id.contains("4x2")
                                val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"
                                if (preview.contains("clock_") || preview.contains("calendar_") || preview.contains("clocks/") || preview.contains("today/")) {
                                    val folder = preview.substringBeforeLast("/")
                                    preview = "$folder/$bgFileName"
                                } else if (preview.isEmpty() || !preview.contains(".png")) {
                                    val cleanTarget = if (entity.targetPath.isNotBlank()) entity.targetPath.removePrefix("/") else "assets_collection/theme/theme_1"
                                    preview = "file:///android_asset/$cleanTarget/widgets/$bgFileName"
                                }
                            }
                        }

                        val resolvedCategory = when {
                            isTheme -> "Theme"
                            isIcon -> "Icons"
                            isWidget -> "Widget"
                            isWallpaper -> "Wallpaper"
                            else -> entity.category
                        }

                        val resolvedRawType = when {
                            isTheme -> "collection_theme"
                            isIcon -> "collection_icon"
                            else -> entity.rawType
                        }

                        val resolvedDownloads = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(
                            entity.downloads,
                            entity.id.ifEmpty { entity.name }
                        )

                        CollectionItem(
                            id = entity.id,
                            name = entity.name,
                            category = resolvedCategory,
                            targetPath = entity.targetPath,
                            previewPath = preview,
                            downloads = resolvedDownloads,
                            rawType = resolvedRawType,
                            extra = entity.extra
                        )
                    }
                    contentAdapter.submitList(collectionItems)
                }
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }
}
