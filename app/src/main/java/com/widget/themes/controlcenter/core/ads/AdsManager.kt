package com.widget.themes.controlcenter.core.ads

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.cscmobi.libraryads.ads.banner_ads.CSCBanner
import com.cscmobi.libraryads.ads.inter_ads.CSCInter
import com.cscmobi.libraryads.ads.native_ads.CSCNativeManager
import com.cscmobi.libraryads.ads.native_ads.callback.INativeAdCallback
import com.cscmobi.libraryads.ads.open_ads.CSCOpenAds
import com.cscmobi.libraryads.ads.reward_ads.CSCReward
import com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs
import com.widget.themes.controlcenter.R

object AdsManager {
    private const val TAG = "AdsManager"

    private var openAdsInstance: CSCOpenAds? = null

    fun registerOpenAds(openAds: CSCOpenAds?) {
        this.openAdsInstance = openAds
    }

    fun disableAOA() {
        try {
            openAdsInstance?.disableShowAOA()
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling AOA", e)
        }
    }

    fun enableAOA() {
        try {
            openAdsInstance?.enableShowAOA()
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling AOA", e)
        }
    }

    /**
     * Show Banner in MainActivity or any screen.
     * If isCollapsible is false, requests standard BANNER_ADAPTIVE.
     * If isCollapsible is true, requests BANNER_COLLAPSIBLE_BOTTOM.
     */
    fun showBanner(
        activity: Activity,
        container: ViewGroup?,
        adUnitId: String = activity.getString(R.string.banner_home),
        isEnabled: Boolean = RemoteConfigs.banner_home,
        isCollapsible: Boolean = false,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (container == null) return
        if (!isEnabled || adUnitId.isBlank()) {
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }

        try {
            Log.d(TAG, "showBanner: unitId=$adUnitId, isEnabled=$isEnabled, isCollapsible=$isCollapsible")
            val frameLayout = if (container is FrameLayout) {
                container
            } else {
                val existing = container.findViewById<FrameLayout>(R.id.flBannerContainer)
                if (existing != null) {
                    existing
                } else {
                    val fl = FrameLayout(activity).apply { id = R.id.flBannerContainer }
                    container.removeAllViews()
                    container.addView(fl, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    fl
                }
            }
            container.visibility = View.VISIBLE
            val bannerType = if (isCollapsible) {
                CSCBanner.TypeAds.BANNER_COLLAPSIBLE_BOTTOM
            } else {
                CSCBanner.TypeAds.BANNER_ADAPTIVE
            }
            CSCBanner.requestBanner(
                activity = activity,
                id = adUnitId,
                typeAds = bannerType,
                adFrame = frameLayout,
                canShowAd = isEnabled,
                onResult = { adView ->
                    activity.runOnUiThread {
                        if (adView != null) {
                            Log.d(TAG, "Banner loaded successfully")
                            container.visibility = View.VISIBLE
                            onLoaded?.invoke()
                        } else {
                            Log.e(TAG, "Banner failed to load (adView is null)")
                            container.visibility = View.GONE
                            onFailed?.invoke()
                        }
                    }
                },
                onShown = {
                    Log.d(TAG, "Banner impression recorded")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting banner", e)
            container.visibility = View.GONE
            onFailed?.invoke()
        }
    }

    /**
     * Show Collapsible Bottom Banner in MainActivity or any screen
     */
    fun showCollapsibleBanner(
        activity: Activity,
        container: ViewGroup?,
        adUnitId: String = activity.getString(R.string.banner_collap_home),
        isEnabled: Boolean = RemoteConfigs.banner_collap_home,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showBanner(
            activity = activity,
            container = container,
            adUnitId = adUnitId,
            isEnabled = isEnabled,
            isCollapsible = true,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    fun destroyBanner(activity: Activity) {
        try {
            CSCBanner.destroyBannerAds(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying banner ad", e)
        }
    }

    /**
     * Show Collapsible Native Ad in FrameLayout / ViewGroup container
     */
    fun showCollapsibleNative(
        activity: androidx.appcompat.app.AppCompatActivity,
        container: ViewGroup?,
        adUnitId: String = activity.getString(R.string.native_collap),
        placementKey: String = "native_collap_${activity.javaClass.simpleName}",
        isEnabled: Boolean = RemoteConfigs.native_collap,
        layoutRes: Int = R.layout.layout_native_collapsible,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null,
        onClosed: (() -> Unit)? = null
    ) {
        if (container == null) return
        if (!isEnabled || adUnitId.isBlank() || activity.isFinishing || activity.isDestroyed) {
            container.visibility = View.GONE
            onFailed?.invoke()
            onClosed?.invoke()
            return
        }

        val frameLayout = if (container is FrameLayout) {
            container
        } else {
            val existing = container.findViewById<FrameLayout>(R.id.flNativeContainer)
            if (existing != null) {
                existing
            } else {
                val fl = FrameLayout(activity).apply { id = R.id.flNativeContainer }
                container.removeAllViews()
                container.addView(fl, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                fl
            }
        }

        try {
            Log.d(TAG, "showCollapsibleNative: key=$placementKey, unitId=$adUnitId, isEnabled=$isEnabled")
            container.visibility = View.VISIBLE
            CSCNativeManager.showCollapsibleNative(
                activity = activity,
                adFrame = frameLayout,
                adName = placementKey,
                adId = adUnitId,
                adLayout = layoutRes,
                refreshTime = 30000L,
                isPreload = false,
                canShowAd = isEnabled,
                callback = object : INativeAdCallback {
                    override fun onAdLoaded(adName: String) {
                        Log.d(TAG, "Collapsible native onAdLoaded: $adName")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.VISIBLE
                            val closeBtn = container.findViewById<View>(com.cscmobi.libraryads.R.id.btn_close_ad)
                            closeBtn?.setOnClickListener {
                                Log.d(TAG, "Collapsible native close button clicked")
                                frameLayout.visibility = View.GONE
                                container.visibility = View.GONE
                                onClosed?.invoke()
                            }
                            onLoaded?.invoke()
                        }
                    }

                    override fun onAdFailedToLoad(adName: String, errorMessage: String) {
                        Log.e(TAG, "Collapsible native onAdFailedToLoad: $adName, error=$errorMessage")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                            onFailed?.invoke()
                            onClosed?.invoke()
                        }
                    }

                    override fun onAdShowFailed(adName: String, reason: String) {
                        Log.e(TAG, "Collapsible native onAdShowFailed: $adName, reason=$reason")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                            onFailed?.invoke()
                            onClosed?.invoke()
                        }
                    }

                    override fun onAdDismissed(adName: String) {
                        Log.d(TAG, "Collapsible native onAdDismissed: $adName")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                            onClosed?.invoke()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error showing collapsible native ad", e)
            container.visibility = View.GONE
            onFailed?.invoke()
            onClosed?.invoke()
        }
    }

    fun stopCollapsible(activity: Activity) {
        try {
            CSCNativeManager.stopCollapsible(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping collapsible ad", e)
        }
    }

    /**
     * Load & Show Native Ad inside a container (FrameLayout or ViewGroup)
     */
    fun loadAndShowNative(
        activity: Activity,
        container: ViewGroup?,
        placementKey: String = "native_all_${activity.javaClass.simpleName}",
        adUnitId: String = activity.getString(R.string.native_all),
        layoutRes: Int = R.layout.layout_native_media,
        isEnabled: Boolean = RemoteConfigs.native_all,
        isShowLoading: Boolean = true,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (container == null) return
        if (!isEnabled || adUnitId.isBlank() || activity.isFinishing || activity.isDestroyed) {
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }

        try {
            val frameLayout: FrameLayout = if (container is FrameLayout) {
                container
            } else {
                val existing = container.findViewById<FrameLayout>(R.id.flNativeContainer)
                if (existing != null) {
                    existing
                } else {
                    val fl = FrameLayout(activity).apply { id = R.id.flNativeContainer }
                    container.removeAllViews()
                    container.addView(fl, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    fl
                }
            }

            val effectiveKey = if (placementKey.isNotBlank()) {
                if (placementKey.contains(activity.javaClass.simpleName)) placementKey else "${placementKey}_${activity.javaClass.simpleName}"
            } else {
                "native_${activity.javaClass.simpleName}"
            }

            Log.d(TAG, "loadAndShowNative: key=$effectiveKey, unitId=$adUnitId, isEnabled=$isEnabled")
            container.visibility = View.VISIBLE
            CSCNativeManager.loadAndShowNative(
                adFrame = frameLayout,
                adName = effectiveKey,
                adId = adUnitId,
                adLayout = layoutRes,
                isPreload = false,
                canShowAd = isEnabled,
                callback = object : INativeAdCallback {
                    override fun onAdLoaded(adName: String) {
                        Log.d(TAG, "Native onAdLoaded: $adName")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.VISIBLE
                            onLoaded?.invoke()
                        }
                    }

                    override fun onAdFailedToLoad(adName: String, errorMessage: String) {
                        Log.e(TAG, "Native onAdFailedToLoad: $adName, error=$errorMessage")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                            onFailed?.invoke()
                        }
                    }

                    override fun onAdShowFailed(adName: String, reason: String) {
                        Log.e(TAG, "Native onAdShowFailed: $adName, reason=$reason")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                            onFailed?.invoke()
                        }
                    }

                    override fun onAdDismissed(adName: String) {
                        Log.d(TAG, "Native onAdDismissed: $adName")
                        activity.runOnUiThread {
                            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                            container.visibility = View.GONE
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading native ad: $placementKey", e)
            container.visibility = View.GONE
            onFailed?.invoke()
        }
    }

    /**
     * Show Interstitial Ad with safety fallback so navigation never hangs
     */
    fun showInter(
        activity: Activity,
        adUnitId: String = activity.getString(R.string.inter_all),
        isEnabled: Boolean = RemoteConfigs.inter_all,
        onNext: () -> Unit
    ) {
        if (!isEnabled || adUnitId.isBlank() || activity.isFinishing || activity.isDestroyed) {
            onNext()
            return
        }

        var executed = false
        val safeNext: () -> Unit = {
            if (!executed) {
                executed = true
                try {
                    com.cscmobi.libraryads.ads.utils.StatusShowAd.isInterstitialShown = false
                } catch (e: Exception) {
                    Log.e(TAG, "Error resetting inter status", e)
                }
                activity.runOnUiThread { onNext() }
            }
        }

        try {
            CSCInter.showInter(
                activity,
                adUnitId,
                { _: Boolean -> safeNext() },
                { safeNext() },
                true,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error showing inter ad", e)
            safeNext()
        }
    }

    /**
     * Load & Show Interstitial Ad with timeout
     */
    fun loadAndShowInter(
        activity: Activity,
        adUnitId: String = activity.getString(R.string.inter_all),
        isEnabled: Boolean = RemoteConfigs.inter_all,
        timeout: Long = 6000L,
        onNext: () -> Unit
    ) {
        if (!isEnabled || adUnitId.isBlank() || activity.isFinishing || activity.isDestroyed) {
            onNext()
            return
        }

        var executed = false
        val safeNext: () -> Unit = {
            if (!executed) {
                executed = true
                try {
                    com.cscmobi.libraryads.ads.utils.StatusShowAd.isInterstitialShown = false
                } catch (e: Exception) {
                    Log.e(TAG, "Error resetting inter status", e)
                }
                activity.runOnUiThread { onNext() }
            }
        }

        try {
            CSCInter.loadAndShowInter(
                activity,
                adUnitId,
                timeout,
                0L,
                { _: Boolean -> safeNext() },
                { safeNext() },
                true,
                { safeNext() },
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loadAndShowInter", e)
            safeNext()
        }
    }

    /**
     * Show Rewarded Ad for unlocking premium features
     */
    fun showReward(
        activity: Activity,
        adUnitId: String = activity.getString(R.string.reward_ad),
        isEnabled: Boolean = RemoteConfigs.reward_ad,
        onUserEarnedReward: () -> Unit,
        onFailed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onFailed?.invoke()
            return
        }
        if (!isEnabled || adUnitId.isBlank()) {
            onUserEarnedReward()
            return
        }

        try {
            CSCReward.loadAndShowRewardAd(
                activity,
                adUnitId,
                8000L,
                { onUserEarnedReward() },
                {},
                { onFailed?.invoke() },
                { onFailed?.invoke() },
                {},
                {},
                true,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error showing reward ad", e)
            onFailed?.invoke()
        }
    }
}
