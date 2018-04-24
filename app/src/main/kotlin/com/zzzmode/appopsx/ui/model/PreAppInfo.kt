package com.zzzmode.appopsx.ui.model

import android.text.TextUtils
import java.util.ArrayList

/**
 * Created by zl on 2017/4/30.
 */

class PreAppInfo {

    var packageName: String
        private set
    var ignoredOps: String? = null
    private var ops: MutableList<Int>? = null

    constructor(packageName: String, ignoredOps: String) {
        this.packageName = packageName
        this.ignoredOps = ignoredOps
    }

    constructor(packageName: String) {
        this.packageName = packageName
    }

    fun getOps(): List<Int> {
        if (ops == null) {
            ops = ArrayList()
            if (!TextUtils.isEmpty(ignoredOps)) {
                val split = ignoredOps!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (s in split) {
                    try {
                        ops!!.add(Integer.valueOf(s))
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return ops!!
    }


    override fun toString(): String {
        return "PreAppInfo{" +
                "packageName='" + packageName + '\''.toString() +
                ", ignoredOps='" + ignoredOps + '\''.toString() +
                '}'.toString()
    }
}
