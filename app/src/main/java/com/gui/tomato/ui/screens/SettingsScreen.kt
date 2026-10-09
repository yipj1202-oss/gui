package com.gui.tomato.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.ui.components.StaggerIn

private val DarkBg = Color(0xFF0D0D0D)
private val DarkRowBg = Color(0xFF141414)
private val DarkPillTrack = Color(0xFF1F1F1F)
private val DarkDivider = Color(0xFF262626)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

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
    val rowBg = if (isDark) DarkRowBg else MaterialTheme.colorScheme.surface
    val pillTrack = if (isDark) DarkPillTrack else Color(0xFFEFEFEF)
    val dividerColor = if (isDark) DarkDivider else Color(0xFFE5E5E5)
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    Scaffold(containerColor = bgColor) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 返回 ──
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                StaggerIn(index = 0, startOffsetY = 8.dp) {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = textMain
                        )
                    }
                }
            }

            // ── 大标题 ──
            Column(Modifier.padding(horizontal = 28.dp)) {
                StaggerIn(index = 1, startOffsetY = 14.dp) {
                    Text(
                        text = "设置",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = 8.sp,
                        color = textMain
                    )
                }
                Spacer(Modifier.height(10.dp))
                StaggerIn(index = 2, startOffsetY = 14.dp) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SETTINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 5.sp,
                            color = textSub
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "07 ITEMS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 4.sp,
                            color = OrangeStart
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── 分组：外观 ──
            GroupHeader(3, "外观", "APPEARANCE", textMain, textSub, dividerColor)

            SettingRow(4, 1, "预设管理", "PRESET MANAGE", textMain, textSub, rowBg, dividerColor,
                onClick = { nav.navigate("presetEdit") })

            SettingRow(5, 2, "主题", "THEME", textMain, textSub, rowBg, dividerColor) {
                PillToggle(
                    options = listOf(
                        stringResource(R.string.settings_theme_light),
                        stringResource(R.string.settings_theme_dark)
                    ),
                    selectedIdx = themeIdx,
                    textSub = textSub,
                    pillTrack = pillTrack,
                    onSelect = { prefs.theme = it }
                )
            }

            SettingRow(6, 3, "语言", "LANGUAGE", textMain, textSub, rowBg, dividerColor) {
                PillToggle(
                    options = listOf(
                        stringResource(R.string.settings_language_zh),
                        stringResource(R.string.settings_language_en)
                    ),
                    selectedIdx = langIdx,
                    textSub = textSub,
                    pillTrack = pillTrack,
                    onSelect = {
                        if (prefs.lang != it) {
                            prefs.lang = it
                            (ctx as? Activity)?.recreate()
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── 分组：提醒 ──
            GroupHeader(7, "提醒", "ALERTS", textMain, textSub, dividerColor)

            SettingRow(8, 4, "通知", "NOTIFICATION", textMain, textSub, rowBg, dividerColor) {
                Switch(
                    notify,
                    onCheckedChange = { notify = it; prefs.notifyEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangeStart
                    )
                )
            }

            SettingRow(9, 5, "震动", "VIBRATE", textMain, textSub, rowBg, dividerColor) {
                Switch(
                    vibrate,
                    onCheckedChange = { vibrate = it; prefs.vibrate = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangeStart
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── 分组：数据 ──
            GroupHeader(10, "数据", "DATA", textMain, textSub, dividerColor)

            SettingRow(11, 6, "导出备份", "EXPORT BACKUP", textMain, textSub, rowBg, dividerColor,
                onClick = { app.backupToPublic() })

            SettingRow(12, 7, "导入备份", "IMPORT BACKUP", textMain, textSub, rowBg, dividerColor,
                onClick = {
                    import.launch(arrayOf("application/zip", "application/octet-stream"))
                })

            Spacer(Modifier.height(36.dp))

            // ── footer ──
            StaggerIn(index = 13, startOffsetY = 16.dp) {
                Text(
                    stringResource(R.string.settings_footer),
                    modifier = Modifier.padding(horizontal = 28.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textSub
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── 分组标题：外观 / APPEARANCE ────────────────
@Composable
private fun GroupHeader(
    index: Int,
    zh: String,
    en: String,
    textMain: Color,
    textSub: Color,
    lineColor: Color,
) {
    StaggerIn(
        index = index,
        modifier = Modifier.fillMaxWidth(),
        startOffsetX = 24.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                zh,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                color = textMain
            )
            Spacer(Modifier.width(10.dp))
            Text(
                en,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                color = textSub
            )
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(lineColor)
            )
        }
    }
}

// ── 单条设置项 ────────────────────────────────
@Composable
private fun SettingRow(
    index: Int,
    number: Int,
    title: String,
    en: String,
    textMain: Color,
    textSub: Color,
    rowBg: Color,
    borderColor: Color,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    StaggerIn(
        index = index,
        modifier = Modifier.fillMaxWidth(),
        startOffsetX = 32.dp,
    ) {
        val shape = RoundedCornerShape(14.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(shape)
                .background(rowBg)
                .border(1.dp, borderColor, shape)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 编号 01~07
            Text(
                text = "%02d".format(number),
                fontSize = 32.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 1.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                ),
                modifier = Modifier.width(52.dp),
            )
            // 细分隔
            Box(
                Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(borderColor)
            )
            Spacer(Modifier.width(14.dp))
            // 标题 + 英文
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = textMain
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    en,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color = textSub
                )
            }
            trailing?.invoke()
        }
    }
}

// ── 胶囊开关 ──────────────────────────────────
@Composable
private fun PillToggle(
    options: List<String>,
    selectedIdx: Int,
    textSub: Color,
    pillTrack: Color,
    onSelect: (Int) -> Unit,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(pillTrack)
            .padding(3.dp),
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIdx
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (selected) Modifier.background(
                            Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                        ) else Modifier
                    )
                    .clickable { onSelect(i) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Light,
                    letterSpacing = 1.sp,
                    color = if (selected) Color.White else textSub,
                )
            }
        }
    }
}