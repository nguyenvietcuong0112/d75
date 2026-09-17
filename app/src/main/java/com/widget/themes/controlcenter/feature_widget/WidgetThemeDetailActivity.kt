package com.widget.themes.controlcenter.feature_widget

import android.app.AlertDialog
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.feature_widget.WidgetTheme
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.utils.IconComposerHelper
import com.widget.themes.controlcenter.databinding.ActivityWidgetThemeDetailBinding
import com.widget.themes.controlcenter.feature_widget.ThemeIconItem
import com.widget.themes.controlcenter.feature_widget.Widget2x2Provider
import com.widget.themes.controlcenter.feature_widget.Widget4x2Provider
import com.widget.themes.controlcenter.feature_widget.Widget4x4Provider
import com.widget.themes.controlcenter.core.ui.RemoteImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetThemeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetThemeDetailBinding
    private lateinit var theme: WidgetTheme
    private val iconIds = listOf(
        "phone", "phonebook", "calendar", "weather", "camera", "calculator",
        "setting", "gallery", "chrome", "facebook", "tiktok", "instagram",
        "twitch", "binance", "healthy", "record"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetThemeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        theme = intent.getSerializableExtra("widget_theme") as? WidgetTheme
            ?: WidgetTheme("widget_theme_category_Animal_theme_1", "Cat & Drops", "category/Animal/theme_1")

        initViews()
    }

    private fun initViews() {
        binding.tvTitle.text = theme.name
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 1. Load Background Theme Preview (Composite Mockup)
        val previewUrl = ResourceConfig.getThemePreviewUrl(theme.folder)
        Glide.with(this)
            .load(previewUrl)
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivWallpaper)

        val wallpaperUrl = ResourceConfig.getWallpaperFullUrl(theme.folder, "bg_wallpaper")

        // 2. Load Widget Preview Card
        val widgetBgUrl = ResourceConfig.getWidgetPreviewUrl(theme.folder, "medium")
        Glide.with(this)
            .load(widgetBgUrl)
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivWidgetBg)

        // 3. Load Horizontal Icon Preview List
        binding.rvIcons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val discoveredIcons = getDiscoveredIconList()
        binding.rvIcons.adapter = HorizontalIconsAdapter(this, theme.folder, discoveredIcons)

        // Action click handlers
        binding.btnWallpaper.setOnClickListener {
            showWallpaperChooserDialog(wallpaperUrl)
        }

        binding.btnIcons.setOnClickListener {
            showIconsSelectionDialog()
        }

        binding.btnWidget.setOnClickListener {
            showWidgetChooserDialog()
        }

        binding.cardWidget.setOnClickListener {
            showWidgetChooserDialog()
        }
    }

    // --- Wallpaper Flow ---
    private fun showWallpaperChooserDialog(url: String) {
        val options = arrayOf(
            getString(R.string.home_screen),
            getString(R.string.lock_screen),
            getString(R.string.both_screens)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.apply_wallpaper)
            .setItems(options) { _, which ->
                applyWallpaper(url, which)
            }
            .show()
    }

    private fun applyWallpaper(url: String, type: Int) {
        Toast.makeText(this, getString(R.string.toast_downloading_wallpaper), Toast.LENGTH_SHORT).show()
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val wm = WallpaperManager.getInstance(this@WidgetThemeDetailActivity)
                            when (type) {
                                0 -> wm.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                                1 -> wm.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                                2 -> {
                                    wm.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                                    wm.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WidgetThemeDetailActivity, getString(R.string.toast_wallpaper_applied_success), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WidgetThemeDetailActivity, getString(R.string.toast_failed_apply_wallpaper), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    // --- Icon Pack Selection Dialog ---
    private fun showIconsSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_selection, null)
        val rvItems = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        rvItems.layoutManager = LinearLayoutManager(this)

        val appMappings = getAppMappings()
        val adapter = IconSelectionAdapter(this, theme.folder, appMappings)
        rvItems.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_select_icons_to_install)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_install_pinned_shortcuts) { _, _ ->
                installSelectedShortcuts(adapter.getSelectedItems())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun installSelectedShortcuts(items: List<IconSelectionItem>) {
        if (items.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_no_icons_selected), Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, getString(R.string.toast_adding_shortcuts), Toast.LENGTH_SHORT).show()
        for (item in items) {
            Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        try {
                            val pm = packageManager
                            val intent = pm.getLaunchIntentForPackage(item.packageName) ?: Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                setPackage(item.packageName)
                            }
                            
                            val finalIconBmp = IconComposerHelper.prepareShortcutBitmap(resource, density = resources.displayMetrics.density)
                            val shortcut = ShortcutInfoCompat.Builder(this@WidgetThemeDetailActivity, item.id)
                                .setShortLabel(item.customName)
                                .setIcon(IconCompat.createWithBitmap(finalIconBmp))
                                .setIntent(intent)
                                .build()

                            ShortcutManagerCompat.requestPinShortcut(this@WidgetThemeDetailActivity, shortcut, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    // --- Widget Flow ---
    private fun showWidgetChooserDialog() {
        val options = arrayOf(
            getString(R.string.widget_clock_2x2),
            getString(R.string.widget_weather_4x2),
            getString(R.string.widget_calendar_4x4)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_widget_title)
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> "clock"
                    1 -> "weather"
                    else -> "calendar"
                }
                val size = when (which) {
                    0 -> "2x2"
                    1 -> "4x2"
                    else -> "4x4"
                }
                
                val typeFolder = when (type) {
                    "clock" -> "clocks"
                    "calendar" -> "today"
                    "weather" -> "weather"
                    else -> type
                }
                val isMedium = size == "4x2"
                val previewFileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
                val previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/${theme.folder}/widgets/$typeFolder/$previewFileName"
                val themeItem = com.widget.themes.controlcenter.core.data.entity.ThemeItem(
                    id = theme.id,
                    name = theme.name,
                    path = theme.folder
                )

                val sheet = SelectWidgetBottomSheet()
                sheet.setParams(
                    theme = themeItem,
                    widgetType = type,
                    size = size,
                    previewUrl = previewUrl
                )
                sheet.show(supportFragmentManager, "select_widget")
            }
            .show()
    }

    private fun getDiscoveredIconList(): List<String> {
        val themeFolder = theme.folder
        val discoveredList = mutableListOf<String>()
        val candidateDirs = listOf(
            "assets_theme/$themeFolder/icons",
            "assets_theme/category/$themeFolder/icons",
            "assets_collection/theme/$themeFolder/icons",
            "assets_collection/icons/$themeFolder",
            "$themeFolder/icons",
            themeFolder
        )
        for (dir in candidateDirs) {
            try {
                val cleanDir = dir.removePrefix("file:///android_asset/").removePrefix("android_asset/").removePrefix("/")
                val files = assets.list(cleanDir)
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (file.endsWith(".png") || file.endsWith(".webp")) {
                            val name = file.substringBeforeLast(".")
                            if (!name.equals("bg_icon", true) && !name.equals("preview", true) && !name.equals("bg_preview", true)) {
                                if (!discoveredList.contains(name)) {
                                    discoveredList.add(name)
                                }
                            }
                        }
                    }
                    if (discoveredList.isNotEmpty()) break
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (discoveredList.isEmpty()) {
            discoveredList.addAll(iconIds)
        }
        return discoveredList
    }

    private fun getAppMappings(): List<IconSelectionItem> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps = pm.queryIntentActivities(mainIntent, 0)
        val discovered = getDiscoveredIconList()

        return discovered.mapNotNull { rawName ->
            val matchedApp = com.widget.themes.controlcenter.core.data.AppMatcherHelper.resolveAppForIcon(this, rawName, installedApps)
            val pkg = matchedApp?.packageName ?: return@mapNotNull null
            val appName = matchedApp.appName
            val imageUrl = ResourceConfig.getSingleIconUrl(theme.folder, rawName)
            IconSelectionItem(rawName, appName, appName, pkg, imageUrl, true)
        }
    }

    // --- Inner Horizontal Icons Adapter ---
    class HorizontalIconsAdapter(
        private val context: Context,
        private val folder: String,
        private val iconIds: List<String>
    ) : RecyclerView.Adapter<HorizontalIconsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_widget_theme_detail_icon, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iconId = iconIds[position]
            val iconUrl = ResourceConfig.getSingleIconUrl(folder, iconId)
            Glide.with(context)
                .load(iconUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivIcon)
        }

        override fun getItemCount(): Int = iconIds.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: RemoteImageView = view.findViewById(R.id.ivIcon)
        }
    }

    // --- Inner Selection Dialog Classes ---
    data class IconSelectionItem(
        val id: String,
        val defaultName: String,
        var customName: String,
        val packageName: String,
        val imageUrl: String,
        var isSelected: Boolean
    )

    class IconSelectionAdapter(
        private val context: Context,
        private val folder: String,
        private val list: List<IconSelectionItem>
    ) : RecyclerView.Adapter<IconSelectionAdapter.ViewHolder>() {

        fun getSelectedItems() = list.filter { it.isSelected }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_dialog_icon_select, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.cbSelect.isChecked = item.isSelected
            holder.etName.setText(item.customName)

            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivIcon)

            holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
            }

            holder.etName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    item.customName = holder.etName.text.toString()
                }
            }
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
            val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
            val etName: EditText = view.findViewById(R.id.etName)
        }
    }
}
