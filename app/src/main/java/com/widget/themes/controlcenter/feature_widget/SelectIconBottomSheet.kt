package com.widget.themes.controlcenter.feature_widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.EventBus
import com.widget.themes.controlcenter.core.data.Subscribe
import com.widget.themes.controlcenter.core.data.ShortcutEvent
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.databinding.FragmentAppBottomSheetDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.widget.themes.controlcenter.core.utils.IconComposerHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class SelectIconBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAppBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private var theme: ThemeItem? = null
    private var selectedIcons: List<ThemeIconItem> = emptyList()
    private val installQueue = ArrayList<ThemeIconItem>()
    private var fallbackJob: Job? = null

    private val shortcutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.widget.themes.controlcenter.SHORTCUT_INSTALLED") {
                EventBus.getDefault().post(ShortcutEvent())
            }
        }
    }

    fun setParams(theme: ThemeItem, selectedIcons: List<ThemeIconItem>) {
        this.theme = theme
        this.selectedIcons = selectedIcons
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (theme == null || selectedIcons.isEmpty()) {
            dismissAllowingStateLoss()
            return
        }
        EventBus.getDefault().register(this)
        
        val filter = IntentFilter("com.widget.themes.controlcenter.SHORTCUT_INSTALLED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(shortcutReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            requireContext().registerReceiver(shortcutReceiver, filter)
        }

        binding.tvTitle.text = getString(R.string.add_to_home)
        binding.tvDone.text = getString(R.string.add)

        binding.tvCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = SelectedIconsPreviewAdapter(selectedIcons)

        binding.tvDone.setOnClickListener {
            if (installQueue.isEmpty() && totalToInstall == 0) {
                startInstallation()
            } else {
                // If in progress, allow user to manually advance to next
                fallbackJob?.cancel()
                processNextShortcut()
            }
        }
    }

    private var totalToInstall = 0

    private fun startInstallation() {
        installQueue.clear()
        installQueue.addAll(selectedIcons)
        totalToInstall = selectedIcons.size

        if (totalToInstall == 0) {
            dismissAllowingStateLoss()
            return
        }

        binding.tvCancel.visibility = View.GONE
        binding.progressBar.max = totalToInstall
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.VISIBLE
        binding.tvSubtitle.text = "Tap 'Add' on system prompt or 'Next' to continue"
        binding.tvDone.text = "${getString(R.string.next)} >"

        processNextShortcut()
    }

    private fun processNextShortcut() {
        fallbackJob?.cancel()
        val currentBinding = _binding
        if (installQueue.isNotEmpty()) {
            val installedCount = totalToInstall - installQueue.size
            currentBinding?.progressBar?.progress = installedCount

            val nextItem = installQueue.removeAt(0)
            val appLabel = nextItem.targetAppName ?: nextItem.iconName.removePrefix("ic_").replaceFirstChar { it.uppercase() }
            currentBinding?.tvTitle?.text = "Adding: $appLabel (${installedCount + 1}/$totalToInstall)"

            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = loadThemeIconBitmap(nextItem)
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    val ctx = context
                    if (ctx != null && bitmap != null) {
                        addShortcut(ctx, nextItem, bitmap)
                        // Fallback auto-advance after 2.5s if OS broadcast is delayed
                        fallbackJob = lifecycleScope.launch {
                            delay(2500)
                            processNextShortcut()
                        }
                    } else {
                        processNextShortcut()
                    }
                }
            }
        } else {
            val currentContext = context?.applicationContext
            val currentTheme = theme
            if (currentContext != null && currentTheme != null) {
                com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.setIconsUnlocked(
                    context = currentContext,
                    itemIds = selectedIcons.map { it.id },
                    unlocked = true
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    val repo = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository(currentContext)
                    val passedPreview = currentTheme.previewPath
                    val previewUrl: String = when {
                        !passedPreview.isNullOrBlank() && passedPreview.startsWith("http") -> passedPreview
                        currentTheme.path.startsWith("http") -> currentTheme.path
                        else -> {
                            val cleanId = currentTheme.path.substringAfterLast("/").substringBeforeLast(".").ifEmpty { currentTheme.id.removePrefix("icon_").removePrefix("theme_") }
                            try {
                                val iconRepo = com.widget.themes.controlcenter.feature_icon.data.IconRepository(currentContext)
                                val detail = iconRepo.getIconPackDetail(cleanId) ?: iconRepo.getIconPackDetail("theme_$cleanId")
                                val remote = detail?.backgroundIconUrl?.takeIf { it.startsWith("http") }
                                    ?: iconRepo.getIconPacks().firstOrNull { it.id.contains(cleanId, ignoreCase = true) || it.name.equals(currentTheme.name, ignoreCase = true) }?.folder
                                    ?: iconRepo.getIconPacks().firstOrNull()?.folder
                                remote ?: "https://api.1teps.com/themes/assets/icons/22ee90_bg_icon.png"
                            } catch (e: Exception) {
                                "https://api.1teps.com/themes/assets/icons/22ee90_bg_icon.png"
                            }
                        }
                    }
                    val saveId = if (currentTheme.id.isNotBlank() && !currentTheme.id.startsWith("http")) currentTheme.id else "icon_${currentTheme.path.replace('/', '_')}"
                    repo.markAsDownloaded(
                        id = saveId,
                        name = currentTheme.name,
                        category = "Icons",
                        targetPath = currentTheme.path,
                        previewPath = previewUrl,
                        rawType = "collection_icon"
                    )
                }
            }
            dismissAllowingStateLoss()
            val activity = activity
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                try {
                    val fm = activity.supportFragmentManager
                    if (!fm.isStateSaved) {
                        SetupSucceedDialogFragment().show(fm, "success")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Subscribe
    fun onShortcutInstalled(event: ShortcutEvent) {
        processNextShortcut()
    }

    private fun addShortcut(context: Context, item: ThemeIconItem, bmp: Bitmap) {
        val targetPkg = item.targetPackageName ?: return
        val currentThemeId = theme?.id ?: "theme"
        val shortcutId = "icon_${targetPkg}_${currentThemeId}_${item.iconName}_${System.currentTimeMillis()}"
        
        val proxyIntent = Intent(context, ChangeIconActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra("target_package", targetPkg)
            data = Uri.parse("customicon://$targetPkg/$shortcutId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val receiverIntent = Intent("com.widget.themes.controlcenter.SHORTCUT_INSTALLED").apply {
            setPackage(context.packageName)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val successCallback = PendingIntent.getBroadcast(
            context,
            item.hashCode(),
            receiverIntent,
            flags
        )

        val label = item.targetAppName ?: item.iconName.removePrefix("ic_").replaceFirstChar { it.uppercase() }
        val density = try { resources.displayMetrics.density } catch (e: Exception) { 2.0f }
        val finalIconBmp = IconComposerHelper.prepareShortcutBitmap(bmp, density = density)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val shortcutInfo = android.content.pm.ShortcutInfo.Builder(context, shortcutId)
                    .setShortLabel(label)
                    .setIcon(android.graphics.drawable.Icon.createWithBitmap(finalIconBmp))
                    .setIntent(proxyIntent)
                    .setActivity(android.content.ComponentName(context, ChangeIconActivity::class.java))
                    .build()

                try {
                    shortcutManager.requestPinShortcut(shortcutInfo, successCallback.intentSender)
                } catch (e: Exception) {
                    e.printStackTrace()
                    EventBus.getDefault().post(ShortcutEvent())
                }
            } else {
                EventBus.getDefault().post(ShortcutEvent())
            }
        } else {
            try {
                val installIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, proxyIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, label)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON, finalIconBmp)
                }
                context.sendBroadcast(installIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            EventBus.getDefault().post(ShortcutEvent())
        }
    }

    private fun loadThemeIconBitmap(item: ThemeIconItem): Bitmap? {
        val context = context ?: return null
        if (item.customBitmap != null) {
            return item.customBitmap
        }
        val path = item.assetPath

        // 1. Remote HTTP/HTTPS URL
        if (path.startsWith("http://") || path.startsWith("https://")) {
            val bmp = try {
                Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (bmp != null) return bmp
        } else {
            // 2. Local Android Asset
            val cleanPath = path
                .removePrefix("file:///android_asset/")
                .removePrefix("file://android_asset/")
                .removePrefix("android_asset/")
                .removePrefix("/")

            val localBmp = try {
                context.assets.open(cleanPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                try {
                    Glide.with(context.applicationContext)
                        .asBitmap()
                        .load(Uri.parse("file:///android_asset/$cleanPath"))
                        .submit()
                        .get()
                } catch (e2: Exception) {
                    null
                }
            }
            if (localBmp != null) return localBmp
        }

        // 3. Fallback: target app icon
        val targetIcon = item.targetAppIcon
        if (targetIcon != null) {
            if (targetIcon is android.graphics.drawable.BitmapDrawable) {
                return targetIcon.bitmap
            }
            val size = 128
            val fallback = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(fallback)
            targetIcon.setBounds(0, 0, size, size)
            targetIcon.draw(canvas)
            return fallback
        }

        val size = 96
        val fallback = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fallback)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FA4D75")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 16f, 16f, paint)
        return fallback
    }

    private fun roundBitmap(bitmap: Bitmap, cornerRadiusDp: Float): Bitmap {
        val density = resources.displayMetrics.density
        val pixels = (cornerRadiusDp * density).toInt()
        
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fallbackJob?.cancel()
        EventBus.getDefault().unregister(this)
        try {
            requireContext().unregisterReceiver(shortcutReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        _binding = null
    }

    private class SelectedIconsPreviewAdapter(
        private val list: List<ThemeIconItem>
    ) : RecyclerView.Adapter<SelectedIconsPreviewAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_theme_detail_icon, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context

            if (item.customBitmap != null) {
                holder.ivIcon.setImageBitmap(item.customBitmap)
                return
            }

            val path = item.assetPath

            val iconUri = if (path.startsWith("http://") || path.startsWith("https://")) {
                Uri.parse(path)
            } else {
                val cleanPath = path
                    .removePrefix("file:///android_asset/")
                    .removePrefix("file://android_asset/")
                    .removePrefix("android_asset/")
                    .removePrefix("/")
                Uri.parse("file:///android_asset/$cleanPath")
            }

            Glide.with(context)
                .load(iconUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivIcon)
        }

        override fun getItemCount(): Int = list.size
    }
}
