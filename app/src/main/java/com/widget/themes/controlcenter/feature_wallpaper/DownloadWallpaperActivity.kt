package com.widget.themes.controlcenter.feature_wallpaper

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.widget.themes.controlcenter.core.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWallpaperActivity : AppCompatActivity() {

    private lateinit var wallpaper: WidgetThemeWallpaper
    private lateinit var ivClose: ImageView
    private lateinit var pbLoading: ProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var btnApply: View

    private var loadedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_wallpaper)

        val path = intent.getStringExtra("wallpaper_path")
        val title = intent.getStringExtra("wallpaper_title") ?: "Wallpaper"
        val id = intent.getStringExtra("collection_id") ?: "wp_${System.currentTimeMillis()}"

        wallpaper = intent.getSerializableExtra("wallpaper_item") as? WidgetThemeWallpaper
            ?: intent.getSerializableExtra("WIDGET_WALLPAPER") as? WidgetThemeWallpaper
            ?: if (!path.isNullOrEmpty()) {
                val cleanPath = path.removePrefix("file:///android_asset/").removePrefix("/")
                WidgetThemeWallpaper(
                    id = id,
                    themeId = cleanPath,
                    name = title,
                    order = 0,
                    folder = cleanPath,
                    imageBg = "wallpaper",
                    category = "collection"
                )
            } else null
            ?: return finish()

        initViews()
        loadWallpaperImage()
    }

    private fun initViews() {
        ivClose = findViewById(R.id.ivClose)
        pbLoading = findViewById(R.id.pbLoading)
        viewPager = findViewById(R.id.viewPager)
        btnApply = findViewById(R.id.btnApply)

        val handleBack = {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = this,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { finish() }
            )
        }

        ivClose.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        btnApply.setOnClickListener {
            val bitmap = loadedBitmap
            if (bitmap != null) {
                val sheet = SetWallpaperBottomSheet(bitmap) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(this@DownloadWallpaperActivity)
                        val directPath = intent.getStringExtra("wallpaper_path")
                        val previewUrl = when {
                            !directPath.isNullOrBlank() && directPath.startsWith("http") -> directPath
                            wallpaper.folder.startsWith("http") -> wallpaper.folder
                            else -> wallpaper.getOnlinePreviewUri(this@DownloadWallpaperActivity).toString()
                        }
                        val targetPath = when {
                            wallpaper.folder.startsWith("http") -> wallpaper.folder
                            !directPath.isNullOrBlank() -> directPath
                            else -> wallpaper.getOnlineImageUri(this@DownloadWallpaperActivity).toString()
                        }
                        val title = intent.getStringExtra("wallpaper_title") ?: "Wallpaper"
                        val wpName = wallpaper.name.ifBlank { title }
                        val wpId = wallpaper.id.ifBlank { intent.getStringExtra("collection_id") ?: "wp_${System.currentTimeMillis()}" }

                        repo.markAsDownloaded(
                            id = wpId,
                            name = wpName,
                            category = "Wallpaper",
                            targetPath = targetPath,
                            previewPath = previewUrl,
                            rawType = "collection_wallpaper"
                        )
                    }
                }
                sheet.show(supportFragmentManager, "set_wallpaper_sheet")
            } else {
                Toast.makeText(this, getString(R.string.toast_wallpaper_downloading), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWallpaperImage() {
        pbLoading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val directPath = intent.getStringExtra("wallpaper_path")
            val onlineUrl = if (!directPath.isNullOrEmpty()) {
                if (directPath.startsWith("http://") || directPath.startsWith("https://") || directPath.startsWith("file://")) {
                    directPath
                } else {
                    "file:///android_asset/${directPath.removePrefix("/")}"
                }
            } else {
                wallpaper.getOnlineImageUri(this@DownloadWallpaperActivity).toString()
            }
            val previewFallbackUrl = wallpaper.getOnlinePreviewUri(this@DownloadWallpaperActivity).toString()
            android.util.Log.d("WallpaperFetch", "Loading high-res wallpaper: $onlineUrl (fallback: $previewFallbackUrl)")

            val bitmap = withContext(Dispatchers.IO) {
                try {
                    com.bumptech.glide.Glide.with(this@DownloadWallpaperActivity)
                        .asBitmap()
                        .load(onlineUrl)
                        .submit()
                        .get()
                } catch (e: Exception) {
                    try {
                        com.bumptech.glide.Glide.with(this@DownloadWallpaperActivity)
                            .asBitmap()
                            .load(previewFallbackUrl)
                            .submit()
                            .get()
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        null
                    }
                }
            }

            pbLoading.visibility = View.GONE
            if (bitmap != null) {
                loadedBitmap = bitmap
                setupViewPager(bitmap)
            } else {
                Toast.makeText(this@DownloadWallpaperActivity, getString(R.string.toast_failed_load_wallpaper), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupViewPager(bitmap: Bitmap) {
        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ImageHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
                val img = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return ImageHolder(img)
            }

            override fun onBindViewHolder(holder: ImageHolder, position: Int) {
                holder.imageView.setImageBitmap(bitmap)
            }

            override fun getItemCount(): Int = 1
        }
    }

    class ImageHolder(val imageView: ImageView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(imageView)
}
