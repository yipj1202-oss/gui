package com.gui.tomato.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u00fa\u0001\u0012y\b\u0002\u0010\u0002\u001as\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0007\u0012\u0015\u0012\u0013\u0018\u00010\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\b\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\n\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u000e0\u0003\u0012b\b\u0002\u0010\u000f\u001a\\\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0011\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u0013\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0014\u0012\u0004\u0012\u00020\u000e0\u0010\u0012\u0014\b\u0002\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u000e0\u0016\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010+\u001a\u00020\u000e2\u0006\u0010,\u001a\u00020\u001fJ\u0018\u0010-\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u0013H\u0002J\u000e\u0010/\u001a\u0002002\u0006\u0010.\u001a\u00020\u0004J\u0006\u00101\u001a\u00020(J\u000e\u00102\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u0004J\u0006\u0010\u001c\u001a\u00020\u0013J\u0006\u00103\u001a\u00020\u0013J\u0010\u00104\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u0004H\u0002J\u000e\u00105\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u0004J\u000e\u00106\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u0004J\u000e\u00107\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u0004J\u001d\u00108\u001a\u00020\u000e2\b\u0010\b\u001a\u0004\u0018\u00010\u00042\u0006\u0010.\u001a\u00020\u0004\u00a2\u0006\u0002\u00109J(\u0010:\u001a\u00020\u000e2\u0006\u0010;\u001a\u00020(2\u0006\u0010\u000b\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010.\u001a\u00020\u0004H\u0002J\r\u0010\b\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\u0002\u0010<R\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0004\n\u0002\u0010\u001aR\u000e\u0010\u001b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u000e0\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000Rh\u0010\u000f\u001a\\\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0011\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u0013\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0014\u0012\u0004\u0012\u00020\u000e0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u007f\u0010\u0002\u001as\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0007\u0012\u0015\u0012\u0013\u0018\u00010\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\b\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\n\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u000e0\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010 \u001a\u00020\u001f2\u0006\u0010\u001e\u001a\u00020\u001f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u000e\u0010#\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020(X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006="}, d2 = {"Lcom/gui/tomato/engine/PomodoroEngine;", "", "onSessionInsert", "Lkotlin/Function5;", "", "Lkotlin/ParameterName;", "name", "presetId", "taskId", "startedAt", "", "type", "", "plannedMin", "", "onSessionClose", "Lkotlin/Function4;", "endedAt", "completedMin", "", "finishedByTime", "onEvent", "Lkotlin/Function1;", "Lcom/gui/tomato/engine/EngineEvent;", "(Lkotlin/jvm/functions/Function5;Lkotlin/jvm/functions/Function4;Lkotlin/jvm/functions/Function1;)V", "activeTaskId", "Ljava/lang/Long;", "cyclePos", "isPaused", "pausedAtMs", "<set-?>", "Lcom/gui/tomato/data/entities/PomodoroPreset;", "preset", "getPreset", "()Lcom/gui/tomato/data/entities/PomodoroPreset;", "running", "segPlannedMs", "segStartMs", "segType", "state", "Lcom/gui/tomato/engine/State;", "totalFocusCount", "totalPausedMs", "applyPreset", "p", "closeCurrent", "nowMs", "currentSnapshot", "Lcom/gui/tomato/engine/Snapshot;", "currentState", "finishAll", "isRunning", "next", "pause", "resume", "skipSegment", "start", "(Ljava/lang/Long;J)V", "startSegment", "st", "()Ljava/lang/Long;", "app_debug"})
public final class PomodoroEngine {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function5<java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.Integer, kotlin.Unit> onSessionInsert = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function4<java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Boolean, kotlin.Unit> onSessionClose = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.gui.tomato.engine.EngineEvent, kotlin.Unit> onEvent = null;
    @org.jetbrains.annotations.NotNull()
    private com.gui.tomato.data.entities.PomodoroPreset preset;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Long activeTaskId;
    @org.jetbrains.annotations.NotNull()
    private com.gui.tomato.engine.State state = com.gui.tomato.engine.State.IDLE;
    private long segStartMs = 0L;
    private long segPlannedMs = 0L;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String segType = "focus";
    private int cyclePos = 0;
    private int totalFocusCount = 0;
    private boolean running = false;
    private boolean isPaused = false;
    private long pausedAtMs = 0L;
    private long totalPausedMs = 0L;
    
    public PomodoroEngine(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function5<? super java.lang.Long, ? super java.lang.Long, ? super java.lang.Long, ? super java.lang.String, ? super java.lang.Integer, kotlin.Unit> onSessionInsert, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function4<? super java.lang.Long, ? super java.lang.Long, ? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> onSessionClose, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.gui.tomato.engine.EngineEvent, kotlin.Unit> onEvent) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.data.entities.PomodoroPreset getPreset() {
        return null;
    }
    
    public final void applyPreset(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.entities.PomodoroPreset p) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long taskId() {
        return null;
    }
    
    public final boolean isRunning() {
        return false;
    }
    
    public final boolean isPaused() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.State currentState() {
        return null;
    }
    
    public final void start(@org.jetbrains.annotations.Nullable()
    java.lang.Long taskId, long nowMs) {
    }
    
    public final void pause(long nowMs) {
    }
    
    public final void resume(long nowMs) {
    }
    
    public final void skipSegment(long nowMs) {
    }
    
    public final void finishAll(long nowMs) {
    }
    
    private final void startSegment(com.gui.tomato.engine.State st, java.lang.String type, int plannedMin, long nowMs) {
    }
    
    private final void closeCurrent(long nowMs, boolean finishedByTime) {
    }
    
    private final void next(long nowMs) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.Snapshot currentSnapshot(long nowMs) {
        return null;
    }
    
    public PomodoroEngine() {
        super();
    }
}