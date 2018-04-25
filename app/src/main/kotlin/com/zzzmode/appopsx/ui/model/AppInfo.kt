package com.zzzmode.appopsx.ui.model

import kotlinx.android.parcel.Parcelize
import android.content.pm.ApplicationInfo
import android.os.Parcelable

/**
 * Created by zl on 2016/11/18.
 */
@Parcelize
class AppInfo(var appName: String="",
              var packageName: String ,
              var time: Long = 0,
              var installTime: Long = 0,
              var updateTime: Long = 0,
              var pinyin: String? = null,
              var versionCode :Int= 0,
              var versionName : String?=null,
              var applicationInfo: ApplicationInfo? = null) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        return (other as AppInfo?)?.let {
            packageName == it.packageName
        } ?: false

    }

    override fun toString(): String {
        return "AppInfo{" +
                "appName='" + appName + '\''.toString() +
                ", packageName='" + packageName + '\''.toString() +
                '}'.toString()
    }

}
