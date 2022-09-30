package com.cwl.sample.util

import android.content.Context
import android.content.Intent

/**
 * @Author cwl
 * @Date 2022/3/30 9:09 上午
 * @Description
 */

inline fun <reified T> Context.jumpActivity(extras: Intent.() -> Unit = {}) {
    Intent(this, T::class.java).apply(extras).also { startActivity(it) }
}
