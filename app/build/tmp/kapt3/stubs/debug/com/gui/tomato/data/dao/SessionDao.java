package com.gui.tomato.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000e\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u000e\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0005J.\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010\u0014\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0005J$\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0016\u001a\u00020\n2\u0006\u0010\u0017\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u0016\u0010\u001a\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/gui/tomato/data/dao/SessionDao;", "", "all", "", "Lcom/gui/tomato/data/entities/PomodoroSession;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearOpen", "", "close", "id", "", "endedAt", "completedMin", "", "ftb", "", "(JJIZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteStatsByTask", "taskId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteStatsForNullTask", "focusInRange", "from", "to", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getOpen", "insert", "s", "(Lcom/gui/tomato/data/entities/PomodoroSession;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface SessionDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroSession s, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getOpen(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.gui.tomato.data.entities.PomodoroSession> $completion);
    
    @androidx.room.Query(value = "UPDATE sessions SET endedAt = :endedAt, completedMin = :completedMin, finishedByTime = :ftb WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object close(long id, long endedAt, int completedMin, boolean ftb, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sessions WHERE endedAt IS NULL")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearOpen(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE type=\'focus\' AND startedAt BETWEEN :from AND :to")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object focusInRange(long from, long to, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gui.tomato.data.entities.PomodoroSession>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object all(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gui.tomato.data.entities.PomodoroSession>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sessions WHERE type=\'focus\' AND endedAt IS NOT NULL AND taskId = :taskId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteStatsByTask(long taskId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sessions WHERE type=\'focus\' AND endedAt IS NOT NULL AND taskId IS NULL")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteStatsForNullTask(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}