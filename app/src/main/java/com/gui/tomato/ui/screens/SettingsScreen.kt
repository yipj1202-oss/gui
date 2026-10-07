package com.gui.tomato.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.ui.prefs.AppPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val prefs = remember { AppPrefs(ctx) }
    var themeIdx by remember { mutableIntStateOf(prefs.theme) }
    var langIdx by remember { mutableIntStateOf(prefs.lang) }
    var notify by remember { mutableStateOf(prefs.notifyEnabled) }
    var vibrate by remember { mutableStateOf(prefs.vibrate) }

    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val res = app.backupImport(uri)
            Toast.makeText(ctx, "导入: $res", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("设置") }, navigationIcon = {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Card(Modifier.fillMaxWidth().padding(8.dp)) { Row(Modifier.padding(12.dp)) {
                Text("主题", Modifier.weight(1f))
                listOf("浅色", "深色").forEachIndexed { i, l ->
                    FilterChip(selected = themeIdx == i, onClick = { themeIdx = i; prefs.theme = i }, label = { Text(l) })
                    if (i == 0) Spacer(Modifier.width(8.dp))
                }
            } }
            Card(Modifier.fillMaxWidth().padding(8.dp)) { Row(Modifier.padding(12.dp)) {
                Text("语言", Modifier.weight(1f))
                listOf("中文", "English").forEachIndexed { i, l ->
                    FilterChip(selected = langIdx == i, onClick = { langIdx = i; prefs.lang = i }, label = { Text(l) })
                    if (i == 0) Spacer(Modifier.width(8.dp))
                }
            } }
            Card(Modifier.fillMaxWidth().padding(8.dp)) { Row(Modifier.padding(12.dp)) {
                Text("通知", Modifier.weight(1f)); Switch(notify, onCheckedChange = { notify = it; prefs.notifyEnabled = it })
            } }
            Card(Modifier.fillMaxWidth().padding(8.dp)) { Row(Modifier.padding(12.dp)) {
                Text("震动", Modifier.weight(1f)); Switch(vibrate, onCheckedChange = { vibrate = it; prefs.vibrate = it })
            } }
            Card(onClick = { app.backupToPublic() }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("导出备份 (.guiback)", Modifier.padding(12.dp))
            }
            Card(onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("导入备份", Modifier.padding(12.dp))
            }
            Text("晷 · 以晷量时，以行成事", Modifier.padding(16.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}