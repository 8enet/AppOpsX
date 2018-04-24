package com.zzzmode.appopsx.ui.permission

import com.zzzmode.appopsx.ui.model.OpEntryInfo

/**
 * Created by zl on 2017/5/1.
 */
internal interface IPermView {

    fun showProgress(show: Boolean)

    fun showError(text: CharSequence)

    fun showPerms(opEntryInfos: List<OpEntryInfo>)

    fun updateItem(info: OpEntryInfo)
}
