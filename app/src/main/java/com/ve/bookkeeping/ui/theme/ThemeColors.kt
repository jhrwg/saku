package com.ve.bookkeeping.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 一套单色主题：一个主色 + 一个容器色，其余中性色由 [Theme.kt] 从主色派生。
 *
 * 预设 8 套 + 1 个 PRO 自选色（`id = "custom"`，颜色由用户选择后存在本地）。
 */
data class ThemePalette(
    val id: String,
    val label: String,
    val primary: Color,
    val container: Color,
) {
    val isCustom: Boolean get() = id == ID_CUSTOM

    companion object {
        const val ID_DEFAULT = "default"
        const val ID_CUSTOM = "custom"

        /** 默认主题：白底黑字（软件整体风格为白色）。 */
        val Default = ThemePalette(ID_DEFAULT, "默认", Color(0xFF1C1C1E), Color(0xFFE9E9EA))

        val presets: List<ThemePalette> = listOf(
            Default,
            ThemePalette("red", "红色", Color(0xFFB3261E), Color(0xFFF6D9D5)),
            ThemePalette("pink", "粉色", Color(0xFFC2185B), Color(0xFFFAD3E2)),
            ThemePalette("cyan", "青色", Color(0xFF00695C), Color(0xFFC1EBE4)),
            ThemePalette("green", "青绿", Color(0xFF2E7D57), Color(0xFFCDEBD9)),
            ThemePalette("sky", "天蓝色", Color(0xFF1565C0), Color(0xFFD3E4FA)),
            ThemePalette("gray", "灰色", Color(0xFF546E7A), Color(0xFFDCE4E8)),
            ThemePalette("brown", "棕色", Color(0xFF795548), Color(0xFFEADDD6)),
            ThemePalette("orange", "橙色", Color(0xFFE65100), Color(0xFFFADFC9)),
        )

        /** 根据保存的 id（可能是 custom）+ 自选色，解析出当前配色。 */
        fun resolve(id: String, customArgb: Int): ThemePalette {
            if (id == ID_CUSTOM) {
                val color = Color(customArgb)
                return ThemePalette(
                    id = ID_CUSTOM,
                    label = "自选",
                    primary = color,
                    container = blend(color, Color.White, 0.78f),
                )
            }
            return presets.firstOrNull { it.id == id } ?: Default
        }
    }
}

/** 两个颜色按比例混合（ratio 是 other 的占比）。 */
fun blend(base: Color, other: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = base.red + (other.red - base.red) * r,
        green = base.green + (other.green - base.green) * r,
        blue = base.blue + (other.blue - base.blue) * r,
        alpha = base.alpha + (other.alpha - base.alpha) * r,
    )
}

fun Color.lighten(ratio: Float): Color = blend(this, Color.White, ratio)

fun Color.darken(ratio: Float): Color = blend(this, Color.Black, ratio)
