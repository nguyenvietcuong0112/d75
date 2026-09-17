package com.widget.themes.controlcenter.feature_theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.data.ResourceConfig

class ThemeAdapter(
    private val onThemeClick: (ThemeItem) -> Unit,
    private val onDeleteClick: ((ThemeItem) -> Unit)? = null
) : RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {

    private var items = listOf<ThemeItem>()

    fun submitList(list: List<ThemeItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_theme_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onThemeClick, onDeleteClick)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val ivDelete: ImageView? = view.findViewById(R.id.ivDelete)
        private val tvDownloads: TextView? = view.findViewById(R.id.tvDownloads)
        private val llDownloadInfo: View? = view.findViewById(R.id.llDownloadInfo)
        private val cardView: View = view.findViewById(R.id.cardView)

        fun bind(
            theme: ThemeItem,
            onClick: (ThemeItem) -> Unit,
            onDelete: ((ThemeItem) -> Unit)?
        ) {
            val context = itemView.context
            tvName.text = theme.name
            tvName.visibility = View.VISIBLE
            val downloadCount = com.widget.themes.controlcenter.core.utils.DownloadCountUtils.resolveDownloads(theme.downloads, theme.id.ifEmpty { theme.name })
            tvDownloads?.text = downloadCount.toString()
            llDownloadInfo?.visibility = View.VISIBLE
            ivDelete?.visibility = View.GONE
            ivPreview.visibility = View.VISIBLE

            val thumbnailUrl = theme.getThumbnailUrl()

            Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPreview)

            cardView.setOnClickListener {
                onClick(theme)
            }
        }
    }
}
