package com.gui.tomato.util;

/**
 * 振动工具。所有振动统一走这里，方便以后调时长/强度。
 * 调用前请自行判断 prefs.vibrate 开关。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0012\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0018\u0010\f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\r\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a8\u0006\u000e"}, d2 = {"Lcom/gui/tomato/util/Haptic;", "", "()V", "buzz", "", "context", "Landroid/content/Context;", "durationMs", "", "doubleTap", "getVibrator", "Landroid/os/Vibrator;", "strongTap", "tap", "app_debug"})
public final class Haptic {
    @org.jetbrains.annotations.NotNull()
    public static final com.gui.tomato.util.Haptic INSTANCE = null;
    
    private Haptic() {
        super();
    }
    
    /**
     * 短震一次，默认 25ms
     */
    public final void tap(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long durationMs) {
    }
    
    /**
     * 长震，用于「阶段结束」之类的强提醒，默认 400ms
     */
    public final void buzz(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long durationMs) {
    }
    
    /**
     * 单次强震，短促有力（一次过，非连续）
     */
    public final void strongTap(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long durationMs) {
    }
    
    /**
     * 双击振动：短-停-短，用于「该休息了 / 该专注了」
     */
    public final void doubleTap(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final android.os.Vibrator getVibrator(android.content.Context context) {
        return null;
    }
}