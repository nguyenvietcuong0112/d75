package com.widget.themes.controlcenter.feature_widget

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.ThemeDetailDto
import com.widget.themes.controlcenter.databinding.FragmentDownloadWidgetBinding
import com.widget.themes.controlcenter.feature_widget.data.WidgetRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWidgetFragment : Fragment() {

    private val TAG = "DownloadWidgetFragment"
    private var _binding: FragmentDownloadWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: ThemeItem

    private val widgetItems = mutableListOf<ThemeWidgetItem>()
    private lateinit var adapter: DownloadWidgetItemAdapter
    
    private var selectedIndex = 0

    companion object {
        fun newInstance(theme: ThemeItem): DownloadWidgetFragment {
            return DownloadWidgetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DownloadThemeViewModel::class.java]
        theme = arguments?.getSerializable("theme") as? ThemeItem
            ?: throw IllegalArgumentException("Theme required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbCreate.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val cleanId = theme.path.substringAfterLast("/").trim().ifEmpty { theme.id }
                val widgetRepo = WidgetRepository(requireContext())
                val items = mutableListOf<ThemeWidgetItem>()

                // 1. Try loading theme detail from API
                val detail = withContext(Dispatchers.IO) {
                    try {
                        val response = ApiClient.get<ApiResponse<ThemeDetailDto>>("api/v1/themes/$cleanId")
                        response?.data
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching theme detail: ${e.message}")
                        null
                    }
                }

                val remoteWidgets = if (!detail?.widgets.isNullOrEmpty()) {
                    detail?.widgets
                } else {
                    withContext(Dispatchers.IO) {
                        try {
                            val allWidgets = widgetRepo.getWidgets(pageSize = 100)
                            allWidgets.filter {
                                it.themeId.equals(cleanId, ignoreCase = true) ||
                                it.themePath.contains(cleanId, ignoreCase = true) ||
                                (it.themeId.isNotEmpty() && theme.id.contains(it.themeId, ignoreCase = true))
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                }

                if (!remoteWidgets.isNullOrEmpty()) {
                    Log.i(TAG, "Found ${remoteWidgets.size} remote widgets for theme '$cleanId'")
                    remoteWidgets.forEachIndexed { idx, w ->
                        val wSize = if (w.size.equals("medium", ignoreCase = true) || w.size.equals("4x2", ignoreCase = true)) "4x2" else "2x2"
                        val wType = w.rawType.ifEmpty { if (wSize == "4x2") "clock_4x2" else "clock_2x2" }
                        val providerClass = if (wSize == "4x2") Widget4x2Provider::class.java else Widget2x2Provider::class.java
                        items.add(
                            ThemeWidgetItem(
                                id = w.id.ifEmpty { "${theme.id}_widget_${wType}_$wSize" },
                                name = w.name.ifEmpty { "${theme.name} $wType" },
                                size = wSize,
                                providerClass = providerClass,
                                previewUrl = w.displayUrl ?: "",
                                rawType = wType,
                                isSelected = idx == 0
                            )
                        )
                    }
                }

                // 2. Fallback to local asset themes widgets if no remote widgets found
                if (items.isEmpty()) {
                    val mappedFolder = ResourceConfig.getThemeFolderByPath(requireContext(), theme.path)
                    items.addAll(getLocalWidgetItems(mappedFolder))
                }

                widgetItems.clear()
                widgetItems.addAll(items)
                setupRecyclerView(theme.path)
                setupActions()
            } catch (e: Exception) {
                Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
                Toast.makeText(context, getString(R.string.toast_error_loading_widgets), Toast.LENGTH_SHORT).show()
            } finally {
                binding.pbCreate.visibility = View.GONE
            }
        }
    }

    private fun getLocalWidgetItems(mappedFolder: String): List<ThemeWidgetItem> {
        val list = mutableListOf<ThemeWidgetItem>()
        // 1. Clock (Digital 4x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_clock_4x2",
                name = "Clock",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/clock_4x2.png"),
                rawType = "clock_4x2",
                isSelected = true
            )
        )
        // 2. Clock (Analog 2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_clock_2x2",
                name = "Clock",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/clock_2x2.png"),
                rawType = "clock_2x2",
                isSelected = false
            )
        )
        // 3. Weather (2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_weather_2x2",
                name = "Weather",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/weather_2x2.png"),
                rawType = "weather_2x2",
                isSelected = false
            )
        )
        // 4. Calendar (2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_calendar_2x2",
                name = "Calendar",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/calendar_2x2.png"),
                rawType = "calendar_2x2",
                isSelected = false
            )
        )
        // 5. Sleep (2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_sleep_2x2",
                name = "Sleep",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/sleep_2x2.png"),
                rawType = "sleep_2x2",
                isSelected = false
            )
        )
        // 6. Checklist (4x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_checklist_4x2",
                name = "Checklist",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/checklist_4x2.png"),
                rawType = "checklist_4x2",
                isSelected = false
            )
        )
        // 7. Countdown (4x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_countdown_4x2",
                name = "Countdown",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/countdown_4x2.png"),
                rawType = "countdown_4x2",
                isSelected = false
            )
        )
        // 8. Note (2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_note_2x2",
                name = "Note",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/note_2x2.png"),
                rawType = "note_2x2",
                isSelected = false
            )
        )
        // 9. Mini calendar (2x2)
        list.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_mini_calendar_2x2",
                name = "Mini calendar",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetAssetUrl(mappedFolder, "widgets/mini_calendar_2x2.png"),
                rawType = "mini_calendar_2x2",
                isSelected = false
            )
        )
        return list
    }

    private fun setupRecyclerView(mappedFolder: String) {
        binding.pbCreate.visibility = View.GONE
        val context = requireContext()
        val spacing = (12 * resources.displayMetrics.density).toInt()

        // 2-column grid layout
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = widgetItems.getOrNull(position)
                return if (item?.size == "4x2") 2 else 1
            }
        }
        binding.recyclerView.layoutManager = layoutManager

        while (binding.recyclerView.itemDecorationCount > 0) {
            binding.recyclerView.removeItemDecorationAt(0)
        }

        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position < 0) return
                val item = widgetItems.getOrNull(position)
                val isFullWidth = item?.size == "4x2"

                outRect.bottom = spacing

                if (isFullWidth) {
                    outRect.left = 0
                    outRect.right = 0
                } else {
                    val spanIndex = layoutManager.spanSizeLookup.getSpanIndex(position, 2)
                    if (spanIndex == 0) {
                        outRect.left = 0
                        outRect.right = spacing / 2
                    } else {
                        outRect.left = spacing / 2
                        outRect.right = 0
                    }
                }
            }
        })

        adapter = DownloadWidgetItemAdapter(widgetItems, mappedFolder) { index ->
            if (selectedIndex != index) {
                val prevIndex = selectedIndex
                selectedIndex = index
                if (prevIndex in widgetItems.indices) {
                    widgetItems[prevIndex].isSelected = false
                    adapter.notifyItemChanged(prevIndex, "PAYLOAD_SELECT")
                }
                if (index in widgetItems.indices) {
                    widgetItems[index].isSelected = true
                    adapter.notifyItemChanged(index, "PAYLOAD_SELECT")
                }
            }
        }
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = adapter

        WeatherHelper.updateWeather(requireContext()) {
            val weatherIndex = widgetItems.indexOfFirst { it.id.contains("_weather_") || it.rawType.contains("weather") }
            if (weatherIndex != -1) {
                widgetItems[weatherIndex].cachedSnapshot = null
                adapter.notifyItemChanged(weatherIndex)
            }
        }
    }

    private fun setupActions() {
        binding.btnAction.setOnClickListener {
            val selectedItem = widgetItems.find { it.isSelected } ?: widgetItems.firstOrNull()
            if (selectedItem == null) {
                Toast.makeText(requireContext(), getString(R.string.toast_please_select_widget), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openSelectWidgetBottomSheet(selectedItem)
        }
    }

    private fun openSelectWidgetBottomSheet(item: ThemeWidgetItem) {
        val sheet = SelectWidgetBottomSheet()
        sheet.setParams(
            theme = theme,
            widgetList = widgetItems,
            initialIndex = widgetItems.indexOf(item).coerceAtLeast(0)
        )
        sheet.show(parentFragmentManager, "select_widget_bottom_sheet")
    }

    private inner class DownloadWidgetItemAdapter(
        private val list: List<ThemeWidgetItem>,
        private val mappedFolder: String,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<DownloadWidgetItemAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: com.widget.themes.controlcenter.databinding.ItemDownloadWidgetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.widget.themes.controlcenter.databinding.ItemDownloadWidgetLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
            if (payloads.isNotEmpty()) {
                val item = list[position]
                holder.binding.ivChecked.visibility = if (item.isSelected) View.VISIBLE else View.GONE
                return
            }
            super.onBindViewHolder(holder, position, payloads)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val binding = holder.binding

            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt() // 16dp each side
            val spacing = (12 * density).toInt()

            val parentWidth = (displayMetrics.widthPixels - horizontalPadding)

            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT

            val itemWidth = (parentWidth - spacing) / 2
            // Equal height for both 4x2 and 2x2 widgets
            lp.height = itemWidth
            holder.itemView.layoutParams = lp

            binding.ivChecked.visibility = if (item.isSelected) View.VISIBLE else View.GONE

            // Render live snapshot or reuse cached snapshot (zero reload, zero flicker)
            if (item.cachedSnapshot != null) {
                binding.ivPreview.setImageBitmap(item.cachedSnapshot)
            } else {
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
                val widgetSize = if (item.size == "4x2") com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize.MEDIUM else com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize.SMALL
                val layoutRes = WidgetRenderHelper.getLayoutForWidget(widgetSize, currentType)
                val widgetItem = com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem(
                    id = theme.id,
                    themeFolder = mappedFolder,
                    name = theme.name,
                    widgetType = currentType,
                    size = item.size,
                    isFree = true,
                    isFavorite = false
                )

                if (item.previewUrl.isNotEmpty()) {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.previewUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                val snapshot = WidgetRenderHelper.getSnapshotImage(
                                    context = context,
                                    layoutId = layoutRes,
                                    widgetSize = widgetSize,
                                    widgetItem = widgetItem,
                                    preloadedBackground = resource
                                )
                                val finalBmp = snapshot ?: resource
                                item.cachedSnapshot = finalBmp
                                binding.ivPreview.setImageBitmap(finalBmp)
                            }

                            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}

                            override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                                val fallbackSnapshot = WidgetRenderHelper.getSnapshotImage(
                                    context = context,
                                    layoutId = layoutRes,
                                    widgetSize = widgetSize,
                                    widgetItem = widgetItem,
                                    preloadedBackground = null
                                )
                                if (fallbackSnapshot != null) {
                                    item.cachedSnapshot = fallbackSnapshot
                                    binding.ivPreview.setImageBitmap(fallbackSnapshot)
                                }
                            }
                        })
                } else {
                    val fallbackSnapshot = WidgetRenderHelper.getSnapshotImage(
                        context = context,
                        layoutId = layoutRes,
                        widgetSize = widgetSize,
                        widgetItem = widgetItem,
                        preloadedBackground = null
                    )
                    if (fallbackSnapshot != null) {
                        item.cachedSnapshot = fallbackSnapshot
                        binding.ivPreview.setImageBitmap(fallbackSnapshot)
                    }
                }
            }

            binding.llContainer.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount(): Int = list.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

