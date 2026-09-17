package com.widget.themes.controlcenter.feature_widget.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem;
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
public final class WidgetItemDao_Impl implements WidgetItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WidgetItem> __insertionAdapterOfWidgetItem;

  public WidgetItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWidgetItem = new EntityInsertionAdapter<WidgetItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `widget_items` (`id`,`themeFolder`,`name`,`widgetType`,`size`,`isFree`,`isFavorite`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetItem entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getThemeFolder());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getWidgetType());
        statement.bindString(5, entity.getSize());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
      }
    };
  }

  @Override
  public void insertWidgetItems(final List<WidgetItem> items) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetItem.insert(items);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<WidgetItem> getAllWidgetItems() {
    final String _sql = "SELECT * FROM widget_items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfThemeFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "themeFolder");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfWidgetType = CursorUtil.getColumnIndexOrThrow(_cursor, "widgetType");
      final int _cursorIndexOfSize = CursorUtil.getColumnIndexOrThrow(_cursor, "size");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<WidgetItem> _result = new ArrayList<WidgetItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WidgetItem _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpThemeFolder;
        _tmpThemeFolder = _cursor.getString(_cursorIndexOfThemeFolder);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpWidgetType;
        _tmpWidgetType = _cursor.getString(_cursorIndexOfWidgetType);
        final String _tmpSize;
        _tmpSize = _cursor.getString(_cursorIndexOfSize);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        _item = new WidgetItem(_tmpId,_tmpThemeFolder,_tmpName,_tmpWidgetType,_tmpSize,_tmpIsFree,_tmpIsFavorite);
        _result.add(_item);
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
