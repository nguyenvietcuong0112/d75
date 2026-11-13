package com.widget.themes.controlcenter.feature_widget

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.makeramen.roundedimageview.RoundedImageView

class DownloadIconItemAdapter(
    private val items: List<ThemeIconItem>,
    private val onSelectToggle: (ThemeIconItem) -> Unit,
    private val onChangeApp: (ThemeIconItem) -> Unit,
    private val onInstallClick: (ThemeIconItem) -> Unit
) : RecyclerView.Adapter<DownloadIconItemAdapter.ViewHolder>() {

    private var activeConfirmDialog: android.app.Dialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_download_icon_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onSelectToggle, onChangeApp, onInstallClick)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSelect: ImageView = itemView.findViewById(R.id.ivSelect)
        private val ivNewIcon: ImageView = itemView.findViewById(R.id.ivNewIcon)
        private val ivEditBadge: ImageView = itemView.findViewById(R.id.ivEditBadge)
        private val tvThemeIconName: TextView = itemView.findViewById(R.id.tvThemeIconName)
        private val llOldIcon: View = itemView.findViewById(R.id.llOldIcon)
        private val ivOldIcon: ImageView = itemView.findViewById(R.id.ivOldIcon)
        private val ivAdd: ImageView = itemView.findViewById(R.id.ivAdd)
        private val etAppName: TextView = itemView.findViewById(R.id.etAppName)
        private val btnAction: FrameLayout = itemView.findViewById(R.id.btnAction)
        private val tvActionText: TextView = itemView.findViewById(R.id.tvActionText)
        private val ivAdBadge: ImageView = itemView.findViewById(R.id.ivAdBadge)

        fun bind(
            item: ThemeIconItem,
            onSelectToggle: (ThemeIconItem) -> Unit,
            onChangeApp: (ThemeIconItem) -> Unit,
            onInstallClick: (ThemeIconItem) -> Unit
        ) {
            val context = itemView.context
            val cleanName = item.iconName
                .removePrefix("ic_")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    when (word.lowercase(java.util.Locale.US)) {
                        "chatgpt" -> "ChatGPT"
                        "tiktok" -> "TikTok"
                        "youtube" -> "YouTube"
                        "yt" -> "YT"
                        "gg" -> "Google"
                        "tv" -> "TV"
                        else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.US) else it.toString() }
                    }
                }

            // 1. Checkbox State
            if (item.isSelected) {
                ivSelect.setImageResource(R.drawable.ic_radio_checked)
                ivSelect.imageTintList = null
            } else {
                ivSelect.setImageResource(R.drawable.bg_circle)
                ivSelect.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            }
            ivSelect.setOnClickListener {
                onSelectToggle(item)
            }

            // 2. Left Icon (Theme Asset Icon)
            tvThemeIconName.text = cleanName

            if (item.customBitmap != null) {
                ivNewIcon.setImageBitmap(item.customBitmap)
            } else {
                val assetPath = item.assetPath
                val cleanAssetPath = when {
                    assetPath.startsWith("file:///android_asset/") -> assetPath.removePrefix("file:///android_asset/")
                    assetPath.startsWith("file://android_asset/") -> assetPath.removePrefix("file://android_asset/")
                    assetPath.startsWith("android_asset/") -> assetPath.removePrefix("android_asset/")
                    else -> assetPath
                }

                val iconUri = if (cleanAssetPath.startsWith("http://") || cleanAssetPath.startsWith("https://")) {
                    Uri.parse(cleanAssetPath)
                } else {
                    Uri.parse("file:///android_asset/$cleanAssetPath")
                }

                Glide.with(context)
                    .load(iconUri)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .error(R.drawable.bg_default_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivNewIcon)
            }

            ivEditBadge.setOnClickListener {
                onChangeApp(item)
            }

            // 3. Right Icon (Device Target App)
            if (item.targetAppIcon != null) {
                etAppName.text = item.targetAppName ?: cleanName
                ivOldIcon.setImageDrawable(item.targetAppIcon)
                ivAdd.visibility = View.GONE
            } else {
                etAppName.text = context.getString(R.string.add_app)
                ivOldIcon.setImageResource(R.drawable.bg_default_placeholder)
                ivAdd.visibility = View.VISIBLE
            }

            llOldIcon.setOnClickListener {
                onChangeApp(item)
            }

            // 4. Action Button (Unlock vs Install)
            val primaryColor = androidx.core.content.ContextCompat.getColor(context, R.color.brand_primary)
            if (item.isUnlocked) {
                ivAdBadge.visibility = View.GONE
                ivAdBadge.setOnClickListener(null)
                btnAction.setBackgroundResource(R.drawable.btn_primary_pill)
                tvActionText.text = context.getString(R.string.install)
                tvActionText.setTextColor(Color.WHITE)
                btnAction.setOnClickListener {
                    onInstallClick(item)
                }
            } else {
                ivAdBadge.visibility = View.VISIBLE
                btnAction.setBackgroundResource(R.drawable.btn_outline_pill)
                tvActionText.text = context.getString(R.string.unlock)
                tvActionText.setTextColor(primaryColor)
                val onUnlockClick = View.OnClickListener {
                    var act: android.app.Activity? = null
                    var ctx: android.content.Context? = context
                    while (ctx is android.content.ContextWrapper) {
                        if (ctx is android.app.Activity) {
                            act = ctx
                            break
                        }
                        ctx = ctx.baseContext
                    }
                    if (act != null && (act.isFinishing || act.isDestroyed)) {
                        return@OnClickListener
                    }
                    val performUnlock = {
                        item.isUnlocked = true
                        com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils.setIconUnlocked(context, item.id, true)
                        ivAdBadge.visibility = View.GONE
                        ivAdBadge.setOnClickListener(null)
                        btnAction.setBackgroundResource(R.drawable.btn_primary_pill)
                        tvActionText.text = context.getString(R.string.install)
                        tvActionText.setTextColor(Color.WHITE)
                        btnAction.setOnClickListener {
                            onInstallClick(item)
                        }
                    }
                    showUnlockConfirmDialog(act ?: context) {
                        if (act != null) {
                            com.widget.themes.controlcenter.core.ads.AdsManager.showReward(
                                activity = act,
                                adUnitId = act.getString(R.string.reward_ad),
                                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.reward_ad,
                                onUserEarnedReward = { performUnlock() }
                            )
                        } else {
                            performUnlock()
                        }
                    }
                }
                btnAction.setOnClickListener(onUnlockClick)
                ivAdBadge.setOnClickListener(onUnlockClick)
            }
        }
    }

    private fun showUnlockConfirmDialog(
        dialogContext: android.content.Context,
        onConfirm: () -> Unit
    ) {
        if (activeConfirmDialog?.isShowing == true) return

        val dialog = android.app.Dialog(dialogContext)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val dialogView = LayoutInflater.from(dialogContext)
            .inflate(R.layout.dialog_unlock_ad_confirm, null, false)
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialogView.findViewById<View>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        dialog.setOnDismissListener {
            if (activeConfirmDialog === dialog) {
                activeConfirmDialog = null
            }
        }

        activeConfirmDialog = dialog
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (dialogContext.resources.displayMetrics.widthPixels * 0.88).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
