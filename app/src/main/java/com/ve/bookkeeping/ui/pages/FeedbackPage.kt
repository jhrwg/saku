@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.ui.components.LogCenterSheet
import com.ve.bookkeeping.ui.components.PageScaffold
import com.ve.bookkeeping.ui.components.SectionCard

/** 意见反馈问卷地址（腾讯问卷，匿名填写，无需登录）。 */
const val FEEDBACK_FORM_URL = "https://wj.qq.com/s2/28766787/ya5f/"

/**
 * 意见反馈：问卷用系统浏览器打开（问卷里的上传附件、拍照等浏览器原生支持），
 * 日志用「我的 → 日志」同一个弹窗，可一条龙导出 txt 再在问卷里上传。
 */
@Composable
fun FeedbackPage(
    appVersion: String,
    proEnabled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showLogs by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    fun openInBrowser() {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(FEEDBACK_FORM_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    fun copyLink() {
        runCatching {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as? android.content.ClipboardManager
            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("问卷链接", FEEDBACK_FORM_URL))
            copied = true
        }
    }

    PageScaffold(
        title = "意见反馈",
        subtitle = "问卷在浏览器里打开，匿名填写，不需要登录",
        onBack = onBack,
        scrollable = true,
        modifier = modifier,
    ) {
        SectionCard(title = "填写问卷") {
            Text(
                text = "用浏览器打开问卷，上传截图、附件、拍照都能正常用；填完直接提交即可。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { openInBrowser() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "在浏览器中填写问卷")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { copyLink() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (copied) "链接已复制" else "复制问卷链接")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(title = "附带日志") {
            Text(
                text = "遇到闪退、数据不对时，建议先导出日志：点下面的按钮 → 导出 txt 文件 → " +
                    "在问卷的「上传附件」里选这个文件。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showLogs = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "日志 · 导出 txt")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "当前版本 v$appVersion" + if (proEnabled) " · Pro 已开通" else "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLogs) {
        LogCenterSheet(
            onDismiss = { showLogs = false },
            withDeviceInfo = true,
            appVersion = appVersion,
            proEnabled = proEnabled,
        )
    }
}
