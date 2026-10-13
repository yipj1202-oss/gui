package com.gui.tomato.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000e\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001d\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u000b2\b\u0010\u001f\u001a\u0004\u0018\u00010 \u00a2\u0006\u0002\u0010!J\u000e\u0010\"\u001a\u00020\u001d2\u0006\u0010#\u001a\u00020\u000bJ\u0006\u0010$\u001a\u00020\u0017J\b\u0010%\u001a\u00020\u000bH\u0002J\u0006\u0010&\u001a\u00020\u001dJ\u0006\u0010\'\u001a\u00020\u001dJ\u0006\u0010(\u001a\u00020\u001dJ\u0006\u0010)\u001a\u00020\u001dJ\u0006\u0010*\u001a\u00020\u001dJ\u0006\u0010+\u001a\u00020\u001dJ\u0015\u0010,\u001a\u00020\u001d2\b\u0010\u001f\u001a\u0004\u0018\u00010 \u00a2\u0006\u0002\u0010-R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0010\u0010\u0010\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\t0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0015R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00170\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015\u00a8\u0006."}, d2 = {"Lcom/gui/tomato/service/PomodoroRepository;", "", "appContext", "Landroid/content/Context;", "db", "Lcom/gui/tomato/data/db/AppDatabase;", "(Landroid/content/Context;Lcom/gui/tomato/data/db/AppDatabase;)V", "_isOverclock", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "cachedDefaultPreset", "Lcom/gui/tomato/data/entities/PomodoroPreset;", "engine", "Lcom/gui/tomato/engine/PomodoroEngine;", "getEngine", "()Lcom/gui/tomato/engine/PomodoroEngine;", "engineRef", "io", "Lkotlinx/coroutines/CoroutineScope;", "isOverclock", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "mutable", "Lcom/gui/tomato/engine/Snapshot;", "prefs", "Lcom/gui/tomato/ui/prefs/AppPrefs;", "ui", "getUi", "activateOverclock", "", "preset", "taskId", "", "(Lcom/gui/tomato/data/entities/PomodoroPreset;Ljava/lang/Long;)V", "applyPreset", "p", "current", "defaultPreset", "exitOverclock", "finish", "pause", "recoverOnStartup", "resume", "skip", "start", "(Ljava/lang/Long;)V", "app_debug"})
public final class PomodoroRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.data.db.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope io = null;
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.ui.prefs.AppPrefs prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.gui.tomato.engine.Snapshot> mutable = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.gui.tomato.engine.Snapshot> ui = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isOverclock = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOverclock = null;
    @org.jetbrains.annotations.Nullable()
    private com.gui.tomato.data.entities.PomodoroPreset cachedDefaultPreset;
    @org.jetbrains.annotations.Nullable()
    private com.gui.tomato.engine.PomodoroEngine engineRef;
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.engine.PomodoroEngine engine = null;
    
    public PomodoroRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context appContext, @org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.db.AppDatabase db) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.gui.tomato.engine.Snapshot> getUi() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOverclock() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.PomodoroEngine getEngine() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.Snapshot current() {
        return null;
    }
    
    public final void applyPreset(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroPreset p) {
    }
    
    public final void start(@org.jetbrains.annotations.Nullable()
    java.lang.Long taskId) {
    }
    
    public final void pause() {
    }
    
    public final void resume() {
    }
    
    public final void skip() {
    }
    
    public final void finish() {
    }
    
    /**
     * 启动狂暴模式：切到狂暴方案 + 标记状态 + 开跑（UI 层负责传 preset）
     */
    public final void activateOverclock(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroPreset preset, @org.jetbrains.annotations.Nullable()
    java.lang.Long taskId) {
    }
    
    /**
     * 手动退出狂暴：切回默认方案（不打断当前计时）
     */
    public final void exitOverclock() {
    }
    
    public final void recoverOnStartup() {
    }
    
    private final com.gui.tomato.data.entities.PomodoroPreset defaultPreset() {
        return null;
    }
}