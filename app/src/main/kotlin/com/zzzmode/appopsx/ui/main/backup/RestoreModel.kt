package com.zzzmode.appopsx.ui.main.backup

import com.zzzmode.appopsx.ui.model.PreAppInfo

/**
 * Created by zl on 2017/5/7.
 */

internal class RestoreModel {

    var createTime: Long = 0
    var version: Int = 0
    var size: Int = 0
    var fileSize: Long = 0
    var path: String? = null
    var fileName: String? = null
    var preAppInfos: List<PreAppInfo>? = null
}
