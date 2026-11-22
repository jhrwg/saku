@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.PageScaffold
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.model.AppIcon
import com.ve.bookkeeping.ui.model.IconCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * 图标库（方案 C）：
 * - 内置 80+ 图标，零包体积（全部复用 Materials Rounded），断网也能用；
 * - 「联网图标」从仓库清单拉取，点选后下载 PNG 缓存到本地，不增加包体积。
 */
@Composable
fun IconLibraryPage(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var remote by remember { mutableStateOf<List<AppIcon>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var downloaded by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun loadCache() {
        downloaded = runCatching {
            File(context.filesDir, "icons").listFiles()
                ?.map { it.nameWithoutExtension }
                ?.toSet()
                .orEmpty()
        }.getOrDefault(emptySet())
    }

    LaunchedEffect(Unit) { loadCache() }

    fun fetchRemote() {
        if (loading) return
        loading = true
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { RemoteIcons.fetch() } }
            loading = false
            result.onSuccess { list ->
                remote = list
                message = if (list.isEmpty()) "清单是空的，先在仓库里加图标吧" else "已载入 ${list.size} 个联网图标"
            }.onFailure {
                message = "拉不到清单（检查网络或仓库地址）：${it.message}"
            }
        }
    }

    fun download(icon: AppIcon) {
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { RemoteIcons.download(context, icon) }.getOrDefault(false)
            }
            message = if (ok) {
                loadCache()
                "已下载「${icon.label}」，可离线使用"
            } else {
                "下载失败，稍后再试"
            }
        }
    }

    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) IconCatalog.builtIn
        else IconCatalog.builtIn.filter { it.label.contains(q, true) || it.id.contains(q, true) }
    }

    PageScaffold(
        title = "图标",
        subtitle = "内置 ${IconCatalog.builtIn.size} 个可直接用；联网图标按需下载，不占安装包",
        onBack = onBack,
        scrollable = false,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text(text = "搜索图标（餐饮 / 交通 / 咖啡…）") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "内置图标") {
                IconGrid(
                    icons = filtered,
                    onClick = { icon ->
                        message = "「${icon.label}」已就绪，可在分类编辑里选用"
                    },
                )
                if (filtered.isEmpty()) {
                    Text(
                        text = "没有匹配的图标，换个关键词试试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "联网图标") {
                Text(
                    text = "从仓库清单按需下载，下载后离线可用，不会打进安装包。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { fetchRemote() },
                        enabled = !loading,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (loading) "正在拉取…" else "获取图标清单")
                    }
                    if (remote.isNotEmpty()) {
                        TextButton(onClick = { remote = emptyList(); message = null }) {
                            Text(text = "清空")
                        }
                    }
                }
                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (remote.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    remote.chunked(5).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { icon ->
                                Box(modifier = Modifier.weight(1f)) {
                                    RemoteIconCell(
                                        icon = icon,
                                        cached = icon.id in downloaded,
                                        onClick = { download(icon) },
                                    )
                                }
                            }
                            repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IconGrid(icons: List<AppIcon>, onClick: (AppIcon) -> Unit) {
    icons.chunked(5).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { icon ->
                Box(modifier = Modifier.weight(1f)) {
                    IconCell(icon = icon, onClick = { onClick(icon) })
                }
            }
            repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun IconCell(icon: AppIcon, onClick: () -> Unit) {
    val vector: ImageVector? = icon.vector
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = vector != null) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (vector != null) {
                Icon(
                    imageVector = vector,
                    contentDescription = icon.label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = icon.label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = icon.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun RemoteIconCell(icon: AppIcon, cached: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(icon.id, cached) {
        if (!cached) null else RemoteIcons.load(context, icon)
    }
    Column(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = icon.label,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = icon.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * 联网图标：清单与图片都放在仓库里。
 * 先试 Gitee（国内快），失败再试 GitHub。
 */
object RemoteIcons {

    private const val MANIFEST_PATH = "icons/manifest.json"

    // 清单放在**公开**的 saku 仓库（源码仓库是私有的，匿名读不到 raw）
    private val mirrors = listOf(
        "https://gitee.com/yijiang772/saku/raw/master/",
        "https://raw.githubusercontent.com/jhrwg/saku/main/",
    )

    private var lastBase: String = mirrors.first()

    fun fetch(): List<AppIcon> {
        val body = mirrors.firstNotNullOfOrNull { base ->
            lastBase = base
            runCatching {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                client.newCall(
                    okhttp3.Request.Builder().url(base + MANIFEST_PATH).build(),
                ).execute().use { resp ->
                    if (resp.isSuccessful) resp.body?.string() else null
                }
            }.getOrNull()
        } ?: return emptyList()

        val root = runCatching { JSONObject(body) }.getOrNull() ?: return emptyList()
        val array = root.optJSONArray("icons") ?: return emptyList()
        val list = mutableListOf<AppIcon>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            list += AppIcon(
                id = item.optString("id"),
                label = item.optString("label", item.optString("id")),
                group = item.optString("group", "联网"),
                remoteUrl = (
                    item.optString("url").ifBlank { null }
                        ?: item.optString("path").ifBlank { null }?.let { lastBase + it }
                    ),
            )
        }
        return list
    }

    fun download(context: Context, icon: AppIcon): Boolean {
        val url = icon.remoteUrl ?: return false
        val dir = File(context.filesDir, "icons").apply { mkdirs() }
        val target = File(dir, "${icon.id}.png")
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        client.newCall(okhttp3.Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val bytes = resp.body?.bytes() ?: return false
            target.writeBytes(bytes)
            return true
        }
    }

    fun load(context: Context, icon: AppIcon): androidx.compose.ui.graphics.ImageBitmap? {
        val file = File(File(context.filesDir, "icons"), "${icon.id}.png")
        if (!file.exists()) return null
        return runCatching {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }
}
