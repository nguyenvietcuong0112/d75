package com.widget.themes.controlcenter.core.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.widget.themes.controlcenter.feature_charging.data.dao.ChargingAnimationDao;
import com.widget.themes.controlcenter.feature_charging.data.dao.ChargingAnimationDao_Impl;
import com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionDao;
import com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionDao_Impl;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetConfigDao;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetConfigDao_Impl;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetItemDao;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetItemDao_Impl;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeIconDao;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeIconDao_Impl;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWallpaperDao;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWallpaperDao_Impl;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWidgetDao;
import com.widget.themes.controlcenter.feature_widget.data.dao.WidgetThemeWidgetDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ThemeItemDao _themeItemDao;

  private volatile WidgetConfigDao _widgetConfigDao;

  private volatile WidgetThemeWallpaperDao _widgetThemeWallpaperDao;

  private volatile WidgetThemeIconDao _widgetThemeIconDao;

  private volatile ChargingAnimationDao _chargingAnimationDao;

  private volatile WidgetThemeWidgetDao _widgetThemeWidgetDao;

  private volatile WidgetItemDao _widgetItemDao;

  private volatile DownloadedCollectionDao _downloadedCollectionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(11) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_themes` (`id` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `name` TEXT NOT NULL, `path` TEXT NOT NULL, `rawType` TEXT NOT NULL, `backgroundPath` TEXT, `previewPath` TEXT, `thumbPath` TEXT, `isPremium` INTEGER NOT NULL, `isFree` INTEGER NOT NULL, `isHot` INTEGER NOT NULL, `isNew` INTEGER NOT NULL, `downloads` INTEGER NOT NULL, `totalIcons` INTEGER NOT NULL, `totalWidgets` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_configs` (`widgetId` INTEGER NOT NULL, `bgType` TEXT NOT NULL, `solidColor` INTEGER NOT NULL, `imageUri` TEXT, `textColor` INTEGER NOT NULL, `fontStyle` TEXT NOT NULL, `gradientStartColor` INTEGER NOT NULL, `gradientEndColor` INTEGER NOT NULL, PRIMARY KEY(`widgetId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_theme_wallpapers` (`id` TEXT NOT NULL, `themeId` TEXT NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, `folder` TEXT NOT NULL, `imageBg` TEXT NOT NULL, `isFree` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `isNew` INTEGER NOT NULL, `category` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_theme_icons` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `folder` TEXT NOT NULL, `category` TEXT NOT NULL, `isFree` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `charging_animations` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `folder` TEXT NOT NULL, `isFree` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_theme_widgets` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `folder` TEXT NOT NULL, `category` TEXT NOT NULL, `isFree` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_items` (`id` TEXT NOT NULL, `themeFolder` TEXT NOT NULL, `name` TEXT NOT NULL, `widgetType` TEXT NOT NULL, `size` TEXT NOT NULL, `isFree` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_collections` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `targetPath` TEXT NOT NULL, `previewPath` TEXT NOT NULL, `downloads` INTEGER NOT NULL, `rawType` TEXT NOT NULL, `extra` TEXT, `appliedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '48b52f428f8fce0bb5c62859e7b5511d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `app_themes`");
        db.execSQL("DROP TABLE IF EXISTS `widget_configs`");
        db.execSQL("DROP TABLE IF EXISTS `widget_theme_wallpapers`");
        db.execSQL("DROP TABLE IF EXISTS `widget_theme_icons`");
        db.execSQL("DROP TABLE IF EXISTS `charging_animations`");
        db.execSQL("DROP TABLE IF EXISTS `widget_theme_widgets`");
        db.execSQL("DROP TABLE IF EXISTS `widget_items`");
        db.execSQL("DROP TABLE IF EXISTS `downloaded_collections`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAppThemes = new HashMap<String, TableInfo.Column>(15);
        _columnsAppThemes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("categoryId", new TableInfo.Column("categoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("rawType", new TableInfo.Column("rawType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("backgroundPath", new TableInfo.Column("backgroundPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("previewPath", new TableInfo.Column("previewPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("thumbPath", new TableInfo.Column("thumbPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("isPremium", new TableInfo.Column("isPremium", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("isHot", new TableInfo.Column("isHot", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("isNew", new TableInfo.Column("isNew", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("downloads", new TableInfo.Column("downloads", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("totalIcons", new TableInfo.Column("totalIcons", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppThemes.put("totalWidgets", new TableInfo.Column("totalWidgets", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppThemes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppThemes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppThemes = new TableInfo("app_themes", _columnsAppThemes, _foreignKeysAppThemes, _indicesAppThemes);
        final TableInfo _existingAppThemes = TableInfo.read(db, "app_themes");
        if (!_infoAppThemes.equals(_existingAppThemes)) {
          return new RoomOpenHelper.ValidationResult(false, "app_themes(com.widget.themes.controlcenter.core.data.entity.ThemeItem).\n"
                  + " Expected:\n" + _infoAppThemes + "\n"
                  + " Found:\n" + _existingAppThemes);
        }
        final HashMap<String, TableInfo.Column> _columnsWidgetConfigs = new HashMap<String, TableInfo.Column>(8);
        _columnsWidgetConfigs.put("widgetId", new TableInfo.Column("widgetId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("bgType", new TableInfo.Column("bgType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("solidColor", new TableInfo.Column("solidColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("imageUri", new TableInfo.Column("imageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("textColor", new TableInfo.Column("textColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("fontStyle", new TableInfo.Column("fontStyle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("gradientStartColor", new TableInfo.Column("gradientStartColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetConfigs.put("gradientEndColor", new TableInfo.Column("gradientEndColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWidgetConfigs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWidgetConfigs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWidgetConfigs = new TableInfo("widget_configs", _columnsWidgetConfigs, _foreignKeysWidgetConfigs, _indicesWidgetConfigs);
        final TableInfo _existingWidgetConfigs = TableInfo.read(db, "widget_configs");
        if (!_infoWidgetConfigs.equals(_existingWidgetConfigs)) {
          return new RoomOpenHelper.ValidationResult(false, "widget_configs(com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig).\n"
                  + " Expected:\n" + _infoWidgetConfigs + "\n"
                  + " Found:\n" + _existingWidgetConfigs);
        }
        final HashMap<String, TableInfo.Column> _columnsWidgetThemeWallpapers = new HashMap<String, TableInfo.Column>(10);
        _columnsWidgetThemeWallpapers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("themeId", new TableInfo.Column("themeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("order", new TableInfo.Column("order", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("folder", new TableInfo.Column("folder", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("imageBg", new TableInfo.Column("imageBg", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("isNew", new TableInfo.Column("isNew", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWallpapers.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWidgetThemeWallpapers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWidgetThemeWallpapers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWidgetThemeWallpapers = new TableInfo("widget_theme_wallpapers", _columnsWidgetThemeWallpapers, _foreignKeysWidgetThemeWallpapers, _indicesWidgetThemeWallpapers);
        final TableInfo _existingWidgetThemeWallpapers = TableInfo.read(db, "widget_theme_wallpapers");
        if (!_infoWidgetThemeWallpapers.equals(_existingWidgetThemeWallpapers)) {
          return new RoomOpenHelper.ValidationResult(false, "widget_theme_wallpapers(com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper).\n"
                  + " Expected:\n" + _infoWidgetThemeWallpapers + "\n"
                  + " Found:\n" + _existingWidgetThemeWallpapers);
        }
        final HashMap<String, TableInfo.Column> _columnsWidgetThemeIcons = new HashMap<String, TableInfo.Column>(6);
        _columnsWidgetThemeIcons.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeIcons.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeIcons.put("folder", new TableInfo.Column("folder", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeIcons.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeIcons.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeIcons.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWidgetThemeIcons = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWidgetThemeIcons = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWidgetThemeIcons = new TableInfo("widget_theme_icons", _columnsWidgetThemeIcons, _foreignKeysWidgetThemeIcons, _indicesWidgetThemeIcons);
        final TableInfo _existingWidgetThemeIcons = TableInfo.read(db, "widget_theme_icons");
        if (!_infoWidgetThemeIcons.equals(_existingWidgetThemeIcons)) {
          return new RoomOpenHelper.ValidationResult(false, "widget_theme_icons(com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeIcon).\n"
                  + " Expected:\n" + _infoWidgetThemeIcons + "\n"
                  + " Found:\n" + _existingWidgetThemeIcons);
        }
        final HashMap<String, TableInfo.Column> _columnsChargingAnimations = new HashMap<String, TableInfo.Column>(5);
        _columnsChargingAnimations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChargingAnimations.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChargingAnimations.put("folder", new TableInfo.Column("folder", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChargingAnimations.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChargingAnimations.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChargingAnimations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChargingAnimations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChargingAnimations = new TableInfo("charging_animations", _columnsChargingAnimations, _foreignKeysChargingAnimations, _indicesChargingAnimations);
        final TableInfo _existingChargingAnimations = TableInfo.read(db, "charging_animations");
        if (!_infoChargingAnimations.equals(_existingChargingAnimations)) {
          return new RoomOpenHelper.ValidationResult(false, "charging_animations(com.widget.themes.controlcenter.feature_charging.data.entity.ChargingAnimation).\n"
                  + " Expected:\n" + _infoChargingAnimations + "\n"
                  + " Found:\n" + _existingChargingAnimations);
        }
        final HashMap<String, TableInfo.Column> _columnsWidgetThemeWidgets = new HashMap<String, TableInfo.Column>(6);
        _columnsWidgetThemeWidgets.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWidgets.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWidgets.put("folder", new TableInfo.Column("folder", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWidgets.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWidgets.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetThemeWidgets.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWidgetThemeWidgets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWidgetThemeWidgets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWidgetThemeWidgets = new TableInfo("widget_theme_widgets", _columnsWidgetThemeWidgets, _foreignKeysWidgetThemeWidgets, _indicesWidgetThemeWidgets);
        final TableInfo _existingWidgetThemeWidgets = TableInfo.read(db, "widget_theme_widgets");
        if (!_infoWidgetThemeWidgets.equals(_existingWidgetThemeWidgets)) {
          return new RoomOpenHelper.ValidationResult(false, "widget_theme_widgets(com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWidget).\n"
                  + " Expected:\n" + _infoWidgetThemeWidgets + "\n"
                  + " Found:\n" + _existingWidgetThemeWidgets);
        }
        final HashMap<String, TableInfo.Column> _columnsWidgetItems = new HashMap<String, TableInfo.Column>(7);
        _columnsWidgetItems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("themeFolder", new TableInfo.Column("themeFolder", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("widgetType", new TableInfo.Column("widgetType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("size", new TableInfo.Column("size", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("isFree", new TableInfo.Column("isFree", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWidgetItems.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWidgetItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWidgetItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWidgetItems = new TableInfo("widget_items", _columnsWidgetItems, _foreignKeysWidgetItems, _indicesWidgetItems);
        final TableInfo _existingWidgetItems = TableInfo.read(db, "widget_items");
        if (!_infoWidgetItems.equals(_existingWidgetItems)) {
          return new RoomOpenHelper.ValidationResult(false, "widget_items(com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem).\n"
                  + " Expected:\n" + _infoWidgetItems + "\n"
                  + " Found:\n" + _existingWidgetItems);
        }
        final HashMap<String, TableInfo.Column> _columnsDownloadedCollections = new HashMap<String, TableInfo.Column>(9);
        _columnsDownloadedCollections.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("targetPath", new TableInfo.Column("targetPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("previewPath", new TableInfo.Column("previewPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("downloads", new TableInfo.Column("downloads", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("rawType", new TableInfo.Column("rawType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("extra", new TableInfo.Column("extra", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedCollections.put("appliedTimestamp", new TableInfo.Column("appliedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDownloadedCollections = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDownloadedCollections = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDownloadedCollections = new TableInfo("downloaded_collections", _columnsDownloadedCollections, _foreignKeysDownloadedCollections, _indicesDownloadedCollections);
        final TableInfo _existingDownloadedCollections = TableInfo.read(db, "downloaded_collections");
        if (!_infoDownloadedCollections.equals(_existingDownloadedCollections)) {
          return new RoomOpenHelper.ValidationResult(false, "downloaded_collections(com.widget.themes.controlcenter.feature_collections.data.DownloadedCollectionItem).\n"
                  + " Expected:\n" + _infoDownloadedCollections + "\n"
                  + " Found:\n" + _existingDownloadedCollections);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "48b52f428f8fce0bb5c62859e7b5511d", "25edb8e80a83e12368db2105b11524f5");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "app_themes","widget_configs","widget_theme_wallpapers","widget_theme_icons","charging_animations","widget_theme_widgets","widget_items","downloaded_collections");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `app_themes`");
      _db.execSQL("DELETE FROM `widget_configs`");
      _db.execSQL("DELETE FROM `widget_theme_wallpapers`");
      _db.execSQL("DELETE FROM `widget_theme_icons`");
      _db.execSQL("DELETE FROM `charging_animations`");
      _db.execSQL("DELETE FROM `widget_theme_widgets`");
      _db.execSQL("DELETE FROM `widget_items`");
      _db.execSQL("DELETE FROM `downloaded_collections`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ThemeItemDao.class, ThemeItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WidgetConfigDao.class, WidgetConfigDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WidgetThemeWallpaperDao.class, WidgetThemeWallpaperDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WidgetThemeIconDao.class, WidgetThemeIconDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChargingAnimationDao.class, ChargingAnimationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WidgetThemeWidgetDao.class, WidgetThemeWidgetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WidgetItemDao.class, WidgetItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DownloadedCollectionDao.class, DownloadedCollectionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ThemeItemDao themeDao() {
    if (_themeItemDao != null) {
      return _themeItemDao;
    } else {
      synchronized(this) {
        if(_themeItemDao == null) {
          _themeItemDao = new ThemeItemDao_Impl(this);
        }
        return _themeItemDao;
      }
    }
  }

  @Override
  public WidgetConfigDao widgetConfigDao() {
    if (_widgetConfigDao != null) {
      return _widgetConfigDao;
    } else {
      synchronized(this) {
        if(_widgetConfigDao == null) {
          _widgetConfigDao = new WidgetConfigDao_Impl(this);
        }
        return _widgetConfigDao;
      }
    }
  }

  @Override
  public WidgetThemeWallpaperDao wallpaperDao() {
    if (_widgetThemeWallpaperDao != null) {
      return _widgetThemeWallpaperDao;
    } else {
      synchronized(this) {
        if(_widgetThemeWallpaperDao == null) {
          _widgetThemeWallpaperDao = new WidgetThemeWallpaperDao_Impl(this);
        }
        return _widgetThemeWallpaperDao;
      }
    }
  }

  @Override
  public WidgetThemeIconDao iconPackDao() {
    if (_widgetThemeIconDao != null) {
      return _widgetThemeIconDao;
    } else {
      synchronized(this) {
        if(_widgetThemeIconDao == null) {
          _widgetThemeIconDao = new WidgetThemeIconDao_Impl(this);
        }
        return _widgetThemeIconDao;
      }
    }
  }

  @Override
  public ChargingAnimationDao chargingDao() {
    if (_chargingAnimationDao != null) {
      return _chargingAnimationDao;
    } else {
      synchronized(this) {
        if(_chargingAnimationDao == null) {
          _chargingAnimationDao = new ChargingAnimationDao_Impl(this);
        }
        return _chargingAnimationDao;
      }
    }
  }

  @Override
  public WidgetThemeWidgetDao widgetThemeDao() {
    if (_widgetThemeWidgetDao != null) {
      return _widgetThemeWidgetDao;
    } else {
      synchronized(this) {
        if(_widgetThemeWidgetDao == null) {
          _widgetThemeWidgetDao = new WidgetThemeWidgetDao_Impl(this);
        }
        return _widgetThemeWidgetDao;
      }
    }
  }

  @Override
  public WidgetItemDao widgetItemDao() {
    if (_widgetItemDao != null) {
      return _widgetItemDao;
    } else {
      synchronized(this) {
        if(_widgetItemDao == null) {
          _widgetItemDao = new WidgetItemDao_Impl(this);
        }
        return _widgetItemDao;
      }
    }
  }

  @Override
  public DownloadedCollectionDao downloadedCollectionDao() {
    if (_downloadedCollectionDao != null) {
      return _downloadedCollectionDao;
    } else {
      synchronized(this) {
        if(_downloadedCollectionDao == null) {
          _downloadedCollectionDao = new DownloadedCollectionDao_Impl(this);
        }
        return _downloadedCollectionDao;
      }
    }
  }
}
