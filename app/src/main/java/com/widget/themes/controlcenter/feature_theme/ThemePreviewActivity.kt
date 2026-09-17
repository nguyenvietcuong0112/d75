package com.widget.themes.controlcenter.feature_theme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.databinding.ActivityThemePreviewBinding
import com.widget.themes.controlcenter.core.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemePreviewBinding
    private var themeId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themeId = intent.getStringExtra("theme_id") ?: ""
        if (themeId.isNotEmpty()) {
            com.widget.themes.controlcenter.core.network.ViewTracker.track("theme", themeId)
        }

        // Lock swipe gestures on ViewPager2
        binding.viewPager.isUserInputEnabled = false

        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        binding.ivClose.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        binding.llLoadingAd.visibility = View.GONE
        binding.ctOverlay.visibility = View.GONE
        binding.pbLoading.visibility = View.GONE

        com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowNative(
            activity = this,
            container = binding.layoutAds,
            placementKey = "native_all",
            adUnitId = getString(R.string.native_all),
            layoutRes = R.layout.layout_native_media,
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_all
        )

        val themeName = intent.getStringExtra("theme_name") ?: ""
        val themePath = intent.getStringExtra("theme_path") ?: ""
        val themeType = intent.getStringExtra("theme_type") ?: ""
        val themePreview = intent.getStringExtra("theme_preview") ?: intent.getStringExtra("theme_thumb") ?: ""
        val themeWallpaper = intent.getStringExtra("theme_wallpaper") ?: ""

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1
            override fun createFragment(position: Int): Fragment {
                return ThemePreviewFragment.newInstance(themeId, themeName, themePath, themeType, themePreview, themeWallpaper)
            }
        }
    }
}
