package com.widget.themes.controlcenter.feature_collections.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DownloadedCollectionDao_Impl implements DownloadedCollectionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DownloadedCollectionItem> __insertionAdapterOfDownloadedCollectionItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDownloaded;

  public DownloadedCollectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDownloadedCollectionItem = new EntityInsertionAdapter<DownloadedCollectionItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `downloaded_collections` (`id`,`name`,`category`,`targetPath`,`previewPath`,`downloads`,`rawType`,`extra`,`appliedTimestamp`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DownloadedCollectionItem entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategory());
        statement.bindString(4, entity.getTargetPath());
        statement.bindString(5, entity.getPreviewPath());
        statement.bindLong(6, entity.getDownloads());
        statement.bindString(7, entity.getRawType());
        if (entity.getExtra() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getExtra());
        }
        statement.bindLong(9, entity.getAppliedTimestamp());
      }
    };
    this.__preparedStmtOfDeleteDownloaded = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM downloaded_collections WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDownloaded(final DownloadedCollectionItem item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDownloadedCollectionItem.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDownloaded(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDownloaded.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDownloaded.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DownloadedCollectionItem>> getDownloadedByCategoryFlow(final String category) {
    final String _sql = "SELECT * FROM downloaded_collections WHERE LOWER(category) = LOWER(?) ORDER BY appliedTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_collections"}, new Callable<List<DownloadedCollectionItem>>() {
      @Override
      @NonNull
      public List<DownloadedCollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfTargetPath = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPath");
          final int _cursorIndexOfPreviewPath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewPath");
          final int _cursorIndexOfDownloads = CursorUtil.getColumnIndexOrThrow(_cursor, "downloads");
          final int _cursorIndexOfRawType = CursorUtil.getColumnIndexOrThrow(_cursor, "rawType");
          final int _cursorIndexOfExtra = CursorUtil.getColumnIndexOrThrow(_cursor, "extra");
          final int _cursorIndexOfAppliedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "appliedTimestamp");
          final List<DownloadedCollectionItem> _result = new ArrayList<DownloadedCollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedCollectionItem _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpTargetPath;
            _tmpTargetPath = _cursor.getString(_cursorIndexOfTargetPath);
            final String _tmpPreviewPath;
            _tmpPreviewPath = _cursor.getString(_cursorIndexOfPreviewPath);
            final long _tmpDownloads;
            _tmpDownloads = _cursor.getLong(_cursorIndexOfDownloads);
            final String _tmpRawType;
            _tmpRawType = _cursor.getString(_cursorIndexOfRawType);
            final String _tmpExtra;
            if (_cursor.isNull(_cursorIndexOfExtra)) {
              _tmpExtra = null;
            } else {
              _tmpExtra = _cursor.getString(_cursorIndexOfExtra);
            }
            final long _tmpAppliedTimestamp;
            _tmpAppliedTimestamp = _cursor.getLong(_cursorIndexOfAppliedTimestamp);
            _item = new DownloadedCollectionItem(_tmpId,_tmpName,_tmpCategory,_tmpTargetPath,_tmpPreviewPath,_tmpDownloads,_tmpRawType,_tmpExtra,_tmpAppliedTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDownloadedByCategory(final String category,
      final Continuation<? super List<DownloadedCollectionItem>> $completion) {
    final String _sql = "SELECT * FROM downloaded_collections WHERE LOWER(category) = LOWER(?) ORDER BY appliedTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DownloadedCollectionItem>>() {
      @Override
      @NonNull
      public List<DownloadedCollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfTargetPath = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPath");
          final int _cursorIndexOfPreviewPath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewPath");
          final int _cursorIndexOfDownloads = CursorUtil.getColumnIndexOrThrow(_cursor, "downloads");
          final int _cursorIndexOfRawType = CursorUtil.getColumnIndexOrThrow(_cursor, "rawType");
          final int _cursorIndexOfExtra = CursorUtil.getColumnIndexOrThrow(_cursor, "extra");
          final int _cursorIndexOfAppliedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "appliedTimestamp");
          final List<DownloadedCollectionItem> _result = new ArrayList<DownloadedCollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedCollectionItem _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpTargetPath;
            _tmpTargetPath = _cursor.getString(_cursorIndexOfTargetPath);
            final String _tmpPreviewPath;
            _tmpPreviewPath = _cursor.getString(_cursorIndexOfPreviewPath);
            final long _tmpDownloads;
            _tmpDownloads = _cursor.getLong(_cursorIndexOfDownloads);
            final String _tmpRawType;
            _tmpRawType = _cursor.getString(_cursorIndexOfRawType);
            final String _tmpExtra;
            if (_cursor.isNull(_cursorIndexOfExtra)) {
              _tmpExtra = null;
            } else {
              _tmpExtra = _cursor.getString(_cursorIndexOfExtra);
            }
            final long _tmpAppliedTimestamp;
            _tmpAppliedTimestamp = _cursor.getLong(_cursorIndexOfAppliedTimestamp);
            _item = new DownloadedCollectionItem(_tmpId,_tmpName,_tmpCategory,_tmpTargetPath,_tmpPreviewPath,_tmpDownloads,_tmpRawType,_tmpExtra,_tmpAppliedTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllDownloaded(
      final Continuation<? super List<DownloadedCollectionItem>> $completion) {
    final String _sql = "SELECT * FROM downloaded_collections ORDER BY appliedTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DownloadedCollectionItem>>() {
      @Override
      @NonNull
      public List<DownloadedCollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfTargetPath = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPath");
          final int _cursorIndexOfPreviewPath = CursorUtil.getColumnIndexOrThrow(_cursor, "previewPath");
          final int _cursorIndexOfDownloads = CursorUtil.getColumnIndexOrThrow(_cursor, "downloads");
          final int _cursorIndexOfRawType = CursorUtil.getColumnIndexOrThrow(_cursor, "rawType");
          final int _cursorIndexOfExtra = CursorUtil.getColumnIndexOrThrow(_cursor, "extra");
          final int _cursorIndexOfAppliedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "appliedTimestamp");
          final List<DownloadedCollectionItem> _result = new ArrayList<DownloadedCollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedCollectionItem _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpTargetPath;
            _tmpTargetPath = _cursor.getString(_cursorIndexOfTargetPath);
            final String _tmpPreviewPath;
            _tmpPreviewPath = _cursor.getString(_cursorIndexOfPreviewPath);
            final long _tmpDownloads;
            _tmpDownloads = _cursor.getLong(_cursorIndexOfDownloads);
            final String _tmpRawType;
            _tmpRawType = _cursor.getString(_cursorIndexOfRawType);
            final String _tmpExtra;
            if (_cursor.isNull(_cursorIndexOfExtra)) {
              _tmpExtra = null;
            } else {
              _tmpExtra = _cursor.getString(_cursorIndexOfExtra);
            }
            final long _tmpAppliedTimestamp;
            _tmpAppliedTimestamp = _cursor.getLong(_cursorIndexOfAppliedTimestamp);
            _item = new DownloadedCollectionItem(_tmpId,_tmpName,_tmpCategory,_tmpTargetPath,_tmpPreviewPath,_tmpDownloads,_tmpRawType,_tmpExtra,_tmpAppliedTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object countDownloaded(final String id, final String prefixedId, final String path,
      final String name, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM downloaded_collections WHERE (? != '' AND (id = ? OR id = ?)) OR (? != '' AND targetPath = ?) OR (? != '' AND LOWER(name) = LOWER(?))";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 7);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    _argIndex = 2;
    _statement.bindString(_argIndex, id);
    _argIndex = 3;
    _statement.bindString(_argIndex, prefixedId);
    _argIndex = 4;
    _statement.bindString(_argIndex, path);
    _argIndex = 5;
    _statement.bindString(_argIndex, path);
    _argIndex = 6;
    _statement.bindString(_argIndex, name);
    _argIndex = 7;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
