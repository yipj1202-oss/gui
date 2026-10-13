package com.gui.tomato.util;

/**
 * 番茄钟到点提示音 —— AudioTrack 合成，不用系统铃声。
 * 0 → 暮鼓（沉钟，单声长音）
 * 1 → 松风（两声柔拨弦，第二声后快速淡出）
 * 2 → 涧泉（三声柔拨弦，间隔大）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0017\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u001eB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\r\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u00020\u000fH\u0002J\b\u0010\u0011\u001a\u00020\u000fH\u0002J\b\u0010\u0012\u001a\u00020\u000fH\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002J\u0010\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u001e\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u00042\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00170\u001bH\u0002J\u000e\u0010\u001c\u001a\u00020\u00142\u0006\u0010\u001d\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/gui/tomato/util/SoundPlayer;", "", "()V", "SR", "", "initialized", "", "main", "Landroid/os/Handler;", "t0", "Landroid/media/AudioTrack;", "t1", "t2", "build", "samples", "", "buildDrum", "buildPineWind", "buildStream", "ensureInit", "", "karplus", "p", "Lcom/gui/tomato/util/SoundPlayer$Pluck;", "mixSamples", "totalMs", "parts", "", "play", "idx", "Pluck", "app_debug"})
public final class SoundPlayer {
    @org.jetbrains.annotations.NotNull()
    private static final android.os.Handler main = null;
    private static final int SR = 44100;
    @org.jetbrains.annotations.Nullable()
    private static android.media.AudioTrack t0;
    @org.jetbrains.annotations.Nullable()
    private static android.media.AudioTrack t1;
    @org.jetbrains.annotations.Nullable()
    private static android.media.AudioTrack t2;
    private static boolean initialized = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.gui.tomato.util.SoundPlayer INSTANCE = null;
    
    private SoundPlayer() {
        super();
    }
    
    public final void play(int idx) {
    }
    
    private final void ensureInit() {
    }
    
    private final android.media.AudioTrack build(short[] samples) {
        return null;
    }
    
    private final short[] buildDrum() {
        return null;
    }
    
    private final short[] buildPineWind() {
        return null;
    }
    
    private final short[] buildStream() {
        return null;
    }
    
    private final short[] mixSamples(int totalMs, java.util.List<com.gui.tomato.util.SoundPlayer.Pluck> parts) {
        return null;
    }
    
    private final short[] karplus(com.gui.tomato.util.SoundPlayer.Pluck p) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003JE\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001e\u001a\u00020\u001fH\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\fR\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000f\u00a8\u0006 "}, d2 = {"Lcom/gui/tomato/util/SoundPlayer$Pluck;", "", "startMs", "", "freq", "", "durMs", "damping", "amp", "pluckPos", "(IDIDDD)V", "getAmp", "()D", "getDamping", "getDurMs", "()I", "getFreq", "getPluckPos", "getStartMs", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    static final class Pluck {
        private final int startMs = 0;
        private final double freq = 0.0;
        private final int durMs = 0;
        private final double damping = 0.0;
        private final double amp = 0.0;
        private final double pluckPos = 0.0;
        
        public Pluck(int startMs, double freq, int durMs, double damping, double amp, double pluckPos) {
            super();
        }
        
        public final int getStartMs() {
            return 0;
        }
        
        public final double getFreq() {
            return 0.0;
        }
        
        public final int getDurMs() {
            return 0;
        }
        
        public final double getDamping() {
            return 0.0;
        }
        
        public final double getAmp() {
            return 0.0;
        }
        
        public final double getPluckPos() {
            return 0.0;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final double component2() {
            return 0.0;
        }
        
        public final int component3() {
            return 0;
        }
        
        public final double component4() {
            return 0.0;
        }
        
        public final double component5() {
            return 0.0;
        }
        
        public final double component6() {
            return 0.0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.gui.tomato.util.SoundPlayer.Pluck copy(int startMs, double freq, int durMs, double damping, double amp, double pluckPos) {
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
}