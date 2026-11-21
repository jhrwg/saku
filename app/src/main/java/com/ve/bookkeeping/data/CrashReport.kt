package com.ve.bookkeeping.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 崩溃后的「一键提交日志」能力。
 *
 * 流程：
 * 1. 崩溃时 [AppLog] 已经写好 `logs/crash-*.log`；
 * 2. 下次启动时 [pending] 找出**还没提交过**的崩溃文件；
 * 3. 界面弹出底部弹窗（见 `CrashReportSheet`），用户可以一键上传；
 * 4. [markHandled] 打标记，同一条崩溃不再重复打扰。
 *
 * 上传通道：
 * - 配了 [UPLOAD_TOPIC] 就直接 POST 到 ntfy.sh 的匿名主题（开发者订阅即可收到，零服务器成本）；
 * - 没配或上传失败就退回系统分享面板（微信 / QQ / 邮件 / 网盘任选），始终可用。
 */
object CrashReport {

    /** ntfy.sh 的匿名主题名；留空则只走系统分享。改这里即可开启自动上传。 */
    const val UPLOAD_TOPIC = ""

    private const val MARKER = ".reported"

    /** 还没提交过的崩溃文件（最新一条）。 */
    fun pending(context: Context): File? =
        AppLog.files(context)
            .firstOrNull { it.name.startsWith("crash-") && !marker(it).exists() }

    /** 标记为已处理（提交过或用户点了以后再说）。 */
    fun markHandled(context: Context, file: File) {
        runCatching { marker(file).writeText(now()) }
    }

    /** 弹窗里展示的一行摘要：异常类型 + 首行信息。 */
    fun summary(file: File): String {
        val text = runCatching { file.readText() }.getOrDefault("")
        val line = text.lineSequence().dropWhile { !it.startsWith("---- 堆栈 ----") }.drop(1)
            .firstOrNull { it.isNotBlank() }
            ?: text.lineSequence().firstOrNull { it.contains("Exception") || it.contains("Error") }
            ?: "未知异常"
        return line.trim().take(160)
    }

    fun timeOf(file: File): String =
        SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(file.lastModified()))

    /** 提交用的完整文本：崩溃堆栈 + 环境 + 崩溃前的运行日志尾部。 */
    fun buildText(context: Context, file: File): String = buildString {
        appendLine("—— 折耳根记账 崩溃报告 ——")
        appendLine("App 版本：v${versionName(context)}")
        appendLine("机型：${Build.BRAND} ${Build.MODEL}（API ${Build.VERSION.SDK_INT}）")
        appendLine("系统：Android ${Build.VERSION.RELEASE}")
        appendLine("时间：${timeOf(file)}")
        appendLine()
        appendLine("—— 崩溃日志 ——")
        appendLine(runCatching { file.readText() }.getOrDefault("(读取失败)"))
        runCatching {
            val runtime = AppLog.files(context).firstOrNull { it.name.startsWith("app-") }
            if (runtime != null) {
                appendLine()
                appendLine("—— 运行日志（最近 40 行）——")
                appendLine(runtime.readLines().takeLast(40).joinToString("\n"))
            }
        }
    }

    /** 生成要分享/上传的 txt 文件。 */
    fun exportFile(context: Context, file: File): File? = runCatching {
        val dir = File(context.cacheDir, "crash_reports").apply { mkdirs() }
        val out = File(dir, "vebk-crash-${stamp()}.txt")
        out.writeText(buildText(context, file))
        out
    }.getOrNull()

    /** 系统分享面板（兜底通道，也允许用户自己选发给谁）。 */
    fun share(context: Context, file: File) {
        val report = exportFile(context, file) ?: return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", report)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "折耳根记账 崩溃报告")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "提交崩溃日志"))
    }

    /**
     * 尝试自动上传。返回 true 表示已经送出去了（此时界面不再弹分享面板）。
     * 上传是后台线程，调用方在 IO 线程执行即可。
     */
    fun upload(context: Context, file: File): Boolean {
        if (UPLOAD_TOPIC.isBlank()) return false
        val body = buildText(context, file).take(3500) // ntfy 单条上限 4KB
        return runCatching {
            val conn = (java.net.URL("https://ntfy.sh/$UPLOAD_TOPIC").openConnection()
                as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                setRequestProperty("Title", "vebk crash ${versionName(context)}")
                setRequestProperty("Tags", "boom")
            }
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        }.getOrDefault(false)
    }

    /** 提交（自动上传优先，失败退回分享）。必须在主线程调用（分享需要）。 */
    fun submit(context: Context, file: File) {
        markHandled(context, file)
        if (UPLOAD_TOPIC.isNotBlank()) {
            // 上传走后台，失败再回到主线程弹分享
            Thread {
                if (upload(context, file)) {
                    AppLog.info("崩溃报告", "已上传")
                } else {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        runCatching { share(context, file) }
                    }
                }
            }.start()
        } else {
            share(context, file)
        }
    }

    private fun marker(file: File) = File(file.parentFile, file.name + MARKER)

    private fun versionName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

    private fun stamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date())

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
}

/**
 * 卡顿（ANR）看门狗：主线程超过 [TIMEOUT_MS] 没心跳就记一份日志，
 * 下次启动时和崩溃一样会提示用户提交。
 */
class AnrWatchdog(private val context: Context, private val timeoutMs: Long = 8000) {

    @Volatile
    private var tick = System.nanoTime()

    /** 上次上报时间，防止同一次卡顿反复刷日志。 */
    private var lastReportAt = 0L

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            tick = System.nanoTime()
            handler.postDelayed(this, 1000)
        }
    }

    fun start() {
        handler.post(ticker)
        Thread {
            while (true) {
                Thread.sleep(timeoutMs / 2)
                val last = tick
                val gap = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - last)
                val nowMs = System.currentTimeMillis()
                // 只有「心跳真的停了」才算卡死；5 分钟内只报一次，避免刷屏
                if (gap in (timeoutMs + 1)..(timeoutMs * 6) && nowMs - lastReportAt > 300_000) {
                    lastReportAt = nowMs
                    val trace = android.os.Looper.getMainLooper().thread.stackTrace
                        .joinToString("\n") { "  at $it" }
                    AppLog.warn("卡顿", "主线程阻塞 ${gap}ms\n$trace")
                    // 顺便写一条崩溃文件，复用「启动后提示提交」的通道
                    AppLog.crash(
                        context,
                        RuntimeException("主线程阻塞 ${gap}ms"),
                        "ANR 看门狗",
                    )
                    tick = System.nanoTime()
                }
            }
        }.apply { isDaemon = true; name = "anr-watchdog" }.start()
    }
}
