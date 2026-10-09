package com.gui.tomato.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.Task
import com.gui.tomato.ui.components.StaggerIn
import kotlinx.coroutines.delay

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

// 乱码池：符号 + 少量片假名
private val ScramblePool = "!@#$%&*+-=?<>[]{}|/\\~^;:アイウエオカキ".toList()

// 每个字占的额外宽度
private val CharWidthDp = 22.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var activeList by remember { mutableStateOf<List<Task>>(emptyList()) }
    var doneList by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(Unit) {
        app.db.taskDao().activeFlow().collect { activeList = it }
    }
    LaunchedEffect(Unit) {
        app.db.taskDao().doneFlow().collect { doneList = it }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val cardColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        Text(
                            stringResource(R.string.nav_tasks),
                            color = textMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    StaggerIn(index = 1, startOffsetY = 14.dp) {
                        IconButton(onClick = { nav.navigate("settings") }) {
                            Text(
                                text = "晷",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = 1.sp,
                                style = TextStyle(
                                    brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                                )
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            StaggerIn(index = 9, startOffsetY = 20.dp, startScale = 0.85f) {
                ExpandableAddFab(
                    onClick = { nav.navigate("taskEdit/0") },
                    fullText = "新建任务",
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (activeList.isEmpty() && doneList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StaggerIn(index = 2) {
                        Text(
                            "暂无任务，点右下角 + 添加",
                            color = textSub,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeList.isNotEmpty()) {
                        item {
                            StaggerIn(index = 2) {
                                Text(
                                    "进行中",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = textSub,
                                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                        itemsIndexed(activeList, key = { _, t -> t.id }) { idx, t ->
                            StaggerIn(index = 3 + idx, modifier = Modifier.fillMaxWidth()) {
                                TaskCard(
                                    task = t,
                                    cardColor = cardColor,
                                    textMain = textMain,
                                    textSub = textSub,
                                    onToggle = {
                                        kotlinx.coroutines.runBlocking {
                                            app.db.taskDao().update(
                                                t.copy(completed = true, completedAt = System.currentTimeMillis())
                                            )
                                        }
                                    },
                                    onClick = { nav.navigate("taskDetail/" + t.id) }
                                )
                            }
                        }
                    }
                    if (doneList.isNotEmpty()) {
                        item {
                            StaggerIn(index = 3 + activeList.size) {
                                Text(
                                    "已完成",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = textSub,
                                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                                )
                            }
                        }
                        itemsIndexed(doneList, key = { _, t -> t.id }) { idx, t ->
                            StaggerIn(
                                index = 4 + activeList.size + idx,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TaskCard(
                                    task = t,
                                    cardColor = cardColor,
                                    textMain = textMain,
                                    textSub = textSub,
                                    onToggle = {
                                        kotlinx.coroutines.runBlocking {
                                            app.db.taskDao().update(
                                                t.copy(completed = false, completedAt = null)
                                            )
                                        }
                                    },
                                    onClick = { nav.navigate("taskDetail/" + t.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 展开式添加 FAB：逐步"长出来"每个字 ──
@Composable
private fun ExpandableAddFab(
    onClick: () -> Unit,
    fullText: String,
) {
    // shownChars: 已解码的汉字数
    // scrambling: 正在为下一个字显示乱码
    var shownChars by remember { mutableIntStateOf(0) }
    var scrambling by remember { mutableStateOf(false) }
    var scrambleSeed by remember { mutableIntStateOf(0) }

    // 乱码跳动：每 40ms 换一次（比上一版快）
    LaunchedEffect(Unit) {
        while (true) {
            scrambleSeed++
            delay(40)
        }
    }

    // 主流程：一个字一个字"长出来"
    LaunchedEffect(Unit) {
        delay(700)  // 让圆形 + 先被看到
        for (i in fullText.indices) {
            // 阶段 1：撑长 + 显示一个乱码位
            scrambling = true
            delay(320)
            // 阶段 2：破解 → 汉字
            shownChars = i + 1
            scrambling = false
            delay(220)  // 停顿一下，让汉字被看清
        }
    }

    // 宽度：圆形 56dp + 每个可见字符 CharWidthDp
    val visibleChars = if (scrambling) shownChars + 1 else shownChars
    val targetWidth = 56.dp + CharWidthDp * visibleChars
    val widthDp by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "fabWidth",
    )

    // 显示文本：已解码的汉字 + 正在破解的那一位乱码
    val displayText = buildString {
        append(fullText.take(shownChars))
        if (scrambling && shownChars < fullText.length) {
            val idx = ((scrambleSeed * 7 + shownChars * 3) % ScramblePool.size + ScramblePool.size) % ScramblePool.size
            append(ScramblePool[idx])
        }
    }

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(widthDp)
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 17.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = fullText,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            if (displayText.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    cardColor: Color,
    textMain: Color,
    textSub: Color,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = cardColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = textSub
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) textSub else textMain,
                    fontWeight = FontWeight.Medium
                )
                if (task.note.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub,
                        maxLines = 2
                    )
                }
            }
        }
    }
}