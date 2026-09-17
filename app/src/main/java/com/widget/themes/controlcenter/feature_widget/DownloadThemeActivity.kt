package com.widget.themes.controlcenter.feature_widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.databinding.ActivityDownloadThemeBinding

class DownloadThemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadThemeBinding
    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: ThemeItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(DownloadThemeViewModel::class.java)

        val widgetTheme = intent.getSerializableExtra("widget_theme") as? com.widget.themes.controlcenter.feature_widget.data.entity.WidgetTheme
        val passedThemeItem = intent.getSerializableExtra("theme_item") as? ThemeItem
        val isCustom = intent.getBooleanExtra("is_custom", false)
        val passedWallpaper = intent.getStringExtra("theme_wallpaper")?.takeIf { it.isNotBlank() }

        if (passedThemeItem != null) {
            theme = if (passedThemeItem.backgroundPath.isNullOrBlank() && !passedWallpaper.isNullOrBlank()) {
                passedThemeItem.copy(backgroundPath = passedWallpaper)
            } else {
                passedThemeItem
            }
        } else if (widgetTheme != null) {
            theme = ThemeItem(
                id = widgetTheme.id.toString(),
                name = widgetTheme.name,
                path = widgetTheme.folder,
                rawType = if (widgetTheme.isCustom) "custom" else "widget_theme",
                backgroundPath = passedWallpaper
            )
        } else {
            val themeId = intent.getStringExtra("theme_id") ?: ""
            val themeName = intent.getStringExtra("theme_name") ?: "Default Theme"
            val rawThemePath = intent.getStringExtra("theme_path") ?: ""
            val themeType = intent.getStringExtra("theme_type") ?: "default"
            val passedPreview = intent.getStringExtra("theme_preview")?.takeIf { it.isNotBlank() }
                ?: intent.getStringExtra("theme_thumb")?.takeIf { it.isNotBlank() }
            val safePath = if (rawThemePath.isNotBlank()) {
                rawThemePath
            } else when {
                themeName.equals("Dark Moon", ignoreCase = true) -> "assets_collection/theme/theme_1"
                themeName.equals("Matcha Latte", ignoreCase = true) -> "assets_collection/theme/theme_2"
                themeName.equals("Dreamy Bear", ignoreCase = true) -> "assets_collection/theme/theme_3"
                themeName.equals("Trending", ignoreCase = true) -> "assets_theme/category/Trending/theme_1"
                else -> "assets_collection/theme/theme_1"
            }
            theme = ThemeItem(
                id = themeId.ifEmpty { "theme_${safePath.replace('/', '_')}" },
                name = themeName,
                path = safePath,
                rawType = themeType,
                previewPath = passedPreview,
                thumbPath = passedPreview,
                backgroundPath = passedWallpaper
            )
        }

        initToolbar()
        initViewPager()
        initTabs()
        val startTab = intent.getIntExtra("start_tab", 0)
        binding.viewPager.setCurrentItem(startTab, false)
        updateTabUI(startTab)
    }

    private fun getAttrColor(attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        this.getTheme().resolveAttribute(attrId, typedValue, true)
        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
    }

    private fun initToolbar() {
        val toolbarBinding = binding.toolbar
        toolbarBinding.titleTextView.text = getString(R.string.theme_setup)
        toolbarBinding.titleTextView.visibility = View.VISIBLE
        
        com.widget.themes.controlcenter.core.ads.AdsManager.showCollapsibleNative(
            activity = this,
            container = binding.layoutAds,
            adUnitId = getString(R.string.native_collap),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_collap
        )

        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        toolbarBinding.ivBack.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        toolbarBinding.ivInfo.setOnClickListener {
            val intent = Intent(this, com.widget.themes.controlcenter.feature_setting.InfoActivity::class.java)
            startActivity(intent)
        }

        toolbarBinding.llCreateWidget.root.setOnClickListener {
            val intent = Intent(this, com.widget.themes.controlcenter.feature_widget.WidgetConfigActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViewPager() {
        val isDownloaded = intent.getBooleanExtra("is_downloaded", false)
        binding.viewPager.adapter = DownloadThemePagerAdapter(this, theme, isDownloaded)
        // Lock manual swiping
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabUI(position)
            }
        })
    }

    private fun initTabs() {
        binding.tvWallpaper.setOnClickListener {
            binding.viewPager.currentItem = 0
        }
        binding.tvIcon.setOnClickListener {
            binding.viewPager.currentItem = 1
        }
        binding.tvWidget.setOnClickListener {
            binding.viewPager.currentItem = 2
        }
    }

    private fun updateTabUI(position: Int) {
        val tabs = listOf(binding.tvWallpaper, binding.tvIcon, binding.tvWidget)
        for (i in tabs.indices) {
            val tab = tabs[i]
            if (i == position) {
                tab.isSelected = true
                tab.setTextColor(Color.WHITE)
                tab.setBackgroundResource(R.drawable.bg_theme_setup_tab_selected)
                tab.typeface = ResourcesCompat.getFont(this, R.font.inter_semi_bold)
            } else {
                tab.isSelected = false
                tab.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                tab.setBackgroundResource(0)
                tab.typeface = ResourcesCompat.getFont(this, R.font.inter_medium)
            }
        }
    }

    override fun onDestroy() {
        com.widget.themes.controlcenter.core.ads.AdsManager.stopCollapsible(this)
        super.onDestroy()
    }

    class DownloadThemePagerAdapter(
        activity: AppCompatActivity,
        private val theme: ThemeItem,
        private val isDownloaded: Boolean = false
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> DownloadWallpaperFragment.newInstance(theme, isDownloaded)
                1 -> DownloadIconFragment.newInstance(theme)
                2 -> DownloadWidgetFragment.newInstance(theme)
                else -> throw IllegalStateException("Invalid position")
            }
        }
    }
}
