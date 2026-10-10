package com.gui.tomato.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import com.gui.tomato.util.Haptic
import com.gui.tomato.util.SoundPlayer

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
    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var soundIdx by remember { mutableIntStateOf(prefs.soundIdx) }

    // 音波预览状态
    var waveOrigin by remember { mutableStateOf<Offset?>(null) }
    var waveSoundIdx by remember { mutableIntStateOf(0) }
    var waveTrigger by remember { mutableIntStateOf(0) }
    val waveAnim = remember { Animatable(0f) }

    LaunchedEffect(waveTrigger) {
        if (waveTrigger > 0) {
            waveAnim.snapTo(0f)
            waveAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = LinearOutSlowInEasing
                )
            )
            waveOrigin = null
        }
    }

    val importToastTemplate = stringResource(R.string.settings_import_toast)

    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val res = app.backupImport(uri)
            Toast.makeText(ctx, importToastTemplate.format(res), Toast.LENGTH_SHORT).show()
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val rowBg = if (isDark) DarkRowBg else MaterialTheme.colorScheme.surface
    val pillTrack = if (isDark) DarkPillTrack else Color(0xFFEFEFEF)
    val dividerColor = if (isDark) DarkDivider else Color(0xFFE5E5E5)
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val waveProgress = waveAnim.value

    Box(Modifier.fillMaxSize()) {
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
                            text = stringResource(R.string.settings_header),
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
                                text = stringResource(R.string.settings_items_count),
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
                GroupHeader(
                    index = 3,
                    title = stringResource(R.string.settings_group_appearance),
                    subtitle = stringResource(R.string.settings_group_appearance_sub),
                    textMain = textMain,
                    textSub = textSub,
                    lineColor = dividerColor,
                )

                SettingRow(
                    index = 4, number = 1,
                    title = stringResource(R.string.settings_card_preset),
                    subtitle = stringResource(R.string.settings_card_preset_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor,
                    onClick = { nav.navigate("presetEdit") }
                )

                SettingRow(
                    index = 5, number = 2,
                    title = stringResource(R.string.settings_card_theme),
                    subtitle = stringResource(R.string.settings_card_theme_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor
                ) {
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

                SettingRow(
                    index = 6, number = 3,
                    title = stringResource(R.string.settings_card_language),
                    subtitle = stringResource(R.string.settings_card_language_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor
                ) {
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
                GroupHeader(
                    index = 7,
                    title = stringResource(R.string.settings_group_alerts),
                    subtitle = stringResource(R.string.settings_group_alerts_sub),
                    textMain = textMain,
                    textSub = textSub,
                    lineColor = dividerColor,
                )

                SettingRow(
                    index = 8, number = 4,
                    title = stringResource(R.string.settings_card_notify),
                    subtitle = stringResource(R.string.settings_card_notify_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor
                ) {
                    Switch(
                        notify,
                        onCheckedChange = { notify = it; prefs.notifyEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangeStart
                        )
                    )
                }

                SettingRow(
                    index = 9, number = 5,
                    title = stringResource(R.string.settings_card_vibrate),
                    subtitle = stringResource(R.string.settings_card_vibrate_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor
                ) {
                    Switch(
                        vibrate,
                        onCheckedChange = { vibrate = it; prefs.vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangeStart
                        )
                    )
                }

                SettingRow(
                    index = 10, number = 6,
                    title = stringResource(R.string.settings_card_sound),
                    subtitle = stringResource(R.string.settings_card_sound_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor
                ) {
                    Switch(
                        soundEnabled,
                        onCheckedChange = { soundEnabled = it; prefs.soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangeStart
                        )
                    )
                }

                // ── 音效选择（带音波预览）──
                SoundPickerRow(
                    index = 11,
                    number = 7,
                    selectedIdx = soundIdx,
                    textMain = textMain,
                    textSub = textSub,
                    rowBg = rowBg,
                    borderColor = dividerColor,
                    onPick = { idx, origin ->
                        // 同步三件事：音波 → 音效 → 振动
                        waveSoundIdx = idx
                        waveOrigin = origin
                        waveTrigger++

                        soundIdx = idx
                        prefs.soundIdx = idx
                        if (prefs.soundEnabled) SoundPlayer.play(idx)
                        if (prefs.vibrate) Haptic.tap(ctx)
                    }
                )

                Spacer(Modifier.height(8.dp))

                // ── 分组：数据 ──
                GroupHeader(
                    index = 12,
                    title = stringResource(R.string.settings_group_data),
                    subtitle = stringResource(R.string.settings_group_data_sub),
                    textMain = textMain,
                    textSub = textSub,
                    lineColor = dividerColor,
                )

                SettingRow(
                    index = 13, number = 8,
                    title = stringResource(R.string.settings_card_export),
                    subtitle = stringResource(R.string.settings_card_export_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor,
                    onClick = { app.backupToPublic() }
                )

                SettingRow(
                    index = 14, number = 9,
                    title = stringResource(R.string.settings_card_import),
                    subtitle = stringResource(R.string.settings_card_import_sub),
                    textMain = textMain, textSub = textSub, rowBg = rowBg, borderColor = dividerColor,
                    onClick = {
                        import.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                )

                Spacer(Modifier.height(36.dp))

                // ── footer ──
                StaggerIn(index = 15, startOffsetY = 16.dp) {
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

        // ── 音波 overlay ──
        val origin = waveOrigin
        if (origin != null && waveProgress > 0f) {
            Canvas(Modifier.fillMaxSize()) {
                val maxR = kotlin.math.sqrt(
                    size.width * size.width + size.height * size.height
                )

                // 每个音效的每一"声"都有一圈：
                //   offset  = 该声音在 1500ms 时间轴上的起始进度
                //   alpha   = 基础亮度
                //   widthMul= 线宽倍率
                val rings: List<Triple<Float, Float, Float>> = when (waveSoundIdx) {
                    0 -> listOf(
                        // 暮鼓：1 声 @ 0ms
                        Triple(0f, 0.85f, 1.6f)
                    )
                    1 -> listOf(
                        // 晨钟：声1 @ 0ms，声2 @ 280ms
                        Triple(0f, 0.65f, 1.0f),
                        Triple(280f / 1500f, 0.65f, 1.0f)
                    )
                    else -> listOf(
                        // 更漏：声1 @ 0ms，声2 @ 180ms，声3 @ 360ms
                        Triple(0f, 0.55f, 0.9f),
                        Triple(180f / 1500f, 0.55f, 0.9f),
                        Triple(360f / 1500f, 0.55f, 0.9f)
                    )
                }

                // 每一圈的扩散时长（占比），900ms 左右
                val ringDur = 0.6f

                for ((offset, baseAlpha, widthMul) in rings) {
                    if (waveProgress < offset) continue
                    val p = (waveProgress - offset) / ringDur
                    if (p <= 0f || p >= 1f) continue
                    val radius = p * maxR
                    val alpha = ((1f - p) * baseAlpha).coerceAtLeast(0f)
                    drawCircle(
                        color = OrangeStart.copy(alpha = alpha),
                        radius = radius,
                        center = origin,
                        style = Stroke(width = 2.dp.toPx() * widthMul)
                    )
                }
            }
        }
    }
}

// ── 分组标题 ──────────────────────────────────
@Composable
private fun GroupHeader(
    index: Int,
    title: String,
    subtitle: String,
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
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                color = textMain
            )
            Spacer(Modifier.width(10.dp))
            Text(
                subtitle,
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
    subtitle: String,
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
            Box(
                Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(borderColor)
            )
            Spacer(Modifier.width(14.dp))
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
                    subtitle,
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

// ── 音效选择行（编号 + 标题 + 3 个音效胶囊）──
@Composable
private fun SoundPickerRow(
    index: Int,
    number: Int,
    selectedIdx: Int,
    textMain: Color,
    textSub: Color,
    rowBg: Color,
    borderColor: Color,
    onPick: (Int, Offset) -> Unit,
) {
    StaggerIn(
        index = index,
        modifier = Modifier.fillMaxWidth(),
        startOffsetX = 32.dp,
    ) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(shape)
                .background(rowBg)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(borderColor)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        stringResource(R.string.settings_card_tone),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = textMain
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.settings_card_tone_sub),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp,
                        color = textSub
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToneChip(stringResource(R.string.tone_dusk_drum), 0, selectedIdx == 0, textSub, onPick)
                ToneChip(stringResource(R.string.tone_dawn_bell), 1, selectedIdx == 1, textSub, onPick)
                ToneChip(stringResource(R.string.tone_water_clock), 2, selectedIdx == 2, textSub, onPick)
            }
        }
    }
}

@Composable
private fun RowScope.ToneChip(
    label: String,
    idx: Int,
    selected: Boolean,
    textSub: Color,
    onPick: (Int, Offset) -> Unit,
) {
    var centerPos by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .weight(1f)
            .onGloballyPositioned { coords ->
                val p = coords.positionInRoot()
                val s = coords.size
                centerPos = Offset(p.x + s.width / 2f, p.y + s.height / 2f)
            }
            .clip(RoundedCornerShape(50))
            .then(
                if (selected)
                    Modifier.background(Brush.linearGradient(listOf(OrangeStart, OrangeEnd)))
                else
                    Modifier.border(1.dp, textSub.copy(alpha = 0.30f), RoundedCornerShape(50))
            )
            .clickable { onPick(idx, centerPos) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Light,
            letterSpacing = 2.sp,
            color = if (selected) Color.White else textSub
        )
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