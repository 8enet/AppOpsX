package com.zzzmode.appopsx.ui.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import androidx.core.text.BidiFormatter;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.permission.AlertInstalledPremActivity;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/16.
 */

public class AppInstalledReceiver extends BroadcastReceiver {

  private static final String TAG = "AppInstalledRevicer";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.e(TAG, "onReceive --> " + action);
    //忽略更新
    if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
      return;
    }
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    Helper.updataShortcuts(context.getApplicationContext());
    if (sp.getBoolean("ignore_premission", true)) {
      try {
        String pkgName = intent.getData().getEncodedSchemeSpecificPart();
        //disable(context.getApplicationContext(),pkgName);
        showDlg(context.getApplicationContext(), pkgName);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void showDlg(final Context context, String pkg) {
    Helper.getAppInfo(context, pkg)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<AppInfo>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(AppInfo value) {
            Intent intent = new Intent(context, AlertInstalledPremActivity.class);
            intent.putExtra(AlertInstalledPremActivity.EXTRA_APP, value);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
          }

          @Override
          public void onError(Throwable e) {

          }
        });
  }

  private void disable(final Context context, final String pkgName) {
    Helper.autoDisable(context, pkgName)
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
              String label = BidiFormatter.getInstance()
                  .unicodeWrap(packageInfo.applicationInfo.loadLabel(context.getPackageManager()))
                  .toString();

              Toast
                  .makeText(context, context.getString(R.string.disable_toast, label, value.size()),
                      Toast.LENGTH_LONG).show();
              ATracker.send(context.getApplicationContext(), AEvent.U_AUTO_IGNORE, null);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
          }

        });

  }
}
