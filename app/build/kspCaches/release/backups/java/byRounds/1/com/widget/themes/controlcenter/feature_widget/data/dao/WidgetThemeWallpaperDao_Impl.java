package com.widget.themes.controlcenter.feature_widget.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WidgetThemeWallpaperDao_Impl implements WidgetThemeWallpaperDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WidgetThemeWallpaper> __insertionAdapterOfWidgetThemeWallpaper;

  private final EntityDeletionOrUpdateAdapter<WidgetThemeWallpaper> __deletionAdapterOfWidgetThemeWallpaper;

  private final EntityDeletionOrUpdateAdapter<WidgetThemeWallpaper> __updateAdapterOfWidgetThemeWallpaper;

  public WidgetThemeWallpaperDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWidgetThemeWallpaper = new EntityInsertionAdapter<WidgetThemeWallpaper>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `widget_theme_wallpapers` (`id`,`themeId`,`name`,`order`,`folder`,`imageBg`,`isFree`,`isFavorite`,`isNew`,`category`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetThemeWallpaper entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getThemeId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getOrder());
        statement.bindString(5, entity.getFolder());
        statement.bindString(6, entity.getImageBg());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isNew() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindString(10, entity.getCategory());
      }
    };
    this.__deletionAdapterOfWidgetThemeWallpaper = new EntityDeletionOrUpdateAdapter<WidgetThemeWallpaper>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `widget_theme_wallpapers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetThemeWallpaper entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfWidgetThemeWallpaper = new EntityDeletionOrUpdateAdapter<WidgetThemeWallpaper>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `widget_theme_wallpapers` SET `id` = ?,`themeId` = ?,`name` = ?,`order` = ?,`folder` = ?,`imageBg` = ?,`isFree` = ?,`isFavorite` = ?,`isNew` = ?,`category` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetThemeWallpaper entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getThemeId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getOrder());
        statement.bindString(5, entity.getFolder());
        statement.bindString(6, entity.getImageBg());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isNew() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindString(10, entity.getCategory());
        statement.bindString(11, entity.getId());
      }
    };
  }

  @Override
  public void insertWallpaper(final WidgetThemeWallpaper wallpaper) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetThemeWallpaper.insert(wallpaper);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertWallpapers(final List<WidgetThemeWallpaper> wallpapers) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetThemeWallpaper.insert(wallpapers);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteWallpaper(final WidgetThemeWallpaper wallpaper) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfWidgetThemeWallpaper.handle(wallpaper);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateWallpaper(final WidgetThemeWallpaper wallpaper) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfWidgetThemeWallpaper.handle(wallpaper);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<WidgetThemeWallpaper> getAllWallpapers() {
    final String _sql = "SELECT * FROM widget_theme_wallpapers ORDER BY `order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfThemeId = CursorUtil.getColumnIndexOrThrow(_cursor, "themeId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfImageBg = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBg");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfIsNew = CursorUtil.getColumnIndexOrThrow(_cursor, "isNew");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final List<WidgetThemeWallpaper> _result = new ArrayList<WidgetThemeWallpaper>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WidgetThemeWallpaper _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpThemeId;
        _tmpThemeId = _cursor.getString(_cursorIndexOfThemeId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final int _tmpOrder;
        _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final String _tmpImageBg;
        _tmpImageBg = _cursor.getString(_cursorIndexOfImageBg);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        final boolean _tmpIsNew;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsNew);
        _tmpIsNew = _tmp_2 != 0;
        final String _tmpCategory;
        _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        _item = new WidgetThemeWallpaper(_tmpId,_tmpThemeId,_tmpName,_tmpOrder,_tmpFolder,_tmpImageBg,_tmpIsFree,_tmpIsFavorite,_tmpIsNew,_tmpCategory);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<WidgetThemeWallpaper> getWallpapersByCategory(final String category) {
    final String _sql = "SELECT * FROM widget_theme_wallpapers WHERE category = ? ORDER BY `order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfThemeId = CursorUtil.getColumnIndexOrThrow(_cursor, "themeId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfImageBg = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBg");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfIsNew = CursorUtil.getColumnIndexOrThrow(_cursor, "isNew");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final List<WidgetThemeWallpaper> _result = new ArrayList<WidgetThemeWallpaper>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WidgetThemeWallpaper _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpThemeId;
        _tmpThemeId = _cursor.getString(_cursorIndexOfThemeId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final int _tmpOrder;
        _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final String _tmpImageBg;
        _tmpImageBg = _cursor.getString(_cursorIndexOfImageBg);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        final boolean _tmpIsNew;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsNew);
        _tmpIsNew = _tmp_2 != 0;
        final String _tmpCategory;
        _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        _item = new WidgetThemeWallpaper(_tmpId,_tmpThemeId,_tmpName,_tmpOrder,_tmpFolder,_tmpImageBg,_tmpIsFree,_tmpIsFavorite,_tmpIsNew,_tmpCategory);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public WidgetThemeWallpaper getWallpaperById(final String id) {
    final String _sql = "SELECT * FROM widget_theme_wallpapers WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfThemeId = CursorUtil.getColumnIndexOrThrow(_cursor, "themeId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfImageBg = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBg");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final int _cursorIndexOfIsNew = CursorUtil.getColumnIndexOrThrow(_cursor, "isNew");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final WidgetThemeWallpaper _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpThemeId;
        _tmpThemeId = _cursor.getString(_cursorIndexOfThemeId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final int _tmpOrder;
        _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final String _tmpImageBg;
        _tmpImageBg = _cursor.getString(_cursorIndexOfImageBg);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        final boolean _tmpIsNew;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsNew);
        _tmpIsNew = _tmp_2 != 0;
        final String _tmpCategory;
        _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        _result = new WidgetThemeWallpaper(_tmpId,_tmpThemeId,_tmpName,_tmpOrder,_tmpFolder,_tmpImageBg,_tmpIsFree,_tmpIsFavorite,_tmpIsNew,_tmpCategory);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
