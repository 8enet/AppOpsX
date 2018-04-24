package com.zzzmode.appopsx.ui.main.group

import android.content.Context
import android.preference.PreferenceManager
import com.zzzmode.appopsx.common.OpsResult
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.model.PermissionChildItem
import com.zzzmode.appopsx.ui.model.PermissionGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.ResourceObserver
import io.reactivex.observers.ResourceSingleObserver
import io.reactivex.schedulers.Schedulers

/**
 * Created by zl on 2017/7/17.
 */

internal class PermGroupPresenter(private val mView: IPermGroupView, private val context: Context) {

    private var subscriber: ResourceSingleObserver<List<PermissionGroup>>? = null

    var isLoadSuccess = false
        private set


    fun loadPerms() {
        val showSysApp = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("show_sysapp", false)
        val reqNet = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("key_g_show_net", false)

        val showIgnored = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("key_g_show_ignored", false)

        subscriber = object : ResourceSingleObserver<List<PermissionGroup>>() {
            override fun onSuccess(value: List<PermissionGroup>) {
                isLoadSuccess = true
                mView.showList(value)
            }

            override fun onError(e: Throwable) {
                mView.showError(e)
            }

        }

        Helper.getPermissionGroup(context, showSysApp, reqNet, showIgnored)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber!!)

    }

    fun changeMode(groupPosition: Int, childPosition: Int,
                   info: PermissionChildItem) {

        info.opEntryInfo.changeStatus()

        Helper.setMode(context, info.appInfo.packageName, info.opEntryInfo)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ResourceObserver<OpsResult>() {
                    override fun onNext(value: OpsResult) {

                        mView.changeTitle(groupPosition, childPosition, info.opEntryInfo.isAllowed)
                    }

                    override fun onError(e: Throwable) {
                        try {
                            info.opEntryInfo.changeStatus()
                            mView.refreshItem(groupPosition, childPosition)
                        } catch (e2: Exception) {
                            e2.printStackTrace()
                        }

                    }

                    override fun onComplete() {

                    }
                })
    }


    fun destroy() {
        subscriber?.apply {
            if(!isDisposed){
                dispose()
            }
        }
    }
}
