package com.widget.themes.controlcenter.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.widget.themes.controlcenter.core.data.entity.ThemeItem
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetConfigDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWallpaperDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeIconDao
import com.widget.themes.controlcenter.feature_charging.data.dao.ChargingAnimationDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWidgetDao
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetItemDao
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeIcon
import com.widget.themes.controlcenter.feature_charging.data.entity.ChargingAnimation
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWidget
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem
import com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionItem
import com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionDao

@Database(
    entities = [
        ThemeItem::class,
        WidgetConfig::class,
        WidgetThemeWallpaper::class,
        WidgetThemeIcon::class,
        ChargingAnimation::class,
        WidgetThemeWidget::class,
        WidgetItem::class,
        DownloadedCollectionItem::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themeDao(): ThemeItemDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun wallpaperDao(): WidgetThemeWallpaperDao
    abstract fun iconPackDao(): WidgetThemeIconDao
    abstract fun chargingDao(): ChargingAnimationDao
    abstract fun widgetThemeDao(): WidgetThemeWidgetDao
    abstract fun widgetItemDao(): WidgetItemDao
    abstract fun downloadedCollectionDao(): DownloadedCollectionDao

    companion object {
        const val DATABASE_NAME = "personalization_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        com.widget.themes.controlcenter.feature_widget.data.migration.MigrateWidgetTheme.INSTANCE.migrate(db)
                        com.widget.themes.controlcenter.feature_widget.data.migration.MigrateWidgetThemeWallpaperCommon.INSTANCE.migrate(db)
                        com.widget.themes.controlcenter.feature_widget.data.migration.MigrateWidgetThemeIconV2.INSTANCE.migrate(db)
                        com.widget.themes.controlcenter.feature_charging.data.migration.MigrateChargingAnimation.INSTANCE.migrate(db)
                        com.widget.themes.controlcenter.feature_widget.data.migration.MigrateWidgetThemeWidget.INSTANCE.migrate(db)
                        com.widget.themes.controlcenter.feature_widget.data.migration.MigrateWidgetItemWeather.INSTANCE.migrate(db)
                    }
                })
                .fallbackToDestructiveMigration() // Prevent crashes due to schema changes
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
