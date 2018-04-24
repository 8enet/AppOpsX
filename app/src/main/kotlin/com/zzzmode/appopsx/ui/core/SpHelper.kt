package com.zzzmode.appopsx.ui.core

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate

/**
 * Created by zl on 2017/4/19.
 */

object SpHelper {

    fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getThemeMode(context: Context): Int {
        return if (getSharedPreferences(context).getBoolean("pref_app_daynight_mode", false)) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else AppCompatDelegate.MODE_NIGHT_NO
    }


    fun isIgnoredNetOps(context: Context, op: Int): Boolean {
        return getSharedPreferences(context).getBoolean("pref_ignore_op_code_" + op, false)
    }

    fun ignoredNetOps(context: Context, op: Int) {
        getSharedPreferences(context).edit().putBoolean("pref_ignore_op_code_" + op, true).apply()
    }

}
