package com.widget.themes.controlcenter.feature_widget

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.feature_widget.data.WidgetRepository
import com.widget.themes.controlcenter.feature_theme.data.model.DecorateCategory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WidgetConfigFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvWidgets: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var categoriesData = listOf<CategoryDto>()
    private var activeCategoryId: String = "all"
    private var widgetItems = mutableListOf<WidgetTemplateItem>()
    private lateinit var widgetAdapter: WidgetConfigAdapter

    data class WidgetTemplateItem(
        val id: String,
        val name: String,
        val size: String,
        val widgetType: String,
        val previewUrl: String,
        val themePath: String,
        val themeName: String,
        var cachedSnapshot: Bitmap? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_widget_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rvCategories = view.findViewById(R.id.categoryRecyclerView)
        rvWidgets = view.findViewById(R.id.recyclerView)

        setupRecyclerViews()
        loadCategoriesAndWidgets()
    }

    private fun setupRecyclerViews() {
        val context = requireContext()
        val spacing = (12 * resources.displayMetrics.density).toInt()
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.itemAnimator = null

        val columns = 2
        val gridLayoutManager = GridLayoutManager(context, columns)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = widgetItems.getOrNull(position)
                return if (item?.size == "4x2" || item?.size == "4x4") 2 else 1
            }
        }
        rvWidgets.layoutManager = gridLayoutManager

        while (rvWidgets.itemDecorationCount > 0) {
            rvWidgets.removeItemDecorationAt(0)
        }

        rvWidgets.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position < 0) return
                val item = widgetItems.getOrNull(position)
                val isFullWidth = item?.size == "4x2" || item?.size == "4x4"

                outRect.bottom = spacing

                if (isFullWidth) {
                    outRect.left = 0
                    outRect.right = 0
                } else {
                    val spanIndex = gridLayoutManager.spanSizeLookup.getSpanIndex(position, 2)
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

        widgetAdapter = WidgetConfigAdapter(widgetItems) { item ->
            onWidgetClicked(item)
        }
        rvWidgets.itemAnimator = null
        rvWidgets.adapter = widgetAdapter
    }

    private fun loadCategoriesAndWidgets() {
        progressBar.visibility = View.VISIBLE
        val context = requireContext()

        lifecycleScope.launch(Dispatchers.IO) {
            val widgetRepo = WidgetRepository(context)
            val serverCats = widgetRepo.getCategories()
            val allCat = CategoryDto(id = "all", name = "All", type = "widget")
            val filteredCats = serverCats.filter { !it.id.equals("aesthetic", ignoreCase = true) }.distinctBy { it.id.lowercase() }
            val fullCats = listOf(allCat) + filteredCats
            categoriesData = fullCats

            withContext(Dispatchers.Main) {
                activeCategoryId = "all"
                updateCategoryAdapter()
                loadWidgetsForCategory(activeCategoryId)
            }
        }
    }

    private fun updateCategoryAdapter() {
        val cats = categoriesData.map { it.name }
        val activeName = categoriesData.firstOrNull { it.id.equals(activeCategoryId, ignoreCase = true) }?.name ?: cats.firstOrNull() ?: ""

        rvCategories.adapter = CategoryFilterAdapter(cats, activeName) { selectedName ->
            val foundCat = categoriesData.firstOrNull { it.name == selectedName }
            if (foundCat != null) {
                activeCategoryId = foundCat.id
                loadWidgetsForCategory(activeCategoryId)
            }
        }
    }

    private fun loadWidgetsForCategory(catId: String) {
        if (widgetItems.isEmpty()) {
            progressBar.visibility = View.VISIBLE
        }
        val context = requireContext()

        lifecycleScope.launch(Dispatchers.IO) {
            val widgetRepo = WidgetRepository(context)
            val remoteWidgets = widgetRepo.getWidgets(if (catId == "all") null else catId)
            val items = mutableListOf<WidgetTemplateItem>()

            if (remoteWidgets.isNotEmpty()) {
                val mapped = remoteWidgets.map { dto ->
                    val rawType = dto.rawType.ifEmpty { "clock" }
                    val size = dto.size.ifEmpty { "4x2" }
                    val preview = dto.displayUrl ?: ""
                    WidgetTemplateItem(
                        id = dto.id.ifEmpty { "${dto.themePath}_$rawType" },
                        name = dto.name.ifEmpty { "${dto.themeName} Widget" },
                        size = size,
                        widgetType = rawType,
                        previewUrl = preview,
                        themePath = dto.themePath.ifEmpty { dto.themeId },
                        themeName = dto.themeName.ifEmpty { dto.name }
                    )
                }
                items.addAll(mapped)
            } else {
                // Fallback to local asset themes
                val jsonCategories = ServiceLocator.getThemeRepository(context).getCategories()
                val targetCategories = if (catId == "all") {
                    jsonCategories.filter { !it.category.equals("Aesthetic", ignoreCase = true) }
                } else {
                    jsonCategories.filter { it.category.equals(catId, ignoreCase = true) || it.name.equals(catId, ignoreCase = true) }
                }
                for (catData in targetCategories) {
                    items.addAll(getWidgetItemsFromCategory(catData))
                }
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                widgetItems.clear()
                widgetItems.addAll(items)
                widgetAdapter.notifyDataSetChanged()

                WeatherHelper.updateWeather(context) {
                    val weatherIndices = widgetItems.indices.filter { widgetItems[it].widgetType.contains("weather") }
                    for (idx in weatherIndices) {
                        widgetItems[idx].cachedSnapshot = null
                        widgetAdapter.notifyItemChanged(idx)
                    }
                }
            }
        }
    }

    private fun getWidgetItemsFromCategory(catData: DecorateCategory): List<WidgetTemplateItem> {
        val list = mutableListOf<WidgetTemplateItem>()
        for (theme in catData.themes) {
            val path = theme.themePath
            val name = theme.themeName

            val widgets = listOf(
                // 1. Digital Clock (4x2)
                WidgetTemplateItem(
                    id = "${path}_clock_4x2",
                    name = "$name Clock",
                    size = "4x2",
                    widgetType = "clock",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/clock_4x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 2. Analog Clock (2x2)
                WidgetTemplateItem(
                    id = "${path}_clock_2x2",
                    name = "$name Clock",
                    size = "2x2",
                    widgetType = "clock",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/clock_2x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 3. Weather (2x2)
                WidgetTemplateItem(
                    id = "${path}_weather_2x2",
                    name = "$name Weather",
                    size = "2x2",
                    widgetType = "weather",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/weather_2x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 4. Calendar (2x2)
                WidgetTemplateItem(
                    id = "${path}_calendar_2x2",
                    name = "$name Calendar",
                    size = "2x2",
                    widgetType = "calendar",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/calendar_2x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 5. Sleep (2x2)
                WidgetTemplateItem(
                    id = "${path}_sleep_2x2",
                    name = "$name Sleep",
                    size = "2x2",
                    widgetType = "sleep",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/sleep_2x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 6. Checklist (4x2)
                WidgetTemplateItem(
                    id = "${path}_checklist_4x2",
                    name = "$name Checklist",
                    size = "4x2",
                    widgetType = "checklist",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/checklist_4x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 7. Countdown (4x2)
                WidgetTemplateItem(
                    id = "${path}_countdown_4x2",
                    name = "$name Countdown",
                    size = "4x2",
                    widgetType = "countdown",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/countdown_4x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 8. Note (2x2)
                WidgetTemplateItem(
                    id = "${path}_note_2x2",
                    name = "$name Note",
                    size = "2x2",
                    widgetType = "note",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/note_2x2.png"),
                    themePath = path,
                    themeName = name
                ),
                // 9. Mini Calendar (2x2)
                WidgetTemplateItem(
                    id = "${path}_mini_calendar_2x2",
                    name = "$name Mini calendar",
                    size = "2x2",
                    widgetType = "mini_calendar",
                    previewUrl = ResourceConfig.getWidgetAssetUrl(path, "widgets/mini_calendar_2x2.png"),
                    themePath = path,
                    themeName = name
                )
            )

            list.addAll(widgets)
        }
        return list
    }

    private fun onWidgetClicked(item: WidgetTemplateItem) {
        com.widget.themes.controlcenter.core.network.ViewTracker.track("widget", item.id)
        val showSheet = {
            val sheet = SelectWidgetBottomSheet()
            val dummyTheme = ThemeItem(
                id = "widget_${item.themePath.replace('/', '_')}",
                name = item.themeName,
                path = item.themePath
            )
            sheet.setParams(dummyTheme, item.widgetType, item.size, item.previewUrl, android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID)
            sheet.show(parentFragmentManager, "select_widget")
        }
        val act = activity
        if (act != null) {
            com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                activity = act,
                adUnitId = getString(R.string.inter_all),
                isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                onNext = { showSheet() }
            )
        } else {
            showSheet()
        }
    }

    private inner class WidgetConfigAdapter(
        private val items: List<WidgetTemplateItem>,
        private val onItemClick: (WidgetTemplateItem) -> Unit
    ) : RecyclerView.Adapter<WidgetConfigAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val llContainer: View = view.findViewById(R.id.llContainer) ?: view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_config_widget,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context

            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt()
            val spacing = (12 * density).toInt()
            val parentWidth = displayMetrics.widthPixels - horizontalPadding

            val itemWidth = (parentWidth - spacing) / 2

            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemWidth
            )
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = itemWidth
            holder.itemView.layoutParams = lp

            if (item.cachedSnapshot != null) {
                holder.ivPreview.setImageBitmap(item.cachedSnapshot)
            } else {
                holder.ivPreview.setImageResource(R.drawable.bg_widget_default_gradient)
                val currentType = when {
                    item.id.contains("_checklist_") -> "checklist_4x2"
                    item.id.contains("_countdown_") -> "countdown_4x2"
                    item.id.contains("_mini_calendar_") -> "mini_calendar_2x2"
                    item.id.contains("_calendar_") -> "calendar_2x2"
                    item.id.contains("_sleep_") -> "sleep_2x2"
                    item.id.contains("_note_") -> "note_2x2"
                    item.id.contains("_weather_") -> if (item.size == "4x2") "weather_4x2" else "weather_2x2"
                    item.id.contains("_clock_") || item.id.contains("_clocks_") -> if (item.size == "4x2") "clock_4x2" else "clock_2x2"
                    else -> item.widgetType
                }
                val widgetSize = if (item.size == "4x2") com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize.MEDIUM else com.widget.themes.controlcenter.feature_widget.data.entity.WidgetSize.SMALL
                val layoutRes = WidgetRenderHelper.getLayoutForWidget(widgetSize, currentType)
                val mappedFolder = ResourceConfig.getThemeFolderByPath(context, item.themePath)
                val widgetItem = com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem(
                    id = item.id,
                    themeFolder = mappedFolder,
                    name = item.themeName,
                    widgetType = currentType,
                    size = item.size,
                    isFree = true,
                    isFavorite = false
                )

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
                            holder.ivPreview.setImageBitmap(finalBmp)
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
                                holder.ivPreview.setImageBitmap(fallbackSnapshot)
                            }
                        }
                    })
            }

            holder.llContainer.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        fun newInstance() = WidgetConfigFragment()
    }
}
