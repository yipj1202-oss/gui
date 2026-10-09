package com.gui.tomato.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val prefs = app.prefs
    val themeIdx = prefs.theme
    val langIdx = prefs.lang
    var notify by remember { mutableStateOf(prefs.notifyEnabled) }
    var vibrate by remember { mutableStateOf(prefs.vibrate) }

    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val res = app.backupImport(uri)
            Toast.makeText(ctx, "导入: $res", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )
    }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            Card(
                onClick = { nav.navigate("presetEdit") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("番茄方案管理", style = MaterialTheme.typography.bodyLarge)
                    Text("新建/编辑 ❯", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("主题", Modifier.weight(1f))
                    listOf("浅色", "深色").forEachIndexed { i, l ->
                        FilterChip(
                            selected = themeIdx == i,
                            onClick = { prefs.theme = i },
                            label = { Text(l) }
                        )
                        if (i == 0) Spacer(Modifier.width(8.dp))
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("语言", Modifier.weight(1f))
                    listOf("中文", "English").forEachIndexed { i, l ->
                        FilterChip(
                            selected = langIdx == i,
                            onClick = { prefs.lang = i },
                            label = { Text(l) }
                        )
                        if (i == 0) Spacer(Modifier.width(8.dp))
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("通知", Modifier.weight(1f))
                    Switch(notify, onCheckedChange = { notify = it; prefs.notifyEnabled = it })
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("震动", Modifier.weight(1f))
                    Switch(vibrate, onCheckedChange = { vibrate = it; prefs.vibrate = it })
                }
            }
            Card(
                onClick = { app.backupToPublic() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("导出备份 (.guiback)", Modifier.padding(12.dp))
            }
            Card(
                onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("导入备份", Modifier.padding(12.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "晷 · 以晷量时，以行成事",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}