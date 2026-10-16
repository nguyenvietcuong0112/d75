package com.widget.themes.controlcenter.feature_setting

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.cscmobi.libraryads.commons.utils.Constants
import com.cscmobi.libraryads.views.language.CSCLanguageActivity
import com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs
import com.widget.themes.controlcenter.BuildConfig
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.ads.AdsManager
import com.widget.themes.controlcenter.core.utils.SystemUtil
import com.widget.themes.controlcenter.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var currentLanguageCode: String = ""

    override fun attachBaseContext(newBase: Context) {
        SystemUtil.setLocale(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguageCode = SystemUtil.getPreLanguage(this) ?: "en"
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVersionInfo()
        setupClickListeners()
        loadAds()
    }

    private fun setupVersionInfo() {
        val versionName = try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }
        binding.tvVersionName.text = "v$versionName"
    }

    private fun loadAds() {
        AdsManager.loadAndShowNative(
            activity = this,
            container = binding.layoutAds,
            placementKey = "native_all",
            adUnitId = getString(R.string.native_all),
            layoutRes = R.layout.layout_native_media,
            isEnabled = RemoteConfigs.native_all
        )
    }

    override fun onResume() {
        super.onResume()
        val latestLang = SystemUtil.getPreLanguage(this) ?: "en"
        if (latestLang != currentLanguageCode) {
            currentLanguageCode = latestLang
            recreate()
        }
    }

    private fun setupClickListeners() {
        val handleBack = {
            AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        // Back Button
        binding.btnBack.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        // VIP / Crown Button
        binding.btnPremium.setOnClickListener {
            showPremiumDialog()
        }

        // 1. Language
        binding.btnLanguage.setOnClickListener {
            val intent = Intent(this, CSCLanguageActivity::class.java).apply {
                putExtra(Constants.FROM_SETTING, true)
            }
            startActivity(intent)
        }

        // 2. Rate App
        binding.btnRateApp.setOnClickListener {
            rateApp()
        }

        // 3. Share App
        binding.btnShareApp.setOnClickListener {
            shareApp()
        }

        // 4. Policy
        binding.btnPolicy.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    private fun rateApp() {
        val packageName = packageName
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun shareApp() {
        val packageName = packageName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_app_text, "https://play.google.com/store/apps/details?id=$packageName")
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_chooser_title)))
    }

    private fun openPrivacyPolicy() {
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/game-empire-studio")
            )
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.toast_error_privacy_policy, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.vip_premium_title)
            .setMessage(R.string.vip_premium_message)
            .setPositiveButton(R.string.upgrade) { dialog, _ ->
                Toast.makeText(this, R.string.toast_welcome_vip, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
