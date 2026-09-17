package com.widget.themes.controlcenter.feature_widget.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetThemeWidget;
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
public final class WidgetThemeWidgetDao_Impl implements WidgetThemeWidgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WidgetThemeWidget> __insertionAdapterOfWidgetThemeWidget;

  public WidgetThemeWidgetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWidgetThemeWidget = new EntityInsertionAdapter<WidgetThemeWidget>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `widget_theme_widgets` (`id`,`name`,`folder`,`category`,`isFree`,`isFavorite`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetThemeWidget entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getFolder());
        statement.bindString(4, entity.getCategory());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
      }
    };
  }

  @Override
  public void insertWidgetThemes(final List<WidgetThemeWidget> themes) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetThemeWidget.insert(themes);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertWidgetTheme(final WidgetThemeWidget theme) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetThemeWidget.insert(theme);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<WidgetThemeWidget> getAllWidgetThemes() {
    final String _sql = "SELECT * FROM widget_theme_widgets";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<WidgetThemeWidget> _result = new ArrayList<WidgetThemeWidget>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WidgetThemeWidget _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final String _tmpCategory;
        _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        _item = new WidgetThemeWidget(_tmpId,_tmpName,_tmpFolder,_tmpCategory,_tmpIsFree,_tmpIsFavorite);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public WidgetThemeWidget getWidgetThemeByFolder(final String folder) {
    final String _sql = "SELECT * FROM widget_theme_widgets WHERE folder = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, folder);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final WidgetThemeWidget _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final String _tmpCategory;
        _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        _result = new WidgetThemeWidget(_tmpId,_tmpName,_tmpFolder,_tmpCategory,_tmpIsFree,_tmpIsFavorite);
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
