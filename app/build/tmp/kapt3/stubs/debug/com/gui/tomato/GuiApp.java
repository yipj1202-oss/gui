package com.gui.tomato;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fJ\u0006\u0010 \u001a\u00020!J\u001c\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#2\u0006\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\fJ\u001c\u0010\'\u001a\b\u0012\u0004\u0012\u00020(0#2\u0006\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\fJ\b\u0010)\u001a\u00020\fH\u0002J\u0015\u0010*\u001a\u00020!2\b\u0010+\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\u0011J\b\u0010,\u001a\u00020!H\u0016J\u0006\u0010-\u001a\u00020!J\u0006\u0010.\u001a\u00020!J\u0006\u0010/\u001a\u000200J\u0006\u00101\u001a\u000200J\u0006\u00102\u001a\u000200J\u0006\u00103\u001a\u000200R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR/\u0010\r\u001a\u0004\u0018\u00010\f2\b\u0010\u000b\u001a\u0004\u0018\u00010\f8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\u0014@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u001e\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u000b\u001a\u00020\u0018@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001b\u00a8\u00064"}, d2 = {"Lcom/gui/tomato/GuiApp;", "Landroid/app/Application;", "()V", "backupStore", "Lcom/gui/tomato/data/backup/BackupStore;", "db", "Lcom/gui/tomato/data/db/AppDatabase;", "getDb", "()Lcom/gui/tomato/data/db/AppDatabase;", "setDb", "(Lcom/gui/tomato/data/db/AppDatabase;)V", "<set-?>", "", "pendingStartTaskId", "getPendingStartTaskId", "()Ljava/lang/Long;", "setPendingStartTaskId", "(Ljava/lang/Long;)V", "pendingStartTaskId$delegate", "Landroidx/compose/runtime/MutableState;", "Lcom/gui/tomato/ui/prefs/AppPrefs;", "prefs", "getPrefs", "()Lcom/gui/tomato/ui/prefs/AppPrefs;", "Lcom/gui/tomato/service/PomodoroRepository;", "repo", "getRepo", "()Lcom/gui/tomato/service/PomodoroRepository;", "backupImport", "", "uri", "Landroid/net/Uri;", "backupToPublic", "", "byHourSafe", "", "Lcom/gui/tomato/data/HourStatRow;", "from", "to", "byTaskSafe", "Lcom/gui/tomato/data/TaskStatRow;", "dayStartMs", "deleteTaskStats", "taskId", "onCreate", "serviceStart", "serviceStop", "todayFocusCount", "", "todayFocusCountSafe", "todayFocusMinutes", "todayFocusMinutesSafe", "app_debug"})
public final class GuiApp extends android.app.Application {
    public com.gui.tomato.data.db.AppDatabase db;
    private com.gui.tomato.service.PomodoroRepository repo;
    private com.gui.tomato.ui.prefs.AppPrefs prefs;
    @org.jetbrains.annotations.Nullable()
    private com.gui.tomato.data.backup.BackupStore backupStore;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState pendingStartTaskId$delegate = null;
    
    public GuiApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.data.db.AppDatabase getDb() {
        return null;
    }
    
    public final void setDb(@org.jetbrains.annotations.NotNull()
    com.gui.tomato.data.db.AppDatabase p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.service.PomodoroRepository getRepo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gui.tomato.ui.prefs.AppPrefs getPrefs() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getPendingStartTaskId() {
        return null;
    }
    
    public final void setPendingStartTaskId(@org.jetbrains.annotations.Nullable()
    java.lang.Long p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    public final void serviceStart() {
    }
    
    public final void serviceStop() {
    }
    
    public final int todayFocusCount() {
        return 0;
    }
    
    public final int todayFocusMinutes() {
        return 0;
    }
    
    private final long dayStartMs() {
        return 0L;
    }
    
    public final int todayFocusCountSafe() {
        return 0;
    }
    
    public final int todayFocusMinutesSafe() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.gui.tomato.data.TaskStatRow> byTaskSafe(long from, long to) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.gui.tomato.data.HourStatRow> byHourSafe(long from, long to) {
        return null;
    }
    
    /**
     * 删除某任务的所有专注 session；taskId 传 null 表示清理"未指定"
     */
    public final void deleteTaskStats(@org.jetbrains.annotations.Nullable()
    java.lang.Long taskId) {
    }
    
    public final void backupToPublic() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String backupImport(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
}