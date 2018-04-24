package com.zzzmode.appopsx.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by zl on 2017/5/7.
 */

object Formatter {

    private val sdfYMD = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault())


    private const val  B = "B"
    private const val KB = "KB"
    private const val MB = "MB"
    private const val GB = "GB"
    private const val TB = "TB"
    private const val PB = "PB"

    fun formatDate(time: Long): String {
        return sdfYMD.format(Date(time))
    }


    fun formatFileSize(number: Long): String {
        var result = number.toFloat()
        var suffix = B
        if (result > 900) {
            suffix = KB
            result /=  1024
        }
        if (result > 900) {
            suffix = MB
            result /=  1024
        }
        if (result > 900) {
            suffix = GB
            result /=  1024
        }
        if (result > 900) {
            suffix = TB
            result /=  1024
        }
        if (result > 900) {
            suffix = PB
            result /=  1024
        }
        val value = when {
            result < 1 -> String.format("%.2f", result)
            result < 10 -> String.format("%.2f", result)
            result < 100 -> String.format("%.2f", result)
            else -> String.format("%.0f", result)
        }
        return value + suffix
    }


}
