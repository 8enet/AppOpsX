package com.zzzmode.appopsx.ui.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.support.v4.text.BidiFormatter;
import android.util.Log;
import android.widget.Toast;

import com.zzzmode.appopsx.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/16.
 */

public class AppInstalledRevicer extends BroadcastReceiver {

    private static final String TAG = "AppInstalledRevicer";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if(sp.getBoolean("ignore_premission",false)){
            try{
                String pkgName = intent.getData().getEncodedSchemeSpecificPart();
                disable(context.getApplicationContext(),pkgName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void disable(final Context context, final String pkgName){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                AppOpsx.getInstance(context).disableAllPermission(pkgName);
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                String label = BidiFormatter.getInstance().unicodeWrap(packageInfo.applicationInfo.loadLabel(context.getPackageManager())).toString();
                e.onNext(label);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new ResourceObserver<String>() {
            @Override
            public void onNext(String value) {
                Toast.makeText(context,context.getString(R.string.disable_toast,value),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
            }
        });
    }
}
