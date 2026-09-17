package com.widget.themes.controlcenter.feature_theme.my_theme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.widget.themes.controlcenter.R
import com.widget.themes.controlcenter.core.di.ServiceLocator
import com.widget.themes.controlcenter.feature_theme.ThemeAdapter
import com.widget.themes.controlcenter.feature_theme.ThemePreviewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyThemeListFragment : Fragment() {

    private lateinit var rvThemes: RecyclerView
    private lateinit var adapter: ThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme, container, false)
        view.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
        view.findViewById<View>(R.id.actionView)?.visibility = View.GONE
        view.findViewById<View>(R.id.categoryRecyclerView)?.visibility = View.GONE
        view.findViewById<View>(R.id.pbCreate)?.visibility = View.GONE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvThemes = view.findViewById(R.id.recyclerView)
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        // Keyboard grid: Target ~160dp width per item
        val columns = (screenWidthDp / 160).toInt().coerceAtLeast(2)
        rvThemes.layoutManager = GridLayoutManager(context, columns)

        adapter = ThemeAdapter(
            onThemeClick = { theme ->
                val thumbUrl = theme.getThumbnailUrl()
                val intent = Intent(context, com.widget.themes.controlcenter.feature_widget.DownloadThemeActivity::class.java).apply {
                    putExtra("theme_item", theme)
                    putExtra("theme_id", theme.id)
                    putExtra("theme_name", theme.name)
                    putExtra("theme_path", theme.path)
                    putExtra("theme_type", theme.rawType)
                    putExtra("theme_preview", thumbUrl)
                    putExtra("theme_thumb", thumbUrl)
                    putExtra("theme_wallpaper", theme.backgroundPath ?: theme.getBackgroundUrl())
                    putExtra("is_downloaded", true)
                }
                startActivity(intent)
            },
            onDeleteClick = { theme ->
                deleteTheme(theme)
            }
        )
        rvThemes.adapter = adapter

        loadDiyThemes()
    }

    override fun onResume() {
        super.onResume()
        loadDiyThemes()
    }

    private fun loadDiyThemes() {
        lifecycleScope.launch {
            val context = context ?: return@launch
            val diyThemes = withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.getThemeDao(context).getAllThemes()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            adapter.submitList(diyThemes)
        }
    }

    private fun deleteTheme(theme: com.widget.themes.controlcenter.core.data.entity.ThemeItem) {
        lifecycleScope.launch {
            val context = context ?: return@launch
            withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.getThemeDao(context).deleteTheme(theme)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            loadDiyThemes()
        }
    }
}
