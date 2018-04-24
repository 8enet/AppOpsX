package com.zzzmode.appopsx.ui.main.backup

/**
 * Created by zl on 2017/5/7.
 */

internal interface IConfigView {

    fun showProgress(show: Boolean, max: Int)

    fun setProgress(progress: Int)
}
