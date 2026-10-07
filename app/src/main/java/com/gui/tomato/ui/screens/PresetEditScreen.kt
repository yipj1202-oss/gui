package com.gui.tomato.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.data.entities.PomodoroPreset

@Composable
fun PresetEditScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var name by remember { mutableStateOf("新方案") }
    var focus by remember { mutableStateOf(25) }
    var shortB by remember { mutableStateOf(5) }
    var everyN by remember { mutableStateOf(4) }
    var longB by remember { mutableStateOf(15) }
    var isDefault by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("番茄方案", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(name, { name = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IntField("专注", focus, { focus = it }, Modifier.weight(1f))
            IntField("短休", shortB, { shortB = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IntField("每N长休", everyN, { everyN = it }, Modifier.weight(1f))
            IntField("长休", longB, { longB = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(isDefault, { isDefault = it }); Text("设为默认")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val p = PomodoroPreset(name = name, focusMin = focus, shortBreakMin = shortB,
                longBreakEvery = everyN, longBreakMin = longB, isDefault = isDefault)
            kotlinx.coroutines.runBlocking {
                if (isDefault) app.db.presetDao().clearDefault()
                val id = app.db.presetDao().insert(p)
                if (isDefault) app.db.presetDao().setDefault(id)
            }
            nav.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) { Text("保存方案") }
    }
}

@Composable
fun IntField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value.toString(), onValueChange = { onChange(it.toIntOrNull() ?: value) },
        label = { Text(label) }, modifier = modifier)
}