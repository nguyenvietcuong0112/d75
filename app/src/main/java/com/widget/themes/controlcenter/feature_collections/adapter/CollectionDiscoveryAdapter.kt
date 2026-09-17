package com.widget.themes.controlcenter.feature_collections.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_collections.data.CollectionItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

import com.widget.themes.controlcenter.feature_widget.WidgetRenderHelper
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.bumptech.glide.load.engine.DiskCacheStrategy

class CollectionDiscoveryAdapter(
    private var items: List<CollectionItem>,
    private val onItemClick: (CollectionItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DEFAULT_CARD = 0
        private const val TYPE_WIDGET_CARD = 1
        private const val TYPE_ICON_CARD = 2

        fun parseImageUri(rawPath: String): Uri? {
            if (rawPath.isBlank()) return null
            val trimmed = rawPath.trim()
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return Uri.parse(trimmed)
            if (trimmed.startsWith("content://")) return Uri.parse(trimmed)
            if (trimmed.startsWith("file:///android_asset/")) return Uri.parse(trimmed)
            if (trimmed.startsWith("file://")) {
                val pathWithoutScheme = trimmed.removePrefix("file://")
                val file = java.io.File(pathWithoutScheme)
                if (file.exists()) return Uri.fromFile(file)
                return Uri.parse(trimmed)
            }
            val directFile = java.io.File(trimmed)
            if (directFile.isAbsolute && directFile.exists()) {
                return Uri.fromFile(directFile)
            }
            val clean = trimmed
                .removePrefix("file:///android_asset/")
                .removePrefix("file://android_asset/")
                .removePrefix("android_asset/")
                .removePrefix("/")
            val normalizedAsset = when {
                clean.startsWith("assets_theme/") || clean.startsWith("assets_collection/") || clean.startsWith("assets_charging/") || clean.startsWith("assets_control_center/") -> clean
                clean.startsWith("category/") -> "assets_theme/$clean"
                clean.startsWith("theme/") || clean.startsWith("widget/") || clean.startsWith("icons/") || clean.startsWith("wallpaper/") || clean.startsWith("control_center/") -> "assets_collection/$clean"
                else -> "assets_theme/category/$clean"
            }
            return Uri.parse("file:///android_asset/$normalizedAsset")
        }
    }

    fun submitList(newItems: List<CollectionItem>) {
        Log.d("CollectionDebug", "Adapter: submitList called with ${newItems.size} items. First item category: ${newItems.firstOrNull()?.category}")
        items = newItems
        notifyDataSetChanged()
    }

    fun getItem(position: Int): CollectionItem? = items.getOrNull(position)

    override fun getItemViewType(position: Int): Int {
        val item = items.getOrNull(position)
        val cat = item?.category?.lowercase() ?: ""
        val raw = item?.rawType?.lowercase() ?: ""
        return when {
            cat.contains("theme") -> TYPE_DEFAULT_CARD
            cat.contains("widget") || (raw.contains("widget") && !raw.contains("theme")) -> TYPE_WIDGET_CARD
            cat.contains("icon") || raw.contains("icon") -> TYPE_ICON_CARD
            else -> TYPE_DEFAULT_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_WIDGET_CARD -> {
                val view = inflater.inflate(R.layout.item_config_widget, parent, false)
                WidgetViewHolder(view)
            }
            TYPE_ICON_CARD -> {
                val view = inflater.inflate(R.layout.item_icon_pack_layout, parent, false)
                IconCardViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_collection_card, parent, false)
                DefaultCardViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is WidgetViewHolder -> holder.bind(item)
            is IconCardViewHolder -> holder.bind(item)
            is DefaultCardViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // 1. ViewHolder for Widget Items
    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPreview: ImageView = itemView.findViewById(R.id.ivPreview)
        private val llContainer: View = itemView.findViewById(R.id.llContainer) ?: itemView

        fun bind(item: CollectionItem) {
            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt()
            val spacing = (12 * density).toInt()
            val parentWidth = displayMetrics.widthPixels - horizontalPadding

            val isMedium = item.extra == "4x2" || item.extra == "4x4" || item.extra?.contains("medium", ignoreCase = true) == true || item.id.contains("4x2") || item.rawType.contains("4x2")
            val itemWidth = if (isMedium) parentWidth else (parentWidth - spacing) / 2
            val itemHeight = if (isMedium) (parentWidth * 0.5f).toInt() else itemWidth

            val lp = itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight
            )
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = itemHeight
            itemView.layoutParams = lp

            if (item.cachedSnapshot != null) {
                ivPreview.setImageBitmap(item.cachedSnapshot)
            } else {
                ivPreview.setImageResource(R.drawable.bg_widget_default_gradient)
                val raw = (item.rawType + "_" + item.id).lowercase()
                val currentType = when {
                    raw.contains("checklist") -> "checklist_4x2"
                    raw.contains("countdown") -> "countdown_4x2"
                    raw.contains("mini_calendar") -> "mini_calendar_2x2"
                    raw.contains("calendar") -> "calendar_2x2"
                    raw.contains("sleep") -> "sleep_2x2"
                    raw.contains("note") || raw.contains("quote") -> "note_2x2"
                    raw.contains("weather") -> if (isMedium) "weather_4x2" else "weather_2x2"
                    raw.contains("clock") -> if (isMedium) "clock_4x2" else "clock_2x2"
                    else -> item.rawType.ifEmpty { if (isMedium) "clock_4x2" else "clock_2x2" }
                }
                val widgetSize = if (isMedium) WidgetSize.MEDIUM else WidgetSize.SMALL
                val layoutRes = WidgetRenderHelper.getLayoutForWidget(widgetSize, currentType)
                val mappedFolder = ResourceConfig.getThemeFolderByPath(context, item.targetPath)
                val widgetItem = WidgetItem(
                    id = item.id,
                    themeFolder = mappedFolder,
                    name = item.name,
                    widgetType = currentType,
                    size = if (isMedium) "4x2" else "2x2",
                    isFree = true,
                    isFavorite = false
                )

                val preview = when {
                    item.previewPath.isNotBlank() -> item.previewPath
                    item.targetPath.isNotBlank() -> item.targetPath
                    else -> ""
                }
                val imageUri = parseImageUri(preview)

                Glide.with(context)
                    .asBitmap()
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val snapshot = WidgetRenderHelper.getSnapshotImage(
                                context = context,
                                layoutId = layoutRes,
                                widgetSize = widgetSize,
                                widgetItem = widgetItem,
                                preloadedBackground = resource
                            )
                            val finalBmp = snapshot ?: resource
                            item.cachedSnapshot = finalBmp
                            ivPreview.setImageBitmap(finalBmp)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            val fallbackSnapshot = WidgetRenderHelper.getSnapshotImage(
                                context = context,
                                layoutId = layoutRes,
                                widgetSize = widgetSize,
                                widgetItem = widgetItem,
                                preloadedBackground = null
                            )
                            if (fallbackSnapshot != null) {
                                item.cachedSnapshot = fallbackSnapshot
                                ivPreview.setImageBitmap(fallbackSnapshot)
                            }
                        }
                    })
            }

            llContainer.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // 2. ViewHolder for Icon Cards (Matching IconFragment layout)
    inner class IconCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: View = itemView.findViewById(R.id.cardContainer) ?: itemView
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val ivPreview: ImageView = itemView.findViewById(R.id.ivPreview)
        private val llViewAll: View? = itemView.findViewById(R.id.llViewAll)

        fun bind(item: CollectionItem) {
            tvName.text = item.name

            val preview = when {
                item.previewPath.startsWith("http://") || item.previewPath.startsWith("https://") -> item.previewPath
                item.targetPath.startsWith("http://") || item.targetPath.startsWith("https://") -> item.targetPath
                else -> item.previewPath
            }
            val imageUri = parseImageUri(preview)

            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPreview)

            cardContainer.setOnClickListener {
                onItemClick(item)
            }
            llViewAll?.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // 3. ViewHolder for Default Collection Cards (Theme, Control Center, Wallpaper)
    inner class DefaultCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardThumb: CardView? = itemView.findViewById(R.id.cardThumb)
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDownloads: TextView = itemView.findViewById(R.id.tvDownloads)
        private val ivDownloadIcon: ImageView = itemView.findViewById(R.id.ivDownloadIcon)

        fun bind(item: CollectionItem) {
            tvName.text = item.name
            val downloadCount = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(item.downloads, item.id.ifEmpty { item.name })
            tvDownloads.text = downloadCount.toString()

            val categoryLower = item.category.lowercase()
            val isWallpaper = categoryLower.contains("wallpaper") || item.rawType.contains("wallpaper", ignoreCase = true)

            val preview = when {
                item.previewPath.startsWith("http://") || item.previewPath.startsWith("https://") -> item.previewPath
                item.targetPath.startsWith("http://") || item.targetPath.startsWith("https://") -> item.targetPath
                isWallpaper -> {
                    when {
                        item.targetPath.startsWith("http") && (item.targetPath.contains("wallpaper", ignoreCase = true) || item.targetPath.endsWith(".png") || item.targetPath.endsWith(".jpg")) -> item.targetPath
                        item.previewPath.contains("bg_preview.png") -> item.previewPath.replace("bg_preview.png", "bg_wallpaper.png")
                        item.previewPath.contains("preview.png") -> item.previewPath.replace("preview.png", "wallpaper.png")
                        item.targetPath.isNotEmpty() -> {
                            val clean = item.targetPath.removePrefix("/")
                            when {
                                clean.startsWith("assets_collection/wallpaper/") -> "file:///android_asset/$clean/wallpaper.png"
                                clean.contains("wallpaper") -> "file:///android_asset/$clean"
                                else -> "file:///android_asset/$clean/bg_wallpaper.png"
                            }
                        }
                        else -> item.previewPath
                    }
                }
                item.previewPath.isNotEmpty() && !item.previewPath.contains("category//") -> item.previewPath
                item.targetPath.isNotEmpty() -> {
                    val clean = item.targetPath.removePrefix("/")
                    when {
                        clean.startsWith("assets_collection/") || clean.startsWith("assets_theme/") -> "file:///android_asset/$clean/bg_preview.png"
                        clean.startsWith("category/") -> "file:///android_asset/assets_theme/$clean/bg_preview.png"
                        clean.startsWith("theme/") -> "file:///android_asset/assets_collection/$clean/bg_preview.png"
                        clean.isNotEmpty() -> "file:///android_asset/assets_theme/category/$clean/bg_preview.png"
                        else -> "file:///android_asset/assets_collection/theme/theme_1/bg_preview.png"
                    }
                }
                item.name.equals("Dark Moon", ignoreCase = true) -> "file:///android_asset/assets_collection/theme/theme_1/bg_preview.png"
                item.name.equals("Matcha Latte", ignoreCase = true) -> "file:///android_asset/assets_collection/theme/theme_2/bg_preview.png"
                item.name.equals("Dreamy Bear", ignoreCase = true) -> "file:///android_asset/assets_collection/theme/theme_3/bg_preview.png"
                else -> "file:///android_asset/assets_collection/theme/theme_1/bg_preview.png"
            }

            val imageUri = parseImageUri(preview)

            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivThumbnail)

            itemView.setOnClickListener {
                onItemClick(item)
            }
            cardThumb?.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
