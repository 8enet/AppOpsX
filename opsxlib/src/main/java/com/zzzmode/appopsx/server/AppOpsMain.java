package com.zzzmode.appopsx.server;


import android.app.ActivityThread;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.system.Os;
import com.zzzmode.appopsx.common.FLog;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AppOpsMain {

  private static final String APPOPSX_PKG="com.zzzmode.appopsx";

  public static void main(String[] args) {

    try {
      FLog.writeLog = true;
      FLog.log("start ops server args:" + Arrays.toString(args));
      if (args == null) {
        return;
      }

      try {
        Looper.prepareMainLooper();
        ActivityThread.systemMain();
      } catch (Exception e) {
        e.printStackTrace();
      }

      String[] split = args[0].split(",");
      Map<String, String> params = new HashMap<>();
      for (String s : split) {
        String[] param = s.split(":");
        params.put(param[0], param[1]);
      }
      new AppOpsMain(params);

      Looper.loop();
    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
    } finally {
      FLog.log("close log ... ");
      FLog.close();
    }
  }



  private Context mContext;
  private RemoteHandler mCallHandler;

  private AppOpsMain(Map<String, String> params) throws IOException {

    try {
      mCallHandler = new RemoteHandler(params);

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            mCallHandler.start();
          } catch (Exception e) {
            e.printStackTrace();
            FLog.log(e);
          }finally {
            FLog.log("call end ----");
            destory();
          }
        }
      }).start();

      System.out.println("AppOpsX server start successful, enjoy it! \uD83D\uDE0E");
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }

  }



  private void destory() {
    try {
      if(mCallHandler != null){
        mCallHandler.destory();
      }
      Looper.getMainLooper().quitSafely();
      FLog.log("call destory ----- ");
    } catch (Exception e) {
      e.printStackTrace();
    }
    stop();
  }

  private void stop(){
    try {
      FLog.log(" STOP ---- pid: "+Process.myPid());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Os.execve("/system/bin/kill", new String[]{"-9", String.valueOf(Process.myPid())}, null);
      } else {
        Runtime.getRuntime().exec("/system/bin/kill -9 " + Process.myPid()); //kill self
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


}
