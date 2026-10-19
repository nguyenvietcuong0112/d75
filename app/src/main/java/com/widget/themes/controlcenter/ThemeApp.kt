package com.widget.themes.controlcenter

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.widget.themes.controlcenter.core.utils.EnumSelectLanguage
import com.widget.themes.controlcenter.core.utils.SystemUtil
import com.widget.themes.controlcenter.feature_charging.ChargingActivity
import com.widget.themes.controlcenter.feature_main.MainActivity
import com.widget.themes.controlcenter.feature_main.PermissionActivity
import com.cscmobi.libraryads.CSCApplication
import com.cscmobi.libraryads.commons.sharepreference.CSCSPFHolder
import com.cscmobi.libraryads.data.AdsLanguageConfig
import com.cscmobi.libraryads.data.AdsOBConfig
import com.cscmobi.libraryads.data.AdsSplashConfig
import com.cscmobi.libraryads.data.LanguageConfig
import com.cscmobi.libraryads.data.LanguageSetting
import com.cscmobi.libraryads.data.OBConfig
import com.cscmobi.libraryads.data.OnActivityCallBack
import com.cscmobi.libraryads.data.SplashConfig
import com.cscmobi.libraryads.data.UiLanguageConfig
import com.cscmobi.libraryads.data.UiOBConfig
import com.cscmobi.libraryads.data.UiSplashConfig
import com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs

class ThemeApp : Application() {

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_CONNECTED) {
                val launchIntent = Intent(context, ChargingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName) {
                try {
                    android.webkit.WebView.setDataDirectorySuffix(processName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        initFO()
        try {
            val filter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
            registerReceiver(chargingReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val widgetFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    when (intent.action) {
                        Intent.ACTION_SCREEN_OFF -> {
                            com.widget.themes.controlcenter.feature_widget.WidgetClockTicker.onScreenOff()
                        }
                        Intent.ACTION_SCREEN_ON,
                        Intent.ACTION_USER_PRESENT -> {
                            com.widget.themes.controlcenter.feature_widget.WidgetClockTicker.onScreenOn(context)
                            com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.updateAllWidgets(context)
                        }
                        else -> {
                            com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.updateAllWidgets(context)
                            com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.schedulePeriodicUpdate(context)
                            com.widget.themes.controlcenter.feature_widget.WidgetClockTicker.start(context)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, widgetFilter)

        com.widget.themes.controlcenter.feature_widget.BaseWidgetProvider.schedulePeriodicUpdate(this)
        com.widget.themes.controlcenter.feature_widget.WidgetClockTicker.start(this)

        setupFullScreenLifecycle()
    }

    companion object {
        fun applyFullScreen(activity: Activity?) {
            if (activity == null || activity.isFinishing || activity.isDestroyed) return
            applyFullScreen(activity.window)
        }

        fun applyFullScreen(window: Window?) {
            if (window == null) return
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }

                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                    window.isStatusBarContrastEnforced = false
                }

                val decorView = window.decorView
                val insetsController = WindowCompat.getInsetsController(window, decorView)
                // Hide both status bars and navigation bars
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupFullScreenLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                try {
                    activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                applyFullScreen(activity)

                activity.window?.decorView?.let { decor ->
                    decor.post {
                        applyFullScreen(activity)
                        decor.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
                            if (hasFocus) {
                                applyFullScreen(activity)
                            }
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {
                applyFullScreen(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                applyFullScreen(activity)
                activity.window?.decorView?.post {
                    applyFullScreen(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun initFO() {
        /*
        try {
            val testConfig = com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("BC1F52F968E97CA36AFAA3BDB937A169"))
                .build()
            com.google.android.libraries.ads.mobile.sdk.MobileAds.setRequestConfiguration(testConfig)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        */

        CSCSPFHolder.init(this)
        val cscLibrary = CSCApplication(this, RemoteConfigs, getString(R.string.app_id), BuildConfig.DEBUG)



        cscLibrary.initSdk(
            adjustAppToken = "oiyndlpwhjpc",
            gsmAppId = "6a9e6d42dd74000d5e5ffbb0",
            splashConfig = SplashConfig(
                uiSplashConfig = UiSplashConfig(
                    resLayout = R.layout.activity_splash,
                    showFOForever = false,
                    homeActivity = MainActivity::class.java,
                    timeout = 45_000
                ),
                adsSplashConfig = AdsSplashConfig(
                    bannerId = getString(R.string.banner_splash),
                    interHighId = getString(R.string.inter_splash_high),
                    interAllId = getString(R.string.inter_splash),
//                    nativeFullId = getString(R.string.native_splash_full),
//                    nativeFullHighId = getString(R.string.native_splash_full_high),
                    nativeFullLayout = R.layout.layout_native_full,
                    admobAOAId = getString(R.string.resume_open_app),
                    isCheckOrganicUser = true
                )
            ),
            languageConfig =
                LanguageConfig(
                    uiLanguageConfig = UiLanguageConfig(
                        resLayout = R.layout.activity_language_app,
                        itemLangDefault = R.layout.item_select_language_default,
                        itemLangSelected = R.layout.item_select_language_selected,
                        listLanguage = EnumSelectLanguage.toLanguageModelList(),
                        languageSetting = object : LanguageSetting {
                            override fun onDone(activity: Activity) {
                                val code = CSCSPFHolder.get().language_code_selected
                                val finalCode = if (!code.isNullOrBlank()) code else SystemUtil.getPreLanguage(activity)
                                android.util.Log.d("LanguageDebug", "App languageSetting.onDone: code=$code, finalCode=$finalCode")
                                SystemUtil.changeLang(finalCode, activity)
                                activity.finish()
                            }
                        }
                    ),
                    adsLanguageConfig = AdsLanguageConfig(
                        nativeLangHighId = getString(R.string.native_language_high),
                        nativeLangId = getString(R.string.native_language),
                        nativeLangClickHighId = getString(R.string.native_language_high_click),
                        nativeLangClickId = getString(R.string.native_language_click),
                        layoutNative = R.layout.layout_native_media,
                        layoutNativeClick = R.layout.layout_native_media_click
                    )
                ),
            obConfig =
                OBConfig(
                    uiOBConfig = UiOBConfig(
                        resFragmentOB1 = R.layout.fragment_intro1,
                        resFragmentOB2 = R.layout.fragment_intro2,
                        resFragmentOB3 = R.layout.fragment_intro3,
                        resFragmentOB4 = R.layout.fragment_intro4,
                        activityCallback = object : OnActivityCallBack {
                            override fun onNextActivity(activity: Activity, inSession2: Boolean) {
                                val savedLang = CSCSPFHolder.get().language_code_selected
                                if (!savedLang.isNullOrBlank()) {
                                    SystemUtil.changeLang(savedLang, activity)
                                }
                                if (inSession2) {
                                    val isOverlayGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        android.provider.Settings.canDrawOverlays(activity)
                                    } else {
                                        true
                                    }

                                    val targetClass = if (isOverlayGranted) {
                                        MainActivity::class.java
                                    } else {
                                        PermissionActivity::class.java
                                    }
                                    val intent = Intent(activity, targetClass).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    }
                                    activity.startActivity(intent)
                                }
                            }
                        },
                        nextOBActivity = PermissionActivity::class.java
                    ),
                    adsOBConfig = AdsOBConfig(
                        nativeOB1Id = getString(R.string.native_onboarding_1),
                        nativeOB4Id = getString(R.string.native_onboarding_4),
                        nativeOBFull12Id = getString(R.string.native_onboarding_full_1),
                        nativeOBFull23Id = getString(R.string.native_onboarding_full_2),
                        layoutNativeOB1 = R.layout.layout_native_media,
                        layoutNativeOB4 = R.layout.layout_native_media,
                        layoutNativeFullOB = R.layout.admob_layout_native_full,
                        isLoadNativeOBInLanguage = true,
                    )
                ))

        try {
            val field = CSCApplication::class.java.getDeclaredField("cscOpenAds").apply { isAccessible = true }
            com.widget.themes.controlcenter.core.ads.AdsManager.registerOpenAds(field.get(cscLibrary) as? com.cscmobi.libraryads.ads.open_ads.CSCOpenAds)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

