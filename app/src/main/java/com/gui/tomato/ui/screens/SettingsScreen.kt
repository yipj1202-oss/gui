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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R

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
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                    Text(stringResource(R.string.settings_preset_manage), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.settings_preset_manage_action), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
                    Text(stringResource(R.string.settings_theme), Modifier.weight(1f))
                    val themes = listOf(stringResource(R.string.settings_theme_light), stringResource(R.string.settings_theme_dark))
                    themes.forEachIndexed { i, l ->
                        FilterChip(
                            selected = themeIdx == i,
                            onClick = {
                                prefs.theme = i
                                (ctx as? android.app.Activity)?.recreate()
                            },
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
                    Text(stringResource(R.string.settings_language), Modifier.weight(1f))
                    val langs = listOf(stringResource(R.string.settings_language_zh), stringResource(R.string.settings_language_en))
                    langs.forEachIndexed { i, l ->
                        FilterChip(
                            selected = langIdx == i,
                            onClick = {
                                prefs.lang = i
                                (ctx as? android.app.Activity)?.recreate()
                            },
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
                    Text(stringResource(R.string.settings_notification), Modifier.weight(1f))
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
                    Text(stringResource(R.string.settings_vibrate), Modifier.weight(1f))
                    Switch(vibrate, onCheckedChange = { vibrate = it; prefs.vibrate = it })
                }
            }
            Card(
                onClick = { app.backupToPublic() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(stringResource(R.string.settings_export_backup), Modifier.padding(12.dp))
            }
            Card(
                onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(stringResource(R.string.settings_import_backup), Modifier.padding(12.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_footer),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
