package com.zzzmode.appopsx.ui.permission

import android.app.AppOpsManager
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.util.SparseIntArray
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.common.OpsResult
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.core.AppOpsx
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.observers.ResourceObserver
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.ConnectException
import java.util.*

/**
 * Created by zl on 2017/5/1.
 */

internal class PermPresenter(private val mView: IPermView, private val appInfo: AppInfo, private val context: Context) {

    private var observable: Observable<List<OpEntryInfo>>? = null

    var isLoadSuccess = false
        private set

    private var autoDisabled = true

    private var sortByMode = false

    fun setSortByMode(sortByMode: Boolean) {
        this.sortByMode = sortByMode
    }

    fun setUp() {
        mView.showProgress(!AppOpsx.getInstance(context).isRunning)
        load()
    }

    fun load() {
        observable = Helper.getAppPermission(context, appInfo.packageName,
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean("key_show_no_prems", false))

        observable?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : ResourceObserver<List<OpEntryInfo>>() {

                    override fun onNext(opEntryInfos: List<OpEntryInfo>) {

                        if (!opEntryInfos.isEmpty()) {
                            if (autoDisabled) {

                                if (sortByMode) {
                                    reSortByModePerms(opEntryInfos)
                                } else {
                                    mView.showProgress(false)
                                    mView.showPerms(opEntryInfos)
                                }
                            } else {
                                autoDisable()
                            }

                        } else {
                            mView.showError(context.getString(R.string.no_perms))
                        }
                        isLoadSuccess = true
                    }

                    override fun onError(e: Throwable) {
                        mView.showError(getHandleError(e))

                        isLoadSuccess = false
                    }

                    override fun onComplete() {}
                })
    }

    private fun getHandleError(e: Throwable): String {
        Log.e(TAG,"error --->",e)
        val config = AppOpsx.getInstance(context).config
        var msg = ""
        val errorMsg = e.message!!
        if (config.useAdb) {
            //adb
            if (e is ConnectException) {
                msg = context.getString(R.string.error_no_adb)
            }
        } else {
            //root
            if (e is IOException) {
                if (errorMsg.contains("error=13")) {
                    msg = context.getString(R.string.error_no_su)
                }
            } else if (e is RuntimeException) {
                if (errorMsg.contains("RootAccess denied")) {
                    msg = context.getString(R.string.error_su_timeout)
                } else if (errorMsg.contains("connect fail")) {
                    msg = context.getString(R.string.error_connect_fail)
                }
            }

        }

        return context.getString(R.string.error_msg, msg, errorMsg)
    }

    fun setAutoDisabled(autoDisabled: Boolean) {
        this.autoDisabled = autoDisabled
    }

    fun autoDisable() {
        Helper.autoDisable(context, appInfo.packageName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<SparseIntArray> {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onSuccess(value: SparseIntArray) {
                        autoDisabled = true
                        load()
                    }

                    override fun onError(e: Throwable) {
                        autoDisabled = true
                        load()
                    }
                })
    }


    fun reSortByModePerms(list: List<OpEntryInfo>) {

        Helper.groupByMode(context, list).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<OpEntryInfo>> {

                    override fun onSubscribe(@NonNull d: Disposable) {

                    }

                    override fun onSuccess(@NonNull opEntryInfos: List<OpEntryInfo>) {
                        mView.showProgress(false)

                        if (!opEntryInfos.isEmpty()) {
                            mView.showPerms(opEntryInfos)
                        } else {
                            mView.showError(context.getString(R.string.no_perms))
                        }
                        isLoadSuccess = true
                    }

                    override fun onError(@NonNull e: Throwable) {
                        mView.showProgress(false)
                        mView.showError(getHandleError(e))

                        isLoadSuccess = false
                    }
                })

    }

    fun switchMode(info: OpEntryInfo, v: Boolean) {
        if (v) {
            info.mode = AppOpsManager.MODE_ALLOWED
        } else {
            info.mode = AppOpsManager.MODE_IGNORED
        }
        val map = HashMap<String, String>(2)
        map["new_mode"] = info.mode.toString()
        map["op_name"] = info.opName!!
        ATracker.send(AEvent.C_PERM_ITEM, map)

        setMode(info)
    }

    fun setMode(info: OpEntryInfo) {
        Helper.setMode(context, appInfo.packageName, info)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ResourceObserver<OpsResult>() {
                    override fun onNext(value: OpsResult) {

                    }

                    override fun onError(e: Throwable) {
                        mView.updateItem(info)
                    }

                    override fun onComplete() {

                    }
                })
    }

    fun reset() {
        Helper.resetMode(context, appInfo.packageName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<OpsResult> {

                    override fun onSubscribe(@NonNull d: Disposable) {

                    }

                    override fun onSuccess(@NonNull opsResult: OpsResult) {
                        load()
                    }

                    override fun onError(@NonNull e: Throwable) {

                    }
                })
    }

    fun destory() {
        try {
            observable?.unsubscribeOn(Schedulers.io())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {

        private const val TAG = "PermPresenter"
    }
}
