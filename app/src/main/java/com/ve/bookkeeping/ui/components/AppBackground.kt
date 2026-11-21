package com.ve.bookkeeping.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ve.bookkeeping.R
import java.io.File

/**
 * 内置背景图（8 张）。
 *
 * [themeId] 是这张图配套的主题色：选中背景时会自动切到这套配色，
 * 免得出现「深色背景 + 浅色主题」看瞎眼的情况。用户仍可再手动改主题色。
 * 用户自选图片走 `backgroundIndex = -1`。
 */
data class BuiltInBackground(
    val res: Int,
    val name: String,
    val themeId: String,
)

val builtInBackgroundList = listOf(
    BuiltInBackground(R.drawable.bg_leaf, "青叶", "green"),
    BuiltInBackground(R.drawable.bg_dawn, "晨光", "orange"),
    BuiltInBackground(R.drawable.bg_night, "夜色", "gray"),
    BuiltInBackground(R.drawable.bg_rose, "粉霞", "pink"),
    BuiltInBackground(R.drawable.bg_ember, "赤陶", "red"),
    BuiltInBackground(R.drawable.bg_ocean, "海雾", "sky"),
    BuiltInBackground(R.drawable.bg_mint, "薄荷", "cyan"),
    BuiltInBackground(R.drawable.bg_sand, "沙岩", "brown"),
)

val builtInBackgrounds = builtInBackgroundList.map { it.res }

val builtInBackgroundNames = builtInBackgroundList.map { it.name }

/** 与 [builtInBackgrounds] 一一对应的推荐主题色 id。 */
val builtInBackgroundThemes = builtInBackgroundList.map { it.themeId }

/**
 * 顶层背景层：图片 + 上下重、中间轻的渐变遮罩。
 *
 * - [backgroundIndex] 为 -1 时使用 [customPath] 指向的本地图片（PRO 自选）；
 * - 遮罩保证任何主题色下文字都能读清；卡片半透明（见 Theme 的 `withTranslucentSurfaces`）。
 */
@Composable
fun AppBackgroundLayer(
    enabled: Boolean,
    backgroundIndex: Int,
    customPath: String?,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    Box(modifier = modifier.fillMaxSize()) {
        if (backgroundIndex == CUSTOM_BACKGROUND_INDEX) {
            val bitmap = remember(customPath) {
                customPath?.let { path ->
                    runCatching { BitmapFactory.decodeFile(File(path).absolutePath) }.getOrNull()
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Image(
                    painter = painterResource(builtInBackgrounds.first()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            Image(
                painter = painterResource(
                    builtInBackgrounds[backgroundIndex.coerceIn(builtInBackgrounds.indices)],
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (dark) {
                        Brush.verticalGradient(
                            listOf(Color(0x99000000), Color(0x3D000000), Color(0x8C000000)),
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0x99FFFFFF), Color(0x26FFFFFF), Color(0x8CFFFFFF)),
                        )
                    },
                ),
        )
    }
}

const val CUSTOM_BACKGROUND_INDEX = -1
