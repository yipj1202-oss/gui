package com.gui.tomato.ui.screens

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "任务",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FloatingActionButton(onClick = { nav.navigate("taskEdit/0") }) {
                    Icon(Icons.Filled.Add, contentDescription = "新建任务")
                }
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (activeList.isNotEmpty()) {
                    item {
                        Text(
                            "进行中",
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                items(activeList) { t ->
                    TaskRow(
                        t = t,
                        onClick = { nav.navigate("taskDetail/" + t.id) },
                        onToggleComplete = { isChecked ->
                            kotlinx.coroutines.runBlocking {
                                app.db.taskDao().update(
                                    t.copy(
                                        completed = isChecked,
                                        completedAt = if (isChecked) System.currentTimeMillis() else null
                                    )
                                )
                            }
                        }
                    )
                }
                if (doneList.isNotEmpty()) {
                    item {
                        Text(
                            "已完成",
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                items(doneList) { t ->
                    TaskRow(
                        t = t,
                        onClick = { nav.navigate("taskDetail/" + t.id) },
                        onToggleComplete = { isChecked ->
                            kotlinx.coroutines.runBlocking {
                                app.db.taskDao().update(
                                    t.copy(
                                        completed = isChecked,
                                        completedAt = if (isChecked) System.currentTimeMillis() else null
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    t: Task,
    onClick: () -> Unit,
    onToggleComplete: (Boolean) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(t.title, style = MaterialTheme.typography.titleMedium)
                if (t.note.isNotEmpty()) {
                    Text(
                        t.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Checkbox(
                checked = t.completed,
                onCheckedChange = { isChecked -> onToggleComplete(isChecked) }
            )
        }
    }
}
