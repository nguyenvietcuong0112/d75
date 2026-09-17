package com.widget.themes.controlcenter.feature_theme

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.data.ResourceConfig
import com.widget.themes.controlcenter.feature_main.MainActivity
import com.widget.themes.controlcenter.feature_theme.my_theme.MyThemeActivity
import com.widget.themes.controlcenter.feature_widget.WidgetConfigActivity
import com.widget.themes.controlcenter.core.ui.HomeActionView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class ThemeFragment : Fragment() {

    private lateinit var viewModel: ThemeViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var pbCreate: ProgressBar
    private var vpBanner: ViewPager2? = null
    private var rvCategories: RecyclerView? = null
    private var rvThemes: RecyclerView? = null

    private val bannerHandler = Handler(Looper.getMainLooper())
    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null || !isVisible) return
            vpBanner?.let { vp ->
                val nextItem = vp.currentItem + 1
                vp.setCurrentItem(nextItem, true)
                bannerHandler.postDelayed(this, 3500)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setupBannerCarousel(view)
        setupQuickActions(view)
        setupCategoryList(view)
        setupThemeGrid(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        startBannerAutoScroll()
        // Reload categories and themes to capture any new DIY themes
        viewModel.selectCategory(viewModel.selectedCategoryId)
    }

    override fun onPause() {
        super.onPause()
        stopBannerAutoScroll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBannerAutoScroll()
        vpBanner = null
        rvCategories = null
        rvThemes = null
    }

    private fun setupBannerCarousel(view: View) {
        val viewPager = view.findViewById<ViewPager2>(R.id.vpBanner) ?: return
        vpBanner = viewPager

        val banners = listOf(
            ThemeBannerAdapter.TYPE_BANNER_1,
            ThemeBannerAdapter.TYPE_BANNER_2,
            ThemeBannerAdapter.TYPE_BANNER_3
        )

        val bannerAdapter = ThemeBannerAdapter(banners) { bannerType ->
            onBannerClicked(bannerType)
        }
        viewPager.adapter = bannerAdapter
        viewPager.offscreenPageLimit = 3

        val density = resources.displayMetrics.density
        val peekPadding = (36 * density).toInt()
        val pageMargin = (8 * density).toInt()

        viewPager.clipToPadding = false
        viewPager.clipChildren = false

        val childRv = viewPager.getChildAt(0) as? RecyclerView
        childRv?.apply {
            clipToPadding = false
            clipChildren = false
            setPadding(peekPadding, 0, peekPadding, 0)
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val compositeTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(pageMargin))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.94f + r * 0.06f
                page.alpha = 0.85f + r * 0.15f
            }
        }
        viewPager.setPageTransformer(compositeTransformer)

        val initialPosition = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % banners.size)
        viewPager.setCurrentItem(initialPosition, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopBannerAutoScroll()
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startBannerAutoScroll()
                }
            }
        })
    }

    private fun startBannerAutoScroll() {
        stopBannerAutoScroll()
        bannerHandler.postDelayed(bannerRunnable, 3500)
    }

    private fun stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    private fun onBannerClicked(bannerType: Int) {
        startBannerAutoScroll()
        val categories = viewModel.categories.value ?: return
        if (categories.isEmpty()) return

        val targetIndex = when (bannerType) {
            ThemeBannerAdapter.TYPE_BANNER_1 -> {
                val idx = categories.indexOfFirst {
                    it.id.contains("trend", ignoreCase = true) ||
                    it.name.contains("trend", ignoreCase = true) ||
                    it.id.contains("hot", ignoreCase = true) ||
                    it.name.contains("hot", ignoreCase = true) ||
                    it.name.contains("xu hướng", ignoreCase = true)
                }
                if (idx >= 0) idx else 0
            }
            ThemeBannerAdapter.TYPE_BANNER_2 -> {
                val idx = categories.indexOfFirst {
                    it.id.contains("aesthetic", ignoreCase = true) ||
                    it.name.contains("aesthetic", ignoreCase = true) ||
                    it.id.contains("new", ignoreCase = true) ||
                    it.name.contains("new", ignoreCase = true) ||
                    it.name.contains("phong cách", ignoreCase = true)
                }
                if (idx >= 0) idx else if (categories.size > 1) 1 else 0
            }
            ThemeBannerAdapter.TYPE_BANNER_3 -> {
                val idx = categories.indexOfFirst {
                    it.id.contains("custom", ignoreCase = true) ||
                    it.name.contains("custom", ignoreCase = true) ||
                    it.id.contains("pink", ignoreCase = true) ||
                    it.name.contains("pink", ignoreCase = true) ||
                    it.id.contains("cute", ignoreCase = true) ||
                    it.name.contains("cute", ignoreCase = true) ||
                    it.id.contains("anime", ignoreCase = true) ||
                    it.name.contains("anime", ignoreCase = true) ||
                    it.id.contains("sport", ignoreCase = true) ||
                    it.name.contains("tùy biến", ignoreCase = true)
                }
                if (idx >= 0) idx else if (categories.size > 2) 2 else 0
            }
            else -> 0
        }

        val targetCategory = categories.getOrNull(targetIndex) ?: return
        viewModel.selectCategory(targetCategory.id)
        rvCategories?.smoothScrollToPosition(targetIndex)
        rvThemes?.scrollToPosition(0)
    }

    private fun setupQuickActions(view: View) {
        val actionView = view.findViewById<HomeActionView>(R.id.actionView) ?: return
        actionView.setListener(object : HomeActionView.OnHomeActionViewListener {
            override fun onSelect(action: HomeActionView.HomeActionType) {
                when (action) {
                    HomeActionView.HomeActionType.WALLPAPER -> {
                        (activity as? MainActivity)?.selectTab(1)
                    }
                    HomeActionView.HomeActionType.THEME -> {
                        startActivity(Intent(context, MyThemeActivity::class.java))
                    }
                    HomeActionView.HomeActionType.WIDGET -> {
                        startActivity(Intent(context, WidgetConfigActivity::class.java))
                    }
                }
            }
        })
    }

    private fun setupCategoryList(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.categoryRecyclerView) ?: return
        rvCategories = rv
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        categoryAdapter = ThemeCategoryAdapter { categoryTag ->
            viewModel.selectCategory(categoryTag.id)
        }
        rv.adapter = categoryAdapter
    }

    private fun setupThemeGrid(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView) ?: return
        rvThemes = rv
        rv.layoutManager = GridLayoutManager(context, 3)

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        while (rv.itemDecorationCount > 0) {
            rv.removeItemDecorationAt(0)
        }
        rv.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        
        pbCreate = view.findViewById(R.id.pbCreate)
        pbCreate.visibility = View.VISIBLE

        themeAdapter = ThemeAdapter(
            onThemeClick = { theme ->
                val thumbUrl = theme.getThumbnailUrl()
                val intent = Intent(context, com.widget.themes.controlcenter.feature_widget.DownloadThemeActivity::class.java).apply {
                    putExtra("theme_item", theme)
                    putExtra("theme_id", theme.id)
                    putExtra("theme_name", theme.name)
                    putExtra("theme_path", theme.path)
                    putExtra("theme_type", theme.rawType)
                    putExtra("theme_preview", theme.previewPath ?: thumbUrl)
                    putExtra("theme_thumb", thumbUrl)
                    putExtra("theme_wallpaper", theme.backgroundPath ?: theme.getBackgroundUrl())
                }
                val act = activity
                if (act != null) {
                    com.widget.themes.controlcenter.core.ads.AdsManager.loadAndShowInter(
                        activity = act,
                        adUnitId = getString(R.string.inter_all),
                        isEnabled = com.iconchanger.wallpaper.rolling.icons.utils.RemoteConfigs.inter_all,
                        onNext = { startActivity(intent) }
                    )
                } else {
                    startActivity(intent)
                }
            }
        )
        rv.adapter = themeAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.themes.observe(viewLifecycleOwner) { themes ->
            pbCreate.visibility = View.GONE
            themeAdapter.submitList(themes)
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
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacingHorizontal - column * spacingHorizontal / spanCount
                outRect.right = (column + 1) * spacingHorizontal / spanCount
                if (position < spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            } else {
                outRect.left = column * spacingHorizontal / spanCount
                outRect.right = spacingHorizontal - (column + 1) * spacingHorizontal / spanCount
                if (position >= spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ThemeFragment()
    }
}
