package com.widget.themes.controlcenter.feature_widget

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.widget.themes.controlcenter.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionBottomSheet : BottomSheetDialogFragment() {

    data class AppItem(
        val resolveInfo: ResolveInfo,
        val appName: String,
        val packageName: String,
        val icon: Drawable?
    )

    private var currentPackageName: String? = null
    private var iconTitle: String? = null
    private var onAppSelected: ((ResolveInfo) -> Unit)? = null

    private var allApps: List<AppItem> = emptyList()
    private var filteredApps: List<AppItem> = emptyList()
    private lateinit var adapter: AppAdapter

    fun setParams(
        currentPackage: String?,
        iconName: String? = null,
        onSelected: (ResolveInfo) -> Unit
    ) {
        this.currentPackageName = currentPackage
        this.iconTitle = iconName
        this.onAppSelected = onSelected
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_select_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val ivClose = view.findViewById<ImageView>(R.id.ivClose)
        val etSearchApp = view.findViewById<EditText>(R.id.etSearchApp)
        val ivClearSearch = view.findViewById<ImageView>(R.id.ivClearSearch)
        val rvApps = view.findViewById<RecyclerView>(R.id.rvApps)
        val llEmptyState = view.findViewById<View>(R.id.llEmptyState)
        val pbLoading = view.findViewById<View>(R.id.pbLoading)

        if (!iconTitle.isNullOrEmpty()) {
            tvSubtitle.text = "${getString(R.string.select_app_to_link)}: $iconTitle"
        } else {
            tvSubtitle.text = getString(R.string.select_app_to_link)
        }

        ivClose.setOnClickListener {
            dismiss()
        }

        adapter = AppAdapter(
            items = emptyList(),
            selectedPackage = currentPackageName,
            onItemClicked = { app ->
                onAppSelected?.invoke(app.resolveInfo)
                dismiss()
            }
        )
        rvApps.layoutManager = LinearLayoutManager(context)
        rvApps.adapter = adapter

        // Search logic
        etSearchApp.doAfterTextChanged { text ->
            val query = text?.toString()?.trim() ?: ""
            ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            filterList(query, llEmptyState)
        }

        ivClearSearch.setOnClickListener {
            etSearchApp.text?.clear()
        }

        // Load apps asynchronously
        pbLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val ctx = context ?: return@launch
            val pm = ctx.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val myPackage = ctx.packageName

            val items = resolveInfos
                .filter { it.activityInfo.packageName != myPackage }
                .map { info ->
                    AppItem(
                        resolveInfo = info,
                        appName = info.loadLabel(pm).toString(),
                        packageName = info.activityInfo.packageName,
                        icon = try { info.loadIcon(pm) } catch (e: Exception) { null }
                    )
                }
                .sortedWith { a, b ->
                    a.appName.compareTo(b.appName, ignoreCase = true)
                }

            allApps = items
            filteredApps = items

            withContext(Dispatchers.Main) {
                if (isAdded) {
                    pbLoading.visibility = View.GONE
                    adapter.updateList(filteredApps)
                    llEmptyState.visibility = if (filteredApps.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun filterList(query: String, emptyView: View) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filteredApps)
        emptyView.visibility = if (filteredApps.isEmpty()) View.VISIBLE else View.GONE
    }

    private class AppAdapter(
        private var items: List<AppItem>,
        private val selectedPackage: String?,
        private val onItemClicked: (AppItem) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        fun updateList(newList: List<AppItem>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_app_selection,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, selectedPackage, onItemClicked)
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
            private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
            private val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)
            private val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)
            private val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron)

            fun bind(
                item: AppItem,
                selectedPackage: String?,
                onClick: (AppItem) -> Unit
            ) {
                tvAppName.text = item.appName
                tvPackageName.text = item.packageName
                if (item.icon != null) {
                    ivAppIcon.setImageDrawable(item.icon)
                } else {
                    ivAppIcon.setImageResource(R.drawable.bg_default_placeholder)
                }

                val isSelected = item.packageName == selectedPackage
                if (isSelected) {
                    ivCheck.visibility = View.VISIBLE
                    ivChevron.visibility = View.GONE
                } else {
                    ivCheck.visibility = View.GONE
                    ivChevron.visibility = View.VISIBLE
                }

                itemView.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}
