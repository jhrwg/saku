@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.IconPrefs
import com.ve.bookkeeping.ui.components.AppSheet
import com.ve.bookkeeping.ui.components.PageScaffold
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.model.AppIcon
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.IconCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * 图标库（方案 C）：
 * - 内置图标零包体积，断网可用；
 * - 联网图标从公开仓库按需下载，支持单选 / 多选批量下载；
 * - 下载中在图标上盖一层半透明遮罩 + 圆形进度，下载完自动显示图标；
 * - 选中图标可直接设为某个分类的图标。
 */
@Composable
fun IconLibraryPage(
    categories: List<EntryCategory>,
    accounts: List<com.ve.bookkeeping.ui.model.AccountOption> = emptyList(),
    onBack: () -> Unit,
    onIconChanged: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var remote by remember { mutableStateOf<List<AppIcon>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var cached by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 本地缓存里的图标（含 id 与中文名），不需要联网就能列出来
    var localExtra by remember { mutableStateOf<List<AppIcon>>(emptyList()) }
    var selecting by remember { mutableStateOf(false) }
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    val progress = remember { mutableStateMapOf<String, Float>() }
    var assignTarget by remember { mutableStateOf<AppIcon?>(null) }
    var accountTarget by remember { mutableStateOf<AppIcon?>(null) }

    fun refreshCache() {
        val files = RemoteIcons.cachedFiles(context)
        val ids = files.map { it.nameWithoutExtension }.toSet()
        cached = ids
        // 有清单就用清单里的中文名，没有就退回 id
        // 内置 + 已下载统统进「本地图标」，名字用缓存清单里的中文
        localExtra = RemoteIcons.localIcons(context)
            .filter { it.id !in IconCatalog.builtIn.map { b -> b.id } }
            .sortedBy { it.label }
    }

    LaunchedEffect(Unit) { refreshCache() }
    BackHandler(onBack = onBack)

    fun fetchRemote() {
        if (loading) return
        loading = true
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { RemoteIcons.fetch() } }
            loading = false
            result.onSuccess { list ->
                remote = list
                runCatching { RemoteIcons.saveCached(context, list) }
                refreshCache()
                val pending = list.count { it.id !in cached }
                message = when {
                    list.isEmpty() -> "清单是空的，先在仓库里加图标吧"
                    pending == 0 -> "已是最新，全部图标都在本地了"
                    else -> "有 $pending 个图标可下载"
                }
            }.onFailure { message = "拉不到清单：${it.message}" }
        }
    }

    fun downloadOne(icon: AppIcon, onDone: (() -> Unit)? = null) {
        if (progress.containsKey(icon.id)) return
        progress[icon.id] = 0.01f
        scope.launch {
            withContext(Dispatchers.IO) {
                runCatching { RemoteIcons.download(context, icon, onProgress = { p -> progress[icon.id] = p }) }
            }
            progress.remove(icon.id)
            refreshCache()
            onDone?.invoke()
        }
    }

    fun downloadSelected() {
        val targets = remote.filter { selected[it.id] == true && it.id !in cached }
        if (targets.isEmpty()) return
        message = "正在下载 ${targets.size} 个图标…"
        targets.forEach { icon -> downloadOne(icon) }
        selected.clear()
        selecting = false
    }

    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) IconCatalog.builtIn
        else IconCatalog.builtIn.filter { it.label.contains(q, true) || it.id.contains(q, true) }
    }

    PageScaffold(
        title = "图标",
        subtitle = "内置 ${IconCatalog.builtIn.size} 个；联网图标按需下载，不占安装包",
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
            SectionCard(title = "本地图标（内置 ${IconCatalog.builtIn.size} + 已下载）") {
                Text(
                    text = "点一下图标 → 选一个分类，立刻换上；下载过的图标也会出现在这里",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconGrid(icons = filtered, onClick = { assignTarget = it })
                if (localExtra.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    localExtra.chunked(5).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { icon ->
                                Box(modifier = Modifier.weight(1f)) {
                                    LocalImageCell(icon = icon, onClick = { assignTarget = icon })
                                }
                            }
                            repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
                if (filtered.isEmpty()) {
                    Text(
                        text = "没有匹配的图标，换个关键词试试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 银行 / 支付 logo：只能用在账户上
            val bankIcons = localExtra.filter { it.scope == "account" }
            if (bankIcons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard(title = "账户图标（银行 / 支付 ${bankIcons.size} 个）") {
                    Text(
                        text = "这些 logo 只能用在账户上：点一下 → 选一个账户换上。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    bankIcons.chunked(5).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { icon ->
                                Box(modifier = Modifier.weight(1f)) {
                                    LocalImageCell(icon = icon, onClick = { accountTarget = icon })
                                }
                            }
                            repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(title = "联网图标") {
                Text(
                    text = "下载后离线可用，不会打进安装包。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                        TextButton(onClick = {
                            selecting = !selecting
                            if (!selecting) selected.clear()
                        }) {
                            Text(text = if (selecting) "取消多选" else "多选")
                        }
                    }
                }

                if (selecting) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "已选 ${selected.count { it.value }} 个",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                val pending = remote.filter { it.id !in cached }
                                val allSelected = pending.isNotEmpty() && pending.all { selected[it.id] == true }
                                pending.forEach { selected[it.id] = !allSelected }
                            },
                        ) { Text(text = "全选未下载") }
                        TextButton(onClick = { downloadSelected() }) { Text(text = "下载选中") }
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
                                        cached = icon.id in cached,
                                        progress = progress[icon.id],
                                        selecting = selecting,
                                        checked = selected[icon.id] == true,
                                        onClick = {
                                            if (selecting) {
                                                selected[icon.id] = !(selected[icon.id] ?: false)
                                            } else if (icon.id !in cached) {
                                                downloadOne(icon)
                                            } else {
                                                assignTarget = icon.copy(remoteUrl = null)
                                            }
                                        },
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

    accountTarget?.let { icon ->
        AccountAssignSheet(
            icon = icon,
            accounts = accounts,
            onDismiss = { accountTarget = null },
            onPick = { account ->
                IconPrefs.setAccountIcon(context, account.id, icon.id)
                accountTarget = null
                message = "「${account.name}」的图标已换成「${icon.label}」"
            },
        )
    }

    assignTarget?.let { icon ->
        IconAssignSheet(
            icon = icon,
            categories = categories,
            onDismiss = { assignTarget = null },
            onPick = { category ->
                IconPrefs.setCategoryIcon(context, category.id, icon.id)
                assignTarget = null
                message = "「${category.label}」的图标已换成「${icon.label}」（重开应用生效）"
            },
        )
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
        modifier = Modifier.fillMaxWidth().clickable(enabled = vector != null) { onClick() },
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
private fun RemoteIconCell(
    icon: AppIcon,
    cached: Boolean,
    progress: Float?,
    selecting: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap = remember(icon.id, cached) {
        if (!cached) null else RemoteIcons.load(context, icon.id)
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

            // 下载中：半透明遮罩 + 圆形进度
            if (progress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                    )
                }
            }

            if (selecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (checked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            } else {
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)
                            },
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Text(
                        text = if (checked) "✓" else "",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(2.dp),
                    )
                }
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

/** 选一个分类套用这个图标。 */
@Composable
private fun IconAssignSheet(
    icon: AppIcon,
    categories: List<EntryCategory>,
    onDismiss: () -> Unit,
    onPick: (EntryCategory) -> Unit,
) {
    AppSheet(onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon.vector?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "把「${icon.label}」用到哪个分类？",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        categories.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { category ->
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { onPick(category) }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * 联网图标：清单与图片都放在**公开**的 saku 仓库。
 */
object RemoteIcons {

    private const val MANIFEST_PATH = "icons/manifest.json"

    private val mirrors = listOf(
        "https://gitee.com/yijiang772/saku/raw/master/",
        "https://raw.githubusercontent.com/jhrwg/saku/main/",
    )

    private var lastBase: String = mirrors.first()

    private const val CACHE_NAME = "manifest.json"

    /** 把清单缓存到本地：断网也能拿到中文名，不再出现 net_ 这种 id。 */
    private fun cacheManifest(context: Context, raw: String) {
        runCatching { File(File(context.filesDir, "icons").apply { mkdirs() }, CACHE_NAME).writeText(raw) }
    }

    fun saveCached(context: Context, list: List<AppIcon>) {
        val arr = org.json.JSONArray()
        list.forEach { icon ->
            arr.put(
                JSONObject()
                    .put("id", icon.id)
                    .put("label", icon.label)
                    .put("group", icon.group)
                    .put("scope", icon.scope)
                    .put("path", icon.remoteUrl ?: ""),
            )
        }
        cacheManifest(context, JSONObject().put("version", 6).put("icons", arr).toString())
    }

    fun cachedManifest(context: Context): org.json.JSONObject? = runCatching {
        val f = File(File(context.filesDir, "icons"), CACHE_NAME)
        if (!f.exists()) null else org.json.JSONObject(f.readText())
    }.getOrNull()

    /** id → 中文名（来自缓存清单）。 */
    fun labelOf(context: Context, id: String): String? = runCatching {
        val arr = cachedManifest(context)?.optJSONArray("icons") ?: return null
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            if (item.optString("id") == id) return item.optString("label", id)
        }
        null
    }.getOrNull()

    /**
     * 本地图标（内置 + 已下载），下载过的带中文名。
     * 供图标页、分类编辑、账户图标选择共用。
     */
    fun localIcons(context: Context, scope: String? = null): List<AppIcon> {
        val out = mutableListOf<AppIcon>()
        out += com.ve.bookkeeping.ui.model.IconCatalog.builtIn
        cachedFiles(context).forEach { f ->
            val id = f.nameWithoutExtension
            val isBank = id.startsWith("bank_")
            val iconScope = if (isBank) "account" else "category"
            if (scope != null && scope != iconScope) return@forEach
            out += AppIcon(
                id = id,
                label = labelOf(context, id) ?: fallbackLabel(id),
                group = if (isBank) "银行 / 支付" else "已下载",
                scope = iconScope,
            )
        }
        return out
    }

    private fun fallbackLabel(id: String): String = when {
        id.startsWith("bank_") -> "银行图标"
        id.startsWith("net_") -> "已下载图标"
        else -> id
    }

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
        // 缓存清单（这里没有 context，交给调用方保存；见 rememberManifest）
        val array = root.optJSONArray("icons") ?: return emptyList()
        val list = mutableListOf<AppIcon>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            list += AppIcon(
                id = item.optString("id"),
                label = item.optString("label", item.optString("id")),
                group = item.optString("group", "其他"),
                scope = item.optString("scope", "category"),
                remoteUrl = item.optString("url").ifBlank { null }
                    ?: item.optString("path").ifBlank { null }?.let { lastBase + it },
            )
        }
        return list
    }

    /** 本地已缓存的图标文件（png 或 svg），不需要联网就能列出来。 */
    fun cachedFiles(context: Context): List<File> =
        File(context.filesDir, "icons").listFiles()
            ?.filter { it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".svg")) }
            ?.sortedBy { it.name }
            .orEmpty()

    fun isCached(context: Context, iconId: String): Boolean =
        File(File(context.filesDir, "icons"), "$iconId.png").exists() ||
            File(File(context.filesDir, "icons"), "$iconId.svg").exists()

    fun download(context: Context, icon: AppIcon, onProgress: (Float) -> Unit = {}): Boolean {
        val url = icon.remoteUrl ?: return false
        val ext = if (url.substringBefore('?').endsWith(".svg", true)) "svg" else "png"
        val dir = File(context.filesDir, "icons").apply { mkdirs() }
        val target = File(dir, "${icon.id}.$ext")
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        client.newCall(okhttp3.Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val body = resp.body ?: return false
            val total = body.contentLength().coerceAtLeast(0L)
            val bytes = body.byteStream().use { input ->
                val out = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var read: Int
                var received = 0L
                while (input.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                    received += read
                    if (total > 0) onProgress((received.toFloat() / total).coerceIn(0.02f, 1f))
                }
                out.toByteArray()
            }
            target.writeBytes(bytes)
            return true
        }
    }

    /** 读本地图标：png 直接解码，svg 用 androidsvg 渲染。 */
    fun load(context: Context, iconId: String, sizePx: Int = 96): androidx.compose.ui.graphics.ImageBitmap? {
        val dir = File(context.filesDir, "icons")
        val png = File(dir, "$iconId.png")
        if (png.exists()) {
            return runCatching {
                android.graphics.BitmapFactory.decodeFile(png.absolutePath)?.asImageBitmap()
            }.getOrNull()
        }
        val svg = File(dir, "$iconId.svg")
        if (svg.exists()) {
            return runCatching {
                val picture = com.caverock.androidsvg.SVG.getFromInputStream(svg.inputStream())
                picture.documentWidth = sizePx.toFloat()
                picture.documentHeight = sizePx.toFloat()
                val bitmap = android.graphics.Bitmap.createBitmap(
                    sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888,
                )
                val canvas = android.graphics.Canvas(bitmap)
                picture.renderToCanvas(canvas)
                bitmap.asImageBitmap()
            }.getOrNull()
        }
        return null
    }
}

/** 已下载到本地的图标（png/svg 可能是银行 logo，只在账户上用）。 */
@Composable
private fun LocalImageCell(icon: AppIcon, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(icon.id) { RemoteIcons.load(context, icon.id) }
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
                    modifier = Modifier.size(24.dp),
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

/** 选一个账户套用这个图标。 */
@Composable
private fun AccountAssignSheet(
    icon: AppIcon,
    accounts: List<com.ve.bookkeeping.ui.model.AccountOption>,
    onDismiss: () -> Unit,
    onPick: (com.ve.bookkeeping.ui.model.AccountOption) -> Unit,
) {
    val context = LocalContext.current
    val bitmap = remember(icon.id) { RemoteIcons.load(context, icon.id, 128) }
    AppSheet(onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "把「${icon.label}」用到哪个账户？",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        accounts.forEach { account ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(account) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = account.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = account.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
