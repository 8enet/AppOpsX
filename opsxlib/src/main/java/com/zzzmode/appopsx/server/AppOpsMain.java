package com.zzzmode.appopsx.server;


import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.system.Os;
import android.util.EventLog;
import android.util.EventLog.Event;
import android.util.EventLogTags;
import android.util.Log;
import com.zzzmode.appopsx.common.FLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AppOpsMain {

  private static final String APPOPSX_PKG="com.zzzmode.appopsx";

  public static void main(String[] args) {

    try {
      FLog.writeLog = false;
      FLog.log("start ops server args:" + Arrays.toString(args));
      if (args == null) {
        return;
      }

      String[] split = args[0].split(",");
      Map<String, String> params = new HashMap<>();
      for (String s : split) {
        String[] param = s.split(":");
        params.put(param[0], param[1]);
      }
      new AppOpsMain(params);

    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
    } finally {
      FLog.log("close log ... ");
      FLog.close();
    }
    stop();
  }


  private static void stop(){
    int pid = Process.myPid();
    try {
      System.out.println(" STOP: kill myself ---- pid: " + pid);
      Process.killProcess(pid);
    } catch (Throwable e) {
      e.printStackTrace();
    }finally {
      try {
        //only killProcess fail will be call.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          Os.execve("/system/bin/kill", new String[]{"-9", String.valueOf(pid)}, null);
        } else {
          Runtime.getRuntime().exec("kill -9 " + pid);
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }


  private RemoteHandler mCallHandler;

  private AppOpsMain(Map<String, String> params) throws Exception {

    try {
      mCallHandler = new RemoteHandler(params);
      LifecycleAgent.sParams = new HashMap<>(params);
      System.out.println("AppOpsX server start successful, enjoy it! \uD83D\uDE0E");
      int pid = Process.myPid();
      System.out.println(Helper.getProcessName(pid)+"   pid:"+ pid);
      LifecycleAgent.onStarted();
      mCallHandler.start();
    } finally {
      destory();
    }


  }


  private void destory() {
    try {
      if(mCallHandler != null){
        mCallHandler.destory();
      }
      FLog.log("handler destory ----- ");
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }

    LifecycleAgent.onStoped();
  }

}
