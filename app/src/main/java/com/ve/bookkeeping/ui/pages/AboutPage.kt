@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.SectionCard
import androidx.compose.foundation.layout.statusBarsPadding

/** 关于：版本、隐私说明、开源许可。 */
@Composable
fun AboutPage(
    appVersion: String,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "关于",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "折耳根记账 v$appVersion",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "隐私") {
            Text(
                text = "所有账目只存在这台设备上，不上传服务器；" +
                    "WebDAV 备份是你自己的网盘，凭证也只存本机。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "查看完整隐私说明")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "开源许可") {
            Text(
                text = "本应用使用了 Jetpack Compose、OkHttp、AndroidX WorkManager 等开源库，" +
                    "均按 Apache-2.0 许可使用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenLicenses,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "查看许可清单")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "数据与备份") {
            Text(
                text = "账目保存在应用私有目录，卸载应用会一并删除；" +
                    "建议定期做 WebDAV 备份或导出 CSV。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/** 隐私说明全文。 */
@Composable
fun PrivacyPage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "隐私说明",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(10.dp))
        listOf(
            "1. 账目、账户、预算等数据只保存在本机应用私有目录，不上传任何服务器。",
            "2. 本应用不收集设备标识、位置、通讯录等个人信息，也不含统计 SDK。",
            "3. WebDAV 备份使用你自己填写的网盘地址与授权码，凭证仅存本机，备份内容也只发往你的网盘。",
            "4. 检查更新只读取公开的版本描述文件，不携带任何用户数据。",
            "5. 崩溃日志仅保存在本机，只有你主动分享时才会离开设备。",
            "6. 卸载应用会删除本机全部数据，请提前导出或备份。",
        ).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

/** 开源许可清单。 */
@Composable
fun LicensesPage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "开源许可",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(10.dp))
        listOf(
            "AndroidX Core / Activity / Lifecycle — Apache-2.0",
            "Jetpack Compose（UI / Material3）— Apache-2.0",
            "AndroidX WorkManager — Apache-2.0",
            "AndroidX Biometric — Apache-2.0",
            "OkHttp — Apache-2.0",
            "Kotlin / Kotlin Coroutines — Apache-2.0",
            "AndroidSVG（com.caverock:androidsvg）— Apache-2.0",
            "",
            "图标来源",
            "Material Symbols / Material Icons（Google）— Apache-2.0",
            "Material Symbols Rounded 变量字体用于渲染「联网图标」",
            "银行 / 支付 logo：icongo/bank-logos（第三方整理）",
            "以上银行与支付标识的商标权归各机构所有，App 内仅用于账户识别，不代表任何关联或背书",
            "内置分类图标来自 Material Icons（随 compose material-icons-extended 提供）",
        ).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

