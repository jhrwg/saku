package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.IconPrefs
import com.ve.bookkeeping.ui.pages.RemoteIcons

/**
 * 账户图标：优先显示用户下载的银行/支付 logo（图片），否则用内置矢量图标。
 *
 * 银行 logo 是多色的，不能 tint，所以这里单独渲染成图片。
 */
@Composable
fun AccountIconView(
    accountId: String,
    fallback: ImageVector,
    tint: Color,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
    showBackground: Boolean = false,
    backgroundTint: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    val context = LocalContext.current
    // 读一下版本号，换图标后立即重组
    val version = IconPrefs.version
    val bitmap = remember(accountId, version) {
        IconPrefs.accountIcon(context, accountId)?.let { RemoteIcons.load(context, it, 128) }
    }

    Box(
        modifier = if (showBackground) {
            modifier
                .size(size + 16.dp)
                .background(backgroundTint, RoundedCornerShape(12.dp))
        } else {
            modifier.size(size)
        },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(size),
            )
        } else {
            Icon(
                imageVector = fallback,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size),
            )
        }
    }
}
