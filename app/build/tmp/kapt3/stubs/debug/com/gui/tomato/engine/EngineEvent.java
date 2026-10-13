package com.gui.tomato.engine;

/**
 * 引擎事件，用于让外层做振动/声音/通知等副作用
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/gui/tomato/engine/EngineEvent;", "", "(Ljava/lang/String;I)V", "Started", "Paused", "Resumed", "Skipped", "Finished", "PhaseCompleted", "app_debug"})
public enum EngineEvent {
    /*public static final*/ Started /* = new Started() */,
    /*public static final*/ Paused /* = new Paused() */,
    /*public static final*/ Resumed /* = new Resumed() */,
    /*public static final*/ Skipped /* = new Skipped() */,
    /*public static final*/ Finished /* = new Finished() */,
    /*public static final*/ PhaseCompleted /* = new PhaseCompleted() */;
    
    EngineEvent() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.gui.tomato.engine.EngineEvent> getEntries() {
        return null;
    }
}