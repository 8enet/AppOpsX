package com.zzzmode.appopsx.server;

import android.app.ActivityThread;
import android.content.Intent;
import com.zzzmode.appopsx.common.FLog;

/**
 * Created by zl on 2017/10/12.
 */

class ApiCompat {

  static void sendBroadcast(Intent intent) {
    try {

      ActivityThread.currentApplication().sendBroadcast(intent);
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }
  }

}
