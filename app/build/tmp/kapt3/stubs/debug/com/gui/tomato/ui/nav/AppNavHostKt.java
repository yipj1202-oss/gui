package com.gui.tomato.ui.nav;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\\\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a$\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0002H\u0007\u001ax\u0010\u0013\u001a\u00020\r2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00012\b\u0010\u0016\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u0019\u001a\u00020\u00072\u0006\u0010\u001a\u001a\u00020\u001b2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\r0\u001d2\u0018\u0010\u001e\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00020\u0001\u0012\u0004\u0012\u00020\r0\u001dH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001f\u0010 \u001a\b\u0010!\u001a\u00020\"H\u0002\u001a\b\u0010#\u001a\u00020$H\u0002\u001a\u0010\u0010%\u001a\u00020\u00022\u0006\u0010&\u001a\u00020\u0002H\u0003\u001a\u0016\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010(\u001a\u00020)H\u0002\u001a\u001e\u0010*\u001a\u00020\r2\u0006\u0010(\u001a\u00020)2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001H\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\"\u0010\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\b\"\u0010\u0010\t\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\b\"\u000e\u0010\n\u001a\u00020\u0002X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u000b\u001a\u00020\u0002X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006,"}, d2 = {"DEFAULT_ORDER", "", "", "DUR_ENTER", "", "DUR_EXIT", "DarkBg", "Landroidx/compose/ui/graphics/Color;", "J", "DarkCard", "KEY_ORDER", "PREFS_NAME", "AppNavHost", "", "nav", "Landroidx/navigation/NavHostController;", "modifier", "Landroidx/compose/ui/Modifier;", "start", "DraggableNavBar", "items", "Lcom/gui/tomato/ui/nav/NavItem;", "currentRoute", "barColor", "selectedColor", "unselectedColor", "hapticsEnabled", "", "onNavigate", "Lkotlin/Function1;", "onReorder", "DraggableNavBar-aDBTMWw", "(Ljava/util/List;Ljava/lang/String;JJJZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "detailEnter", "Landroidx/compose/animation/EnterTransition;", "detailPopExit", "Landroidx/compose/animation/ExitTransition;", "labelForRoute", "route", "loadOrder", "context", "Landroid/content/Context;", "saveOrder", "order", "app_debug"})
public final class AppNavHostKt {
    private static final long DarkBg = 0L;
    private static final long DarkCard = 0L;
    private static final int DUR_ENTER = 320;
    private static final int DUR_EXIT = 240;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "gui_nav_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ORDER = "bottom_bar_order";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> DEFAULT_ORDER = null;
    
    private static final androidx.compose.animation.EnterTransition detailEnter() {
        return null;
    }
    
    private static final androidx.compose.animation.ExitTransition detailPopExit() {
        return null;
    }
    
    private static final java.util.List<java.lang.String> loadOrder(android.content.Context context) {
        return null;
    }
    
    private static final void saveOrder(android.content.Context context, java.util.List<java.lang.String> order) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final java.lang.String labelForRoute(java.lang.String route) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AppNavHost(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavHostController nav, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String start) {
    }
}