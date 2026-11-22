package com.ve.bookkeeping.data

import androidx.compose.runtime.mutableStateOf

/** App 是否在前台：看门狗只在用户真正在用的时候才采样，后台一律跳过。 */
object AppForeground {
    val visible = mutableStateOf(false)

    val isVisible: Boolean get() = visible.value

    fun onResume() {
        visible.value = true
    }

    fun onPause() {
        visible.value = false
    }
}
