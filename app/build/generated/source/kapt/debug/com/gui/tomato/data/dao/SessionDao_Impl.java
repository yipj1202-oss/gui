package com.gui.tomato.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gui.tomato.data.entities.PomodoroSession;
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
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PomodoroSession> __insertionAdapterOfPomodoroSession;

  private final SharedSQLiteStatement __preparedStmtOfClose;

  private final SharedSQLiteStatement __preparedStmtOfClearOpen;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStatsByTask;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStatsForNullTask;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPomodoroSession = new EntityInsertionAdapter<PomodoroSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sessions` (`id`,`presetId`,`taskId`,`startedAt`,`endedAt`,`type`,`plannedMin`,`completedMin`,`finishedByTime`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PomodoroSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPresetId());
        if (entity.getTaskId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getTaskId());
        }
        statement.bindLong(4, entity.getStartedAt());
        if (entity.getEndedAt() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getEndedAt());
        }
        if (entity.getType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getType());
        }
        statement.bindLong(7, entity.getPlannedMin());
        statement.bindLong(8, entity.getCompletedMin());
        final int _tmp = entity.getFinishedByTime() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfClose = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sessions SET endedAt = ?, completedMin = ?, finishedByTime = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearOpen = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE endedAt IS NULL";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteStatsByTask = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE type='focus' AND endedAt IS NOT NULL AND taskId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteStatsForNullTask = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE type='focus' AND endedAt IS NOT NULL AND taskId IS NULL";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PomodoroSession s, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPomodoroSession.insertAndReturnId(s);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object close(final long id, final long endedAt, final int completedMin, final boolean ftb,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClose.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, endedAt);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, completedMin);
        _argIndex = 3;
        final int _tmp = ftb ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 4;
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
          __preparedStmtOfClose.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearOpen(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearOpen.acquire();
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
          __preparedStmtOfClearOpen.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStatsByTask(final long taskId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStatsByTask.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, taskId);
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
          __preparedStmtOfDeleteStatsByTask.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStatsForNullTask(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStatsForNullTask.acquire();
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
          __preparedStmtOfDeleteStatsForNullTask.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getOpen(final Continuation<? super PomodoroSession> $completion) {
    final String _sql = "SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PomodoroSession>() {
      @Override
      @Nullable
      public PomodoroSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "presetId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPlannedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMin");
          final int _cursorIndexOfCompletedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "completedMin");
          final int _cursorIndexOfFinishedByTime = CursorUtil.getColumnIndexOrThrow(_cursor, "finishedByTime");
          final PomodoroSession _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPresetId;
            _tmpPresetId = _cursor.getLong(_cursorIndexOfPresetId);
            final Long _tmpTaskId;
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null;
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            }
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final int _tmpPlannedMin;
            _tmpPlannedMin = _cursor.getInt(_cursorIndexOfPlannedMin);
            final int _tmpCompletedMin;
            _tmpCompletedMin = _cursor.getInt(_cursorIndexOfCompletedMin);
            final boolean _tmpFinishedByTime;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFinishedByTime);
            _tmpFinishedByTime = _tmp != 0;
            _result = new PomodoroSession(_tmpId,_tmpPresetId,_tmpTaskId,_tmpStartedAt,_tmpEndedAt,_tmpType,_tmpPlannedMin,_tmpCompletedMin,_tmpFinishedByTime);
          } else {
            _result = null;
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
  public Object focusInRange(final long from, final long to,
      final Continuation<? super List<PomodoroSession>> $completion) {
    final String _sql = "SELECT * FROM sessions WHERE type='focus' AND startedAt BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PomodoroSession>>() {
      @Override
      @NonNull
      public List<PomodoroSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "presetId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPlannedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMin");
          final int _cursorIndexOfCompletedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "completedMin");
          final int _cursorIndexOfFinishedByTime = CursorUtil.getColumnIndexOrThrow(_cursor, "finishedByTime");
          final List<PomodoroSession> _result = new ArrayList<PomodoroSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PomodoroSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPresetId;
            _tmpPresetId = _cursor.getLong(_cursorIndexOfPresetId);
            final Long _tmpTaskId;
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null;
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            }
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final int _tmpPlannedMin;
            _tmpPlannedMin = _cursor.getInt(_cursorIndexOfPlannedMin);
            final int _tmpCompletedMin;
            _tmpCompletedMin = _cursor.getInt(_cursorIndexOfCompletedMin);
            final boolean _tmpFinishedByTime;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFinishedByTime);
            _tmpFinishedByTime = _tmp != 0;
            _item = new PomodoroSession(_tmpId,_tmpPresetId,_tmpTaskId,_tmpStartedAt,_tmpEndedAt,_tmpType,_tmpPlannedMin,_tmpCompletedMin,_tmpFinishedByTime);
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
  public Object all(final Continuation<? super List<PomodoroSession>> $completion) {
    final String _sql = "SELECT * FROM sessions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PomodoroSession>>() {
      @Override
      @NonNull
      public List<PomodoroSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "presetId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPlannedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMin");
          final int _cursorIndexOfCompletedMin = CursorUtil.getColumnIndexOrThrow(_cursor, "completedMin");
          final int _cursorIndexOfFinishedByTime = CursorUtil.getColumnIndexOrThrow(_cursor, "finishedByTime");
          final List<PomodoroSession> _result = new ArrayList<PomodoroSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PomodoroSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPresetId;
            _tmpPresetId = _cursor.getLong(_cursorIndexOfPresetId);
            final Long _tmpTaskId;
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null;
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            }
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final int _tmpPlannedMin;
            _tmpPlannedMin = _cursor.getInt(_cursorIndexOfPlannedMin);
            final int _tmpCompletedMin;
            _tmpCompletedMin = _cursor.getInt(_cursorIndexOfCompletedMin);
            final boolean _tmpFinishedByTime;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFinishedByTime);
            _tmpFinishedByTime = _tmp != 0;
            _item = new PomodoroSession(_tmpId,_tmpPresetId,_tmpTaskId,_tmpStartedAt,_tmpEndedAt,_tmpType,_tmpPlannedMin,_tmpCompletedMin,_tmpFinishedByTime);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
