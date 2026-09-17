package com.widget.themes.controlcenter.feature_control_center

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_control_center.model.ControlCenterPreferences
import com.widget.themes.controlcenter.feature_control_center.service.ControlCenterOverlayService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlCenterPreviewActivity : AppCompatActivity() {

    private lateinit var ivPreviewThumb: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var btnApplyTheme: View

    private lateinit var prefs: ControlCenterPreferences

    private var themePath: String = ""
    private var themeName: String = ""
    private var previewPath: String = ""
    private var thumbPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_center_preview)

        prefs = ControlCenterPreferences(this)

        themePath = intent.getStringExtra(EXTRA_THEME_PATH) ?: prefs.activeThemePath
        themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: prefs.activeThemeName
        previewPath = intent.getStringExtra(EXTRA_PREVIEW_PATH) ?: ""
        thumbPath = intent.getStringExtra(EXTRA_THUMB_PATH) ?: ""

        if (themePath.isNotEmpty()) {
            com.widget.themes.controlcenter.core.network.ViewTracker.track("control_center", themePath)
        }

        initViews()
        loadThemePreview()
        setupListeners()
    }

    private fun initViews() {
        ivPreviewThumb = findViewById(R.id.ivPreviewThumb)
        btnBack = findViewById(R.id.btnBack)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        btnApplyTheme = findViewById(R.id.btnApplyTheme)

        val layoutAds = findViewById<android.view.ViewGroup>(R.id.layoutAds)
        com.widget.themes.controlcenter.core.ads.AdsManager.showCollapsibleNative(
            activity = this,
            container = layoutAds,
            adUnitId = getString(R.string.native_collap),
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_collap
        )
    }

    private fun resolveImageUrl(rawPath: String): String {
        if (rawPath.isEmpty()) return ""
        if (rawPath.endsWith(".zip", ignoreCase = true)) return ""
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://") || rawPath.startsWith("file://")) {
            return rawPath
        }
        val clean = rawPath.removePrefix("/")
        return "file:///android_asset/$clean"
    }

    private fun loadThemePreview() {
        val primaryImage = when {
            previewPath.isNotEmpty() -> resolveImageUrl(previewPath)
            thumbPath.isNotEmpty() -> resolveImageUrl(thumbPath)
            !themePath.endsWith(".zip", ignoreCase = true) && (themePath.startsWith("http://") || themePath.startsWith("https://") || themePath.startsWith("file://")) -> themePath
            themePath.isNotEmpty() && !themePath.startsWith("http") -> {
                val clean = themePath.removePrefix("/")
                if (clean.startsWith("file:///android_asset/")) clean
                else "file:///android_asset/$clean/preview.jpg"
            }
            else -> ""
        }

        val fallbackImage = when {
            thumbPath.isNotEmpty() -> resolveImageUrl(thumbPath)
            themePath.isNotEmpty() && !themePath.startsWith("http") -> {
                val clean = themePath.removePrefix("/")
                if (clean.startsWith("file:///android_asset/")) clean
                else "file:///android_asset/$clean/thumb.webp"
            }
            else -> ""
        }

        if (primaryImage.isNotEmpty()) {
            val req = Glide.with(this)
                .load(primaryImage)
                .placeholder(R.color.grayF2F2F2)
            if (fallbackImage.isNotEmpty() && fallbackImage != primaryImage) {
                req.error(
                    Glide.with(this)
                        .load(fallbackImage)
                        .placeholder(R.color.grayF2F2F2)
                        .error(R.color.grayF2F2F2)
                )
            } else {
                req.error(R.color.grayF2F2F2)
            }
            req.into(ivPreviewThumb)
        } else if (fallbackImage.isNotEmpty()) {
            Glide.with(this)
                .load(fallbackImage)
                .placeholder(R.color.grayF2F2F2)
                .error(R.color.grayF2F2F2)
                .into(ivPreviewThumb)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || hasOverlayPermission()) {
            applyControlTheme(finishOnSuccess = false)
        }
    }

    private fun setupListeners() {
        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        btnBack.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        btnApplyTheme.setOnClickListener {
            if (!hasOverlayPermission()) {
                openPermissionActivity()
            } else {
                applyControlTheme(finishOnSuccess = true)
            }
        }
    }

    private fun openPermissionActivity() {
        val intent = Intent(this, ControlCenterPermissionActivity::class.java)
        permissionLauncher.launch(intent)
    }

    private fun applyControlTheme(finishOnSuccess: Boolean = true) {
        lifecycleScope.launch(Dispatchers.Main) {
            var finalPath = themePath
            val isRemote = themePath.startsWith("http://") || themePath.startsWith("https://")
            if (isRemote) {
                Toast.makeText(this@ControlCenterPreviewActivity, getString(R.string.toast_downloading_control_center), Toast.LENGTH_SHORT).show()
                val extracted = ControlCenterDownloadManager.downloadAndExtractZip(
                    this@ControlCenterPreviewActivity,
                    themeName,
                    themePath
                )
                if (extracted != null) {
                    finalPath = extracted.absolutePath
                } else {
                    Toast.makeText(this@ControlCenterPreviewActivity, getString(R.string.toast_failed_download_control_center), Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            prefs.activeThemePath = finalPath
            prefs.activeThemeName = themeName
            prefs.isEnabled = true

            val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(this@ControlCenterPreviewActivity)
            val savedPreviewPath = when {
                thumbPath.isNotEmpty() -> resolveImageUrl(thumbPath)
                previewPath.isNotEmpty() -> resolveImageUrl(previewPath)
                !themePath.startsWith("http") && themePath.isNotEmpty() -> {
                    val clean = themePath.removePrefix("/")
                    if (clean.startsWith("file:///android_asset/")) clean
                    else "file:///android_asset/$clean/thumb.webp"
                }
                else -> ""
            }
            withContext(Dispatchers.IO) {
                repo.markAsDownloaded(
                    id = "ctrl_${themePath.replace('/', '_')}",
                    name = themeName,
                    category = "Control center",
                    targetPath = finalPath,
                    previewPath = savedPreviewPath,
                    rawType = "control_center"
                )
            }

            ControlCenterOverlayService.start(this@ControlCenterPreviewActivity)
            ControlCenterOverlayService.reloadTheme(this@ControlCenterPreviewActivity)

            Toast.makeText(this@ControlCenterPreviewActivity, getString(R.string.toast_control_center_applied), Toast.LENGTH_SHORT).show()
            if (finishOnSuccess) {
                finish()
            }
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    companion object {
        const val EXTRA_THEME_PATH = "extra_theme_path"
        const val EXTRA_THEME_NAME = "extra_theme_name"
        const val EXTRA_PREVIEW_PATH = "extra_preview_path"
        const val EXTRA_THUMB_PATH = "extra_thumb_path"

        fun start(
            context: Context,
            themePath: String,
            themeName: String,
            previewPath: String = "",
            thumbPath: String = ""
        ) {
            val intent = Intent(context, ControlCenterPreviewActivity::class.java).apply {
                putExtra(EXTRA_THEME_PATH, themePath)
                putExtra(EXTRA_THEME_NAME, themeName)
                putExtra(EXTRA_PREVIEW_PATH, previewPath)
                putExtra(EXTRA_THUMB_PATH, thumbPath)
            }
            context.startActivity(intent)
        }
    }

    override fun onDestroy() {
        com.widget.themes.controlcenter.core.ads.AdsManager.stopCollapsible(this)
        super.onDestroy()
    }
}
