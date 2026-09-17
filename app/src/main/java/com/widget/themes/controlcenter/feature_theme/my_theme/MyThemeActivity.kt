package com.widget.themes.controlcenter.feature_theme.my_theme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.widget.themes.controlcenter.R

class MyThemeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_theme)

        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        findViewById<View>(R.id.toolbar)?.findViewById<View>(R.id.ivBack)?.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        findViewById<View>(R.id.llTab)?.visibility = View.GONE

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1
            override fun createFragment(position: Int): Fragment {
                return MyThemeListFragment()
            }
        }

        com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowNative(
            activity = this,
            container = findViewById(R.id.layoutAds),
            placementKey = "native_all",
            adUnitId = getString(R.string.native_all),
            layoutRes = R.layout.layout_native_media,
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_all
        )
    }
}
