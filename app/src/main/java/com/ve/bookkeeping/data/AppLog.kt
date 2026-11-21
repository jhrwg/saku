package com.ve.bookkeeping.data

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 日志分类：全部 / 崩溃 / 运行过程。 */
enum class LogFilter(val label: String) {
    All("全部"),
    Crash("崩溃"),
    Runtime("运行"),
}

/**
 * 全量日志：运行过程（记账、删除、备份、更新检查…）+ 崩溃堆栈都落盘，
 * 方便用户导出 txt 发给开发者排查。
 *
 * - 运行日志：`logs/app-yyyyMMdd.log`，按天分文件，保留最近 [MAX_FILES] 份；
 * - 崩溃日志：`logs/crash-yyyyMMdd-HHmmss.log`，一次崩溃一个文件。
 *
 * 兼容旧版本：`crash/` 目录下的旧日志也会一起读出来、一起清空。
 */
object AppLog {

    private const val DIR_NAME = "logs"
    private const val LEGACY_CRASH_DIR = "crash"
    private const val MAX_FILES = 14
    private const val MAX_TEXT_CHARS = 60_000

    @Volatile
    private var installed = false

    @Volatile
    private var version = "unknown"

    @Volatile
    private var appContext: Context? = null

    /** 在第一帧之前调用：读版本号 + 挂崩溃钩子。 */
    fun install(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        version = runCatching {
            val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            "${info.versionName} (${info.longVersionCode})"
        }.getOrDefault("unknown")
        if (installed) return
        installed = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { crash(ctx, throwable) }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun info(tag: String, message: String) = write("INFO", tag, message)

    fun warn(tag: String, message: String) = write("WARN", tag, message)

    fun error(tag: String, message: String) = write("ERROR", tag, message)

    /** 手动记一次崩溃（已捕获的异常也能记）。 */
    fun crash(context: Context, throwable: Throwable, extra: String? = null) {
        val ctx = context.applicationContext
        val dir = File(ctx.filesDir, DIR_NAME).apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date())
        val file = File(dir, "crash-$stamp.log")
        val writer = StringWriter()
        PrintWriter(writer).use { out ->
            out.println("时间: ${now()}")
            out.println("版本: $version")
            out.println("设备: ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})")
            out.println("线程: ${Thread.currentThread().name}")
            if (!extra.isNullOrBlank()) out.println("上下文: $extra")
            out.println("---- 堆栈 ----")
            throwable.printStackTrace(out)
        }
        runCatching { file.writeText(writer.toString()) }
        prune(ctx)
    }

    /** 全部日志文件，新的在前。 */
    fun files(context: Context): List<File> {
        val ctx = context.applicationContext
        val main = File(ctx.filesDir, DIR_NAME)
        val legacy = File(ctx.filesDir, LEGACY_CRASH_DIR)
        return (listOf(main, legacy)).flatMap { dir ->
            dir.listFiles { f -> f.isFile && f.name.endsWith(".log") }?.toList().orEmpty()
        }.sortedByDescending { it.lastModified() }
    }

    /** 按筛选条件拼出要展示/导出的文本。 */
    fun text(context: Context, filter: LogFilter): String {
        val picked = files(context).filter { file ->
            when (filter) {
                LogFilter.All -> true
                LogFilter.Crash -> file.name.startsWith("crash-")
                LogFilter.Runtime -> file.name.startsWith("app-")
            }
        }
        if (picked.isEmpty()) return ""
        val sb = StringBuilder()
        for (file in picked) {
            val body = runCatching { file.readText() }.getOrNull() ?: continue
            sb.append("===== ").append(file.name).append(" =====\n")
            // 单个文件太长只留尾部，整体再截断，避免弹窗卡死
            val text = if (body.length > 20_000) "…（前段省略）…\n" + body.takeLast(20_000) else body
            sb.append(text).append("\n")
            if (sb.length > MAX_TEXT_CHARS) {
                sb.append("…（日志过长，已截断，导出文件含完整内容）\n")
                break
            }
        }
        return sb.toString()
    }

    fun count(context: Context, filter: LogFilter): Int = files(context).count { file ->
        when (filter) {
            LogFilter.All -> true
            LogFilter.Crash -> file.name.startsWith("crash-")
            LogFilter.Runtime -> file.name.startsWith("app-")
        }
    }

    fun clear(context: Context) {
        files(context).forEach { runCatching { it.delete() } }
    }

    private fun write(level: String, tag: String, message: String) {
        val ctx = appContext ?: return
        runCatching {
            val dir = File(ctx.filesDir, DIR_NAME).apply { mkdirs() }
            val day = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
            val file = File(dir, "app-$day.log")
            val line = "${now()} [$level] [$tag] $message\n"
            // 超过 512KB 就不再追加，避免日志吃满存储
            if (file.exists() && file.length() > 512 * 1024) return@runCatching
            file.appendText(line)
            prune(ctx)
        }
    }

    private fun prune(context: Context) {
        val dir = File(context.filesDir, DIR_NAME)
        val all = dir.listFiles { f -> f.isFile && f.name.endsWith(".log") }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
        all.drop(MAX_FILES).forEach { runCatching { it.delete() } }
    }

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
}
