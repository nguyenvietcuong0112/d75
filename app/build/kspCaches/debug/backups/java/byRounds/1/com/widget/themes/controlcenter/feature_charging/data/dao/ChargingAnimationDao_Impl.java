package com.widget.themes.controlcenter.feature_charging.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.widget.themes.controlcenter.feature_charging.data.entity.ChargingAnimation;
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
public final class ChargingAnimationDao_Impl implements ChargingAnimationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChargingAnimation> __insertionAdapterOfChargingAnimation;

  private final EntityDeletionOrUpdateAdapter<ChargingAnimation> __updateAdapterOfChargingAnimation;

  public ChargingAnimationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChargingAnimation = new EntityInsertionAdapter<ChargingAnimation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `charging_animations` (`id`,`name`,`folder`,`isFree`,`isFavorite`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChargingAnimation entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getFolder());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
      }
    };
    this.__updateAdapterOfChargingAnimation = new EntityDeletionOrUpdateAdapter<ChargingAnimation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `charging_animations` SET `id` = ?,`name` = ?,`folder` = ?,`isFree` = ?,`isFavorite` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChargingAnimation entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getFolder());
        final int _tmp = entity.isFree() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindString(6, entity.getId());
      }
    };
  }

  @Override
  public void insertAnimations(final List<ChargingAnimation> animations) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfChargingAnimation.insert(animations);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateAnimation(final ChargingAnimation animation) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfChargingAnimation.handle(animation);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<ChargingAnimation> getAllAnimations() {
    final String _sql = "SELECT * FROM charging_animations";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfFolder = CursorUtil.getColumnIndexOrThrow(_cursor, "folder");
      final int _cursorIndexOfIsFree = CursorUtil.getColumnIndexOrThrow(_cursor, "isFree");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<ChargingAnimation> _result = new ArrayList<ChargingAnimation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ChargingAnimation _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final String _tmpFolder;
        _tmpFolder = _cursor.getString(_cursorIndexOfFolder);
        final boolean _tmpIsFree;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFree);
        _tmpIsFree = _tmp != 0;
        final boolean _tmpIsFavorite;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _tmpIsFavorite = _tmp_1 != 0;
        _item = new ChargingAnimation(_tmpId,_tmpName,_tmpFolder,_tmpIsFree,_tmpIsFavorite);
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
