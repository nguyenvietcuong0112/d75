package com.widget.themes.controlcenter.feature_icon

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.AppItemData
import com.widget.themes.controlcenter.core.utils.IconComposerHelper
import com.widget.themes.controlcenter.databinding.ActivityDownloadIconBinding
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.feature_widget.DownloadIconItemAdapter
import com.widget.themes.controlcenter.feature_widget.SelectIconBottomSheet
import com.widget.themes.controlcenter.feature_widget.ThemeIconItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadIconActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadIconBinding
    private lateinit var theme: ThemeItem

    private val iconItems = mutableListOf<ThemeIconItem>()
    private lateinit var adapter: DownloadIconItemAdapter
    
    private var isAllSelected = true
    private var defaultBgBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadIconBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeId = intent.getStringExtra("theme_id") ?: ""
        val themeName = intent.getStringExtra("theme_name") ?: "Default Theme"
        val rawThemePath = intent.getStringExtra("theme_path") ?: ""
        val themeType = intent.getStringExtra("theme_type") ?: "default"
        val safePath = if (rawThemePath.isNotBlank()) {
            rawThemePath
        } else when {
            themeName.contains("Dark Moon", ignoreCase = true) -> "assets_collection/icons/icon_1"
            themeName.contains("Matcha Latte", ignoreCase = true) -> "assets_collection/icons/icon_2"
            themeName.contains("Dreamy Bear", ignoreCase = true) -> "assets_collection/icons/icon_3"
            else -> "assets_collection/icons/icon_1"
        }
        theme = ThemeItem(id = themeId.ifEmpty { "icon_${safePath.replace('/', '_')}" }, name = themeName, path = safePath, rawType = themeType)

        if (themeId.isNotEmpty()) {
            com.widget.themes.controlcenter.core.network.ViewTracker.track("icon", themeId)
        }

        initToolbar()
        
        binding.pbCreate.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    initPresetIcons()
                }
                setupRecyclerView()
                setupHeader()
                setupActions()
                binding.llHeader.visibility = View.VISIBLE
                binding.actionView.root.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DownloadIconActivity, getString(R.string.toast_error_loading_icons), Toast.LENGTH_SHORT).show()
            } finally {
                binding.pbCreate.visibility = View.GONE
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.titleTextView.text = getString(R.string.install_icons)
        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        binding.toolbar.ivBack.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        com.widget.themes.controlcenter.core.ads.AdsManager.showCollapsibleNative(
            activity = this,
            container = binding.layoutAds,
            adUnitId = getString(R.string.native_collap),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_collap
        )
    }

    private suspend fun initPresetIcons() {
        val pm = packageManager
        iconItems.clear()

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps = pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != packageName }
        val matchedPackages = mutableSetOf<String>()

        val targetId = if (theme.id.isNotEmpty() && !theme.id.startsWith("http")) theme.id else theme.path
        try {
            val repo = com.widget.themes.controlcenter.feature_icon.data.IconRepository(this)
            var remoteDetail = repo.getIconPackDetail(targetId)
            if (remoteDetail == null && (theme.name.isNotBlank() || theme.path.isNotBlank())) {
                try {
                    val packs = repo.getIconPacks()
                    val match = packs.firstOrNull {
                        it.id.equals(theme.id, true) ||
                        it.name.equals(theme.name, true) ||
                        it.folder.contains(theme.name, true) ||
                        it.folder.equals(theme.path, true)
                    }
                    if (match != null) {
                        remoteDetail = repo.getIconPackDetail(match.id)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
            val remoteItems = remoteDetail?.allIcons
            if (!remoteItems.isNullOrEmpty()) {
                for (item in remoteItems) {
                    val rawIconName = item.name.ifEmpty { item.appName }
                    val normalizedName = rawIconName.removePrefix("ic_").lowercase()
                    val matchedApp = com.widget.themes.controlcenter.core.data.AppMatcherHelper.resolveAppForIcon(
                        this,
                        rawIconName,
                        installedApps
                    )

                    var targetPkg: String? = matchedApp?.packageName ?: item.packageCandidates.firstOrNull { pkg ->
                        try { pm.getPackageInfo(pkg, 0); true } catch (e: Exception) { false }
                    }
                    var targetAppName: String? = matchedApp?.appName ?: item.label.ifEmpty { rawIconName }
                    var targetIcon: android.graphics.drawable.Drawable? = if (targetPkg != null) {
                        try { pm.getApplicationIcon(targetPkg) } catch (e: Exception) { null }
                    } else null

                    val isMatched = !targetPkg.isNullOrEmpty()
                    if (isMatched && targetPkg != null) {
                        matchedPackages.add(targetPkg)
                    }
                    val displayName = targetAppName ?: item.label.ifEmpty { rawIconName }
                    val itemId = "${theme.id}_$normalizedName"
                    val isUnlocked = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.isIconUnlocked(
                        context = this,
                        itemId = itemId,
                        themeId = theme.id
                    )

                    iconItems.add(
                        ThemeIconItem(
                            id = itemId,
                            iconName = rawIconName,
                            assetPath = item.iconUrl,
                            targetPackageName = targetPkg,
                            targetAppName = if (isMatched) displayName else null,
                            targetAppIcon = targetIcon,
                            isSelected = isMatched,
                            isUnlocked = isUnlocked
                        )
                    )
                }

                // Load default background frame plate if available
                val defaultBgUrl = remoteDetail.backgroundIconDefaultUrl
                if (!defaultBgUrl.isNullOrEmpty()) {
                    defaultBgBitmap = try {
                        Glide.with(applicationContext)
                            .asBitmap()
                            .load(defaultBgUrl)
                            .submit()
                            .get()
                    } catch (e: Exception) {
                        null
                    }
                }

                // Generate composite themed icons for all remaining installed apps on device
                val bgBmp = defaultBgBitmap
                if (bgBmp != null) {
                    for (app in installedApps) {
                        val pkg = app.activityInfo.packageName
                        if (pkg == packageName || matchedPackages.contains(pkg)) {
                            continue
                        }
                        val appLabel = app.loadLabel(pm).toString()
                        val appIcon = try {
                            app.loadIcon(pm)
                        } catch (e: Exception) {
                            null
                        } ?: continue

                        val compositeBmp = IconComposerHelper.composeThemedIcon(bgBmp, appIcon)
                        val itemId = "${theme.id}_composite_$pkg"
                        val isUnlocked = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.isIconUnlocked(
                            context = this,
                            itemId = itemId,
                            themeId = theme.id
                        )

                        iconItems.add(
                            ThemeIconItem(
                                id = itemId,
                                iconName = appLabel,
                                assetPath = "",
                                targetPackageName = pkg,
                                targetAppName = appLabel,
                                targetAppIcon = appIcon,
                                isSelected = false,
                                isUnlocked = isUnlocked,
                                customBitmap = compositeBmp,
                                isGenerated = true
                            )
                        )
                        matchedPackages.add(pkg)
                    }
                }

                if (iconItems.isNotEmpty()) {
                    iconItems.sortWith(
                        compareByDescending<ThemeIconItem> { !it.isGenerated && it.targetAppIcon != null }
                            .thenByDescending { it.isGenerated }
                            .thenByDescending { it.targetAppIcon != null }
                            .thenBy { it.iconName }
                    )
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fallback: Local Asset Scanning
        val themePath = theme.path
            .removePrefix("file:///android_asset/")
            .removePrefix("file://android_asset/")
            .removePrefix("android_asset/")
            .removePrefix("/")

        val discoveredIconNames = mutableListOf<String>()

        // Check asset directories for icons
        val candidateDirs = listOf(
            themePath,
            "$themePath/icons",
            "assets_collection/icons/$themePath",
            "assets_collection/theme/$themePath/icons",
            "assets_theme/category/$themePath/icons",
            "assets_theme/$themePath/icons",
            "assets_collection/theme/theme_1/icons",
            "assets_collection/icons/icon_1",
            "assets_theme/category/Trending/theme_1/icons"
        )

        var matchedDir: String? = null
        for (dir in candidateDirs) {
            try {
                val cleanDir = dir.removePrefix("file:///android_asset/").removePrefix("android_asset/").removePrefix("/")
                val files = assets.list(cleanDir)
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (file.endsWith(".png") || file.endsWith(".webp")) {
                            val iconName = file.substringBeforeLast(".")
                            if (iconName.equals("bg_icon", true) || iconName.equals("preview", true) || iconName.equals("bg_preview", true)) {
                                continue
                            }
                            if (!discoveredIconNames.contains(iconName)) {
                                discoveredIconNames.add(iconName)
                            }
                        }
                    }
                    if (discoveredIconNames.isNotEmpty()) {
                        matchedDir = cleanDir
                        break
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Fallback default list if no asset folder found
        if (discoveredIconNames.isEmpty()) {
            val fallbackAssetsDir = listOf(
                "assets_collection/theme/theme_1/icons",
                "assets_collection/icons/icon_1",
                "assets_theme/category/Trending/theme_1/icons"
            )
            for (fallbackDir in fallbackAssetsDir) {
                try {
                    val files = assets.list(fallbackDir)
                    if (!files.isNullOrEmpty()) {
                        matchedDir = fallbackDir
                        for (file in files) {
                            if (file.endsWith(".png") || file.endsWith(".webp")) {
                                val iconName = file.substringBeforeLast(".")
                                if (iconName.equals("bg_icon", true) || iconName.equals("preview", true) || iconName.equals("bg_preview", true)) {
                                    continue
                                }
                                if (!discoveredIconNames.contains(iconName)) {
                                    discoveredIconNames.add(iconName)
                                }
                            }
                        }
                        if (discoveredIconNames.isNotEmpty()) break
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        for (rawIconName in discoveredIconNames) {
            val normalizedName = rawIconName.removePrefix("ic_").lowercase()
            val matchedApp = com.widget.themes.controlcenter.core.data.AppMatcherHelper.resolveAppForIcon(this, rawIconName, installedApps)
            val targetPkg: String? = matchedApp?.packageName
            val targetAppName: String? = matchedApp?.appName
            val targetIcon: android.graphics.drawable.Drawable? = matchedApp?.icon

            val cleanName = rawIconName.removePrefix("ic_")
            val candidateFilePaths = mutableListOf<String>()
            if (matchedDir != null) {
                candidateFilePaths.add("$matchedDir/$rawIconName.png")
                candidateFilePaths.add("$matchedDir/ic_$cleanName.png")
                candidateFilePaths.add("$matchedDir/$cleanName.png")
            }
            candidateFilePaths.add("$themePath/$rawIconName.png")
            candidateFilePaths.add("$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_collection/icons/$themePath/$rawIconName.png")
            candidateFilePaths.add("assets_collection/theme/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_collection/theme/$themePath/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_theme/category/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_theme/category/$themePath/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_theme/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_theme/$themePath/icons/ic_$cleanName.png")
            // Fallbacks
            candidateFilePaths.add("assets_collection/theme/theme_1/icons/$rawIconName.png")
            candidateFilePaths.add("assets_collection/theme/theme_1/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_collection/icons/icon_1/$rawIconName.png")
            candidateFilePaths.add("assets_collection/icons/icon_1/ic_$cleanName.png")
            candidateFilePaths.add("assets_theme/category/Trending/theme_1/icons/$rawIconName.png")
            candidateFilePaths.add("assets_theme/category/Trending/theme_1/icons/ic_$cleanName.png")
            // Theme's bg_icon fallback if specific app icon is not in pack
            if (matchedDir != null) {
                candidateFilePaths.add("$matchedDir/bg_icon.png")
            }
            candidateFilePaths.add("$themePath/icons/bg_icon.png")
            candidateFilePaths.add("$themePath/bg_icon.png")
            candidateFilePaths.add("assets_collection/theme/theme_1/icons/bg_icon.png")
            candidateFilePaths.add("assets_collection/icons/icon_1/bg_icon.png")

            var resolvedAssetPath: String? = null
            for (candidate in candidateFilePaths) {
                try {
                    val stream = assets.open(candidate)
                    stream.close()
                    resolvedAssetPath = candidate
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }
            if (resolvedAssetPath == null) {
                resolvedAssetPath = "assets_collection/theme/theme_1/icons/bg_icon.png"
            }

            val assetPath = "file:///android_asset/$resolvedAssetPath"
            val isMatched = !targetPkg.isNullOrEmpty()
            if (isMatched && targetPkg != null) {
                matchedPackages.add(targetPkg)
            }
            val displayName = targetAppName ?: cleanName

            val itemId = "${theme.id}_$normalizedName"
            val isUnlocked = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.isIconUnlocked(
                context = this,
                itemId = itemId,
                themeId = theme.id
            )

            iconItems.add(
                ThemeIconItem(
                    id = itemId,
                    iconName = rawIconName,
                    assetPath = assetPath,
                    targetPackageName = targetPkg,
                    targetAppName = if (isMatched) displayName else null,
                    targetAppIcon = targetIcon,
                    isSelected = isMatched, // Only select if app is bound
                    isUnlocked = isUnlocked
                )
            )
        }

        if (defaultBgBitmap == null) {
            val candidateBgFiles = listOf(
                if (matchedDir != null) "$matchedDir/bg_icon.png" else "",
                if (matchedDir != null) "$matchedDir/bg_icon_default.png" else "",
                "$themePath/icons/bg_icon.png",
                "$themePath/bg_icon.png",
                "assets_collection/icons/$themePath/bg_icon.png",
                "assets_collection/icons/$themePath/bg_icon_default.png",
                "assets_collection/theme/$themePath/icons/bg_icon.png",
                "assets_theme/category/$themePath/icons/bg_icon.png",
                "assets_theme/$themePath/icons/bg_icon.png",
                "assets_collection/theme/theme_1/icons/bg_icon.png",
                "assets_collection/icons/icon_1/bg_icon.png",
                "assets_theme/category/Trending/theme_1/icons/bg_icon.png"
            )
            for (candidate in candidateBgFiles) {
                if (candidate.isBlank()) continue
                try {
                    val stream = assets.open(candidate)
                    defaultBgBitmap = BitmapFactory.decodeStream(stream)
                    stream.close()
                    if (defaultBgBitmap != null) break
                } catch (e: Exception) {
                    // Try next
                }
            }
        }

        val localBgBmp = defaultBgBitmap
        if (localBgBmp != null) {
            for (app in installedApps) {
                val pkg = app.activityInfo.packageName
                if (pkg == packageName || matchedPackages.contains(pkg)) {
                    continue
                }
                val appLabel = app.loadLabel(pm).toString()
                val appIcon = try {
                    app.loadIcon(pm)
                } catch (e: Exception) {
                    null
                } ?: continue

                val compositeBmp = IconComposerHelper.composeThemedIcon(localBgBmp, appIcon)
                val itemId = "${theme.id}_composite_$pkg"
                val isUnlocked = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.isIconUnlocked(
                    context = this,
                    itemId = itemId,
                    themeId = theme.id
                )

                iconItems.add(
                    ThemeIconItem(
                        id = itemId,
                        iconName = appLabel,
                        assetPath = "",
                        targetPackageName = pkg,
                        targetAppName = appLabel,
                        targetAppIcon = appIcon,
                        isSelected = false,
                        isUnlocked = isUnlocked,
                        customBitmap = compositeBmp,
                        isGenerated = true
                    )
                )
                matchedPackages.add(pkg)
            }
        }

        // Requirement: Sort icons that match installed device apps to the TOP of the list
        iconItems.sortWith(
            compareByDescending<ThemeIconItem> { !it.isGenerated && it.targetAppIcon != null }
                .thenByDescending { it.isGenerated }
                .thenByDescending { it.targetAppIcon != null }
                .thenBy { it.iconName }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DownloadIconItemAdapter(
            items = iconItems,
            onSelectToggle = { item ->
                if (item.targetPackageName.isNullOrEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_select_app_to_bind), Toast.LENGTH_SHORT).show()
                    showAppSelectionDialog(item)
                } else {
                    item.isSelected = !item.isSelected
                    adapter.notifyDataSetChanged()
                    updateSelectAllUI()
                }
            },
            onChangeApp = { item ->
                showAppSelectionDialog(item)
            },
            onInstallClick = { item ->
                installSingleIcon(item)
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun setupHeader() {
        updateSelectAllUI()
        
        binding.ivSelectAll.setOnClickListener {
            val bindableItems = iconItems.filter { !it.targetPackageName.isNullOrEmpty() }
            val hasUnselected = bindableItems.any { !it.isSelected }
            
            for (item in iconItems) {
                if (!item.targetPackageName.isNullOrEmpty()) {
                    item.isSelected = hasUnselected
                } else {
                    item.isSelected = false
                }
            }
            adapter.notifyDataSetChanged()
            updateSelectAllUI()
        }

        binding.llUnlockAll.visibility = View.GONE
    }

    private fun updateSelectAllUI() {
        val bindableItems = iconItems.filter { !it.targetPackageName.isNullOrEmpty() }
        val allSelected = bindableItems.isNotEmpty() && bindableItems.all { it.isSelected }
        isAllSelected = allSelected
        if (allSelected) {
            binding.ivSelectAll.setImageResource(R.drawable.ic_radio_checked)
            binding.ivSelectAll.imageTintList = null
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.bg_circle)
            binding.ivSelectAll.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
        }
    }

    private fun setupActions() {
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = getString(R.string.take_all)

        binding.actionView.clInstall.setOnClickListener {
            val selected = iconItems.filter { it.isSelected && !it.targetPackageName.isNullOrEmpty() }
            if (selected.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_select_at_least_one_icon), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            com.widget.themes.controlcenter.core.ads.AdsManager.showReward(
                activity = this,
                adUnitId = getString(R.string.reward_ad),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.reward_ad,
                onUserEarnedReward = { installSelectedIcons() }
            )
        }
    }

    private fun showAppSelectionDialog(item: ThemeIconItem) {
        val bottomSheet = com.widget.themes.controlcenter.feature_widget.AppSelectionBottomSheet()
        bottomSheet.setParams(
            currentPackage = item.targetPackageName,
            iconName = item.iconName.removePrefix("ic_").replace("_", " ").replaceFirstChar { it.uppercase() },
            onSelected = { resolveInfo ->
                val pm = packageManager
                item.targetPackageName = resolveInfo.activityInfo.packageName
                item.targetAppName = resolveInfo.loadLabel(pm).toString()
                item.targetAppIcon = try { resolveInfo.loadIcon(pm) } catch (e: Exception) { null }
                item.isSelected = true

                val bgBmp = defaultBgBitmap
                if (item.isGenerated && bgBmp != null && item.targetAppIcon != null) {
                    item.customBitmap = IconComposerHelper.composeThemedIcon(bgBmp, item.targetAppIcon!!)
                }
                adapter.notifyDataSetChanged()
                updateSelectAllUI()
            }
        )
        bottomSheet.show(supportFragmentManager, "app_selection")
    }

    private fun installSingleIcon(item: ThemeIconItem) {
        if (item.targetPackageName.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_app_to_bind), Toast.LENGTH_SHORT).show()
            showAppSelectionDialog(item)
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, listOf(item))
        sheet.show(supportFragmentManager, "select_icons")
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected && !it.targetPackageName.isNullOrEmpty() }
        if (selected.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_at_least_one_icon), Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, selected)
        sheet.show(supportFragmentManager, "select_icons")
    }

    override fun onDestroy() {
        com.widget.themes.controlcenter.core.ads.AdsManager.stopCollapsible(this)
        super.onDestroy()
    }
}
