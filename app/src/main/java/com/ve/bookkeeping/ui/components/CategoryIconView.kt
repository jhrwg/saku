package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.IconPrefs
import com.ve.bookkeeping.ui.model.IconCatalog
import com.ve.bookkeeping.ui.pages.RemoteIcons

/** 分类图标：优先显示图标库里下载的图片图标，否则用内置矢量图标。 */
@Composable
fun CategoryIconView(
    categoryId: String,
    fallback: ImageVector,
    tint: Color,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val overrides = IconPrefs.categoryIcon(context, categoryId)
    val bitmap = remember(categoryId, IconPrefs.version) {
        val id = overrides ?: return@remember null
        if (IconCatalog.find(id) != null) null else RemoteIcons.load(context, id, 128)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier.size(size),
        )
    } else {
        val vec = overrides?.let { IconCatalog.find(it)?.vector } ?: fallback
        Icon(
            imageVector = vec,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size),
        )
    }
}
