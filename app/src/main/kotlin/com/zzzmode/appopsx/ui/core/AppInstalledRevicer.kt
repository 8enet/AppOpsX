package com.zzzmode.appopsx.ui.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.text.BidiFormatter
import android.util.Log
import android.util.SparseIntArray
import android.widget.Toast

import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.permission.AlertInstalledPremActivity

import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by zl on 2017/1/16.
 */

class AppInstalledRevicer : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.e(TAG, "onReceive --> $action")
        //忽略更新
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            return
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Helper.updataShortcuts(context.applicationContext)
        }
        if (sp.getBoolean("ignore_premission", true)) {
            try {
                intent.data?.encodedSchemeSpecificPart?.let {
                    showDlg(context.applicationContext, it)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }

    private fun showDlg(context: Context, pkg: String) {
        Helper.getAppInfo(context, pkg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<AppInfo> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(value: AppInfo) {
                        val intent = Intent(context, AlertInstalledPremActivity::class.java)
                        intent.putExtra(AlertInstalledPremActivity.EXTRA_APP, value)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        context.startActivity(intent)
                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    private fun disable(context: Context, pkgName: String) {
        Helper.autoDisable(context, pkgName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<SparseIntArray> {

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(value: SparseIntArray) {
                        try {
                            val packageInfo = context.packageManager.getPackageInfo(pkgName, 0)
                            val label = BidiFormatter.getInstance()
                                    .unicodeWrap(packageInfo.applicationInfo.loadLabel(context.packageManager))
                                    .toString()

                            Toast
                                    .makeText(context, context.getString(R.string.disable_toast, label, value.size()),
                                            Toast.LENGTH_LONG).show()
                            ATracker.send(context.applicationContext, AEvent.U_AUTO_IGNORE, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }

                })

    }

    companion object {

        private val TAG = "AppInstalledRevicer"
    }
}
