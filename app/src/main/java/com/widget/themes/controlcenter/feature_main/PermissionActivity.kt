package com.widget.themes.controlcenter.feature_main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.ui.ThemeSwitch
import com.widget.themes.controlcenter.core.utils.SystemUtil

class PermissionActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        SystemUtil.setLocale(newBase)
        super.attachBaseContext(newBase)
    }



    private lateinit var cardOverlay: View
    private lateinit var switchOverlay: ThemeSwitch

    private lateinit var cardWriteSettings: View
    private lateinit var switchWriteSettings: ThemeSwitch

    private lateinit var cardNotifications: View
    private lateinit var switchNotifications: ThemeSwitch

    private lateinit var cardLocation: View
    private lateinit var switchLocation: ThemeSwitch

    private lateinit var btnGetStarted: Button

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        updateSwitchStates()
        if (results.values.all { it }) {
            Toast.makeText(this, getString(R.string.toast_perm_location_granted), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updateSwitchStates()
        if (isGranted) {
            Toast.makeText(this, getString(R.string.toast_perm_notification_granted), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forceShow = intent.getBooleanExtra("force_show", false)
        // If core permissions already granted and not forced, jump directly to Main
        if (!forceShow && isOverlayGranted()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_permission)

        initViews()
        setupClickListeners()
        updateSwitchStates(animate = false)
        loadNativeAd()
    }

    override fun onResume() {
        super.onResume()
        com.widget.themes.controlcenter.core.ads.AdsManager.enableAOA()
        updateSwitchStates(animate = true)
    }

    private fun loadNativeAd() {
        com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowNative(
            activity = this,
            container = findViewById(R.id.layoutAds),
            placementKey = "native_permission",
            adUnitId = getString(R.string.native_permission),
            layoutRes = R.layout.layout_native_media,
            isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.native_permission
        )
    }

    private fun initViews() {
        cardOverlay = findViewById(R.id.cardOverlay)
        switchOverlay = findViewById(R.id.switchOverlay)

        cardWriteSettings = findViewById(R.id.cardWriteSettings)
        switchWriteSettings = findViewById(R.id.switchWriteSettings)

        cardNotifications = findViewById(R.id.cardNotifications)
        switchNotifications = findViewById(R.id.switchNotifications)

        cardLocation = findViewById(R.id.cardLocation)
        switchLocation = findViewById(R.id.switchLocation)

        btnGetStarted = findViewById(R.id.btnGetStarted)
    }

    private var lastClickTime = 0L

    private fun isClickAllowed(): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastClickTime < 1000L) {
            return false
        }
        lastClickTime = now
        return true
    }

    private fun setupClickListeners() {
        // 1. Display Over Other Apps (Overlay)
        val onOverlayClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (isOverlayGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
                return@OnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                com.widget.themes.controlcenter.core.ads.AdsManager.disableAOA()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                }
                startActivity(intent)

                com.widget.themes.controlcenter.core.utils.PermissionDetector.startDetectingPermission(
                    activity = this,
                    checkPermission = { isOverlayGranted() },
                    onGranted = {
                        lastClickTime = android.os.SystemClock.elapsedRealtime()
                        updateSwitchStates(animate = true)
                        Toast.makeText(this, getString(R.string.toast_perm_overlay_granted), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        cardOverlay.setOnClickListener(onOverlayClick)
        switchOverlay.setOnClickListener(onOverlayClick)

        // 3. Write Settings
        val onWriteSettingsClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (isWriteSettingsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                    com.widget.themes.controlcenter.core.ads.AdsManager.disableAOA()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
                return@OnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                com.widget.themes.controlcenter.core.ads.AdsManager.disableAOA()
                val intent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                }
                startActivity(intent)

                com.widget.themes.controlcenter.core.utils.PermissionDetector.startDetectingPermission(
                    activity = this,
                    checkPermission = { isWriteSettingsGranted() },
                    onGranted = {
                        lastClickTime = android.os.SystemClock.elapsedRealtime()
                        updateSwitchStates(animate = true)
                        Toast.makeText(this, getString(R.string.toast_perm_settings_granted), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        cardWriteSettings.setOnClickListener(onWriteSettingsClick)
        switchWriteSettings.setOnClickListener(onWriteSettingsClick)

        // 4. Notifications
        val onNotificationsClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (isNotificationsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                    openNotificationSettings()
                } else {
                    Toast.makeText(this, getString(R.string.toast_perm_notification_not_required), Toast.LENGTH_SHORT).show()
                }
                return@OnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(this, getString(R.string.toast_perm_notification_not_required), Toast.LENGTH_SHORT).show()
            }
        }
        cardNotifications.setOnClickListener(onNotificationsClick)
        switchNotifications.setOnClickListener(onNotificationsClick)

        // 5. Location
        val onLocationClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (isLocationGranted()) {
                Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                openAppSettings()
                return@OnClickListener
            }

            requestLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        cardLocation.setOnClickListener(onLocationClick)
        switchLocation.setOnClickListener(onLocationClick)

        // Continue
        btnGetStarted.setOnClickListener {
            navigateToMain()
        }
    }

    private fun updateSwitchStates(animate: Boolean = true) {
        switchOverlay.setChecked(isOverlayGranted(), animate)
        switchWriteSettings.setChecked(isWriteSettingsGranted(), animate)
        switchNotifications.setChecked(isNotificationsGranted(), animate)
        switchLocation.setChecked(isLocationGranted(), animate)
    }

    private fun isOverlayGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun isWriteSettingsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        } else {
            true
        }
    }

    private fun isLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun openAppSettings() {
        com.widget.themes.controlcenter.core.ads.AdsManager.disableAOA()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun openNotificationSettings() {
        com.widget.themes.controlcenter.core.ads.AdsManager.disableAOA()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        } else {
            openAppSettings()
        }
    }

    override fun onDestroy() {
        com.widget.themes.controlcenter.core.utils.PermissionDetector.stopDetecting()
        super.onDestroy()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}
