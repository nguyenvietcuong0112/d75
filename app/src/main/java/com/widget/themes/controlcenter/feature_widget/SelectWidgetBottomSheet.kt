package com.widget.themes.controlcenter.feature_widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.EventBus
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.data.Subscribe
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.ui.PreviewWidgetView
import com.widget.themes.controlcenter.databinding.FragmentSelectWidgetBottomSheetBinding
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import com.widget.themes.controlcenter.feature_widget.event.WidgetAddSucceedEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectWidgetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectWidgetBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var theme: ThemeItem
    private var widgetList: MutableList<ThemeWidgetItem> = mutableListOf()
    private var currentIndex: Int = 0
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            context?.let { WeatherHelper.updateWeather(it) }
            continueDownloadAndPin()
        } else {
            Toast.makeText(context, getString(R.string.toast_perm_location_required_weather), Toast.LENGTH_SHORT).show()
        }
    }

    fun setParams(
        theme: ThemeItem,
        widgetType: String,
        size: String,
        previewUrl: String? = null,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    ) {
        this.theme = theme
        this.widgetId = widgetId
        val resolvedPreview = previewUrl ?: run {
            val isMedium = size.lowercase() == "4x2" || size.lowercase() == "medium"
            val fileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
            val typeFolder = when (widgetType.lowercase()) {
                "clock" -> "clocks"
                "calendar", "date" -> "today"
                "weather" -> "weather"
                "image" -> "image"
                else -> widgetType.lowercase()
            }
            "${ResourceConfig.ASSET_BASE_URL}/assets_theme/${theme.path}/widgets/$typeFolder/$fileName"
        }
        val providerClass = if (size == "4x2") Widget4x2Provider::class.java else Widget2x2Provider::class.java
        this.widgetList = mutableListOf(
            ThemeWidgetItem(
                id = "${theme.id}_widget_${widgetType}_$size",
                name = "$widgetType $size",
                size = size,
                providerClass = providerClass,
                previewUrl = resolvedPreview,
                isSelected = true
            )
        )
        this.currentIndex = 0
    }

    fun setParams(
        theme: ThemeItem,
        widgetList: List<ThemeWidgetItem>,
        initialIndex: Int = 0,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    ) {
        this.theme = theme
        this.widgetList = widgetList.toMutableList()
        this.currentIndex = initialIndex.coerceIn(0, (widgetList.size - 1).coerceAtLeast(0))
        this.widgetId = widgetId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectWidgetBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        binding.clInstall.visibility = View.VISIBLE
        binding.tvDownload.text = if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "Add to Home" else "Apply Widget"

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.clInstall.setOnClickListener {
            downloadAndPinWidget()
        }

        setupCarousel()
    }

    private fun setupCarousel() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = SelectWidgetAdapter(widgetList)
        binding.recyclerView.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper)
        adapter.registerAdapterDataObserver(binding.indicator.adapterDataObserver)

        if (currentIndex in widgetList.indices) {
            layoutManager.scrollToPositionWithOffset(currentIndex, 0)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val pos = layoutManager.getPosition(snapView)
                    if (pos in widgetList.indices) {
                        currentIndex = pos
                    }
                }
            }
        })
    }

    private inner class SelectWidgetAdapter(
        private val list: List<ThemeWidgetItem>
    ) : RecyclerView.Adapter<SelectWidgetAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val cardView: CardView = view.findViewById(R.id.cardView)
            val previewView: PreviewWidgetView = view.findViewById(R.id.previewView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_preview_layout, parent, false
            )
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val density = context.resources.displayMetrics.density

            val currentType = when {
                item.rawType.isNotEmpty() -> item.rawType
                item.id.contains("_checklist_") -> "checklist_4x2"
                item.id.contains("_countdown_") -> "countdown_4x2"
                item.id.contains("_mini_calendar_") -> "mini_calendar_2x2"
                item.id.contains("_calendar_") -> "calendar_2x2"
                item.id.contains("_sleep_") -> "sleep_2x2"
                item.id.contains("_note_") -> "note_2x2"
                item.id.contains("_weather_") -> if (item.size == "4x2") "weather_4x2" else "weather_2x2"
                item.id.contains("_clock_") || item.id.contains("_clocks_") -> if (item.size == "4x2") "clock_4x2" else "clock_2x2"
                else -> if (item.size == "4x2") "clock_4x2" else "clock_2x2"
            }
            val currentSize = item.size

            val widgetSize = when (currentSize.lowercase()) {
                "2x2", "small" -> WidgetSize.SMALL
                "4x2", "medium" -> WidgetSize.MEDIUM
                "4x4", "large" -> WidgetSize.LARGE
                else -> WidgetSize.SMALL
            }

            val layoutRes = WidgetRenderHelper.getLayoutForWidget(widgetSize, currentType)

            val targetWidth = if (currentSize == "4x2") (320 * density).toInt() else (180 * density).toInt()
            val targetHeight = if (currentSize == "4x2") (160 * density).toInt() else (180 * density).toInt()

            holder.cardView.layoutParams = holder.cardView.layoutParams.apply {
                width = targetWidth
                height = targetHeight
            }
            holder.ivPreview.layoutParams = holder.ivPreview.layoutParams.apply {
                width = targetWidth
                height = targetHeight
            }

            val widgetItem = WidgetItem(
                id = theme.id,
                themeFolder = theme.path,
                name = theme.name,
                widgetType = currentType,
                size = currentSize,
                isFree = true,
                isFavorite = false
            )

            val placeholderBmp = WidgetRenderHelper.getSnapshotImage(
                context = context,
                layoutId = layoutRes,
                widgetSize = widgetSize,
                widgetItem = widgetItem,
                widgetId = widgetId,
                preloadedBackground = null
            )
            if (placeholderBmp != null) {
                holder.ivPreview.visibility = View.VISIBLE
                holder.cardView.visibility = View.GONE
                holder.ivPreview.setImageBitmap(placeholderBmp)
            } else {
                holder.ivPreview.visibility = View.GONE
                holder.cardView.visibility = View.VISIBLE
            }

            Glide.with(context)
                .asBitmap()
                .load(item.previewUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val realBmp = WidgetRenderHelper.getSnapshotImage(
                            context = context,
                            layoutId = layoutRes,
                            widgetSize = widgetSize,
                            widgetItem = widgetItem,
                            widgetId = widgetId,
                            preloadedBackground = resource
                        )
                        if (realBmp != null) {
                            holder.ivPreview.visibility = View.VISIBLE
                            holder.cardView.visibility = View.GONE
                            holder.ivPreview.setImageBitmap(realBmp)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        override fun getItemCount(): Int = list.size
    }

    private fun getCurrentWidgetInfo(): Pair<String, String> {
        val item = widgetList.getOrNull(currentIndex)
        val currentSize = item?.size ?: "2x2"
        val currentType = item?.let {
            when {
                it.rawType.isNotEmpty() -> it.rawType
                it.id.contains("_checklist_") -> "checklist_4x2"
                it.id.contains("_countdown_") -> "countdown_4x2"
                it.id.contains("_mini_calendar_") -> "mini_calendar_2x2"
                it.id.contains("_calendar_") -> "calendar_2x2"
                it.id.contains("_sleep_") -> "sleep_2x2"
                it.id.contains("_note_") -> "note_2x2"
                it.id.contains("_weather_") -> if (currentSize == "4x2") "weather_4x2" else "weather_2x2"
                it.id.contains("_clock_") || it.id.contains("_clocks_") -> if (currentSize == "4x2") "clock_4x2" else "clock_2x2"
                else -> if (currentSize == "4x2") "clock_4x2" else "clock_2x2"
            }
        } ?: if (currentSize == "4x2") "clock_4x2" else "clock_2x2"
        return Pair(currentType, currentSize)
    }

    private fun downloadAndPinWidget() {
        val activity = activity ?: return
        val (currentType, _) = getCurrentWidgetInfo()
        if (currentType.lowercase().contains("weather")) {
            val hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return
            }
        }
        continueDownloadAndPin()
    }

    private fun continueDownloadAndPin() {
        val activity = requireActivity()
        val (currentType, currentSize) = getCurrentWidgetInfo()

        val typeFolder = when (currentType.lowercase()) {
            "clock", "clocks" -> "clocks"
            "calendar", "today", "date" -> "today"
            "weather" -> "weather"
            "image", "photo" -> "image"
            else -> currentType.lowercase()
        }

        val isMedium = currentSize.lowercase() == "4x2" || currentSize.lowercase() == "medium"
        val previewFileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
        val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"

        lifecycleScope.launch(Dispatchers.IO) {
            val currentItem = widgetList.getOrNull(currentIndex)
            val isRemotePreview = currentItem?.previewUrl?.startsWith("http://") == true || currentItem?.previewUrl?.startsWith("https://") == true

            if (isRemotePreview && !currentItem?.previewUrl.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    val downloadDialog = DownloadDialogFragment()
                    downloadDialog.setParams(currentItem!!.previewUrl, object : DownloadDialogFragment.DownloadCallback {
                        override fun onDownloadComplete(bitmap: Bitmap) {
                            saveWidgetBackground(activity, bitmap, currentType, currentSize)
                            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                                dismissAllowingStateLoss()
                                requestPinWidget(activity, currentType, currentSize)
                            } else {
                                applyToExistingWidget(activity, bitmap, currentType, currentSize)
                            }
                        }

                        override fun onDownloadFailed() {
                            Toast.makeText(activity, getString(R.string.toast_failed_download_widget_assets), Toast.LENGTH_SHORT).show()
                        }
                    })
                    downloadDialog.show(parentFragmentManager, "download")
                }
                return@launch
            }

            val folder = ResourceConfig.getThemeFolderByPath(activity, theme.path)

            val cleanFolder = folder
                .removePrefix("file:///android_asset/")
                .removePrefix("file://android_asset/")
                .removePrefix("android_asset/")
                .removePrefix("/")

            val directName = when {
                currentType.lowercase().contains("checklist") -> "checklist_4x2"
                currentType.lowercase().contains("countdown") -> "countdown_4x2"
                currentType.lowercase().contains("mini_calendar") -> "mini_calendar_2x2"
                currentType.lowercase().contains("calendar") -> "calendar_2x2"
                currentType.lowercase().contains("sleep") -> "sleep_2x2"
                currentType.lowercase().contains("note") -> "note_2x2"
                currentType.lowercase().contains("weather") -> if (isMedium) "weather_4x2" else "weather_2x2"
                currentType.lowercase().contains("clock") -> if (isMedium) "clock_4x2" else "clock_2x2"
                else -> if (isMedium) "clock_4x2" else "clock_2x2"
            }

            var localBitmap: Bitmap? = null
            val isMedium = currentSize.lowercase() == "4x2" || currentSize.lowercase() == "medium"
            val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"
            val candidatePaths = listOf(
                // 1. Clean background files
                "$cleanFolder/widgets/$bgFileName",
                "$cleanFolder/$bgFileName",
                "assets_collection/$cleanFolder/widgets/$bgFileName",
                "assets_collection/theme/$cleanFolder/widgets/$bgFileName",
                "assets_collection/widget/$cleanFolder/$bgFileName",
                "assets_theme/$cleanFolder/widgets/$bgFileName",
                "assets_theme/category/$cleanFolder/widgets/$bgFileName",
                "assets_theme/category/Animal/$cleanFolder/widgets/$bgFileName",
                "assets_theme/category/Kawaii/$cleanFolder/widgets/$bgFileName",
                "assets_theme/category/Trending/$cleanFolder/widgets/$bgFileName",
                // 2. Direct name fallbacks
                "$cleanFolder/widgets/$directName.png",
                "assets_collection/$cleanFolder/widgets/$directName.png",
                "assets_theme/$cleanFolder/widgets/$directName.png",
                "assets_theme/category/$cleanFolder/widgets/$directName.png",
                // 3. Fallback defaults
                "assets_collection/theme/theme_1/widgets/$bgFileName",
                "assets_collection/widget/widget_1/$bgFileName",
                "assets_theme/category/Animal/theme_1/widgets/$bgFileName"
            )

            for (path in candidatePaths) {
                try {
                    activity.assets.open(path).use { stream ->
                        localBitmap = BitmapFactory.decodeStream(stream)
                    }
                    if (localBitmap != null) break
                } catch (e: Exception) {
                    // Try next
                }
            }

            if (localBitmap != null) {
                val finalBmp = localBitmap!!
                withContext(Dispatchers.Main) {
                    saveWidgetBackground(activity, finalBmp, currentType, currentSize)
                    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                        dismissAllowingStateLoss()
                        requestPinWidget(activity, currentType, currentSize)
                    } else {
                        applyToExistingWidget(activity, finalBmp, currentType, currentSize)
                    }
                }
            } else {
                val cdnUrl = if (folder.startsWith("assets_collection/")) {
                    val clean = folder.removePrefix("assets_collection/").removePrefix("widget/")
                    "${ResourceConfig.ASSET_BASE_URL}/assets_collection/widget/$clean/$previewFileName"
                } else {
                    "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$folder/widgets/$typeFolder/$previewFileName"
                }

                withContext(Dispatchers.Main) {
                    val downloadDialog = DownloadDialogFragment()
                    downloadDialog.setParams(cdnUrl, object : DownloadDialogFragment.DownloadCallback {
                        override fun onDownloadComplete(bitmap: Bitmap) {
                            saveWidgetBackground(activity, bitmap, currentType, currentSize)
                            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                                dismissAllowingStateLoss()
                                requestPinWidget(activity, currentType, currentSize)
                            } else {
                                applyToExistingWidget(activity, bitmap, currentType, currentSize)
                            }
                        }

                        override fun onDownloadFailed() {
                            Toast.makeText(activity, getString(R.string.toast_failed_download_widget_assets), Toast.LENGTH_SHORT).show()
                        }
                    })
                    downloadDialog.show(parentFragmentManager, "download")
                }
            }
        }
    }

    private fun saveWidgetBackground(context: Context, bitmap: Bitmap, currentType: String, currentSize: String) {
        try {
            val normSize = when (currentSize.lowercase()) {
                "small", "2x2" -> "2x2"
                "medium", "4x2" -> "4x2"
                "large", "4x4" -> "4x4"
                else -> currentSize
            }
            val cleanId = theme.id.replace('/', '_').replace('\\', '_')
            val cleanPath = theme.path.replace('/', '_').replace('\\', '_')
            val widgetGenId = "widget_${cleanPath}_${currentType}_$normSize"
            val fileName = "widget_bg_${cleanId}_${currentType}_$normSize.png"
            val fileGenName = "widget_bg_${widgetGenId}_${currentType}_$normSize.png"
            val fileRawSizeName = "widget_bg_${cleanId}_${currentType}_$currentSize.png"

            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (fileRawSizeName != fileName) {
                try {
                    context.openFileOutput(fileRawSizeName, Context.MODE_PRIVATE).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
            try {
                context.openFileOutput(fileGenName, Context.MODE_PRIVATE).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: Exception) {
                // ignore
            }
            com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.setWidgetBgPath(
                context = context,
                cleanId = cleanId,
                type = currentType,
                size = normSize,
                fileName = fileName
            )
            if (normSize != currentSize) {
                com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.setWidgetBgPath(
                    context = context,
                    cleanId = cleanId,
                    type = currentType,
                    size = currentSize,
                    fileName = fileName
                )
            }

            val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(context)
            lifecycleScope.launch(Dispatchers.IO) {
                val savedFile = context.getFileStreamPath(fileName)
                val isMedium = normSize == "4x2"
                val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"
                val resolvedPreview = if (savedFile != null && savedFile.exists()) {
                    savedFile.absolutePath
                } else {
                    when {
                        theme.path.startsWith("assets_collection/") -> "file:///android_asset/${theme.path}/widgets/$bgFileName"
                        theme.path.startsWith("assets_theme/") -> "file:///android_asset/${theme.path}/widgets/$bgFileName"
                        theme.path.startsWith("category/") -> "file:///android_asset/assets_theme/${theme.path}/widgets/$bgFileName"
                        else -> "file:///android_asset/assets_theme/category/${theme.path}/widgets/$bgFileName"
                    }
                }
                repo.markAsDownloaded(
                    id = widgetGenId,
                    name = "${theme.name} ${currentType.replaceFirstChar { it.uppercase() }}",
                    category = "Widget",
                    targetPath = theme.path,
                    previewPath = resolvedPreview,
                    rawType = currentType,
                    extra = normSize
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPinWidget(context: Context, currentType: String, currentSize: String) {
        val widgetSize = when (currentSize.lowercase()) {
            "2x2", "small" -> WidgetSize.SMALL
            "4x2", "medium" -> WidgetSize.MEDIUM
            "4x4", "large" -> WidgetSize.LARGE
            else -> WidgetSize.SMALL
        }

        val providerClass = when (widgetSize) {
            WidgetSize.SMALL -> Widget2x2Provider::class
            WidgetSize.MEDIUM -> Widget4x2Provider::class
            WidgetSize.LARGE -> Widget4x4Provider::class
        }

        val normSize = when (currentSize.lowercase()) {
            "2x2", "small" -> "2x2"
            "4x2", "medium" -> "4x2"
            "4x4", "large" -> "4x4"
            else -> currentSize
        }

        val widgetItem = WidgetItem(
            id = theme.id,
            themeFolder = theme.path,
            name = theme.name,
            widgetType = currentType,
            size = normSize,
            isFree = true,
            isFavorite = false
        )

        context.addWidget(providerClass, widgetItem, isMineOrCustom = false)
    }

    private fun applyToExistingWidget(activity: FragmentActivity, bitmap: Bitmap, currentType: String, currentSize: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val normSize = when (currentSize.lowercase()) {
                    "small", "2x2" -> "2x2"
                    "medium", "4x2" -> "4x2"
                    "large", "4x4" -> "4x4"
                    else -> currentSize
                }
                val cleanId = theme.id.replace('/', '_').replace('\\', '_')
                val fileName = "widget_bg_${cleanId}_${currentType}_$normSize.png"
                val file = activity.getFileStreamPath(fileName)
                val imageUriStr = if (file != null && file.exists()) Uri.fromFile(file).toString() else null

                val config = WidgetConfig(
                    widgetId = widgetId,
                    bgType = if (imageUriStr != null) "IMAGE" else "COLOR",
                    solidColor = 0xFF232433.toInt(),
                    imageUri = imageUriStr,
                    textColor = android.graphics.Color.WHITE,
                    fontStyle = "normal",
                    gradientStartColor = 0,
                    gradientEndColor = 0
                )

                com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.getPrefs(
                    activity, com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.PREFS_WIDGET
                ).edit()
                    .putString("widget_type_$widgetId", currentType)
                    .putString("theme_folder_$widgetId", theme.path)
                    .putString("theme_id_$widgetId", theme.id)
                    .putString("widget_size_$widgetId", normSize)
                    .apply()

                ServiceLocator.getWidgetConfigDao(activity).saveConfig(config)
                withContext(Dispatchers.Main) {
                    val appWidgetManager = AppWidgetManager.getInstance(activity)
                    Widget2x2Provider().updateWidget(activity, appWidgetManager, widgetId)
                    Widget4x2Provider().updateWidget(activity, appWidgetManager, widgetId)
                    Widget4x4Provider().updateWidget(activity, appWidgetManager, widgetId)

                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    }
                    activity.setResult(Activity.RESULT_OK, resultValue)
                    Toast.makeText(activity, getString(R.string.toast_widget_applied_success), Toast.LENGTH_SHORT).show()
                    dismissAllowingStateLoss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, getString(R.string.toast_failed_apply_widget_style), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Subscribe
    fun onWidgetAddSucceed(event: WidgetAddSucceedEvent) {
        val activity = activity as? FragmentActivity
        dismissAllowingStateLoss()
        if (activity != null) {
            SetupSucceedDialogFragment().show(activity.supportFragmentManager, "success")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
    }
}
