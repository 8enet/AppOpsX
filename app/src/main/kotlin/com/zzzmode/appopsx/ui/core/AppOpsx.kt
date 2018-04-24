package com.zzzmode.appopsx.ui.core

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import com.zzzmode.appopsx.OpsxManager
import com.zzzmode.appopsx.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by zl on 2016/11/19.
 */

object AppOpsx {

    private val LOG_FILE = "appopsx.log"

    private var sManager: OpsxManager? = null

    fun getInstance(context: Context): OpsxManager {
        if (sManager == null) {
            synchronized(AppOpsx::class.java) {
                if (sManager == null) {
                    val config = OpsxManager.Config()
                    updateConfig(context, config)
                    sManager = OpsxManager(context.applicationContext, config)
                }
            }
        }
        return sManager!!
    }

    fun updateConfig(context: Context) {
        sManager?.config?.let{
            updateConfig(context,it)
        }
    }


    private fun updateConfig(context: Context, config: OpsxManager.Config) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        config.allowBgRunning = sp.getBoolean("allow_bg_remote", true)
        config.logFile = context.getFileStreamPath(LOG_FILE).absolutePath
        config.useAdb = sp.getBoolean("use_adb", false)
        config.adbPort = sp.getInt("use_adb_port", 5555)
        config.rootOverAdb = sp.getBoolean("allow_root_over_adb", false)
        Log.e("test", "buildConfig --> " + context.getFileStreamPath(LOG_FILE).absolutePath)
    }

    fun readLogs(context: Context): String {
        val sb = StringBuilder()
        sb.append("SELinux:")
        if (OpsxManager.isEnableSELinux()) {
            sb.append("Enforcing")
        }
        sb.append("\n")

        try {
            val packageInfo = context.packageManager
                    .getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
            sb.append("GitCommitId:").append(packageInfo.applicationInfo.metaData.getString("GIT_COMMIT_ID"))
        } catch (e: Exception) {
            e.printStackTrace()
        }


        sb.append("\n\n")

        val file = context.getFileStreamPath(LOG_FILE)
        if (file.exists()) {
            sb.append( file.readText())

        } else {

            sb.append(context.getString(R.string.log_empty))
        }

        return sb.toString()
    }


}
