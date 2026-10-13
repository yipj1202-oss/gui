package com.gui.tomato.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0010\u0010\t\u001a\u0004\u0018\u00010\nH\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\n0\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0011\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\r\u00a8\u0006\u0012"}, d2 = {"Lcom/gui/tomato/data/dao/PresetDao;", "", "clearDefault", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDefault", "Lcom/gui/tomato/data/entities/PomodoroPreset;", "insert", "p", "(Lcom/gui/tomato/data/entities/PomodoroPreset;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "listAll", "", "setDefault", "update", "app_debug"})
@androidx.room.Dao()
public abstract interface PresetDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroPreset p, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroPreset p, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM presets WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM presets WHERE isDefault = 1 LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDefault(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.gui.tomato.data.entities.PomodoroPreset> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM presets ORDER BY name")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object listAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gui.tomato.data.entities.PomodoroPreset>> $completion);
    
    @androidx.room.Query(value = "UPDATE presets SET isDefault = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearDefault(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE presets SET isDefault = 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setDefault(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}