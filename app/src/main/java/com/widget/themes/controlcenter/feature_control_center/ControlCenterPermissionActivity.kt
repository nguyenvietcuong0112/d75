package com.widget.themes.controlcenter.feature_control_center

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.ui.ThemeSwitch
import com.widget.themes.controlcenter.core.utils.PermissionDetector
import com.widget.themes.controlcenter.core.utils.SystemUtil

class ControlCenterPermissionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    private lateinit var layoutCardWriteSettings: View
    private lateinit var switchWriteSettings: ThemeSwitch

    private lateinit var layoutCardNotifications: View
    private lateinit var switchNotifications: ThemeSwitch

    private lateinit var layoutCardOverlay: View
    private lateinit var switchOverlay: ThemeSwitch

    private var lastClickTime = 0L
    private var isFinishingAutomatically = false

    override fun attachBaseContext(newBase: Context) {
        SystemUtil.setLocale(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_center_permission)

        initViews()
        setupClickListeners()
        updateSwitchStates(animate = false)

        if (isAllPermissionsGranted()) {
            setResult(Activity.RESULT_OK)
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        updateSwitchStates(animate = true)
        checkAllPermissionsAndFinish()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)

        layoutCardWriteSettings = findViewById(R.id.layoutCardWriteSettings)
        switchWriteSettings = findViewById(R.id.switchWriteSettings)

        layoutCardNotifications = findViewById(R.id.layoutCardNotifications)
        switchNotifications = findViewById(R.id.switchNotifications)

        layoutCardOverlay = findViewById(R.id.layoutCardOverlay)
        switchOverlay = findViewById(R.id.switchOverlay)
    }

    private fun isClickAllowed(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < 1000L) {
            return false
        }
        lastClickTime = now
        return true
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            handleCompletionAndExit()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleCompletionAndExit()
            }
        })

        // 1. Write Settings
        val onWriteSettingsClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (!isWriteSettingsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
                    startActivity(intent)

                    PermissionDetector.startDetectingPermission(
                        activity = this,
                        checkPermission = { isWriteSettingsGranted() },
                        onGranted = {
                            lastClickTime = SystemClock.elapsedRealtime()
                            updateSwitchStates(animate = true)
                            if (!isAllPermissionsGranted()) {
                                Toast.makeText(this, getString(R.string.toast_perm_settings_granted_sys), Toast.LENGTH_SHORT).show()
                            }
                            checkAllPermissionsAndFinish()
                        }
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
            }
        }
        layoutCardWriteSettings.setOnClickListener(onWriteSettingsClick)
        switchWriteSettings.setOnClickListener(onWriteSettingsClick)

        // 3. Notifications
        val onNotificationsClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (!isNotificationsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
                } else {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
                    startActivity(intent)

                    PermissionDetector.startDetectingPermission(
                        activity = this,
                        checkPermission = { isNotificationsGranted() },
                        onGranted = {
                            lastClickTime = SystemClock.elapsedRealtime()
                            updateSwitchStates(animate = true)
                            if (!isAllPermissionsGranted()) {
                                Toast.makeText(this, getString(R.string.toast_perm_notification_granted), Toast.LENGTH_SHORT).show()
                            }
                            checkAllPermissionsAndFinish()
                        }
                    )
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_turn_off_in_settings), Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
            }
        }
        layoutCardNotifications.setOnClickListener(onNotificationsClick)
        switchNotifications.setOnClickListener(onNotificationsClick)

        // 4. Display Over Other Apps (Overlay)
        val onOverlayClick = View.OnClickListener {
            if (!isClickAllowed()) return@OnClickListener

            if (!isOverlayGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    }
                    startActivity(intent)

                    PermissionDetector.startDetectingPermission(
                        activity = this,
                        checkPermission = { isOverlayGranted() },
                        onGranted = {
                            lastClickTime = SystemClock.elapsedRealtime()
                            updateSwitchStates(animate = true)
                            if (!isAllPermissionsGranted()) {
                                Toast.makeText(this, getString(R.string.toast_perm_overlay_granted_disp), Toast.LENGTH_SHORT).show()
                            }
                            checkAllPermissionsAndFinish()
                        }
                    )
                }
            } else {
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
            }
        }
        layoutCardOverlay.setOnClickListener(onOverlayClick)
        switchOverlay.setOnClickListener(onOverlayClick)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            lastClickTime = SystemClock.elapsedRealtime()
            updateSwitchStates(animate = true)
            if (isNotificationsGranted() && !isAllPermissionsGranted()) {
                Toast.makeText(this, getString(R.string.toast_perm_notification_granted), Toast.LENGTH_SHORT).show()
            }
            checkAllPermissionsAndFinish()
        }
    }

    private fun handleCompletionAndExit() {
        if (isOverlayGranted()) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private fun isAllPermissionsGranted(): Boolean {
        return isWriteSettingsGranted() &&
                isNotificationsGranted() &&
                isOverlayGranted()
    }

    private fun checkAllPermissionsAndFinish() {
        if (isFinishingAutomatically) return
        if (isAllPermissionsGranted()) {
            isFinishingAutomatically = true
            Toast.makeText(this, getString(R.string.toast_all_permissions_granted), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            window.decorView.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    finish()
                }
            }, 350)
        }
    }

    private fun updateSwitchStates(animate: Boolean = true) {
        switchWriteSettings.setChecked(isWriteSettingsGranted(), animate)
        switchNotifications.setChecked(isNotificationsGranted(), animate)
        switchOverlay.setChecked(isOverlayGranted(), animate)
    }

    private fun isWriteSettingsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        } else {
            true
        }
    }

    private fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isOverlayGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    override fun onDestroy() {
        PermissionDetector.stopDetecting()
        super.onDestroy()
    }

    companion object {
        fun start(activity: Activity, requestCode: Int = 1001) {
            val intent = Intent(activity, ControlCenterPermissionActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
