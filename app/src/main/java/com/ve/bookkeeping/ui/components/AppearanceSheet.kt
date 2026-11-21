@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.R
import com.ve.bookkeeping.ui.theme.ThemePalette
import java.io.File
import androidx.compose.material3.FilterChip

/**
 * 外观面板：8 套预设主题色 + PRO 自选色；背景图 3 张内置 + PRO 自选图片。
 */
@Composable
fun AppearanceSheet(
    palette: ThemePalette,
    customColorArgb: Int,
    backgroundEnabled: Boolean,
    backgroundIndex: Int,
    customBackgroundPath: String?,
    proEnabled: Boolean,
    onSelectPreset: (ThemePalette) -> Unit,
    onSelectCustomColor: (Int) -> Unit,
    onToggleBackground: (Boolean) -> Unit,
    onSelectBuiltIn: (Int) -> Unit,
    onPickCustomImage: (Uri) -> Unit,
    onNeedPro: () -> Unit,
    themeMode: String,
    onThemeMode: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showColorPicker by remember { mutableStateOf(false) }
    val customColor = Color(customColorArgb)

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPickCustomImage(uri) }

    val customBitmap = remember(customBackgroundPath) {
        customBackgroundPath?.let { path ->
            runCatching { BitmapFactory.decodeFile(File(path).absolutePath) }.getOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "改完立刻生效并记住；带 Pro 标记的项需要开通 Pro",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "深浅模式",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "system" to "跟随系统",
                    "light" to "浅色",
                    "dark" to "深色",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = themeMode == value,
                        onClick = { onThemeMode(value) },
                        label = { Text(text = label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "主题色",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))

            val swatches = remember { ThemePalette.presets }
            swatches.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { item ->
                        ThemeSwatch(
                            color = item.primary,
                            label = item.label,
                            selected = palette.id == item.id,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectPreset(item) },
                        )
                    }
                    // 第一轮后面接「自选」
                    if (row.last().id == swatches.last().id) {
                        CustomColorSwatch(
                            current = customColor,
                            selected = palette.isCustom,
                            proEnabled = proEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (proEnabled) showColorPicker = true else onNeedPro()
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "背景图",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (backgroundEnabled) "已开启 · 卡片半透明" else "默认关闭，使用纯色底",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = backgroundEnabled,
                    onCheckedChange = onToggleBackground,
                )
            }

            if (backgroundEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                // 8 张内置背景 + 1 张自选，按 3 列排；每张背景都带一个推荐主题色
                val cells = builtInBackgroundList.indices.toList() + CUSTOM_BACKGROUND_INDEX
                cells.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { index ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (index == CUSTOM_BACKGROUND_INDEX) {
                                    BackgroundThumb(
                                        selected = backgroundIndex == CUSTOM_BACKGROUND_INDEX,
                                        label = if (proEnabled) "自选" else "Pro",
                                        onClick = {
                                            if (!proEnabled) {
                                                onNeedPro()
                                            } else {
                                                pickImage.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                                    ),
                                                )
                                            }
                                        },
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(96.dp)
                                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (customBitmap != null) {
                                                Image(
                                                    bitmap = customBitmap.asImageBitmap(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxWidth().height(96.dp),
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = if (proEnabled) {
                                                            Icons.Rounded.AddPhotoAlternate
                                                        } else {
                                                            Icons.Rounded.Lock
                                                        },
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (proEnabled) "选图片" else "Pro",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val item = builtInBackgroundList[index]
                                    BackgroundThumb(
                                        selected = backgroundIndex == index,
                                        label = item.name,
                                        onClick = { onSelectBuiltIn(index) },
                                    ) {
                                        Image(
                                            painter = painterResource(item.res),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().height(96.dp),
                                        )
                                    }
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "完成")
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initial = if (palette.isCustom) customColor else palette.primary,
            onConfirm = { picked ->
                showColorPicker = false
                onSelectCustomColor(picked.toArgb())
            },
            onDismiss = { showColorPicker = false },
        )
    }
}

@Composable
private fun ThemeSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            CircleShape,
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.6f) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun CustomColorSwatch(
    current: Color,
    selected: Boolean,
    proEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFFF5A5F), Color(0xFFFFC65A), Color(0xFF5AD1A0),
                                Color(0xFF5AA9FF), Color(0xFFB45AFF), Color(0xFFFF5A5F),
                            ),
                        ),
                    )
                    .then(
                        if (selected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                CircleShape,
                            )
                        },
                    ),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(current),
                )
            }
            if (!proEnabled) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.32f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "自选",
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun BackgroundThumb(
    selected: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                },
            )
            .clickable { onClick() },
    ) {
        content()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000))))
                .padding(horizontal = 6.dp, vertical = 5.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue

