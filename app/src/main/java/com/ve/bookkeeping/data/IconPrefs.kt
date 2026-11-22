package com.ve.bookkeeping.data

import android.content.Context
import com.ve.bookkeeping.ui.model.IconCatalog

/**
 * 图标覆盖表：分类/账户 id → 图标 id。
 *
 * 不写进账本 JSON，单独放 SharedPreferences：
 * 读取时套上覆盖图标，删掉即恢复默认；改动后通过 [version] 触发界面即时刷新。
 */
object IconPrefs {

    private const val PREFS = "icon_overrides"
    private const val KEY_CATEGORY = "category_"
    private const val KEY_ACCOUNT = "account_"

    /** 全局版本号：每次设置图标都 +1，界面据此重组（不需要重启）。 */
    val versionState = androidx.compose.runtime.mutableStateOf(0)

    val version: Int get() = versionState.value

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun categoryIcon(context: Context, categoryId: String): String? =
        prefs(context).getString(KEY_CATEGORY + categoryId, null)

    fun accountIcon(context: Context, accountId: String): String? =
        prefs(context).getString(KEY_ACCOUNT + accountId, null)

    fun setCategoryIcon(context: Context, categoryId: String, iconId: String) {
        prefs(context).edit().putString(KEY_CATEGORY + categoryId, iconId).apply()
        versionState.value++
    }

    fun setAccountIcon(context: Context, accountId: String, iconId: String) {
        prefs(context).edit().putString(KEY_ACCOUNT + accountId, iconId).apply()
        versionState.value++
    }

    fun clearCategoryIcon(context: Context, categoryId: String) {
        prefs(context).edit().remove(KEY_CATEGORY + categoryId).apply()
        versionState.value++
    }

    fun resolveCategory(context: Context, categoryId: String, fallback: androidx.compose.ui.graphics.vector.ImageVector) =
        categoryIcon(context, categoryId)
            ?.let { IconCatalog.find(it)?.vector }
            ?: fallback
}
