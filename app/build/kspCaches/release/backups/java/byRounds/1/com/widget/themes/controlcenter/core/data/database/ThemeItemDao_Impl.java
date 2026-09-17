package com.widget.themes.controlcenter.core.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.core.data.entity.ThemeItem;
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
public final class ThemeItemDao_Impl implements ThemeItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ThemeItem> __insertionAdapterOfThemeItem;

  private final EntityDeletionOrUpdateAdapter<ThemeItem> __deletionAdapterOfThemeItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteThemeById;

  public ThemeItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfThemeItem = new EntityInsertionAdapter<ThemeItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_themes` (`id`,`categoryId`,`name`,`path`,`rawType`,`backgroundPath`,`previewPath`,`thumbPath`,`isPremium`,`isFree`,`isHot`,`isNew`,`downloads`,`totalIcons`,`totalWidgets`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ThemeItem entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCategoryId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getPath());
        statement.bindString(5, entity.getRawType());
        if (entity.getBackgroundPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBackgroundPath());
        }
        if (entity.getPreviewPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPreviewPath());
        }
        if (entity.getThumbPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getThumbPath());
        }
        final int _tmp = entity.isPremium() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isFree() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.isHot() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        final int _tmp_3 = entity.isNew() ? 1 : 0;
        statement.bindLong(12, _tmp_3);
        statement.bindLong(13, entity.getDownloads());
        statement.bindLong(14, entity.getTotalIcons());
        statement.bindLong(15, entity.getTotalWidgets());
      }
    };
    this.__deletionAdapterOfThemeItem = new EntityDeletionOrUpdateAdapter<ThemeItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `app_themes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ThemeItem entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteThemeById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM app_themes WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertTheme(final ThemeItem theme) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfThemeItem.insertAndReturnId(theme);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteTheme(final ThemeItem theme) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfThemeItem.handle(theme);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteThemeById(final String id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteThemeById.acquire();
    int _argIndex = 1;
    _stmt.bindString(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteThemeById.release(_stmt);
    }
  }

  @Override
  public List<ThemeItem> getAllThemes() {
    final String _sql = "SELECT * FROM app_themes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfRawType = CursorUtil.getColumnIndexOrThrow(_cursor, "rawType");
      final int _cursorIndexOfBackgroundPath = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundPath");
      final int _cursorIndexOfPreviewPath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewPath");
      final int _cursorIndexOfThumbPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbPath");
      final int _cursorIndexOfIsPremium = CursorUtil.getColumnIndexOrThrow(_cursor, "isPremium");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsHot = CursorUtil.getColumnIndexOrThrow(_cursor, "isHot");
      final int _cursorIndexOfIsNew = CursorUtil.getColumnIndexOrThrow(_cursor, "isNew");
      final int _cursorIndexOfDownloads = CursorUtil.getColumnIndexOrThrow(_cursor, "downloads");
      final int _cursorIndexOfTotalIcons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalIcons");
      final int _cursorIndexOfTotalWidgets = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWidgets");
      final List<ThemeItem> _result = new ArrayList<ThemeItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ThemeItem _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpCategoryId;
        _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpPath;
        _tmpPath = _cursor.getString(_cursorIndexOfPath);
        final String _tmpRawType;
        _tmpRawType = _cursor.getString(_cursorIndexOfRawType);
        final String _tmpBackgroundPath;
        if (_cursor.isNull(_cursorIndexOfBackgroundPath)) {
          _tmpBackgroundPath = null;
        } else {
          _tmpBackgroundPath = _cursor.getString(_cursorIndexOfBackgroundPath);
        }
        final String _tmpPreviewPath;
        if (_cursor.isNull(_cursorIndexOfPreviewPath)) {
          _tmpPreviewPath = null;
        } else {
          _tmpPreviewPath = _cursor.getString(_cursorIndexOfPreviewPath);
        }
        final String _tmpThumbPath;
        if (_cursor.isNull(_cursorIndexOfThumbPath)) {
          _tmpThumbPath = null;
        } else {
          _tmpThumbPath = _cursor.getString(_cursorIndexOfThumbPath);
        }
        final boolean _tmpIsPremium;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPremium);
        _tmpIsPremium = _tmp != 0;
        final boolean _tmpIsFree;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp_1 != 0;
        final boolean _tmpIsHot;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsHot);
        _tmpIsHot = _tmp_2 != 0;
        final boolean _tmpIsNew;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsNew);
        _tmpIsNew = _tmp_3 != 0;
        final long _tmpDownloads;
        _tmpDownloads = _cursor.getLong(_cursorIndexOfDownloads);
        final int _tmpTotalIcons;
        _tmpTotalIcons = _cursor.getInt(_cursorIndexOfTotalIcons);
        final int _tmpTotalWidgets;
        _tmpTotalWidgets = _cursor.getInt(_cursorIndexOfTotalWidgets);
        _item = new ThemeItem(_tmpId,_tmpCategoryId,_tmpName,_tmpPath,_tmpRawType,_tmpBackgroundPath,_tmpPreviewPath,_tmpThumbPath,_tmpIsPremium,_tmpIsFree,_tmpIsHot,_tmpIsNew,_tmpDownloads,_tmpTotalIcons,_tmpTotalWidgets);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public ThemeItem getThemeById(final String id) {
    final String _sql = "SELECT * FROM app_themes WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfRawType = CursorUtil.getColumnIndexOrThrow(_cursor, "rawType");
      final int _cursorIndexOfBackgroundPath = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundPath");
      final int _cursorIndexOfPreviewPath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewPath");
      final int _cursorIndexOfThumbPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbPath");
      final int _cursorIndexOfIsPremium = CursorUtil.getColumnIndexOrThrow(_cursor, "isPremium");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsHot = CursorUtil.getColumnIndexOrThrow(_cursor, "isHot");
      final int _cursorIndexOfIsNew = CursorUtil.getColumnIndexOrThrow(_cursor, "isNew");
      final int _cursorIndexOfDownloads = CursorUtil.getColumnIndexOrThrow(_cursor, "downloads");
      final int _cursorIndexOfTotalIcons = CursorUtil.getColumnIndexOrThrow(_cursor, "totalIcons");
      final int _cursorIndexOfTotalWidgets = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWidgets");
      final ThemeItem _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpCategoryId;
        _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpPath;
        _tmpPath = _cursor.getString(_cursorIndexOfPath);
        final String _tmpRawType;
        _tmpRawType = _cursor.getString(_cursorIndexOfRawType);
        final String _tmpBackgroundPath;
        if (_cursor.isNull(_cursorIndexOfBackgroundPath)) {
          _tmpBackgroundPath = null;
        } else {
          _tmpBackgroundPath = _cursor.getString(_cursorIndexOfBackgroundPath);
        }
        final String _tmpPreviewPath;
        if (_cursor.isNull(_cursorIndexOfPreviewPath)) {
          _tmpPreviewPath = null;
        } else {
          _tmpPreviewPath = _cursor.getString(_cursorIndexOfPreviewPath);
        }
        final String _tmpThumbPath;
        if (_cursor.isNull(_cursorIndexOfThumbPath)) {
          _tmpThumbPath = null;
        } else {
          _tmpThumbPath = _cursor.getString(_cursorIndexOfThumbPath);
        }
        final boolean _tmpIsPremium;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPremium);
        _tmpIsPremium = _tmp != 0;
        final boolean _tmpIsFree;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp_1 != 0;
        final boolean _tmpIsHot;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsHot);
        _tmpIsHot = _tmp_2 != 0;
        final boolean _tmpIsNew;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsNew);
        _tmpIsNew = _tmp_3 != 0;
        final long _tmpDownloads;
        _tmpDownloads = _cursor.getLong(_cursorIndexOfDownloads);
        final int _tmpTotalIcons;
        _tmpTotalIcons = _cursor.getInt(_cursorIndexOfTotalIcons);
        final int _tmpTotalWidgets;
        _tmpTotalWidgets = _cursor.getInt(_cursorIndexOfTotalWidgets);
        _result = new ThemeItem(_tmpId,_tmpCategoryId,_tmpName,_tmpPath,_tmpRawType,_tmpBackgroundPath,_tmpPreviewPath,_tmpThumbPath,_tmpIsPremium,_tmpIsFree,_tmpIsHot,_tmpIsNew,_tmpDownloads,_tmpTotalIcons,_tmpTotalWidgets);
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
