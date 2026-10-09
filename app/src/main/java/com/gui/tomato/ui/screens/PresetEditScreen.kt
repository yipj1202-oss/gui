package com.gui.tomato.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.ui.widgets.RotaryKnob

private val DarkBg = Color(0xFF0D0D0D)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "自定义方案",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = 4.sp,
                        color = textMain
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
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.preset_field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeStart,
                    unfocusedBorderColor = textSub.copy(alpha = 0.3f),
                    focusedLabelColor = OrangeStart,
                    unfocusedLabelColor = textSub,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )

            Spacer(Modifier.height(24.dp))

            // 大旋钮：专注时长
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RotaryKnob(
                    value = focus,
                    onChange = { focus = it },
                    min = 1,
                    max = 90,
                    step = 1,
                    unit = "分",
                    label = "专注时长",
                    modifier = Modifier.size(240.dp),
                    isDark = isDark,
                    numberSizeSp = 56
                )
            }

            Spacer(Modifier.height(20.dp))

            // 三个小旋钮：平分宽度，自适应大小
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = shortB,
                    onChange = { shortB = it },
                    min = 1,
                    max = 30,
                    step = 1,
                    unit = "分",
                    label = "短休",
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    numberSizeSp = 24,
                    labelSizeSp = 9
                )
                RotaryKnob(
                    value = longB,
                    onChange = { longB = it },
                    min = 1,
                    max = 60,
                    step = 1,
                    unit = "分",
                    label = "长休",
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    numberSizeSp = 24,
                    labelSizeSp = 9
                )
                RotaryKnob(
                    value = everyN,
                    onChange = { everyN = it },
                    min = 1,
                    max = 10,
                    step = 1,
                    unit = "个",
                    label = "长休间隔",
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    numberSizeSp = 24,
                    labelSizeSp = 9
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OrangeStart,
                        uncheckedColor = textSub
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.preset_checkbox_default),
                    color = textMain,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
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
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.preset_save),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 6.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}