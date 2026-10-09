package com.gui.tomato.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.Task
import com.gui.tomato.data.entities.TaskImage
import com.gui.tomato.data.image.ImageStore
import com.gui.tomato.ui.components.GuiButton
import com.gui.tomato.ui.components.StaggerIn

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkBorder = Color(0xFF262626)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)

private const val MAX_IMAGES = 5

@Composable
fun TaskEditScreen(nav: NavController, taskId: Long) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val imgStore = remember { ImageStore(ctx) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var pickedUris by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }

    LaunchedEffect(taskId) {
        if (taskId != 0L) {
            val t = kotlinx.coroutines.runBlocking { app.db.taskDao().getById(taskId) }
            if (t != null) { title = t.title; note = t.note }
        }
    }

    // 多选图片，最多 5 张
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            pickedUris = (pickedUris + uris).take(MAX_IMAGES)
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val cardColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val borderColor = if (isDark) DarkBorder else Color(0xFFE5E5E5)
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ══ 可滚动内容区 ══
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 返回
            StaggerIn(index = 0, startOffsetY = 14.dp) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = textMain,
                    bg = cardColor,
                ) { nav.popBackStack() }
            }

            Spacer(Modifier.height(28.dp))

            // 大标题
            StaggerIn(index = 1, startOffsetY = 14.dp) {
                Text(
                    text = if (taskId == 0L) stringResource(R.string.task_new)
                    else stringResource(R.string.task_edit),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 6.sp,
                    color = textMain
                )
            }
            Spacer(Modifier.height(8.dp))
            StaggerIn(index = 2, startOffsetY = 10.dp) {
                Text(
                    text = if (taskId == 0L) "NEW TASK" else "EDIT TASK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 5.sp,
                    color = OrangeStart
                )
            }

            Spacer(Modifier.height(36.dp))

            // 标题输入
            StaggerIn(index = 3, startOffsetX = 32.dp, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_field_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeStart,
                        unfocusedBorderColor = borderColor,
                        focusedLabelColor = OrangeStart,
                        unfocusedLabelColor = textSub,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain,
                        cursorColor = OrangeStart,
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                    ),
                )
            }

            Spacer(Modifier.height(14.dp))

            // 备注输入
            StaggerIn(index = 4, startOffsetX = 32.dp, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.task_field_note)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeStart,
                        unfocusedBorderColor = borderColor,
                        focusedLabelColor = OrangeStart,
                        unfocusedLabelColor = textSub,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain,
                        cursorColor = OrangeStart,
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                    ),
                )
            }

            Spacer(Modifier.height(14.dp))

            // 图片预览 + 添加按钮
            StaggerIn(index = 5, startOffsetX = 32.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    if (pickedUris.isNotEmpty()) {
                        // 横向滑动的图片预览
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 2.dp),
                        ) {
                            itemsIndexed(pickedUris) { idx, uri ->
                                Box(
                                    Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cardColor)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                    // 右上角移除
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xB3000000))
                                            .clickable {
                                                pickedUris = pickedUris.toMutableList().apply { removeAt(idx) }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "移除",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // 提示当前张数
                        Text(
                            text = "${pickedUris.size} / $MAX_IMAGES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                            color = textSub,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    GhostPill(
                        icon = Icons.Filled.Image,
                        label = stringResource(R.string.task_add_image),
                        onClick = { picker.launch("image/*") },
                        bgColor = cardColor,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ══ 固定底部：保存按钮 ══
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            StaggerIn(index = 6, startOffsetY = 20.dp, modifier = Modifier.fillMaxWidth()) {
                GuiButton(
                    onClick = {
                        kotlinx.coroutines.runBlocking {
                            val id = if (taskId == 0L)
                                app.db.taskDao().insert(
                                    Task(title = title, note = note, createdAt = System.currentTimeMillis())
                                )
                            else {
                                val old = app.db.taskDao().getById(taskId)
                                app.db.taskDao().update(old!!.copy(title = title, note = note))
                                old.id
                            }
                            // 保存多张图片
                            pickedUris.forEachIndexed { idx, uri ->
                                val path = imgStore.saveUri(id, uri, idx)
                                app.db.taskDao().insertAll(
                                    listOf(TaskImage(taskId = id, filePath = path, idx = idx))
                                )
                            }
                        }
                        nav.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.task_save),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 6.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Composable
private fun GhostPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    bgColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeStart,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = OrangeStart,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 3.sp,
        )
    }
}