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

    /** ntfy 主题名（相当于收件箱）；留空则只走系统分享。 */
    const val NTFY_TOPIC = "vebk-crash-7f3a91"

    /** ntfy 服务器：默认官方；自建（国内 VPS）时改成自己的地址。 */
    const val NTFY_SERVER = "https://ntfy.sh"

    /**
     * 可选：PushPlus token（国内可直连，微信收推送）。
     * 注意：PushPlus 要求账号实名认证后才能发消息，未实名会返回 code 905。
     */
    const val PUSHPLUS_TOKEN = "9fd85dd18d7641ee906d83efcca64ae2"

    /**
     * 可选：企业微信群机器人 webhook 的 key（消息里的 `key=` 后面那串）。
     * 国内直连最稳、不需要实名认证，推荐用这个。
     */
    const val WECOM_KEY = ""

    private const val MARKER = ".reported"

    /** 还没提交过的崩溃文件（最新一条）。 */
    fun pending(context: Context): File? =
        AppLog.files(context)
            .filter { it.name.startsWith("crash-") && !marker(it).exists() }
            // 旧版本看门狗误报的「主线程阻塞」不算崩溃，不再打扰用户
            .firstOrNull { file ->
                runCatching { !file.readText().contains("ANR 看门狗") }.getOrDefault(true)
            }

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
     * 上传日志：先试 ntfy，失败再试 PushPlus（国内直连）。
     * 返回 true 表示已经送出去；false 时调用方退回系统分享。
     * 这是阻塞调用，必须在 IO 线程执行。
     */
    fun upload(context: Context, file: File): Boolean {
        val body = buildText(context, file)
        if (WECOM_KEY.isNotBlank() && sendWeCom(body.take(2000))) return true
        if (PUSHPLUS_TOKEN.isNotBlank() && sendPushPlus(context, body.take(18000))) return true
        if (NTFY_TOPIC.isNotBlank() && sendNtfy(context, body.take(3500))) return true
        return false
    }

    /** 企业微信群机器人：国内直连、无需实名。文本上限约 2048 字节。 */
    private fun sendWeCom(body: String): Boolean = runCatching {
        val payload = org.json.JSONObject()
            .put("msgtype", "text")
            .put("text", org.json.JSONObject().put("content", body))
            .toString()
        val conn = (java.net.URL(
            "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=$WECOM_KEY",
        ).openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 6000
            readTimeout = 8000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        conn.outputStream.use { it.write(payload.toByteArray()) }
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        text.contains("\"errcode\":0") || text.contains("\"errcode\": 0")
    }.getOrDefault(false)

    /** ntfy：POST 纯文本到主题，手机装 ntfy 订阅该主题即可收到。 */
    private fun sendNtfy(context: Context, body: String): Boolean = runCatching {
        val conn = (java.net.URL("$NTFY_SERVER/$NTFY_TOPIC").openConnection()
            as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 6000
            readTimeout = 6000
            doOutput = true
            setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            setRequestProperty("Title", "vebk 崩溃 ${versionName(context)}")
            setRequestProperty("Tags", "boom")
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        conn.disconnect()
        code in 200..299
    }.getOrDefault(false)

    /** PushPlus：国内可直连，token 在 pushplus.plus 微信扫码获取。 */
    private fun sendPushPlus(context: Context, body: String): Boolean = runCatching {
        val payload = org.json.JSONObject()
            .put("token", PUSHPLUS_TOKEN)
            .put("title", "vebk 崩溃 ${versionName(context)}")
            .put("content", body)
            .put("template", "txt")
            .toString()
        val conn = (java.net.URL("https://www.pushplus.plus/send").openConnection()
            as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 6000
            readTimeout = 8000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        conn.outputStream.use { it.write(payload.toByteArray()) }
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        text.contains("\"code\":200") || text.contains("\"code\": 200")
    }.getOrDefault(false)

    /**
     * 提交：自动上传优先；全部通道失效时回调 [onFallback]（进入反馈页走问卷）。
     * 主线程调用。
     */
    fun submit(context: Context, file: File, onFallback: () -> Unit = {}) {
        markHandled(context, file)
        val main = android.os.Handler(android.os.Looper.getMainLooper())
        fun toast(msg: String) = main.post {
            runCatching {
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        if (NTFY_TOPIC.isBlank() && PUSHPLUS_TOKEN.isBlank() && WECOM_KEY.isBlank()) {
            onFallback()
            return
        }
        toast("正在上传日志…")
        Thread {
            val ok = upload(context, file)
            if (ok) {
                AppLog.info("崩溃报告", "已上传（ntfy/pushplus）")
                toast("日志已提交，谢谢！")
            } else {
                AppLog.warn("崩溃报告", "上传失败，转为问卷反馈")
                toast("自动上传失败，已打开意见反馈")
                main.post(onFallback)
            }
        }.start()
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
 * 主线程卡顿看门狗（保守版）。
 *
 * 上一版误报：App 在后台/息屏时主线程本来就停在 `nativePollOnce`（空闲等待消息），
 * 却被当成了「卡死」。现在只在下面三件事同时成立时才记录：
 * 1. 心跳间隔超过阈值；
 * 2. 主线程栈里**没有** `nativePollOnce`（即不是空闲等待）；
 * 3. 连续两次采样都是这样。
 *
 * 另外卡顿只记 WARN 日志，不再生成崩溃文件 —— 避免下次启动弹「崩溃报告」打扰用户。
 */
class AnrWatchdog(private val context: Context, private val timeoutMs: Long = 6000) {

    private val mainThread = android.os.Looper.getMainLooper().thread

    @Volatile
    private var tick = android.os.SystemClock.elapsedRealtime()

    private var strikes = 0

    private var lastReportAt = 0L

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            tick = android.os.SystemClock.elapsedRealtime()
            handler.postDelayed(this, 1000)
        }
    }

    fun start() {
        handler.post(ticker)
        Thread {
            while (true) {
                Thread.sleep(timeoutMs / 2)
                val gap = android.os.SystemClock.elapsedRealtime() - tick
                if (gap <= timeoutMs) {
                    strikes = 0
                    continue
                }
                val stack = runCatching { mainThread.stackTrace }.getOrDefault(emptyArray())
                // 空闲等待消息（nativePollOnce）说明没卡，只是没消息可处理
                val idle = stack.any { it.methodName.contains("nativePollOnce") }
                if (idle || stack.isEmpty()) {
                    strikes = 0
                    tick = android.os.SystemClock.elapsedRealtime()
                    continue
                }
                strikes++
                if (strikes < 2) continue
                val now = System.currentTimeMillis()
                if (now - lastReportAt < 300_000) continue
                lastReportAt = now
                strikes = 0
                val trace = stack.joinToString("\n") { "  at $it" }
                AppLog.warn("卡顿", "主线程忙了 ${gap}ms（未生成崩溃报告）\n$trace")
                tick = android.os.SystemClock.elapsedRealtime()
            }
        }.apply { isDaemon = true; name = "anr-watchdog" }.start()
    }
}
