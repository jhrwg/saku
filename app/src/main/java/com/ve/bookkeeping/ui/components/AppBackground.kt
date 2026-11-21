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
 * 背景图和主题色**互相独立**：每张图自带配色，切换背景不会动主题色。
 * [scrim] 是这张图自己的遮罩强度（1f 为标准）——浅色图压重一点、深色图压轻一点，
 * 保证无论用户选了什么主题色，卡片上的字都读得清。
 * 用户自选图片走 `backgroundIndex = -1`。
 */
data class BuiltInBackground(
    val res: Int,
    val name: String,
    val scrim: Float = 1f,
)

val builtInBackgroundList = listOf(
    BuiltInBackground(R.drawable.bg_leaf, "青叶", 0.95f),
    BuiltInBackground(R.drawable.bg_dawn, "晨光", 1.08f),
    BuiltInBackground(R.drawable.bg_night, "夜色", 0.78f),
    BuiltInBackground(R.drawable.bg_rose, "粉霞", 1.05f),
    BuiltInBackground(R.drawable.bg_ember, "赤陶", 1.0f),
    BuiltInBackground(R.drawable.bg_ocean, "海雾", 1.0f),
    BuiltInBackground(R.drawable.bg_mint, "薄荷", 1.05f),
    BuiltInBackground(R.drawable.bg_sand, "沙岩", 1.02f),
)

val builtInBackgrounds = builtInBackgroundList.map { it.res }

val builtInBackgroundNames = builtInBackgroundList.map { it.name }

/** 取某张背景自己的遮罩强度；自选图按标准强度处理。 */
fun builtInBackgroundScrim(index: Int): Float =
    builtInBackgroundList.getOrNull(index)?.scrim ?: 1f

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

        // 每张背景图用自己调好的遮罩强度，和主题色无关
        val scrim = builtInBackgroundScrim(backgroundIndex)
        fun alphaOf(base: Long): Color = Color(base).copy(
            alpha = (Color(base).alpha * scrim).coerceIn(0f, 1f),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (dark) {
                        Brush.verticalGradient(
                            listOf(
                                alphaOf(0xFF000000).copy(alpha = 0.60f * scrim),
                                Color(0x3D000000).copy(alpha = 0.24f * scrim),
                                Color(0x8C000000).copy(alpha = 0.55f * scrim),
                            ),
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x99FFFFFF).copy(alpha = 0.60f * scrim),
                                Color(0x26FFFFFF).copy(alpha = 0.15f * scrim),
                                Color(0x8CFFFFFF).copy(alpha = 0.55f * scrim),
                            ),
                        )
                    },
                ),
        )
    }
}

const val CUSTOM_BACKGROUND_INDEX = -1
