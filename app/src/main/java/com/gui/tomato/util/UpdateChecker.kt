package com.gui.tomato.util

import android.content.Context
import com.gui.tomato.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 静默检查 GitHub 最新 Release。
 *
 * - 走 GitHub 公开 API：GET /repos/{owner}/{repo}/releases/latest
 * - 拿 tag_name（例如 "v1.2"）和本地 BuildConfig.VERSION_NAME（例如 "1.0"）比较
 * - 有更新 → 返回 UpdateInfo；没更新 / 出错 / 6 小时内已查过 → 返回 null
 * - 用 HttpURLConnection，不引入任何新依赖
 */
object UpdateChecker {

    private const val REPO_OWNER = "yipj1202-oss"
    private const val REPO_NAME = "gui"
    private const val API_URL =
        "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"

    // 冷却：6 小时内不重复请求
    private const val PREFS_NAME = "gui_update_check"
    private const val KEY_LAST_CHECK = "last_check_ms"
    private const val COOLDOWN_MS = 6L * 60 * 60 * 1000

    data class UpdateInfo(
        val versionName: String,   // 例如 "v1.2"
        val htmlUrl: String,       // Release 页面链接
        val body: String           // Release 说明
    )

    /**
     * 检查更新（阻塞，需在 IO 线程调用）。
     * force=true 时跳过 6 小时冷却（用于手动检查）。
     */
    fun check(ctx: Context, force: Boolean = false): UpdateInfo? {
        val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!force) {
            val last = sp.getLong(KEY_LAST_CHECK, 0L)
            if (System.currentTimeMillis() - last < COOLDOWN_MS) {
                return null
            }
        }

        return try {
            val conn = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Gui-Android")
            }

            val code = conn.responseCode
            if (code != 200) {
                conn.disconnect()
                // 404（仓库无 Release）也算"没有更新"
                sp.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
                return null
            }

            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            // 记录本次检查时间
            sp.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()

            val json = JSONObject(text)
            val tag = json.optString("tag_name", "").trim()
            val htmlUrl = json.optString("html_url", "")
            val body = json.optString("body", "")

            if (tag.isEmpty()) return null
            if (htmlUrl.isEmpty()) return null

            // 比较版本：远端 tag 去掉前缀 "v"，本地 BuildConfig.VERSION_NAME
            val remoteVer = tag.removePrefix("v").removePrefix("V")
            val localVer = BuildConfig.VERSION_NAME

            if (isNewer(remoteVer, localVer)) {
                UpdateInfo(tag, htmlUrl, body)
            } else {
                null
            }
        } catch (_: Exception) {
            // 网络异常静默失败
            null
        }
    }

    /** 简单版本号比较：1.2 > 1.0；1.2.1 > 1.2；"1.10" > "1.9" */
    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").mapNotNull { it.toIntOrNull() }
        val l = local.split(".").mapNotNull { it.toIntOrNull() }
        val n = maxOf(r.size, l.size)
        for (i in 0 until n) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv > lv) return true
            if (rv < lv) return false
        }
        return false
    }
}