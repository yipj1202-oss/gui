package com.gui.tomato.ui.nav

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.ui.components.LocalNavDirection
import com.gui.tomato.ui.components.NavDirection
import com.gui.tomato.ui.screens.*
import com.gui.tomato.util.Haptic
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)

private const val DUR_ENTER = 320
private const val DUR_EXIT = 240

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

private data class NavItem(
    val route: String,
    val icon: ImageVector,
)

@Composable
private fun labelForRoute(route: String): String = when (route) {
    "home" -> stringResource(R.string.nav_tasks)
    "tasks" -> stringResource(R.string.nav_pomodoro)
    "calendar" -> stringResource(R.string.nav_calendar)
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

    val hapticsEnabled = (context.applicationContext as GuiApp).prefs.vibrate

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

    val orderForTransition by rememberUpdatedState(order.toList())
    var navDirection by remember { mutableStateOf(NavDirection.None) }

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
                    hapticsEnabled = hapticsEnabled,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            if (hapticsEnabled) Haptic.tap(context, 22)

                            val fromIdx = order.indexOf(currentRoute.orEmpty())
                            val toIdx = order.indexOf(route)
                            navDirection = when {
                                fromIdx >= 0 && toIdx >= 0 && toIdx > fromIdx -> NavDirection.Forward
                                fromIdx >= 0 && toIdx >= 0 && toIdx < fromIdx -> NavDirection.Backward
                                else -> NavDirection.None
                            }
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
        CompositionLocalProvider(LocalNavDirection provides navDirection) {
            NavHost(
                navController = nav,
                startDestination = start,
                modifier = Modifier.padding(padding),

                enterTransition = {
                    val from = initialState.destination.route.orEmpty()
                    val to = targetState.destination.route.orEmpty()
                    val fromIdx = orderForTransition.indexOf(from)
                    val toIdx = orderForTransition.indexOf(to)
                    if (fromIdx >= 0 && toIdx >= 0) {
                        val forward = toIdx > fromIdx
                        slideInHorizontally(
                            initialOffsetX = { full -> if (forward) full else -full },
                            animationSpec = tween(DUR_ENTER, easing = FastOutSlowInEasing),
                        ) + fadeIn(tween(DUR_ENTER, easing = FastOutSlowInEasing))
                    } else {
                        detailEnter()
                    }
                },
                exitTransition = {
                    val from = initialState.destination.route.orEmpty()
                    val to = targetState.destination.route.orEmpty()
                    val fromIdx = orderForTransition.indexOf(from)
                    val toIdx = orderForTransition.indexOf(to)
                    if (fromIdx >= 0 && toIdx >= 0) {
                        val forward = toIdx > fromIdx
                        slideOutHorizontally(
                            targetOffsetX = { full -> if (forward) -full / 3 else full / 3 },
                            animationSpec = tween(DUR_EXIT, easing = FastOutSlowInEasing),
                        ) + fadeOut(tween(DUR_EXIT, easing = FastOutSlowInEasing))
                    } else {
                        fadeOut(tween(DUR_EXIT, easing = FastOutSlowInEasing))
                    }
                },
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
}

// ---------- 底栏：泡泡胶囊 + 挤压/爆裂/融合 + 拖拽让位 + 振动 ----------
@Composable
private fun DraggableNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    barColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    hapticsEnabled: Boolean,
    onNavigate: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current

    var barWidthPx by remember { mutableFloatStateOf(0f) }

    var draggingRoute by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragFromIndex by remember { mutableIntStateOf(-1) }
    var dragToIndex by remember { mutableIntStateOf(-1) }

    val slotW = if (items.isEmpty() || barWidthPx == 0f) 0f else barWidthPx / items.size
    val latestItems by rememberUpdatedState(items)

    val selectedOriginalIdx = items.indexOfFirst { it.route == currentRoute }
    val selectedVisualIdx: Int = when {
        selectedOriginalIdx < 0 -> 0
        draggingRoute == null -> selectedOriginalIdx
        else -> {
            val from = dragFromIndex
            val to = dragToIndex
            when {
                selectedOriginalIdx == from -> to
                from < to && selectedOriginalIdx in (from + 1)..to -> selectedOriginalIdx - 1
                from > to && selectedOriginalIdx in to until from -> selectedOriginalIdx + 1
                else -> selectedOriginalIdx
            }
        }
    }

    val pillWidthBase = slotW * 0.62f
    val pillTargetX = selectedVisualIdx * slotW + (slotW - pillWidthBase) / 2f
    val pillHPx = with(density) { 32.dp.toPx() }
    val pillTopPx = with(density) { 16.dp.toPx() }

    val pillX by animateFloatAsState(
        targetValue = pillTargetX,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pillX",
    )

    val pillWBase by animateFloatAsState(
        targetValue = pillWidthBase,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "pillWBase",
    )

    val squashX = remember { Animatable(1f) }
    val squashY = remember { Animatable(1f) }

    LaunchedEffect(selectedVisualIdx, draggingRoute) {
        if (draggingRoute != null) {
            squashX.snapTo(1f)
            squashY.snapTo(1f)
            return@LaunchedEffect
        }
        squashX.snapTo(1f)
        squashY.snapTo(1f)

        squashX.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 560
                1.00f at 0
                1.22f at 150
                0.86f at 330
                1.05f at 460
                1.00f at 560
            }
        )
    }
    LaunchedEffect(squashX.value) {
        val x = squashX.value
        val targetY = (2f - x).coerceIn(0.85f, 1.15f)
        squashY.animateTo(
            targetValue = targetY,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = Spring.StiffnessMediumLow,
            )
        )
    }

    val finalW = pillWBase * squashX.value
    val finalH = pillHPx * squashY.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(barColor)
            .navigationBarsPadding()
            .height(80.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat() }
    ) {
        if (slotW > 0f) {
            val centerX = pillX + pillWBase / 2f
            val pillLeft = centerX - finalW / 2f
            val pillTop = pillTopPx + (pillHPx - finalH) / 2f

            Box(
                modifier = Modifier
                    .offset { IntOffset(pillLeft.roundToInt(), pillTop.roundToInt()) }
                    .width(with(density) { finalW.toDp() })
                    .height(with(density) { finalH.toDp() })
                    .clip(RoundedCornerShape(50))
                    .background(selectedColor.copy(alpha = 0.16f))
            )

            items.forEachIndexed { index, item ->
                val selected = currentRoute == item.route
                val isDragging = draggingRoute == item.route

                val targetIdx = when {
                    isDragging -> dragToIndex.coerceAtLeast(0)
                    draggingRoute == null -> index
                    else -> {
                        val from = dragFromIndex
                        val to = dragToIndex
                        when {
                            index == from -> to
                            from < to && index in (from + 1)..to -> index - 1
                            from > to && index in to until from -> index + 1
                            else -> index
                        }
                    }
                }

                val effectiveTarget = if (isDragging) {
                    dragFromIndex * slotW + dragOffsetX
                } else {
                    targetIdx * slotW
                }

                val offsetX by animateFloatAsState(
                    targetValue = effectiveTarget,
                    animationSpec = if (isDragging) {
                        tween(0)
                    } else {
                        spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessMediumLow,
                        )
                    },
                    label = "navOffset_${item.route}",
                )

                val liftScale by animateFloatAsState(
                    targetValue = if (isDragging) 1.14f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "navLift_${item.route}",
                )

                Box(
                    modifier = Modifier
                        .width(with(density) { slotW.toDp() })
                        .fillMaxHeight()
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .zIndex(if (isDragging) 3f else 1f)
                        .graphicsLayer {
                            scaleX = liftScale
                            scaleY = liftScale
                        }
                        .pointerInput(item.route) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    val from = latestItems.indexOfFirst { it.route == item.route }
                                    draggingRoute = item.route
                                    dragOffsetX = 0f
                                    dragFromIndex = from
                                    dragToIndex = from
                                    if (hapticsEnabled) Haptic.tap(ctx, 40)
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragOffsetX += amount.x
                                    if (slotW > 0f) {
                                        val shift = (dragOffsetX / slotW).roundToInt()
                                        val newTo = (dragFromIndex + shift)
                                            .coerceIn(0, latestItems.lastIndex)
                                        if (newTo != dragToIndex) {
                                            dragToIndex = newTo
                                            if (hapticsEnabled) Haptic.tap(ctx, 25)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (dragFromIndex >= 0 && dragToIndex >= 0 &&
                                        dragFromIndex != dragToIndex
                                    ) {
                                        val newOrder = latestItems.map { it.route }.toMutableList()
                                        newOrder.add(dragToIndex, newOrder.removeAt(dragFromIndex))
                                        onReorder(newOrder)
                                    }
                                    if (hapticsEnabled) Haptic.tap(ctx, 30)
                                    draggingRoute = null
                                    dragOffsetX = 0f
                                    dragFromIndex = -1
                                    dragToIndex = -1
                                },
                                onDragCancel = {
                                    draggingRoute = null
                                    dragOffsetX = 0f
                                    dragFromIndex = -1
                                    dragToIndex = -1
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
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = labelForRoute(item.route),
                                tint = if (isDragging || selected) selectedColor else unselectedColor
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
}