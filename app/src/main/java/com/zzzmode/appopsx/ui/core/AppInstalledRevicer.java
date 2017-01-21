package com.zzzmode.appopsx.ui.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.text.BidiFormatter;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.zzzmode.appopsx.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
        Helper.updataShortcuts(context.getApplicationContext());
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
        Helper.autoDisable(context,pkgName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<SparseIntArray>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(SparseIntArray value) {
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                    String label = BidiFormatter.getInstance().unicodeWrap(packageInfo.applicationInfo.loadLabel(context.getPackageManager())).toString();

                    Toast.makeText(context,context.getString(R.string.disable_toast,label,value.size()),Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
            }

        });

    }
}
