package com.gui.tomato.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)

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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val cardColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        color = textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = textMain
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // 番茄方案管理
            SettingsCard(cardColor, onClick = { nav.navigate("presetEdit") }) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_preset_manage),
                        color = textMain,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        stringResource(R.string.settings_preset_manage_action),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 主题
            SettingsCard(cardColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_theme),
                        color = textMain,
                        modifier = Modifier.weight(1f)
                    )
                    listOf(
                        stringResource(R.string.settings_theme_light),
                        stringResource(R.string.settings_theme_dark)
                    ).forEachIndexed { i, l ->
                        FilterChip(
                            selected = themeIdx == i,
                            onClick = { prefs.theme = i },
                            label = { Text(l) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                labelColor = textSub
                            )
                        )
                        if (i == 0) Spacer(Modifier.width(8.dp))
                    }
                }
            }

            // 语言
            SettingsCard(cardColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_language),
                        color = textMain,
                        modifier = Modifier.weight(1f)
                    )
                    listOf(
                        stringResource(R.string.settings_language_zh),
                        stringResource(R.string.settings_language_en)
                    ).forEachIndexed { i, l ->
                        FilterChip(
                            selected = langIdx == i,
                            onClick = { prefs.lang = i },
                            label = { Text(l) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                labelColor = textSub
                            )
                        )
                        if (i == 0) Spacer(Modifier.width(8.dp))
                    }
                }
            }

            // 通知
            SettingsCard(cardColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_notification),
                        color = textMain,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        notify,
                        onCheckedChange = { notify = it; prefs.notifyEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // 震动
            SettingsCard(cardColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_vibrate),
                        color = textMain,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        vibrate,
                        onCheckedChange = { vibrate = it; prefs.vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // 导出
            SettingsCard(cardColor, onClick = { app.backupToPublic() }) {
                Text(
                    stringResource(R.string.settings_export_backup),
                    color = textMain,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // 导入
            SettingsCard(cardColor, onClick = {
                import.launch(arrayOf("application/zip", "application/octet-stream"))
            }) {
                Text(
                    stringResource(R.string.settings_import_backup),
                    color = textMain,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_footer),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelSmall,
                color = textSub
            )
        }
    }
}

@Composable
private fun SettingsCard(
    cardColor: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val mod = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Surface(
        modifier = mod,
        color = cardColor,
        shape = shape
    ) {
        Box(Modifier.padding(14.dp)) {
            content()
        }
    }
}