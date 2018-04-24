package com.zzzmode.appopsx.ui.main.group

import com.zzzmode.appopsx.ui.model.PermissionGroup

/**
 * Created by zl on 2017/7/17.
 */
internal interface IPermGroupView {
    fun changeTitle(groupPosition: Int, childPosition: Int, allowed: Boolean)
    fun refreshItem(groupPosition: Int, childPosition: Int)
    fun showList(value: List<PermissionGroup>)
    fun showError(e: Throwable)
}
