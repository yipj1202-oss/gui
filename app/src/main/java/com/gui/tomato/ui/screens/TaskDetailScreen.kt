package com.gui.tomato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.TaskImage
import com.gui.tomato.data.image.ImageStore

@Composable
fun TaskDetailScreen(nav: NavController, taskId: Long) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var imgs by remember { mutableStateOf<List<TaskImage>>(emptyList()) }
    LaunchedEffect(taskId) { app.db.taskDao().imagesForFlow(taskId).collect { imgs = it } }
    val imgStore = remember { ImageStore(ctx) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                app.repo.start(taskId)
                app.serviceStart()
                nav.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }) {
                Text(stringResource(R.string.task_start_pomodoro))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                kotlinx.coroutines.runBlocking { app.db.taskDao().delete(taskId) }
                imgStore.deleteForTask(taskId)
                nav.popBackStack()
            }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.task_delete_desc))
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imgs) { img ->
                AsyncImage(
                    model = imgStore.file(img.filePath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { nav.navigate("taskEdit/$taskId") },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(stringResource(R.string.task_image_count, imgs.size), style = MaterialTheme.typography.labelSmall)
    }
}
