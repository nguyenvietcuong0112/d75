package com.widget.themes.controlcenter.core.di

import android.content.Context
import com.widget.themes.controlcenter.core.data.database.AppDatabase
import com.widget.themes.controlcenter.core.data.database.ThemeItemDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetConfigDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWallpaperDao

object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            database = instance
            instance
        }
    }

    fun getThemeDao(context: Context): ThemeItemDao {
        return getDatabase(context).themeDao()
    }

    fun getWidgetConfigDao(context: Context): WidgetConfigDao {
        return getDatabase(context).widgetConfigDao()
    }

    fun getWallpaperDao(context: Context): WidgetThemeWallpaperDao {
        return getDatabase(context).wallpaperDao()
    }
 
    fun getIconPackDao(context: Context): com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeIconDao {
        return getDatabase(context).iconPackDao()
    }
 
    fun getChargingDao(context: Context): com.widget.themes.controlcenter.feature_charging.data.dao.ChargingAnimationDao {
        return getDatabase(context).chargingDao()
    }
 
    fun getWidgetThemeDao(context: Context): com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWidgetDao {
        return getDatabase(context).widgetThemeDao()
    }
 
    fun getWidgetItemDao(context: Context): com.widget.themes.controlcenter.feature_widget.data.dao.WidgetItemDao {
        return getDatabase(context).widgetItemDao()
    }

    fun getDownloadedCollectionDao(context: Context): com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionDao {
        return getDatabase(context).downloadedCollectionDao()
    }

    @Volatile
    private var themeRepository: com.widget.themes.controlcenter.core.data.repository.ThemeRepository? = null

    fun getThemeRepository(context: Context): com.widget.themes.controlcenter.core.data.repository.ThemeRepository {
        return themeRepository ?: synchronized(this) {
            val apiBase = com.widget.themes.controlcenter.core.data.ResourceConfig.API_BASE_URL
            val apiDataSource = if (apiBase.isNotEmpty()) {
                com.widget.themes.controlcenter.core.data.source.ApiThemeDataSource(apiBase)
            } else null

            val repo = com.widget.themes.controlcenter.core.data.repository.ThemeRepositoryImpl(
                assetDataSource = com.widget.themes.controlcenter.core.data.source.AssetThemeDataSource(context.applicationContext),
                apiDataSource = apiDataSource,
                themeDao = getThemeDao(context.applicationContext)
            )
            if (apiDataSource != null) {
                repo.setDataSourceMode(com.widget.themes.controlcenter.core.data.repository.DataSourceMode.REMOTE_API)
            }
            themeRepository = repo
            repo
        }
    }
}
