package com.gui.tomato.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.Task
import com.gui.tomato.ui.components.StaggerIn
import kotlinx.coroutines.launch
import java.util.Calendar

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkCell = Color(0xFF161616)
private val DarkCellEmpty = Color(0xFF0F0F0F)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)

private val WeekLabelRes = listOf(
    R.string.calendar_weekday_1,
    R.string.calendar_weekday_2,
    R.string.calendar_weekday_3,
    R.string.calendar_weekday_4,
    R.string.calendar_weekday_5,
    R.string.calendar_weekday_6,
    R.string.calendar_weekday_7,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val scope = rememberCoroutineScope()

    var activeTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var doneTasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(Unit) {
        app.db.taskDao().activeFlow().collect { activeTasks = it }
    }
    LaunchedEffect(Unit) {
        app.db.taskDao().doneFlow().collect { doneTasks = it }
    }

    val allTasks = remember(activeTasks, doneTasks) { activeTasks + doneTasks }

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // 左右滑动切月：位移 + 手势
    val swipeOffset = remember { Animatable(0f) }

    fun goPrevMonth() {
        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        selectedDay = -1
    }
    fun goNextMonth() {
        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        selectedDay = -1
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val cellColor = if (isDark) DarkCell else Color(0xFFF7F7F7)
    val cellEmptyColor = if (isDark) DarkCellEmpty else Color(0xFFFCFCFC)
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val year = currentMonth.get(Calendar.YEAR)
    val month = currentMonth.get(Calendar.MONTH)

    val tasksByDay: Map<Int, List<Task>> = remember(allTasks, year, month) {
        allTasks.groupBy { task ->
            val c = Calendar.getInstance().apply { timeInMillis = task.createdAt }
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                c.get(Calendar.DAY_OF_MONTH)
            } else -1
        }.filterKeys { it > 0 }
    }

    val todayCal = Calendar.getInstance()
    val isCurrentMonth = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == month
    val todayDay = if (isCurrentMonth) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    val firstDayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
    val offset = (firstDayOfWeek + 5) % 7
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeks = ((offset + daysInMonth) + 6) / 7

    val cells: List<Int?> = (0 until weeks * 7).map { i ->
        val dayNum = i - offset + 1
        if (dayNum in 1..daysInMonth) dayNum else null
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        Text(
                            text = stringResource(R.string.calendar_title_format, year, month + 1),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Thin,
                            letterSpacing = 4.sp,
                            color = textMain,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showYearPicker = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    StaggerIn(index = 1, startOffsetY = 14.dp) {
                        Row {
                            IconButton(onClick = { goPrevMonth() }) {
                                Icon(
                                    Icons.Filled.ChevronLeft,
                                    contentDescription = stringResource(R.string.calendar_prev_month),
                                    tint = textSub
                                )
                            }
                            IconButton(onClick = { goNextMonth() }) {
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = stringResource(R.string.calendar_next_month),
                                    tint = textSub
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            StaggerIn(index = 9, startOffsetY = 20.dp, startScale = 0.85f) {
                FloatingActionButton(
                    onClick = {
                        if (selectedDay <= 0) selectedDay = todayDay.coerceAtLeast(1)
                        showAddDialog = true
                    },
                    containerColor = OrangeStart,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.task_new_desc)
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // 星期表头（固定，不跟着滑）
            StaggerIn(index = 2, startOffsetY = 10.dp) {
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    WeekLabelRes.forEach { resId ->
                        Text(
                            text = stringResource(resId),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                            color = textSub
                        )
                    }
                }
            }

            // 月历网格：可左右滑动切月
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }
                val threshold = widthPx * 0.22f

                val weeks2 = cells.chunked(7)

                Column(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationX = swipeOffset.value }
                        .pointerInput(year, month) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        val nv = (swipeOffset.value + dragAmount)
                                            .coerceIn(-widthPx, widthPx)
                                        swipeOffset.snapTo(nv)
                                    }
                                },
                                onDragEnd = {
                                    val v = swipeOffset.value
                                    when {
                                        v > threshold -> {
                                            scope.launch {
                                                swipeOffset.animateTo(
                                                    widthPx,
                                                    tween(160, easing = FastOutSlowInEasing)
                                                )
                                                goPrevMonth()
                                                swipeOffset.snapTo(-widthPx)
                                                swipeOffset.animateTo(
                                                    0f,
                                                    tween(220, easing = FastOutSlowInEasing)
                                                )
                                            }
                                        }
                                        v < -threshold -> {
                                            scope.launch {
                                                swipeOffset.animateTo(
                                                    -widthPx,
                                                    tween(160, easing = FastOutSlowInEasing)
                                                )
                                                goNextMonth()
                                                swipeOffset.snapTo(widthPx)
                                                swipeOffset.animateTo(
                                                    0f,
                                                    tween(220, easing = FastOutSlowInEasing)
                                                )
                                            }
                                        }
                                        else -> {
                                            scope.launch {
                                                swipeOffset.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = 0.72f,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        swipeOffset.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = 0.72f,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            )
                        }
                ) {
                    weeks2.forEachIndexed { weekIdx, week ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            week.forEachIndexed { colIdx, day ->
                                val globalIdx = weekIdx * 7 + colIdx
                                Box(Modifier.weight(1f).fillMaxHeight()) {
                                    StaggerIn(
                                        index = 3 + globalIdx,
                                        modifier = Modifier.fillMaxSize(),
                                        baseDelayMs = 40,
                                        durationMs = 420,
                                        startOffsetX = 24.dp,
                                        startOffsetY = 12.dp,
                                    ) {
                                        if (day != null) {
                                            DayCell(
                                                day = day,
                                                tasks = tasksByDay[day] ?: emptyList(),
                                                isToday = day == todayDay,
                                                isSelected = day == selectedDay,
                                                cellColor = cellColor,
                                                textMain = textMain,
                                                textSub = textSub,
                                                onDayClick = { selectedDay = day },
                                                onTaskClick = { t -> nav.navigate("taskDetail/" + t.id) },
                                                onTaskLongPress = { t -> taskToDelete = t }
                                            )
                                        } else {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(cellEmptyColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 年份选择对话框
    if (showYearPicker) {
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = remember { (thisYear - 10..thisYear + 10).toList() }
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = (10 - 3).coerceAtLeast(0)
        )
        AlertDialog(
            onDismissRequest = { showYearPicker = false },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "选择年份",
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp,
                    fontSize = 18.sp,
                    color = textMain
                )
            },
            text = {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(years) { y ->
                        val selected = y == year
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) OrangeStart.copy(alpha = 0.14f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    currentMonth = (currentMonth.clone() as Calendar).apply {
                                        set(Calendar.YEAR, y)
                                    }
                                    selectedDay = -1
                                    showYearPicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = y.toString(),
                                fontSize = 18.sp,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Thin,
                                letterSpacing = 2.sp,
                                color = if (selected) OrangeStart else textMain
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 新建任务对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    stringResource(R.string.calendar_new_task_title, month + 1, selectedDay),
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text(stringResource(R.string.task_field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = newTitle.trim()
                        if (title.isNotEmpty()) {
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val capturedTitle = title
                            val capturedTime = cal.timeInMillis
                            scope.launch {
                                app.db.taskDao().insert(
                                    Task(
                                        title = capturedTitle,
                                        note = "",
                                        createdAt = capturedTime,
                                        completedAt = null,
                                        completed = false
                                    )
                                )
                            }
                            newTitle = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_create), color = OrangeStart)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newTitle = ""
                    showAddDialog = false
                }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 删除确认
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    stringResource(R.string.calendar_delete_title),
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            },
            text = { Text(stringResource(R.string.calendar_delete_message, task.title)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { app.db.taskDao().delete(task.id) }
                    taskToDelete = null
                }) {
                    Text(stringResource(R.string.task_delete_desc), color = OrangeStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    day: Int,
    tasks: List<Task>,
    isToday: Boolean,
    isSelected: Boolean,
    cellColor: Color,
    textMain: Color,
    textSub: Color,
    onDayClick: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskLongPress: (Task) -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val bgColor = when {
        isSelected -> OrangeStart.copy(alpha = 0.12f)
        else -> cellColor
    }
    val borderColor = when {
        isSelected -> OrangeStart
        isToday -> OrangeStart.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val numberColor = when {
        isSelected -> OrangeStart
        isToday -> OrangeStart
        else -> textMain
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable { onDayClick() }
            .padding(horizontal = 3.dp, vertical = 3.dp)
    ) {
        Text(
            text = day.toString(),
            fontSize = 12.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal,
            color = numberColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))

        tasks.take(3).forEach { task ->
            Text(
                text = task.title,
                fontSize = 8.sp,
                fontWeight = FontWeight.Light,
                color = if (task.completed) textSub.copy(alpha = 0.5f) else OrangeStart,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OrangeStart.copy(alpha = if (task.completed) 0.06f else 0.14f))
                    .combinedClickable(
                        onClick = { onTaskClick(task) },
                        onLongClick = { onTaskLongPress(task) }
                    )
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 1500,
                        repeatDelayMillis = 900,
                        velocity = 15.dp
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            )
        }
        if (tasks.size > 3) {
            Text(
                text = "+${tasks.size - 3}",
                fontSize = 7.sp,
                color = OrangeStart,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}