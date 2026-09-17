package com.widget.themes.controlcenter.feature_setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.widget.themes.controlcenter.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.titleTextView.text = "Information"
        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(com.widget.themes.controlcenter.R.string.inter_all),
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

        binding.siFQA.setOnClickListener {
            // FAQ Click action placeholder
        }

        binding.siGetIcon.setOnClickListener {
            // How to get icon guide action placeholder
        }

        binding.siGetWidget.setOnClickListener {
            // How to get widget guide action placeholder
        }

        com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowNative(
            activity = this,
            container = binding.layoutAds,
            placementKey = "native_all",
            adUnitId = getString(com.widget.themes.controlcenter.R.string.native_all),
            layoutRes = com.widget.themes.controlcenter.R.layout.layout_native_media,
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_all
        )
    }
}
