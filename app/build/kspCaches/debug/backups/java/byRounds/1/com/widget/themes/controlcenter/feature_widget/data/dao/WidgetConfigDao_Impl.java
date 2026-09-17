package com.widget.themes.controlcenter.feature_widget.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetConfig;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WidgetConfigDao_Impl implements WidgetConfigDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WidgetConfig> __insertionAdapterOfWidgetConfig;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConfig;

  public WidgetConfigDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWidgetConfig = new EntityInsertionAdapter<WidgetConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `widget_configs` (`widgetId`,`bgType`,`solidColor`,`imageUri`,`textColor`,`fontStyle`,`gradientStartColor`,`gradientEndColor`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WidgetConfig entity) {
        statement.bindLong(1, entity.getWidgetId());
        statement.bindString(2, entity.getBgType());
        statement.bindLong(3, entity.getSolidColor());
        if (entity.getImageUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getImageUri());
        }
        statement.bindLong(5, entity.getTextColor());
        statement.bindString(6, entity.getFontStyle());
        statement.bindLong(7, entity.getGradientStartColor());
        statement.bindLong(8, entity.getGradientEndColor());
      }
    };
    this.__preparedStmtOfDeleteConfig = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM widget_configs WHERE widgetId = ?";
        return _query;
      }
    };
  }

  @Override
  public void saveConfig(final WidgetConfig config) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWidgetConfig.insert(config);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteConfig(final int widgetId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConfig.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, widgetId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteConfig.release(_stmt);
    }
  }

  @Override
  public WidgetConfig getConfigForWidget(final int widgetId) {
    final String _sql = "SELECT * FROM widget_configs WHERE widgetId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, widgetId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfWidgetId = CursorUtil.getColumnIndexOrThrow(_cursor, "widgetId");
      final int _cursorIndexOfBgType = CursorUtil.getColumnIndexOrThrow(_cursor, "bgType");
      final int _cursorIndexOfSolidColor = CursorUtil.getColumnIndexOrThrow(_cursor, "solidColor");
      final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
      final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
      final int _cursorIndexOfFontStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "fontStyle");
      final int _cursorIndexOfGradientStartColor = CursorUtil.getColumnIndexOrThrow(_cursor, "gradientStartColor");
      final int _cursorIndexOfGradientEndColor = CursorUtil.getColumnIndexOrThrow(_cursor, "gradientEndColor");
      final WidgetConfig _result;
      if (_cursor.moveToFirst()) {
        final int _tmpWidgetId;
        _tmpWidgetId = _cursor.getInt(_cursorIndexOfWidgetId);
        final String _tmpBgType;
        _tmpBgType = _cursor.getString(_cursorIndexOfBgType);
        final int _tmpSolidColor;
        _tmpSolidColor = _cursor.getInt(_cursorIndexOfSolidColor);
        final String _tmpImageUri;
        if (_cursor.isNull(_cursorIndexOfImageUri)) {
          _tmpImageUri = null;
        } else {
          _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
        }
        final int _tmpTextColor;
        _tmpTextColor = _cursor.getInt(_cursorIndexOfTextColor);
        final String _tmpFontStyle;
        _tmpFontStyle = _cursor.getString(_cursorIndexOfFontStyle);
        final int _tmpGradientStartColor;
        _tmpGradientStartColor = _cursor.getInt(_cursorIndexOfGradientStartColor);
        final int _tmpGradientEndColor;
        _tmpGradientEndColor = _cursor.getInt(_cursorIndexOfGradientEndColor);
        _result = new WidgetConfig(_tmpWidgetId,_tmpBgType,_tmpSolidColor,_tmpImageUri,_tmpTextColor,_tmpFontStyle,_tmpGradientStartColor,_tmpGradientEndColor);
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
