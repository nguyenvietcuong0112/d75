package com.widget.themes.controlcenter.feature_control_center.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.json.JSONObject
import java.io.InputStream

data class ControlSpec(
    val pullBarColor: Int = Color.WHITE,
    val controlTextColor: Int = Color.WHITE,
    val brightnessTextColor: Int = Color.WHITE,
    val volumeTextColor: Int = Color.WHITE,
    val musicKnownTitleColor: Int = Color.WHITE,
    val musicKnownSingerColor: Int = Color.WHITE
)

data class ControlThemeAssets(
    val themeFolder: String = "",
    val spec: ControlSpec = ControlSpec(),
    val homeBg: Bitmap? = null,
    val controlBg: Bitmap? = null,
    val musicBg: Bitmap? = null,
    val wifiOff: Bitmap? = null,
    val wifiOn: Bitmap? = null,
    val btOff: Bitmap? = null,
    val btOn: Bitmap? = null,
    val airplaneOff: Bitmap? = null,
    val airplaneOn: Bitmap? = null,
    val dataOff: Bitmap? = null,
    val dataOn: Bitmap? = null,
    val lightBg: Bitmap? = null,
    val lightSlide: Bitmap? = null,
    val lightIcon: Bitmap? = null,
    val soundBg: Bitmap? = null,
    val soundSlide: Bitmap? = null,
    val soundBig: Bitmap? = null,
    val soundMute: Bitmap? = null,
    val musicPlay: Bitmap? = null,
    val musicPause: Bitmap? = null,
    val musicNext: Bitmap? = null,
    val musicPrev: Bitmap? = null,
    val lockOff: Bitmap? = null,
    val lockOn: Bitmap? = null,
    val ringOff: Bitmap? = null,
    val ringOn: Bitmap? = null,
    val focusOff: Bitmap? = null,
    val focusOn: Bitmap? = null,
    val flashlightOff: Bitmap? = null,
    val flashlightOn: Bitmap? = null,
    val icCalculator: Bitmap? = null,
    val icCamera: Bitmap? = null,
    val icTiming: Bitmap? = null,
    val icBattery: Bitmap? = null,
    val icSetup: Bitmap? = null,
    val icRecord: Bitmap? = null,
    val icRecording: Bitmap? = null,
    val icScreenshot: Bitmap? = null
)

class ControlThemeLoader(private val context: Context) {

    private val TAG = "ControlThemeLoader"

    fun loadTheme(themePath: String): ControlThemeAssets {
        if (themePath.isBlank()) {
            return ControlThemeAssets()
        }
        val cleanPath = themePath.removePrefix("file:///android_asset/").removePrefix("android_asset/").removePrefix("file://")

        val file = java.io.File(cleanPath)
        val resolvedDir: java.io.File? = if (file.exists() && file.isDirectory) {
            resolveThemeDir(file)
        } else if (cleanPath.startsWith("/") && file.exists()) {
            file
        } else {
            null
        }

        val isFile = resolvedDir != null
        val basePath = resolvedDir?.absolutePath ?: cleanPath
        android.util.Log.d(TAG, "Loading control theme from: $basePath (isFile: $isFile)")

        val spec = loadSpec(basePath, isFile)

        return ControlThemeAssets(
            themeFolder = basePath,
            spec = spec,
            homeBg = loadBitmap(basePath, "home_bg.jpg", isFile)
                ?: loadBitmap(basePath, "home_bg.png", isFile)
                ?: loadBitmap(basePath, "home_bg.webp", isFile),
            controlBg = loadBitmap(basePath, "home_control_bg.png", isFile)
                ?: loadBitmap(basePath, "control_bg.png", isFile),
            musicBg = loadBitmap(basePath, "home_music_bg.png", isFile)
                ?: loadBitmap(basePath, "music_bg.png", isFile),
            wifiOff = loadBitmap(basePath, "common_control_wifi.png", isFile),
            wifiOn = loadBitmap(basePath, "common_control_wifi_select.png", isFile),
            btOff = loadBitmap(basePath, "common_control_ic_bluetooth.png", isFile),
            btOn = loadBitmap(basePath, "common_control_ic_bluetooth_select.png", isFile),
            airplaneOff = loadBitmap(basePath, "common_control_ic_airplane.png", isFile),
            airplaneOn = loadBitmap(basePath, "common_control_ic_airplane_select.png", isFile),
            dataOff = loadBitmap(basePath, "common_control_ic_data.png", isFile),
            dataOn = loadBitmap(basePath, "common_control_ic_data_select.png", isFile),
            lightBg = loadBitmap(basePath, "common_light_bg.png", isFile),
            lightSlide = loadBitmap(basePath, "common_light_slide.png", isFile),
            lightIcon = loadBitmap(basePath, "home_light_icon.png", isFile)
                ?: loadBitmap(basePath, "light_ic_light.png", isFile),
            soundBg = loadBitmap(basePath, "common_home_sound_bg.png", isFile),
            soundSlide = loadBitmap(basePath, "common_home_sound_slide.png", isFile),
            soundBig = loadBitmap(basePath, "common_home_sound_big.png", isFile),
            soundMute = loadBitmap(basePath, "common_home_sound_mute.png", isFile),
            musicPlay = loadBitmap(basePath, "common_music_ic_play.png", isFile),
            musicPause = loadBitmap(basePath, "common_music_ic_pause.png", isFile),
            musicNext = loadBitmap(basePath, "common_music_ic_next.png", isFile),
            musicPrev = loadBitmap(basePath, "common_music_ic_previous.png", isFile),
            lockOff = loadBitmap(basePath, "home_second_ic_locking.png", isFile),
            lockOn = loadBitmap(basePath, "home_second_ic_locking_select.png", isFile),
            ringOff = loadBitmap(basePath, "home_second_ic_ring.png", isFile),
            ringOn = loadBitmap(basePath, "home_second_ic_ring_select.png", isFile),
            focusOff = loadBitmap(basePath, "home_second_ic_focus.png", isFile),
            focusOn = loadBitmap(basePath, "home_second_ic_focus_select.png", isFile),
            flashlightOff = loadBitmap(basePath, "home_bottom_ic_flashlight.png", isFile),
            flashlightOn = loadBitmap(basePath, "home_bottom_ic_flashlight_select.png", isFile),
            icCalculator = loadBitmap(basePath, "home_bottom_ic_calculator.png", isFile),
            icCamera = loadBitmap(basePath, "home_bottom_ic_camera.png", isFile),
            icTiming = loadBitmap(basePath, "home_bottom_ic_timing.png", isFile),
            icBattery = loadBitmap(basePath, "home_bottom_ic_battery.png", isFile),
            icSetup = loadBitmap(basePath, "home_bottom_ic_setup.png", isFile),
            icRecord = loadBitmap(basePath, "home_bottom_ic_record.png", isFile),
            icRecording = loadBitmap(basePath, "home_bottom_ic_recording.png", isFile),
            icScreenshot = loadBitmap(basePath, "home_bottom_ic_screenshot.png", isFile)
        )
    }

    private fun resolveThemeDir(dir: java.io.File): java.io.File {
        if (java.io.File(dir, "home_bg.jpg").exists() ||
            java.io.File(dir, "home_bg.png").exists() ||
            java.io.File(dir, "home_control_bg.png").exists() ||
            java.io.File(dir, "control_spec.json").exists()) {
            return dir
        }
        val subDirs = dir.listFiles()?.filter { it.isDirectory && !it.name.startsWith("__") } ?: emptyList()
        for (sub in subDirs) {
            if (java.io.File(sub, "home_bg.jpg").exists() ||
                java.io.File(sub, "home_bg.png").exists() ||
                java.io.File(sub, "home_control_bg.png").exists() ||
                java.io.File(sub, "control_spec.json").exists()) {
                return sub
            }
        }
        return dir
    }

    private fun loadSpec(basePath: String, isFile: Boolean): ControlSpec {
        return try {
            val jsonStr = if (isFile) {
                val f = java.io.File(basePath, "control_spec.json")
                if (f.exists()) f.readText() else return ControlSpec()
            } else {
                context.assets.open("$basePath/control_spec.json").bufferedReader().use { it.readText() }
            }
            val json = JSONObject(jsonStr)
            ControlSpec(
                pullBarColor = parseColorSafe(json.optString("pullBarColor"), Color.WHITE),
                controlTextColor = parseColorSafe(json.optString("controlTextColor"), Color.WHITE),
                brightnessTextColor = parseColorSafe(json.optString("brightnessTextColor"), Color.WHITE),
                volumeTextColor = parseColorSafe(json.optString("volumeTextColor"), Color.WHITE),
                musicKnownTitleColor = parseColorSafe(json.optString("musicKnownTitleColor"), Color.WHITE),
                musicKnownSingerColor = parseColorSafe(json.optString("musicKnownSingerColor"), Color.WHITE)
            )
        } catch (e: Exception) {
            ControlSpec()
        }
    }

    private fun parseColorSafe(colorStr: String?, defaultColor: Int): Int {
        if (colorStr.isNullOrBlank()) return defaultColor
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            defaultColor
        }
    }

    private fun loadBitmap(basePath: String, fileName: String, isFile: Boolean): Bitmap? {
        return try {
            if (isFile) {
                val f = java.io.File(basePath, fileName)
                if (f.exists()) {
                    BitmapFactory.decodeFile(f.absolutePath)
                } else null
            } else {
                val isStream: InputStream = context.assets.open("$basePath/$fileName")
                isStream.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
