package com.zzzmode.appopsx.ui.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import com.zzzmode.appopsx.BuildConfig;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zl on 2017/1/7.
 */

public class AppGlobal extends Application implements Application.ActivityLifecycleCallbacks {

  private AtomicInteger mAliveActivity = new AtomicInteger(0);

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {

      StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        builder.detectNonSdkApiUsage();
      }
      StrictMode.setVmPolicy(builder.penaltyLog()
          .build());

    }



    LangHelper.updateLanguage(this);
    registerActivityLifecycleCallbacks(this);
    Helper.updataShortcuts(this);
    ATracker.init(getApplicationContext());
    AppOpsx.getInstance(getApplicationContext());
    installReceiver();

  }


  private void installReceiver(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_PACKAGE_ADDED);
      filter.addDataScheme("package");
      registerReceiver(new AppInstalledReceiver(),filter);
    }else {

      ComponentName componentName = new ComponentName(this,AppInstalledReceiver.class);
      getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);

    }

  }


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    LangHelper.updateLanguage(this);
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(LangHelper.attachBaseContext(base));
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    mAliveActivity.getAndIncrement();
  }

  @Override
  public void onActivityStarted(Activity activity) {

  }

  @Override
  public void onActivityResumed(Activity activity) {

  }

  @Override
  public void onActivityPaused(Activity activity) {

  }

  @Override
  public void onActivityStopped(Activity activity) {

  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override
  public void onActivityDestroyed(Activity activity) {
    int i = mAliveActivity.decrementAndGet();
    if (i <= 0) {
      Log.e("test", "onActivityDestroyed --> ");
      Helper.closeBgServer(getApplicationContext());
    }
  }
}
