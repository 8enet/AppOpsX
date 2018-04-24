package com.zzzmode.appopsx.ui.analytics;

import android.app.Activity;
import android.content.Context;

import com.zzzmode.appopsx.BuildConfig;

import java.util.Map;

/**
 * Created by zl on 2017/2/26.
 */

public final class ATracker {

  private static Context sContext;

  public static void init(Context context) {
    sContext = context;

  }

  public static void send(String id) {
    send(sContext, id, null);
  }

  public static void send(String id, Map<String, String> param) {
    send(sContext, id, param);
  }

  public static void send(Context context, String id, Map<String, String> param) {
  }

  public static void onResume(Activity activity) {
  }

  public static void onPause(Activity activity) {
  }

  public static void reportError(Throwable e){
  }
}
