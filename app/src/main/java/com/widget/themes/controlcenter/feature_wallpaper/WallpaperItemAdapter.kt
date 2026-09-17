package com.widget.themes.controlcenter.feature_wallpaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.bumptech.glide.Glide

class WallpaperItemAdapter(
    private val screenWidth: Int,
    private val columns: Int,
    private val onItemClick: (WidgetThemeWallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperItemAdapter.ViewHolder>() {

    private var items = listOf<WidgetThemeWallpaper>()

    fun submitList(list: List<WidgetThemeWallpaper>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_wallpaper_item_layout,
            parent,
            false
        )
        // Dynamically scale item size based on screen width and columns
        val itemW = screenWidth / columns
        view.layoutParams = ViewGroup.LayoutParams(itemW, ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        private val cardView: View = view.findViewById(R.id.cardView)

        fun bind(
            wallpaper: WidgetThemeWallpaper,
            onClick: (WidgetThemeWallpaper) -> Unit
        ) {
            val onlineUrl = wallpaper.getOnlinePreviewUri(itemView.context).toString()
            
            // Log URL to Logcat for verify/debugging
            android.util.Log.d("WallpaperFetch", "Loading wallpaper preview from CDN URL: $onlineUrl")

            Glide.with(itemView.context)
                .load(onlineUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .into(ivPreview)

            cardView.setOnClickListener {
                onClick(wallpaper)
            }
        }
    }
}
