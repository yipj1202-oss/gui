package com.gui.tomato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gui.tomato.GuiApp
import com.gui.tomato.data.image.ImageStore
import com.gui.tomato.data.entities.TaskImage

@Composable
fun TaskDetailScreen(nav: NavController, taskId: Long) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var imgs by remember { mutableStateOf<List<TaskImage>>(emptyList()) }
    LaunchedEffect(Unit) { app.db.taskDao().imagesForFlow(taskId).collect { imgs = it } }
    val imgStore = remember { ImageStore(ctx) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Button(onClick = {
                app.repo.start(taskId)
                app.serviceStart()
                nav.popBackStack()
            }) { Text("开始番茄") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                kotlinx.coroutines.runBlocking { app.db.taskDao().delete(taskId) }
                imgStore.deleteForTask(taskId)
                nav.popBackStack()
            }) {
                Icon(Icons.Filled.Delete, contentDescription = "删除")
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(contentPadding = PaddingValues(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(imgs) { img ->
                AsyncImage(model = imgStore.file(img.filePath), contentDescription = null,
                    modifier = Modifier.size(120.dp).clickable { nav.navigate("taskEdit/$taskId") }
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop)
            }
        }
        Text("${imgs.size} 张图片", style = MaterialTheme.typography.labelSmall)
    }
}