package com.widget.themes.controlcenter.feature_main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.utils.SystemUtil
import com.widget.themes.controlcenter.databinding.ActivityMainBinding
import com.widget.themes.controlcenter.feature_setting.SettingActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentLanguageCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguageCode = SystemUtil.getPreLanguage(this) ?: "en"
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupViewPager()
        setupBottomNavigation()
        setupBackPressed()
        setupBottomAd()
        handleIntent(intent)
    }

    private var isBannerLoadedOrRequested = false

    private fun setupBottomAd() {
        // Load banner ngay khi vừa vào Home
        loadBottomBanner()

        // Đồng thời hiển thị Collapsible Native ở đáy nếu được bật
        if (com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_collap) {
            showCollapsibleNative()
        }
    }

    private fun loadBottomBanner() {
        if (isFinishing || isDestroyed) return
        if (isBannerLoadedOrRequested) {
            binding.bannerContainer.visibility = View.VISIBLE
            return
        }
        isBannerLoadedOrRequested = true
        binding.bannerContainer.visibility = View.VISIBLE
        com.widget.themes.controlcenter.core.ads.AdsManager.showBanner(
            activity = this,
            container = binding.bannerContainer,
            adUnitId = getString(R.string.banner_home),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.banner_home,
            isCollapsible = false,
            onLoaded = {
                binding.bannerContainer.visibility = View.VISIBLE
            },
            onFailed = {
                binding.bannerContainer.visibility = View.GONE
            }
        )
    }

    private fun showCollapsibleNative() {
        binding.layoutAds.visibility = View.VISIBLE

        com.widget.themes.controlcenter.core.ads.AdsManager.showCollapsibleNative(
            activity = this,
            container = binding.layoutAds,
            adUnitId = getString(R.string.native_collap),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_collap,
            onLoaded = {
                // Collapsible native is showing
            },
            onFailed = {
                binding.layoutAds.visibility = View.GONE
                loadBottomBanner()
            },
            onClosed = {
                binding.layoutAds.visibility = View.GONE
                loadBottomBanner()
            }
        )

        var wasNativeVisible = false
        binding.layoutAds.viewTreeObserver.addOnGlobalLayoutListener {
            val isNowVisible = binding.layoutAds.visibility == View.VISIBLE && binding.layoutAds.childCount > 0
            if (wasNativeVisible && !isNowVisible) {
                wasNativeVisible = false
                loadBottomBanner()
            } else if (isNowVisible) {
                wasNativeVisible = true
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetTab = intent?.getIntExtra("target_tab", -1) ?: -1
        if (targetTab != -1) {
            if (targetTab == 4) {
                val category = intent?.getStringExtra("target_collection_category") ?: "Widget"
                androidx.lifecycle.ViewModelProvider(this)[com.widget.themes.controlcenter.feature_collections.CollectionsViewModel::class.java].selectTabByName(category)
            }
            selectTab(targetTab)
        }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        val existing = supportFragmentManager.findFragmentByTag(ExitDialogFragment.TAG)
        if (existing == null || !existing.isAdded) {
            ExitDialogFragment.newInstance().show(supportFragmentManager, ExitDialogFragment.TAG)
        }
    }

    private fun setupHeader() {
        // Premium button click
        binding.btnPremium.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_premium_features), Toast.LENGTH_SHORT).show()
        }

        // Setting button click -> SettingActivity
        binding.btnSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        /*
        // Long-click Setting button -> Open Ad Inspector directly
        binding.btnSetting.setOnLongClickListener {
            try {
                Toast.makeText(this, "Opening Ad Inspector...", Toast.LENGTH_SHORT).show()
                com.google.android.libraries.ads.mobile.sdk.MobileAds.openAdInspector { error ->
                    if (error != null) {
                        Toast.makeText(this, "AdInspector error: ${error.message} (code: ${error.code})", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "AdInspector exception: ${e.message}", Toast.LENGTH_LONG).show()
            }
            true
        }
        */
    }

    override fun attachBaseContext(newBase: Context) {
        SystemUtil.setLocale(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onResume() {
        super.onResume()
        val latestLang = SystemUtil.getPreLanguage(this) ?: "en"
        if (latestLang != currentLanguageCode) {
            currentLanguageCode = latestLang
            recreate()
            return
        }
        if (::binding.isInitialized) {
            updateHeaderTitle(binding.viewpager.currentItem)
            binding.tvNavTheme.setText(R.string.tab_theme)
            binding.tvNavIcons.setText(R.string.tab_icons)
            binding.tvNavControl.setText(R.string.tab_control)
            binding.tvNavWidget.setText(R.string.tab_widget)
            binding.tvNavCollections.setText(R.string.tab_collections)
        }
        com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.updateAllWidgets(this)
        com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.schedulePeriodicUpdate(this)
        com.widget.themes.controlcenter.feature_widget.WeatherHelper.updateWeather(this)
        com.widget.themes.controlcenter.feature_widget.WidgetClockTicker.start(this)
    }

    private fun updateHeaderTitle(position: Int) {
        val title = when (position) {
            0 -> getString(R.string.header_title_theme)
            1 -> getString(R.string.header_title_icons)
            2 -> getString(R.string.header_title_control)
            3 -> getString(R.string.header_title_widgets)
            else -> getString(R.string.header_title_collections)
        }
        binding.tvHeaderTitle.text = title
        if (position != 0) {
            binding.tvHeaderSubtitle.visibility = View.GONE
        } else {
            binding.tvHeaderSubtitle.visibility = View.VISIBLE
            binding.tvHeaderSubtitle.text = getString(R.string.header_subtitle_main)
        }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.isUserInputEnabled = false // disable swipe to change tab
        binding.viewpager.offscreenPageLimit = 4

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateHeaderTitle(position)
                updateNavSelection(position)
                if (position == 4) {
                    androidx.lifecycle.ViewModelProvider(this@MainActivity)[com.widget.themes.controlcenter.feature_collections.CollectionsViewModel::class.java].loadDataForSelectedTab()
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.tabTheme.setOnClickListener { selectTab(0) }
        binding.tabIcons.setOnClickListener { selectTab(1) }
        binding.tabControl.setOnClickListener { selectTab(2) }
        binding.tabWidget.setOnClickListener { selectTab(3) }
        binding.tabCollections.setOnClickListener { selectTab(4) }

        updateNavSelection(0)
    }

    private fun updateNavSelection(position: Int) {
        val navItems = listOf(
            Triple(binding.tabTheme, Pair(binding.ivNavTheme, binding.tvNavTheme), Pair(R.drawable.ic_nav_theme_true, R.drawable.ic_nav_theme_false)),
            Triple(binding.tabIcons, Pair(binding.ivNavIcons, binding.tvNavIcons), Pair(R.drawable.ic_nav_icons_true, R.drawable.ic_nav_icons_false)),
            Triple(binding.tabControl, Pair(binding.ivNavControl, binding.tvNavControl), Pair(R.drawable.ic_nav_control_true, R.drawable.ic_nav_control_false)),
            Triple(binding.tabWidget, Pair(binding.ivNavWidget, binding.tvNavWidget), Pair(R.drawable.ic_nav_widget_true, R.drawable.ic_nav_widget_false)),
            Triple(binding.tabCollections, Pair(binding.ivNavCollections, binding.tvNavCollections), Pair(R.drawable.ic_nav_collections_true, R.drawable.ic_nav_collections_false))
        )

        navItems.forEachIndexed { index, (tabLayout, views, icons) ->
            val isSelected = index == position
            tabLayout.isSelected = isSelected
            views.first.isSelected = isSelected
            views.second.isSelected = isSelected
            views.first.setImageResource(if (isSelected) icons.first else icons.second)
            val fontRes = if (isSelected) R.font.inter_bold else R.font.inter_medium
            views.second.typeface = ResourcesCompat.getFont(this, fontRes)
        }
    }

    fun selectTab(position: Int) {
        if (::binding.isInitialized) {
            if (binding.viewpager.currentItem != position) {
                binding.viewpager.setCurrentItem(position, false)
            } else if (position == 4) {
                androidx.lifecycle.ViewModelProvider(this)[com.widget.themes.controlcenter.feature_collections.CollectionsViewModel::class.java].loadDataForSelectedTab()
            }
            updateHeaderTitle(position)
            updateNavSelection(position)
        }
    }

    override fun onDestroy() {
        com.widget.themes.controlcenter.core.ads.AdsManager.stopCollapsible(this)
        com.widget.themes.controlcenter.core.ads.AdsManager.destroyBanner(this)
        super.onDestroy()
    }
}
