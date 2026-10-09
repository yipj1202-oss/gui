package com.gui.tomato.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.Task
import com.gui.tomato.data.entities.TaskImage
import com.gui.tomato.data.image.ImageStore

@Composable
fun TaskEditScreen(nav: NavController, taskId: Long) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val imgStore = remember { ImageStore(ctx) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    LaunchedEffect(taskId) {
        if (taskId != 0L) {
            val t = kotlinx.coroutines.runBlocking { app.db.taskDao().getById(taskId) }
            if (t != null) { title = t.title; note = t.note }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pickedUri = it }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (taskId == 0L) stringResource(R.string.task_new) else stringResource(R.string.task_edit), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(title, onValueChange = { title = it }, label = { Text(stringResource(R.string.task_field_title)) },
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(note, onValueChange = { note = it }, label = { Text(stringResource(R.string.task_field_note)) },
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            kotlinx.coroutines.runBlocking {
                val id = if (taskId == 0L)
                    app.db.taskDao().insert(Task(title = title, note = note, createdAt = System.currentTimeMillis()))
                else {
                    val old = app.db.taskDao().getById(taskId)
                    app.db.taskDao().update(old!!.copy(title = title, note = note))
                    old.id
                }
                pickedUri?.let { uri ->
                    val path = imgStore.saveUri(id, uri, 0)
                    app.db.taskDao().insertAll(listOf(TaskImage(taskId = id, filePath = path, idx = 0)))
                }
            }
            nav.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.task_save)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.task_add_image)) }
    }
}
