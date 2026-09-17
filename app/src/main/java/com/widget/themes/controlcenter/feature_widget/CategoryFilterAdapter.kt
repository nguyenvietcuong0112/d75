package com.widget.themes.controlcenter.feature_widget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R

class CategoryFilterAdapter(
    private val categories: List<String>,
    private var selectedCategory: String,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_widget_category_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category == selectedCategory) {
            if (category == selectedCategory) return@bind
            val oldSelected = selectedCategory
            selectedCategory = category
            val oldIndex = categories.indexOf(oldSelected)
            if (oldIndex != -1) {
                notifyItemChanged(oldIndex)
            }
            notifyItemChanged(position)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val llContainer: View = view.findViewById(R.id.llContainer)

        fun bind(category: String, isSelected: Boolean, onClick: () -> Unit) {
            llContainer.foreground = null
            tvName.text = category
            val context = tvName.context
            
            if (isSelected) {
                llContainer.setBackgroundResource(R.drawable.bg_category_tab_selected)
                llContainer.backgroundTintList = null
                tvName.setTextColor(Color.parseColor("#FA5783"))
                tvName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_semi_bold)
            } else {
                llContainer.setBackgroundResource(R.drawable.bg_category_tab_unselected)
                llContainer.backgroundTintList = null
                tvName.setTextColor(Color.parseColor("#1A1A1A"))
                tvName.typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.inter_medium)
            }

            llContainer.setOnClickListener {
                onClick()
            }
        }
    }
}
