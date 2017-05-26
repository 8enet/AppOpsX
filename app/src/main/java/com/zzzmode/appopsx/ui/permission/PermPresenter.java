package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseIntArray;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/5/1.
 */

class PermPresenter {
    private static final String TAG = "PermPresenter";

    private IPermView mView;
    private Context context;
    private AppInfo appInfo;

    private Observable<List<OpEntryInfo>> observable;

    private boolean loadSuccess = false;

    private boolean autoDisabled = true;

    private boolean sortByMode = false;

    PermPresenter(IPermView mView, AppInfo appInfo, Context context) {
        this.mView = mView;
        this.context = context;
        this.appInfo = appInfo;
    }

    public void setSortByMode(boolean sortByMode) {
        this.sortByMode = sortByMode;
    }

    void setUp() {
        mView.showProgress(!AppOpsx.getInstance(context).isRunning());
        load();
    }

    void load(){
        observable = Helper.getAppPermission(context, appInfo.packageName, PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_show_no_prems", false));

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<List<OpEntryInfo>>() {

            @Override
            protected void onStart() {
                super.onStart();
            }

            @Override
            public void onNext(List<OpEntryInfo> opEntryInfos) {


                if (opEntryInfos != null && !opEntryInfos.isEmpty()) {
                    if(autoDisabled){

                        if(sortByMode) {
                            reSortByModePerms(opEntryInfos);
                        }else {
                            mView.showProgress(false);
                            mView.showPerms(opEntryInfos);
                        }
                    } else {
                        autoDisable();
                    }

                } else {
                    mView.showError(context.getString(R.string.no_perms));
                }
                loadSuccess = true;
            }

            @Override
            public void onError(Throwable e) {
                mView.showError(context.getString(R.string.error_msg, Log.getStackTraceString(e)));

                loadSuccess = false;
            }

            @Override
            public void onComplete() {
            }
        });
    }


    void setAutoDisabled(boolean autoDisabled) {
        this.autoDisabled = autoDisabled;
    }

    void autoDisable(){
        Helper.autoDisable(context,appInfo.packageName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<SparseIntArray>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(SparseIntArray value) {
                        autoDisabled=true;
                        load();
                    }

                    @Override
                    public void onError(Throwable e) {
                        autoDisabled=true;
                        load();
                    }
                });
    }


    void reSortByModePerms(List<OpEntryInfo> list){

        Helper.groupByMode(context,list).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<List<OpEntryInfo>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<OpEntryInfo> opEntryInfos) {
                mView.showProgress(false);

                if (opEntryInfos != null && !opEntryInfos.isEmpty()) {
                    mView.showPerms(opEntryInfos);
                } else {
                    mView.showError(context.getString(R.string.no_perms));
                }
                loadSuccess = true;
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mView.showProgress(false);
                mView.showError(context.getString(R.string.error_msg, Log.getStackTraceString(e)));

                loadSuccess = false;
            }
        });

    }

    void switchMode(OpEntryInfo info, boolean v) {
        if (v) {
            info.mode = AppOpsManager.MODE_ALLOWED;
        } else {
            info.mode = AppOpsManager.MODE_IGNORED;
        }
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("new_mode", String.valueOf(info.mode));
        map.put("op_name", info.opName);
        ATracker.send(AEvent.C_PERM_ITEM, map);

        setMode(info);
    }

    void setMode(final OpEntryInfo info) {
        Helper.setMode(context, appInfo.packageName, info)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
            @Override
            public void onNext(OpsResult value) {

            }

            @Override
            public void onError(Throwable e) {
                mView.updateItem(info);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    void destory(){
        try {
            if(observable != null){
                observable.unsubscribeOn(Schedulers.io());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isLoadSuccess() {
        return loadSuccess;
    }
}
