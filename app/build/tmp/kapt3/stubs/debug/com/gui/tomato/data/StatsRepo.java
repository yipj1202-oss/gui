package com.gui.tomato.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J$\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\rJ$\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/gui/tomato/data/StatsRepo;", "", "sessionDao", "Lcom/gui/tomato/data/dao/SessionDao;", "taskDao", "Lcom/gui/tomato/data/dao/TaskDao;", "(Lcom/gui/tomato/data/dao/SessionDao;Lcom/gui/tomato/data/dao/TaskDao;)V", "byHour", "", "Lcom/gui/tomato/data/HourStatRow;", "from", "", "to", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "byTask", "Lcom/gui/tomato/data/TaskStatRow;", "app_debug"})
public final class StatsRepo {
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.data.dao.SessionDao sessionDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.data.dao.TaskDao taskDao = null;
    
    public StatsRepo(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.dao.SessionDao sessionDao, @org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.dao.TaskDao taskDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object byTask(long from, long to, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gui.tomato.data.TaskStatRow>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object byHour(long from, long to, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gui.tomato.data.HourStatRow>> $completion) {
        return null;
    }
}