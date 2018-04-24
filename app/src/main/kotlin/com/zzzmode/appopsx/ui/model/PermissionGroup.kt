package com.zzzmode.appopsx.ui.model

class PermissionGroup {

    lateinit var group: String
    var opName: String? = null
    var opPermsName: String? = null
    var opPermsLab: String? = null
    var opPermsDesc: String? = null
    var grants: Int = 0
    var count: Int = 0
    var icon: Int = 0
    var apps: MutableList<PermissionChildItem> = ArrayList()

    override fun toString(): String {
        return "PermissionGroup{" +
                "opName='" + opName + '\''.toString() +
                ", opPermsName='" + opPermsName + '\''.toString() +
                ", opPermsLab='" + opPermsLab + '\''.toString() +
                ", opPermsDesc='" + opPermsDesc + '\''.toString() +
                ", grants=" + grants +
                ", count=" + count +
                ", apps=" + apps +
                '}'.toString()
    }
}
