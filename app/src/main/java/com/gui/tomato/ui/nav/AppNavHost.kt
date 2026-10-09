package com.gui.tomato.ui.nav

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gui.tomato.R
import com.gui.tomato.ui.screens.*
import kotlin.math.roundToInt

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)

// ───────────── 统一时长 ─────────────
private const val DUR_ENTER = 320   // 新页面进入
private const val DUR_EXIT = 240    // 旧页面退场（比进入快，制造层次）

private fun detailEnter(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DUR_ENTER, easing = FastOutSlowInEasing),
    ) + fadeIn(tween(DUR_ENTER, easing = FastOutSlowInEasing))

private fun detailPopExit(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DUR_ENTER, easing = FastOutSlowInEasing),
    ) + fadeOut(tween(DUR_EXIT, easing = FastOutSlowInEasing))

// ---------- 底栏顺序持久化 ----------
private const val PREFS_NAME = "gui_nav_prefs"
private const val KEY_ORDER = "bottom_bar_order"
private val DEFAULT_ORDER = listOf("home", "tasks", "calendar", "stats")

private fun loadOrder(context: Context): List<String> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_ORDER, null)
    val parsed = raw?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    val known = DEFAULT_ORDER.toSet()
    val valid = parsed.filter { it in known }.distinct()
    val missing = DEFAULT_ORDER.filter { it !in valid }
    return valid + missing
}

private fun saveOrder(context: Context, order: List<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_ORDER, order.joinToString(","))
        .apply()
}

// ---------- 底栏项定义 ----------
private data class NavItem(
    val route: String,
    val icon: ImageVector,
)

@Composable
private fun labelForRoute(route: String): String = when (route) {
    "home" -> stringResource(R.string.nav_tasks)
    "tasks" -> "番茄"
    "calendar" -> "日历"
    "stats" -> stringResource(R.string.nav_stats)
    else -> route
}

@Composable
fun AppNavHost(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    start: String = "home"
) {
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "tasks", "calendar", "stats")
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val context = LocalContext.current
    val barColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = if (isDark) Color(0xFF8A8A8A) else MaterialTheme.colorScheme.outline

    val allItems = remember {
        listOf(
            NavItem("home", Icons.AutoMirrored.Filled.List),
            NavItem("tasks", Icons.Filled.Timer),
            NavItem("calendar", Icons.Filled.DateRange),
            NavItem("stats", Icons.Filled.BarChart),
        )
    }
    val order = remember {
        mutableStateListOf<String>().apply { addAll(loadOrder(context)) }
    }
    val orderedItems = order.mapNotNull { r -> allItems.firstOrNull { it.route == r } }

    // 供转场动画计算方向用：当前底栏顺序（快照）
    val orderForTransition by rememberUpdatedState(order.toList())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                DraggableNavBar(
                    items = orderedItems,
                    currentRoute = currentRoute,
                    barColor = barColor,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            if (route == "home") {
                                nav.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                nav.navigate(route) {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onReorder = { newOrder ->
                        order.clear()
                        order.addAll(newOrder)
                        saveOrder(context, newOrder)
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding),

            // ── 进入 ──
            enterTransition = {
                val from = initialState.destination.route.orEmpty()
                val to = targetState.destination.route.orEmpty()
                val fromIdx = orderForTransition.indexOf(from)
                val toIdx = orderForTransition.indexOf(to)
                if (fromIdx >= 0 && toIdx >= 0) {
                    // Tab 之间：方向感横向滑动
                    val forward = toIdx > fromIdx
                    slideInHorizontally(
                        initialOffsetX = { full -> if (forward) full else -full },
                        animationSpec = tween(DUR_ENTER, easing = FastOutSlowInEasing),
                    ) + fadeIn(tween(DUR_ENTER, easing = FastOutSlowInEasing))
                } else {
                    // 进入详情类：右侧全屏滑入
                    detailEnter()
                }
            },

            // ── 退出 ──
            exitTransition = {
                val from = initialState.destination.route.orEmpty()
                val to = targetState.destination.route.orEmpty()
                val fromIdx = orderForTransition.indexOf(from)
                val toIdx = orderForTransition.indexOf(to)
                if (fromIdx >= 0 && toIdx >= 0) {
                    // Tab 之间：旧页只滑走 1/3 + 淡出（让位感）
                    val forward = toIdx > fromIdx
                    slideOutHorizontally(
                        targetOffsetX = { full -> if (forward) -full / 3 else full / 3 },
                        animationSpec = tween(DUR_EXIT, easing = FastOutSlowInEasing),
                    ) + fadeOut(tween(DUR_EXIT, easing = FastOutSlowInEasing))
                } else {
                    fadeOut(tween(DUR_EXIT, easing = FastOutSlowInEasing))
                }
            },

            // ── 返回（从详情回到 Tab）：目标页从左侧 1/4 推回 ──
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(DUR_ENTER, easing = FastOutSlowInEasing),
                ) + fadeIn(tween(DUR_ENTER, easing = FastOutSlowInEasing))
            },
            popExitTransition = { detailPopExit() },
        ) {
            composable("home") { TasksScreen(nav) }
            composable("tasks") { HomeScreen(nav) }
            composable("calendar") { CalendarScreen(nav) }
            composable("stats") { StatsScreen(nav) }

            composable(
                "settings",
                enterTransition = { detailEnter() },
                popExitTransition = { detailPopExit() },
            ) { SettingsScreen(nav) }

            composable(
                "taskDetail/{id}",
                enterTransition = { detailEnter() },
                popExitTransition = { detailPopExit() },
            ) { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskDetailScreen(nav, id)
            }

            composable(
                "taskEdit/{id}",
                enterTransition = { detailEnter() },
                popExitTransition = { detailPopExit() },
            ) { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskEditScreen(nav, id)
            }

            composable(
                "presetEdit",
                enterTransition = { detailEnter() },
                popExitTransition = { detailPopExit() },
            ) { PresetEditScreen(nav) }
        }
    }
}

// ---------- 可拖拽底栏（保持不变） ----------
@Composable
private fun DraggableNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    barColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    onNavigate: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    var draggingRoute by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val latestItems by rememberUpdatedState(items)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(barColor)
            .navigationBarsPadding()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val isDragging = draggingRoute == item.route

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        if (isDragging) {
                            translationX = dragOffsetX
                            scaleX = 1.08f
                            scaleY = 1.08f
                        }
                    }
                    .pointerInput(item.route) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingRoute = item.route
                                dragOffsetX = 0f
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                val list = latestItems
                                val from = list.indexOfFirst { it.route == item.route }
                                if (from >= 0) {
                                    dragOffsetX += amount.x
                                    val w = size.width.toFloat()
                                    if (w > 0f) {
                                        val shift = (dragOffsetX / w).roundToInt()
                                        val to = (from + shift).coerceIn(0, list.lastIndex)
                                        if (to != from) {
                                            val newOrder = list.map { it.route }.toMutableList()
                                            newOrder.add(to, newOrder.removeAt(from))
                                            onReorder(newOrder)
                                            dragOffsetX -= shift * w
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                draggingRoute = null
                                dragOffsetX = 0f
                            },
                            onDragCancel = {
                                draggingRoute = null
                                dragOffsetX = 0f
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember(item.route) { MutableInteractionSource() },
                        indication = null,
                        onClick = { onNavigate(item.route) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) selectedColor.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = labelForRoute(item.route),
                            tint = if (selected) selectedColor else unselectedColor
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = labelForRoute(item.route),
                        color = if (selected) selectedColor else unselectedColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}