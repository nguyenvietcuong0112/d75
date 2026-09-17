package com.widget.themes.controlcenter.feature_control_center.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_control_center.controller.*
import com.widget.themes.controlcenter.feature_control_center.model.ControlThemeAssets

class ControlCenterPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivPanelBackground: ImageView
    private val viewPullBar: View

    // Connectivity
    private val ivConnectivityBg: ImageView
    private val btnWifi: ImageView
    private val btnBluetooth: ImageView
    private val btnAirplane: ImageView
    private val btnData: ImageView

    // Music
    private val ivMusicBg: ImageView
    private val tvMusicTitle: TextView
    private val tvMusicArtist: TextView
    private val btnMusicPrev: ImageView
    private val btnMusicPlayPause: ImageView
    private val btnMusicNext: ImageView

    // Middle Row
    private val btnOrientationLock: View
    private val ivOrientationLock: ImageView
    private val btnSilentMode: View
    private val ivSilentMode: ImageView
    private val btnFocus: View
    private val ivFocus: ImageView
    private val sliderBrightness: VerticalSliderView
    private val sliderVolume: VerticalSliderView

    // Bottom Row 1 Shortcuts
    private val btnFlashlight: View
    private val ivFlashlight: ImageView
    private val btnClock: View
    private val ivClock: ImageView
    private val btnCalculator: View
    private val ivCalculator: ImageView
    private val btnCamera: View
    private val ivCamera: ImageView

    // Bottom Row 2 Shortcuts
    private val btnRecording: View
    private val ivRecording: ImageView
    private val btnRecord: View
    private val ivRecord: ImageView
    private val btnBattery: View
    private val ivBattery: ImageView
    private val btnScreenshot: View
    private val ivScreenshot: ImageView

    // Bottom Row 3 Settings
    private val btnSetup: View
    private val ivSetup: ImageView

    // Controllers
    val flashlightController = FlashlightController(context)
    val volumeController = VolumeController(context)
    val brightnessController = BrightnessController(context)
    val connectivityController = ConnectivityController(context)
    val mediaController = MediaController(context)
    val shortcutController = ShortcutController(context)

    private var currentAssets: ControlThemeAssets? = null
    var onCloseRequested: (() -> Unit)? = null

    private var startY: Float = 0f

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_control_center_panel, this, true)

        ivPanelBackground = findViewById(R.id.ivPanelBackground)
        viewPullBar = findViewById(R.id.viewPullBar)

        ivConnectivityBg = findViewById(R.id.ivConnectivityBg)
        btnWifi = findViewById(R.id.btnWifi)
        btnBluetooth = findViewById(R.id.btnBluetooth)
        btnAirplane = findViewById(R.id.btnAirplane)
        btnData = findViewById(R.id.btnData)

        ivMusicBg = findViewById(R.id.ivMusicBg)
        tvMusicTitle = findViewById(R.id.tvMusicTitle)
        tvMusicArtist = findViewById(R.id.tvMusicArtist)
        btnMusicPrev = findViewById(R.id.btnMusicPrev)
        btnMusicPlayPause = findViewById(R.id.btnMusicPlayPause)
        btnMusicNext = findViewById(R.id.btnMusicNext)

        btnOrientationLock = findViewById(R.id.btnOrientationLock)
        ivOrientationLock = findViewById(R.id.ivOrientationLock)
        btnSilentMode = findViewById(R.id.btnSilentMode)
        ivSilentMode = findViewById(R.id.ivSilentMode)
        btnFocus = findViewById(R.id.btnFocus)
        ivFocus = findViewById(R.id.ivFocus)
        sliderBrightness = findViewById(R.id.sliderBrightness)
        sliderVolume = findViewById(R.id.sliderVolume)

        btnFlashlight = findViewById(R.id.btnFlashlight)
        ivFlashlight = findViewById(R.id.ivFlashlight)
        btnClock = findViewById(R.id.btnClock)
        ivClock = findViewById(R.id.ivClock)
        btnCalculator = findViewById(R.id.btnCalculator)
        ivCalculator = findViewById(R.id.ivCalculator)
        btnCamera = findViewById(R.id.btnCamera)
        ivCamera = findViewById(R.id.ivCamera)

        btnRecording = findViewById(R.id.btnRecording)
        ivRecording = findViewById(R.id.ivRecording)
        btnRecord = findViewById(R.id.btnRecord)
        ivRecord = findViewById(R.id.ivRecord)
        btnBattery = findViewById(R.id.btnBattery)
        ivBattery = findViewById(R.id.ivBattery)
        btnScreenshot = findViewById(R.id.btnScreenshot)
        ivScreenshot = findViewById(R.id.ivScreenshot)

        btnSetup = findViewById(R.id.btnSetup)
        ivSetup = findViewById(R.id.ivSetup)

        setupListeners()
        applyTheme(ControlThemeAssets())
    }

    private fun setupListeners() {
        viewPullBar.setOnClickListener {
            onCloseRequested?.invoke()
        }

        ivPanelBackground.setOnClickListener {
            onCloseRequested?.invoke()
        }

        var startTouchX = 0f
        var startTouchY = 0f

        // Swipe up or swipe right to close
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTouchX = event.x
                    startTouchY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.x - startTouchX
                    val deltaY = event.y - startTouchY
                    if (deltaY < -50 || deltaX > 80) { // Swipe up or swipe right
                        onCloseRequested?.invoke()
                    }
                    true
                }
                else -> false
            }
        }

        // Connectivity Toggles
        btnWifi.setOnClickListener {
            connectivityController.toggleWifi()
            updateUIStates()
        }

        btnBluetooth.setOnClickListener {
            connectivityController.toggleBluetooth()
            updateUIStates()
        }

        btnAirplane.setOnClickListener {
            connectivityController.openAirplaneSettings()
        }

        btnData.setOnClickListener {
            connectivityController.openDataSettings()
        }

        // Quick Action Toggles
        btnOrientationLock.setOnClickListener {
            connectivityController.toggleAutoRotation()
            updateUIStates()
        }

        btnSilentMode.setOnClickListener {
            connectivityController.toggleSilentMode()
            updateUIStates()
        }

        btnFocus.setOnClickListener {
            connectivityController.toggleSilentMode()
            updateUIStates()
        }

        // Shortcuts Row 1
        btnFlashlight.setOnClickListener {
            flashlightController.toggle()
            updateUIStates()
        }

        btnClock.setOnClickListener {
            shortcutController.openClock()
            onCloseRequested?.invoke()
        }

        btnCalculator.setOnClickListener {
            shortcutController.openCalculator()
            onCloseRequested?.invoke()
        }

        btnCamera.setOnClickListener {
            shortcutController.openCamera()
            onCloseRequested?.invoke()
        }

        // Shortcuts Row 2 & 3
        btnRecording.setOnClickListener {
            shortcutController.openVoiceRecorder()
            onCloseRequested?.invoke()
        }

        btnRecord.setOnClickListener {
            shortcutController.openScreenRecorder()
            onCloseRequested?.invoke()
        }

        btnBattery.setOnClickListener {
            shortcutController.openBatterySettings()
            onCloseRequested?.invoke()
        }

        btnScreenshot.setOnClickListener {
            onCloseRequested?.invoke()
            postDelayed({
                shortcutController.takeScreenshot()
            }, 300)
        }

        btnSetup.setOnClickListener {
            shortcutController.openSettings()
            onCloseRequested?.invoke()
        }

        // Media Controls
        btnMusicPlayPause.setOnClickListener {
            mediaController.playPause()
            postDelayed({ updateUIStates() }, 200)
        }

        btnMusicNext.setOnClickListener {
            mediaController.skipToNext()
        }

        btnMusicPrev.setOnClickListener {
            mediaController.skipToPrevious()
        }

        // Vertical Sliders
        sliderVolume.progress = volumeController.getVolumePercent()
        sliderVolume.setOnProgressChangeListener { percent, fromUser ->
            if (fromUser) {
                volumeController.setVolumePercent(percent)
            }
        }

        sliderBrightness.progress = brightnessController.getBrightnessPercent()
        sliderBrightness.setOnProgressChangeListener { percent, fromUser ->
            if (fromUser) {
                brightnessController.setBrightnessPercent(percent)
            }
        }

        // Observers
        volumeController.setOnVolumeChangedListener { _, _, percent ->
            sliderVolume.progress = percent
        }

        brightnessController.setOnBrightnessChangedListener { _, _, percent ->
            sliderBrightness.progress = percent
        }

        connectivityController.setOnConnectivityChangedListener {
            post { updateUIStates() }
        }

        flashlightController.setOnStateChangedListener {
            post { updateUIStates() }
        }
    }

    fun applyTheme(assets: ControlThemeAssets) {
        this.currentAssets = assets

        // Backgrounds
        if (assets.homeBg != null) {
            ivPanelBackground.setImageBitmap(assets.homeBg)
            ivPanelBackground.setBackgroundColor(Color.TRANSPARENT)
        } else {
            ivPanelBackground.setImageBitmap(null)
            ivPanelBackground.setBackgroundColor(Color.parseColor("#CC121214"))
        }

        if (assets.controlBg != null) {
            ivConnectivityBg.setImageBitmap(assets.controlBg)
        } else {
            ivConnectivityBg.setImageBitmap(null)
            ivConnectivityBg.setImageResource(R.drawable.bg_control_card_rounded)
        }

        if (assets.musicBg != null) {
            ivMusicBg.setImageBitmap(assets.musicBg)
        } else {
            ivMusicBg.setImageBitmap(null)
            ivMusicBg.setImageResource(R.drawable.bg_control_card_rounded)
        }

        // Sliders
        val lightIconFallback = if (assets.lightIcon == null) {
            getVectorBitmap(context, R.drawable.ic_cc_sun, (28 * resources.displayMetrics.density).toInt())
        } else assets.lightIcon

        val soundIconFallback = if (assets.soundBig == null) {
            getVectorBitmap(context, R.drawable.ic_cc_speaker, (28 * resources.displayMetrics.density).toInt())
        } else assets.soundBig

        sliderBrightness.setAssets(assets.lightBg, assets.lightSlide, lightIconFallback)
        sliderVolume.setAssets(assets.soundBg, assets.soundSlide, soundIconFallback)

        // Media buttons
        setImageOrFallback(btnMusicPrev, assets.musicPrev, R.drawable.ic_cc_prev)
        setImageOrFallback(btnMusicNext, assets.musicNext, R.drawable.ic_cc_next)

        // Bottom Row 1 Shortcuts
        setImageOrFallback(ivFlashlight, assets.flashlightOff, R.drawable.ic_cc_flashlight)
        setImageOrFallback(ivClock, assets.icTiming, R.drawable.ic_cc_clock)
        setImageOrFallback(ivCalculator, assets.icCalculator, R.drawable.ic_cc_calculator)
        setImageOrFallback(ivCamera, assets.icCamera, R.drawable.ic_cc_camera)

        // Bottom Row 2 Shortcuts
        setImageOrFallback(ivRecording, assets.icRecording ?: assets.icRecord, R.drawable.ic_cc_mic)
        setImageOrFallback(ivRecord, assets.icRecord ?: assets.icRecording, R.drawable.ic_cc_screen_record)
        setImageOrFallback(ivBattery, assets.icBattery, R.drawable.ic_cc_battery)
        setImageOrFallback(ivScreenshot, assets.icScreenshot ?: assets.icCamera, R.drawable.ic_cc_screenshot)

        // Bottom Row 3 Settings
        setImageOrFallback(ivSetup, assets.icSetup, R.drawable.ic_cc_settings)

        // Pull bar color
        viewPullBar.backgroundTintList = android.content.res.ColorStateList.valueOf(assets.spec.pullBarColor)

        updateUIStates()
    }

    fun updateUIStates() {
        val a = currentAssets

        // Wi-Fi
        val isWifi = connectivityController.isWifiEnabled()
        val wifiBmp = if (isWifi) a?.wifiOn ?: a?.wifiOff else a?.wifiOff
        if (wifiBmp != null) {
            btnWifi.setImageBitmap(wifiBmp)
            btnWifi.background = null
            btnWifi.setPadding(0, 0, 0, 0)
        } else {
            btnWifi.setImageResource(R.drawable.ic_cc_wifi)
            btnWifi.setBackgroundResource(if (isWifi) R.drawable.bg_control_circle_button_blue else R.drawable.bg_control_circle_button)
            val p = (12 * resources.displayMetrics.density).toInt()
            btnWifi.setPadding(p, p, p, p)
        }

        // Bluetooth
        val isBt = connectivityController.isBluetoothEnabled()
        val btBmp = if (isBt) a?.btOn ?: a?.btOff else a?.btOff
        if (btBmp != null) {
            btnBluetooth.setImageBitmap(btBmp)
            btnBluetooth.background = null
            btnBluetooth.setPadding(0, 0, 0, 0)
        } else {
            btnBluetooth.setImageResource(R.drawable.ic_cc_bluetooth)
            btnBluetooth.setBackgroundResource(if (isBt) R.drawable.bg_control_circle_button_blue else R.drawable.bg_control_circle_button)
            val p = (12 * resources.displayMetrics.density).toInt()
            btnBluetooth.setPadding(p, p, p, p)
        }

        // Airplane
        val isAirplane = connectivityController.isAirplaneModeOn()
        val airplaneBmp = if (isAirplane) a?.airplaneOn ?: a?.airplaneOff else a?.airplaneOff
        if (airplaneBmp != null) {
            btnAirplane.setImageBitmap(airplaneBmp)
            btnAirplane.background = null
            btnAirplane.setPadding(0, 0, 0, 0)
        } else {
            btnAirplane.setImageResource(R.drawable.ic_cc_airplane)
            btnAirplane.setBackgroundResource(if (isAirplane) R.drawable.bg_control_circle_button_orange else R.drawable.bg_control_circle_button)
            val p = (12 * resources.displayMetrics.density).toInt()
            btnAirplane.setPadding(p, p, p, p)
        }

        // Data
        val dataBmp = a?.dataOff
        if (dataBmp != null) {
            btnData.setImageBitmap(dataBmp)
            btnData.background = null
            btnData.setPadding(0, 0, 0, 0)
        } else {
            btnData.setImageResource(R.drawable.ic_cc_cellular)
            btnData.setBackgroundResource(R.drawable.bg_control_circle_button_green)
            val p = (12 * resources.displayMetrics.density).toInt()
            btnData.setPadding(p, p, p, p)
        }

        // Orientation Lock
        val isAutoRotate = connectivityController.isAutoRotationEnabled()
        val lockBmp = if (!isAutoRotate) a?.lockOn ?: a?.lockOff else a?.lockOff
        setImageOrFallback(ivOrientationLock, lockBmp, R.drawable.ic_cc_lock)

        // Silent & Focus
        val isSilent = connectivityController.isSilentMode()
        val ringBmp = if (isSilent) a?.ringOn ?: a?.ringOff else a?.ringOff
        setImageOrFallback(ivSilentMode, ringBmp, R.drawable.ic_cc_bell)

        val focusBmp = if (isSilent) a?.focusOn ?: a?.focusOff else a?.focusOff
        setImageOrFallback(ivFocus, focusBmp, R.drawable.ic_cc_moon)

        // Flashlight
        val isTorch = flashlightController.isFlashlightOn
        val flashBmp = if (isTorch) a?.flashlightOn ?: a?.flashlightOff else a?.flashlightOff
        setImageOrFallback(ivFlashlight, flashBmp, R.drawable.ic_cc_flashlight)

        // Music Play/Pause
        val isPlaying = mediaController.isMusicActive()
        val playBmp = if (isPlaying) a?.musicPause ?: a?.musicPlay else a?.musicPlay
        setImageOrFallback(btnMusicPlayPause, playBmp, if (isPlaying) R.drawable.ic_cc_pause else R.drawable.ic_cc_play)

        // Sliders sync
        sliderVolume.progress = volumeController.getVolumePercent()
        sliderBrightness.progress = brightnessController.getBrightnessPercent()
    }

    private fun setImageOrFallback(iv: ImageView, bitmap: Bitmap?, @DrawableRes fallbackRes: Int) {
        if (bitmap != null) {
            iv.setImageBitmap(bitmap)
        } else {
            iv.setImageResource(fallbackRes)
        }
    }

    private fun getVectorBitmap(context: Context, @DrawableRes drawableId: Int, size: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        flashlightController.release()
        volumeController.release()
        brightnessController.release()
        connectivityController.release()
    }
}
