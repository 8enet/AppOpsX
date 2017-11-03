package com.zzzmode.appopsx.server;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Process;
import com.zzzmode.appopsx.common.FLog;

/**
 * Created by zl on 2017/10/12.
 */

class ApiCompat {

  static void sendBroadcast(Intent intent){
    try {
      IActivityManager am = ActivityManagerNative.getDefault();
      IntentReceiver receiver=new IntentReceiver();
      intent.setPackage("com.zzzmode.appopsx");
      if(VERSION.SDK_INT >= VERSION_CODES.M) {
        am.broadcastIntent(null, intent, null, receiver, 0, null, null, null, -1, null, true, false,
            -2);
      } else {
        am.broadcastIntent(null, intent, null, receiver, 0, null, null, null, -1, true, false,
            -2);
      }
      FLog.log("  --sendBroadcast --  "+ Process.myUid());
      receiver.waitForFinish();
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }
  }



  private static class IntentReceiver extends IIntentReceiver.Stub {
    private boolean mFinished = false;

    @Override
    public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
        boolean ordered, boolean sticky, int sendingUser) {
      String line = "Broadcast completed: result=" + resultCode;
      if (data != null) line = line + ", data=\"" + data + "\"";
      if (extras != null) line = line + ", extras: " + extras;
      System.out.println(line);
      synchronized (this) {
        mFinished = true;
        notifyAll();
      }
    }

    synchronized void waitForFinish() {
      try {
        while (!mFinished) wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }


}
