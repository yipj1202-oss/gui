package com.gui.tomato.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\"\b\u0086\b\u0018\u00002\u00020\u0001BY\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\u0006\u0010\u000b\u001a\u00020\u0005\u0012\u0006\u0010\f\u001a\u00020\t\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\u0002\u0010\u0011J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0010H\u00c6\u0003J\t\u0010$\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010%\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001eJ\t\u0010&\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\tH\u00c6\u0003J\t\u0010(\u001a\u00020\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\u0005H\u00c6\u0003J\t\u0010*\u001a\u00020\tH\u00c6\u0003J\t\u0010+\u001a\u00020\u000eH\u00c6\u0003Jt\u0010,\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u00052\b\b\u0002\u0010\f\u001a\u00020\t2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u00c6\u0001\u00a2\u0006\u0002\u0010-J\u0013\u0010.\u001a\u00020\u00102\b\u0010/\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00100\u001a\u00020\tH\u00d6\u0001J\t\u00101\u001a\u00020\u000eH\u00d6\u0001R\u0011\u0010\f\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0014R\u0011\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0016R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0016R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!\u00a8\u00062"}, d2 = {"Lcom/gui/tomato/engine/Snapshot;", "", "state", "Lcom/gui/tomato/engine/State;", "remainMs", "", "taskId", "presetId", "segmentIndex", "", "startedAt", "plannedMs", "cyclePos", "type", "", "isPaused", "", "(Lcom/gui/tomato/engine/State;JLjava/lang/Long;JIJJILjava/lang/String;Z)V", "getCyclePos", "()I", "()Z", "getPlannedMs", "()J", "getPresetId", "getRemainMs", "getSegmentIndex", "getStartedAt", "getState", "()Lcom/gui/tomato/engine/State;", "getTaskId", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getType", "()Ljava/lang/String;", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Lcom/gui/tomato/engine/State;JLjava/lang/Long;JIJJILjava/lang/String;Z)Lcom/gui/tomato/engine/Snapshot;", "equals", "other", "hashCode", "toString", "app_debug"})
public final class Snapshot {
    @org.jetbrains.annotations.NotNull()
    private final com.gui.tomato.engine.State state = null;
    private final long remainMs = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long taskId = null;
    private final long presetId = 0L;
    private final int segmentIndex = 0;
    private final long startedAt = 0L;
    private final long plannedMs = 0L;
    private final int cyclePos = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String type = null;
    private final boolean isPaused = false;
    
    public Snapshot(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.engine.State state, long remainMs, @org.jetbrains.annotations.Nullable()
    java.lang.Long taskId, long presetId, int segmentIndex, long startedAt, long plannedMs, int cyclePos, @org.jetbrains.annotations.NotNull()
    java.lang.String type, boolean isPaused) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.State getState() {
        return null;
    }
    
    public final long getRemainMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getTaskId() {
        return null;
    }
    
    public final long getPresetId() {
        return 0L;
    }
    
    public final int getSegmentIndex() {
        return 0;
    }
    
    public final long getStartedAt() {
        return 0L;
    }
    
    public final long getPlannedMs() {
        return 0L;
    }
    
    public final int getCyclePos() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getType() {
        return null;
    }
    
    public final boolean isPaused() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.State component1() {
        return null;
    }
    
    public final boolean component10() {
        return false;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component3() {
        return null;
    }
    
    public final long component4() {
        return 0L;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final long component6() {
        return 0L;
    }
    
    public final long component7() {
        return 0L;
    }
    
    public final int component8() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.engine.Snapshot copy(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.engine.State state, long remainMs, @org.jetbrains.annotations.Nullable()
    java.lang.Long taskId, long presetId, int segmentIndex, long startedAt, long plannedMs, int cyclePos, @org.jetbrains.annotations.NotNull()
    java.lang.String type, boolean isPaused) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}