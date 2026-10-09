package com.gui.tomato.ui.screens

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
import com.gui.tomato.data.entities.PomodoroPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var name by remember { mutableStateOf("自定义方案") }
    var focus by remember { mutableIntStateOf(25) }
    var shortB by remember { mutableIntStateOf(5) }
    var everyN by remember { mutableIntStateOf(4) }
    var longB by remember { mutableIntStateOf(15) }
    var isDefault by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建番茄方案") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("方案名称") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntField("专注 (分钟)", focus, { focus = it }, Modifier.weight(1f))
                IntField("短休 (分钟)", shortB, { shortB = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntField("长休间隔 (个)", everyN, { everyN = it }, Modifier.weight(1f))
                IntField("长休 (分钟)", longB, { longB = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("设为默认方案")
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val p = PomodoroPreset(
                        name = name,
                        focusMin = focus,
                        shortBreakMin = shortB,
                        longBreakEvery = everyN,
                        longBreakMin = longB,
                        isDefault = isDefault
                    )
                    kotlinx.coroutines.runBlocking {
                        if (isDefault) app.db.presetDao().clearDefault()
                        val id = app.db.presetDao().insert(p)
                        if (isDefault) app.db.presetDao().setDefault(id)
                    }
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存方案")
            }
        }
    }
}

@Composable
fun IntField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onChange(it.toIntOrNull() ?: value) },
        label = { Text(label) },
        modifier = modifier
    )
}
