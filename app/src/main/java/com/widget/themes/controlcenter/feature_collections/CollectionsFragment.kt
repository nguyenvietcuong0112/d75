package com.widget.themes.controlcenter.feature_collections

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.feature_collections.adapter.CollectionDiscoveryAdapter
import com.widget.themes.controlcenter.feature_collections.adapter.CollectionTabAdapter

class CollectionsFragment : Fragment() {

    private lateinit var viewModel: CollectionsViewModel
    private lateinit var tabAdapter: CollectionTabAdapter
    private lateinit var discoveryAdapter: CollectionDiscoveryAdapter
    private lateinit var downloadedAdapter: CollectionDiscoveryAdapter

    private lateinit var scrollViewCollections: NestedScrollView
    private lateinit var llDiscoverySection: View
    private lateinit var rvCategoryTabs: RecyclerView
    private lateinit var rvDiscovery: RecyclerView
    private lateinit var rvDownloaded: RecyclerView
    private lateinit var llDownloadedSection: View
    private lateinit var layoutDownloadedEmpty: View
    private lateinit var btnExploreTheme: TextView
    private lateinit var btnSeeMore: View
    private lateinit var pbLoading: ProgressBar

    private val downloadedLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val targetTab = result.data?.getIntExtra("target_main_tab", -1) ?: -1
            if (targetTab != -1) {
                (activity as? com.widget.themes.controlcenter.feature_main.MainActivity)?.selectTab(targetTab)
            } else {
                val category = result.data?.getStringExtra(DownloadedActivity.EXTRA_CATEGORY)
                if (!category.isNullOrEmpty()) {
                    viewModel.selectTabByName(category)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[CollectionsViewModel::class.java]

        initViews(view)
        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDataForSelectedTab()
    }

    private fun initViews(view: View) {
        scrollViewCollections = view.findViewById(R.id.scrollViewCollections)
        llDiscoverySection = view.findViewById(R.id.llDiscoverySection)
        rvCategoryTabs = view.findViewById(R.id.rvCategoryTabs)
        rvDiscovery = view.findViewById(R.id.rvDiscovery)
        rvDownloaded = view.findViewById(R.id.rvDownloaded)
        llDownloadedSection = view.findViewById(R.id.llDownloadedSection)
        layoutDownloadedEmpty = view.findViewById(R.id.layoutDownloadedEmpty)
        btnExploreTheme = view.findViewById(R.id.btnExploreTheme)
        btnSeeMore = view.findViewById(R.id.btnSeeMore)
        pbLoading = view.findViewById(R.id.pbLoading)
    }

    private fun setupAdapters() {
        // 1. Category Tabs Adapter (Horizontal)
        tabAdapter = CollectionTabAdapter(emptyList()) { tab ->
            viewModel.selectTab(tab)
        }
        rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCategoryTabs.adapter = tabAdapter

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)

        // 2. Downloaded Items Adapter (Horizontal row or 3-col grid)
        downloadedAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(requireActivity(), item, isDownloaded = true)
        }
        val downloadedLayoutManager = GridLayoutManager(context, 3)
        val downloadedSpanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val lm = rvDownloaded.layoutManager as? GridLayoutManager ?: return 1
                val spanCount = lm.spanCount
                if (spanCount <= 1) return 1
                val item = downloadedAdapter.getItem(position)
                val isWidget = item?.category.equals("Widget", ignoreCase = true)
                if (isWidget) {
                    val is4x2 = item?.extra == "4x2" || item?.extra == "4x4" || item?.extra?.contains("medium", ignoreCase = true) == true || item?.id?.contains("4x2") == true || item?.rawType?.contains("4x2") == true
                    return if (is4x2) minOf(2, spanCount) else 1
                }
                return 1
            }
        }
        downloadedSpanLookup.isSpanIndexCacheEnabled = false
        downloadedLayoutManager.spanSizeLookup = downloadedSpanLookup
        rvDownloaded.layoutManager = downloadedLayoutManager
        while (rvDownloaded.itemDecorationCount > 0) {
            rvDownloaded.removeItemDecorationAt(0)
        }
        rvDownloaded.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        rvDownloaded.adapter = downloadedAdapter

        // 3. Discovery Grid Adapter (3 Columns for Theme/Icons/Wallpapers, 2 Columns for Widgets)
        discoveryAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(requireActivity(), item)
        }
        val discoveryLayoutManager = GridLayoutManager(context, 3)
        val discoverySpanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val lm = rvDiscovery.layoutManager as? GridLayoutManager ?: return 1
                val spanCount = lm.spanCount
                if (spanCount <= 1) return 1
                val item = discoveryAdapter.getItem(position)
                val isWidget = item?.category.equals("Widget", ignoreCase = true)
                if (isWidget) {
                    val is4x2 = item?.extra == "4x2" || item?.extra == "4x4" || item?.extra?.contains("medium", ignoreCase = true) == true || item?.id?.contains("4x2") == true || item?.rawType?.contains("4x2") == true
                    return if (is4x2) minOf(2, spanCount) else 1
                }
                return 1
            }
        }
        discoverySpanLookup.isSpanIndexCacheEnabled = false
        discoveryLayoutManager.spanSizeLookup = discoverySpanLookup
        rvDiscovery.layoutManager = discoveryLayoutManager
        while (rvDiscovery.itemDecorationCount > 0) {
            rvDiscovery.removeItemDecorationAt(0)
        }
        rvDiscovery.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        rvDiscovery.adapter = discoveryAdapter
    }

    private fun setupListeners() {
        btnSeeMore.setOnClickListener {
            val selectedTabName = viewModel.selectedTab.value?.name ?: "Theme"
            val intent = Intent(requireContext(), DownloadedActivity::class.java).apply {
                putExtra(DownloadedActivity.EXTRA_CATEGORY, selectedTabName)
            }
            downloadedLauncher.launch(intent)
        }

        btnExploreTheme.setOnClickListener {
            val selectedTabName = viewModel.selectedTab.value?.name ?: "Theme"
            val targetTab = when {
                selectedTabName.contains("theme", ignoreCase = true) -> 0
                selectedTabName.contains("icon", ignoreCase = true) -> 1
                selectedTabName.contains("control", ignoreCase = true) -> 2
                selectedTabName.contains("widget", ignoreCase = true) -> 3
                else -> -1
            }
            if (targetTab != -1) {
                (activity as? com.widget.themes.controlcenter.feature_main.MainActivity)?.selectTab(targetTab)
            }
        }
    }

    private fun scrollToDiscovery() {
        view?.postDelayed({
            if (::scrollViewCollections.isInitialized && ::llDiscoverySection.isInitialized) {
                scrollViewCollections.smoothScrollTo(0, llDiscoverySection.top)
            }
        }, 100)
    }

    private fun observeViewModel() {
        viewModel.tabs.observe(viewLifecycleOwner) { tabs ->
            tabAdapter.updateTabs(tabs)
            val selectedIndex = tabs.indexOfFirst { it.isSelected }
            if (selectedIndex != -1) {
                rvCategoryTabs.scrollToPosition(selectedIndex)
            }
        }

        viewModel.selectedTab.observe(viewLifecycleOwner) { tab ->
            val selectedName = tab.name.lowercase()
            val isWallpaper = selectedName.contains("wallpaper")
            val localizedCategory = com.widget.themes.controlcenter.feature_collections.data.CollectionRepository.getLocalizedCategoryName(requireContext(), tab.name)
            btnExploreTheme.visibility = if (isWallpaper) View.GONE else View.VISIBLE
            btnExploreTheme.text = getString(R.string.explore_category, localizedCategory)
            llDownloadedSection.visibility = View.VISIBLE

            val spanCount = when {
                selectedName.contains("icon") -> 1
                selectedName.contains("widget") -> 2
                else -> 3
            }

            (rvDiscovery.layoutManager as? GridLayoutManager)?.spanCount = spanCount
            (rvDownloaded.layoutManager as? GridLayoutManager)?.spanCount = spanCount

            while (rvDiscovery.itemDecorationCount > 0) {
                rvDiscovery.removeItemDecorationAt(0)
            }
            while (rvDownloaded.itemDecorationCount > 0) {
                rvDownloaded.removeItemDecorationAt(0)
            }
            val spacingHorizontal = if (spanCount == 1) 0 else resources.getDimensionPixelSize(R.dimen.dp_16)
            val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
            rvDiscovery.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingHorizontal, spacingVertical, false))
            rvDownloaded.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingHorizontal, spacingVertical, false))

            viewModel.downloadedItems.value?.let { currentDownloadsList ->
                if (currentDownloadsList.isNotEmpty()) {
                    val limit = when {
                        selectedName.contains("icon") -> 1
                        selectedName.contains("widget") -> 2
                        else -> 3
                    }
                    downloadedAdapter.submitList(currentDownloadsList.take(limit))
                }
            }
        }

        viewModel.downloadedItems.observe(viewLifecycleOwner) { items ->
            val isWallpaper = viewModel.selectedTab.value?.name?.contains("wallpaper", ignoreCase = true) == true
            llDownloadedSection.visibility = View.VISIBLE
            if (items.isNullOrEmpty()) {
                rvDownloaded.visibility = View.GONE
                layoutDownloadedEmpty.visibility = View.VISIBLE
                btnExploreTheme.visibility = if (isWallpaper) View.GONE else View.VISIBLE
            } else {
                rvDownloaded.visibility = View.VISIBLE
                layoutDownloadedEmpty.visibility = View.GONE
                val tabName = viewModel.selectedTab.value?.name?.lowercase() ?: ""
                val limit = when {
                    tabName.contains("icon") -> 1
                    tabName.contains("widget") -> 2
                    else -> 3
                }
                downloadedAdapter.submitList(items.take(limit))
            }
        }

        viewModel.discoveryItems.observe(viewLifecycleOwner) { items ->
            Log.d("CollectionDebug", "Fragment: Received discoveryItems size=${items.size}")
            discoveryAdapter.submitList(items)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacingHorizontal: Int,
        private val spacingVertical: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position < 0) return

            val lp = view.layoutParams as? GridLayoutManager.LayoutParams
            val spanIndex = lp?.spanIndex ?: (position % spanCount)
            val spanSize = lp?.spanSize ?: 1

            if (includeEdge) {
                outRect.left = spacingHorizontal - spanIndex * spacingHorizontal / spanCount
                outRect.right = (spanIndex + spanSize) * spacingHorizontal / spanCount
                if (position < spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            } else {
                outRect.left = spanIndex * spacingHorizontal / spanCount
                outRect.right = spacingHorizontal - (spanIndex + spanSize) * spacingHorizontal / spanCount
                if (position >= spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            }
        }
    }

    companion object {
        fun newInstance() = CollectionsFragment()
    }
}
