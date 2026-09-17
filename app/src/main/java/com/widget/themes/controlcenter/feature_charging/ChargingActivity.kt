package com.widget.themes.controlcenter.feature_charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig

class ChargingActivity : AppCompatActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private val powerConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                finish()
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        com.widget.themes.controlcenter.ThemeApp.applyFullScreen(this)

        setContentView(R.layout.activity_charging)

        playerView = findViewById(R.id.playerView)

        // Read animation configuration
        val folder = com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getAppliedChargingFolder(this)

        if (folder.isNotEmpty()) {
            com.widget.themes.controlcenter.core.network.ViewTracker.track("charging", folder)
        }

        val config = resources.configuration
        val isFold = config.smallestScreenWidthDp >= 600
        val videoUrl = if (folder.startsWith("http://") || folder.startsWith("https://")) {
            folder
        } else {
            ResourceConfig.getChargingVideoUrl(folder, isFold)
        }

        setupPlayer(videoUrl)

        // Register power disconnected receiver
        val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionReceiver, filter)

        // Tap to dismiss
        findViewById<View>(R.id.rootView).setOnClickListener {
            finish()
        }
        playerView.setOnClickListener {
            finish()
        }
    }

    private fun setupPlayer(url: String) {
        try {
            val player = ExoPlayer.Builder(this).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
            exoPlayer = player
            playerView.player = player
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.toast_failed_init_video), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(powerConnectionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        releasePlayer()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            player.release()
            exoPlayer = null
        }
    }
}
