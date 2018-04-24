package com.zzzmode.appopsx.ui.model

class AppPermissions(val appInfo: AppInfo) {

    var opEntries: List<OpEntryInfo>? = null

    fun hasPermissions(): Boolean {
        return opEntries != null && !opEntries!!.isEmpty()
    }

    override fun toString(): String {
        return "AppPermissions{" +
                "appInfo=" + appInfo +
                ", opEntries=" + opEntries +
                '}'.toString()
    }
}