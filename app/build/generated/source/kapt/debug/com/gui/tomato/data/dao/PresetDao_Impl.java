package com.gui.tomato.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gui.tomato.data.entities.PomodoroPreset;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PresetDao_Impl implements PresetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PomodoroPreset> __insertionAdapterOfPomodoroPreset;

  private final EntityDeletionOrUpdateAdapter<PomodoroPreset> __updateAdapterOfPomodoroPreset;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfClearDefault;

  private final SharedSQLiteStatement __preparedStmtOfSetDefault;

  public PresetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPomodoroPreset = new EntityInsertionAdapter<PomodoroPreset>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `presets` (`id`,`name`,`focusMin`,`shortBreakMin`,`longBreakEvery`,`longBreakMin`,`isDefault`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PomodoroPreset entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        statement.bindLong(3, entity.getFocusMin());
        statement.bindLong(4, entity.getShortBreakMin());
        statement.bindLong(5, entity.getLongBreakEvery());
        statement.bindLong(6, entity.getLongBreakMin());
        final int _tmp = entity.isDefault() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__updateAdapterOfPomodoroPreset = new EntityDeletionOrUpdateAdapter<PomodoroPreset>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `presets` SET `id` = ?,`name` = ?,`focusMin` = ?,`shortBreakMin` = ?,`longBreakEvery` = ?,`longBreakMin` = ?,`isDefault` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PomodoroPreset entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        statement.bindLong(3, entity.getFocusMin());
        statement.bindLong(4, entity.getShortBreakMin());
        statement.bindLong(5, entity.getLongBreakEvery());
        statement.bindLong(6, entity.getLongBreakMin());
        final int _tmp = entity.isDefault() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM presets WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearDefault = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE presets SET isDefault = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetDefault = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE presets SET isDefault = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PomodoroPreset p, final Continuation<? super Long> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPomodoroPreset.insertAndReturnId(p);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object update(final PomodoroPreset p, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPomodoroPreset.handle(p);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object delete(final long id, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, arg1);
  }

  @Override
  public Object clearDefault(final Continuation<? super Unit> arg0) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDefault.acquire();
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
          __preparedStmtOfClearDefault.release(_stmt);
        }
      }
    }, arg0);
  }

  @Override
  public Object setDefault(final long id, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetDefault.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfSetDefault.release(_stmt);
        }
      }
    }, arg1);
  }

  @Override
  public Object getDefault(final Continuation<? super PomodoroPreset> arg0) {
    final String _sql = "SELECT * FROM presets WHERE isDefault = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PomodoroPreset>() {
      @Override
      @Nullable
      public PomodoroPreset call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfFocusMin = CursorUtil.getColumnIndexOrThrow(_cursor, "focusMin");
          final int _cursorIndexOfShortBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "shortBreakMin");
          final int _cursorIndexOfLongBreakEvery = CursorUtil.getColumnIndexOrThrow(_cursor, "longBreakEvery");
          final int _cursorIndexOfLongBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "longBreakMin");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "isDefault");
          final PomodoroPreset _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final int _tmpFocusMin;
            _tmpFocusMin = _cursor.getInt(_cursorIndexOfFocusMin);
            final int _tmpShortBreakMin;
            _tmpShortBreakMin = _cursor.getInt(_cursorIndexOfShortBreakMin);
            final int _tmpLongBreakEvery;
            _tmpLongBreakEvery = _cursor.getInt(_cursorIndexOfLongBreakEvery);
            final int _tmpLongBreakMin;
            _tmpLongBreakMin = _cursor.getInt(_cursorIndexOfLongBreakMin);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            _result = new PomodoroPreset(_tmpId,_tmpName,_tmpFocusMin,_tmpShortBreakMin,_tmpLongBreakEvery,_tmpLongBreakMin,_tmpIsDefault);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg0);
  }

  @Override
  public Object listAll(final Continuation<? super List<PomodoroPreset>> arg0) {
    final String _sql = "SELECT * FROM presets ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PomodoroPreset>>() {
      @Override
      @NonNull
      public List<PomodoroPreset> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfFocusMin = CursorUtil.getColumnIndexOrThrow(_cursor, "focusMin");
          final int _cursorIndexOfShortBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "shortBreakMin");
          final int _cursorIndexOfLongBreakEvery = CursorUtil.getColumnIndexOrThrow(_cursor, "longBreakEvery");
          final int _cursorIndexOfLongBreakMin = CursorUtil.getColumnIndexOrThrow(_cursor, "longBreakMin");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "isDefault");
          final List<PomodoroPreset> _result = new ArrayList<PomodoroPreset>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PomodoroPreset _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final int _tmpFocusMin;
            _tmpFocusMin = _cursor.getInt(_cursorIndexOfFocusMin);
            final int _tmpShortBreakMin;
            _tmpShortBreakMin = _cursor.getInt(_cursorIndexOfShortBreakMin);
            final int _tmpLongBreakEvery;
            _tmpLongBreakEvery = _cursor.getInt(_cursorIndexOfLongBreakEvery);
            final int _tmpLongBreakMin;
            _tmpLongBreakMin = _cursor.getInt(_cursorIndexOfLongBreakMin);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            _item = new PomodoroPreset(_tmpId,_tmpName,_tmpFocusMin,_tmpShortBreakMin,_tmpLongBreakEvery,_tmpLongBreakMin,_tmpIsDefault);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg0);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
