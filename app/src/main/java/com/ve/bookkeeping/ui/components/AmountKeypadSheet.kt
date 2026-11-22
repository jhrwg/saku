@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode

private val keypadKeys = listOf(
    "1", "2", "3",
    "4", "5", "6",
    "7", "8", "9",
    ".", "0", "⌫",
)

/** 把已有金额转成可继续编辑的字符串：整数不带小数点，小数保留最多两位。 */
fun editableAmount(value: Double): String {
    if (value <= 0.0) return "0"
    val stripped = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
    return stripped.toPlainString()
}

/** 数字键盘的一次按键处理：返回新文本，非法（超长）时返回 null。 */
fun keypadPress(current: String, key: String): String? {
    val next = when {
        key == "⌫" -> if (current.length <= 1) "0" else current.dropLast(1)
        key == "." -> if (current.contains(".")) current else "$current."
        current == "0" -> key
        else -> current + key
    }
    val intPart = next.substringBefore(".")
    val decimalPart = next.substringAfter(".", "")
    // 整数最多 9 位、小数最多 2 位，超出直接忽略
    if (intPart.length > 9 || decimalPart.length > 2) return null
    return next
}

/**
 * 记账页内嵌数字键盘：按键直接改金额，不再弹面板，输入「跟手」。
 *
 * [amountText] 只用于顶部提示当前输入；真正的金额由调用方在 [onKey] 里同步更新。
 */
@Composable
fun InlineAmountKeypad(
    amountText: String,
    onKey: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 46.dp,
    columns: Int = 3,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp),
    ) {
        keypadKeys.chunked(columns).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowKeys.forEach { key ->
                    KeypadKey(
                        label = key,
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        onClick = {
                            haptic.performHapticFeedback(
                                androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove,
                            )
                            onKey(key)
                        },
                    )
                }
                repeat(columns - rowKeys.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "¥ $amountText",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDone) { Text(text = "收起") }
        }
    }
}

/**
 * 金额输入面板：数字显示框 + 3×4 数字键盘 + 右下角「完成」。
 *
 * 首页「+」新建记账、记账页改金额、改手续费都复用它：
 * [title] 面板标题，[subtitle] 右上角备注（账本名 / 类型），[initialAmount] 初始金额。
 */
@Composable
fun AmountKeypadSheet(
    title: String,
    subtitle: String?,
    initialAmount: Double = 0.0,
    onDismiss: () -> Unit,
    onDone: (Double) -> Unit,
) {
    var amountText by remember { mutableStateOf(editableAmount(initialAmount)) }
    // 按键触感反馈：点击立刻"跟手"
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 横屏等矮屏：键矮一点 + 内容可滚动，保证「完成」能点到
    val shortScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 520
    val keyHeight = if (shortScreen) 38.dp else 54.dp

    fun press(key: String) {
        val next = keypadPress(amountText, key) ?: return
        amountText = next
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
    }

    // 记账键盘要完整展示（数字键 + 分类 + 账户），不受半屏限制
    AppSheet(onDismiss = onDismiss, heightFraction = 1f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "¥",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            keypadKeys.chunked(3).forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowKeys.forEach { key ->
                        KeypadKey(
                            label = key,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { press(key) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { amountText = "0" }) {
                    Text(text = "清空")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onDone(amount) },
                    enabled = amount > 0,
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 128.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = "完成")
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "⌫") {
                Icon(
                    imageVector = Icons.Rounded.Backspace,
                    contentDescription = "删除",
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
