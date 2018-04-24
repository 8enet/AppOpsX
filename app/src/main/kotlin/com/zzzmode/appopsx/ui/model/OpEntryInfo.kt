package com.zzzmode.appopsx.ui.model

import android.app.AppOpsManager
import com.zzzmode.appopsx.common.OpEntry
import com.zzzmode.appopsx.common.ReflectUtils

/**
 * Created by zl on 2016/11/18.
 */

class OpEntryInfo(opEntryEx: OpEntry) {

    lateinit var opEntry: OpEntry
    var opName: String?=null
    var opPermsName: String?=null
    var opPermsLab: String? = null
    var opPermsDesc: String? = null
    var mode: Int = 0
    var icon: Int = 0
    var groupName: String? = null

    val isAllowed: Boolean
        get() = this.mode == AppOpsManager.MODE_ALLOWED

    init {
        if (opEntryEx != null) {
            this.opEntry = opEntryEx
            this.mode = opEntry.mode

            if (sMaxLength == null) {
                val sOpNames = ReflectUtils.getFieldValue(AppOpsManager::class.java, "sOpNames")
                if (sOpNames is Array<*>) {
                    sMaxLength = sOpNames.size
                }
            }

            sMaxLength?.let {
                if (opEntry.op < sMaxLength!!) {

                    val sOpNames = ReflectUtils
                            .getArrayFieldValue(AppOpsManager::class.java, "sOpNames", opEntry.op)
                    if (sOpNames != null) {
                        opName = sOpNames.toString()
                        opPermsName = ReflectUtils.getArrayFieldValue(AppOpsManager::class.java, "sOpPerms", opEntry.op)?.toString()
                    }
                }
            }


        }
    }

    fun changeStatus() {
        if (isAllowed) {
            this.mode = AppOpsManager.MODE_IGNORED
        } else {
            this.mode = AppOpsManager.MODE_ALLOWED
        }
    }

    override fun toString(): String {
        return "OpEntryInfo{" +
                ", opName='" + opName + '\''.toString() +
                ", opPermsName='" + opPermsName + '\''.toString() +
                ", opPermsLab='" + opPermsLab + '\''.toString() +
                ", mode=" + mode +
                '}'.toString()
    }

    companion object {

        private var sMaxLength: Int? = null
    }
}
