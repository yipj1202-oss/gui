package com.gui.tomato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.data.entities.Task

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

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("任务", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            FloatingActionButton(onClick = { nav.navigate("taskEdit/0") }) {
                Icon(Icons.Filled.Add, contentDescription = "新建任务")
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            if (activeList.isNotEmpty()) { item { Text("进行中", Modifier.padding(8.dp), style = MaterialTheme.typography.titleSmall) } }
            items(activeList) { t ->
                TaskRow(t) {
                    kotlinx.coroutines.runBlocking {
                        app.db.taskDao().update(t.copy(completed = true, completedAt = System.currentTimeMillis()))
                    }
                }
            }
            if (doneList.isNotEmpty()) { item { Text("已完成", Modifier.padding(8.dp), style = MaterialTheme.typography.titleSmall) } }
            items(doneList) { t ->
                Row(
                    Modifier.fillMaxWidth().padding(8.dp).clickable { nav.navigate("taskDetail/" + t.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t.title, Modifier.weight(1f))
                    Checkbox(checked = t.completed, onCheckedChange = { })
                }
            }
        }
    }
}

@Composable
fun TaskRow(t: Task, onComplete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(4.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t.title, style = MaterialTheme.typography.titleMedium)
                if (t.note.isNotEmpty()) Text(t.note, style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = t.completed, onCheckedChange = { if (!t.completed) onComplete() })
        }
    }
}