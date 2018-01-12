package com.zzzmode.appopsx.remote;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import com.zzzmode.appopsx.common.ClassCallerProcessor;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.ServerRunInfo;

public class RestartHandler extends ClassCallerProcessor {

  private static final String TAG = "RestartHandler";

  public RestartHandler(Context mPackageContext,
      Context mSystemContext, byte[] bytes) {
    super(mPackageContext, mSystemContext, bytes);
  }

  @Override
  public Bundle proxyInvoke(Bundle bundle) throws Throwable {
    Runtime.getRuntime().exec("sh /sdcard/Android/data/com.zzzmode.appopsx/opsx.sh "+Process.myPid());
    FLog.log("RestartHandler -----------exec  --- ");
    return bundle;
  }
}
