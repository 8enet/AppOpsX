package com.zzzmode.appopsx.ui.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

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
    LangHelper.updateLanguage(this);
    registerActivityLifecycleCallbacks(this);
    Helper.updataShortcuts(this);
    ATracker.init(getApplicationContext());
    AppOpsx.getInstance(getApplicationContext());
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
