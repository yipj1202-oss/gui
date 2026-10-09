package com.gui.tomato.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.gui.tomato.data.entities.TaskImage
import com.gui.tomato.data.image.ImageStore
import com.gui.tomato.ui.components.GuiButton

@Composable
fun TaskDetailScreen(nav: NavController, taskId: Long) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var imgs by remember { mutableStateOf<List<TaskImage>>(emptyList()) }
    LaunchedEffect(taskId) { app.db.taskDao().imagesForFlow(taskId).collect { imgs = it } }
    val imgStore = remember { ImageStore(ctx) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = MaterialTheme.colorScheme.background
    val cardColor = if (isDark) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface
    val textPrimary = if (isDark) Color(0xFFEDEDED) else MaterialTheme.colorScheme.onBackground
    val textMuted = if (isDark) Color(0xFF8A8A8A) else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── 顶栏：返回 / 删除 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = textPrimary,
                    bg = cardColor,
                ) { nav.popBackStack() }

                Spacer(Modifier.weight(1f))

                CircleIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.task_delete_desc),
                    tint = textMuted,
                    bg = cardColor,
                ) {
                    kotlinx.coroutines.runBlocking { app.db.taskDao().delete(taskId) }
                    imgStore.deleteForTask(taskId)
                    nav.popBackStack()
                }
            }

            Spacer(Modifier.height(44.dp))

            // ── 图片区 ──
            if (imgs.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(imgs) { img ->
                        AsyncImage(
                            model = imgStore.file(img.filePath),
                            contentDescription = null,
                            modifier = Modifier
                                .size(132.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { nav.navigate("taskEdit/$taskId") },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.task_image_count, imgs.size),
                    color = textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(Modifier.weight(1f))

            // ── 底部主按钮 ──
            GuiButton(
                onClick = {
                    app.repo.start(taskId)
                    app.serviceStart()
                    nav.navigate("tasks") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 18.dp),
            ) {
                Text(
                    text = stringResource(R.string.task_start_pomodoro),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                )
            }

            Spacer(Modifier.height(28.dp))
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