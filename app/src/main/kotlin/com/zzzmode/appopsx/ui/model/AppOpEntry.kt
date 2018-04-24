package com.zzzmode.appopsx.ui.model

import com.zzzmode.appopsx.common.OpsResult

/**
 * Created by zl on 2016/11/24.
 */

class AppOpEntry(var appInfo: AppInfo, var opsResult: OpsResult) {

    var modifyResult: OpsResult? = null

    override fun toString(): String {
        return "AppOpEntry{" +
                "appInfo=" + appInfo +
                ", opsResult=" + opsResult +
                ", modifyResult=" + modifyResult +
                '}'.toString()
    }
}
