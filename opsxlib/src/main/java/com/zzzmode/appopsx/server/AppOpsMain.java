package com.zzzmode.appopsx.server;


import android.annotation.TargetApi;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageInstallerCallback.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.Process;
import android.system.Os;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.ReflectUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppOpsMain extends Stub {

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
//      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//        register2();
//      }
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }

  }


  @TargetApi(VERSION_CODES.LOLLIPOP)
  private void register2(){

    getPI().registerCallback(this,0);

  }

  private void register(){
    try {

      System.out.println("register  ---");

      PackageInfo packageInfo = ActivityThread.getPackageManager()
          .getPackageInfo(APPOPSX_PKG, PackageManager.GET_RECEIVERS | PackageManager.GET_META_DATA, 0);

      String revicerName=null;

      for (ActivityInfo receiver : packageInfo.receivers) {
        FLog.log(receiver.toString());
        System.out.println(receiver);
        if(receiver.metaData != null && receiver.metaData.getBoolean("installer")){
          revicerName = receiver.name;
          break;
        }
      }

      ActivityThread activityThread = ActivityThread.currentActivityThread();
      Context context = activityThread.getSystemContext();

      System.out.println(context.getPackageName());

      ApplicationInfo applicationInfo = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).applicationInfo;

      Object mPackageInfo = ReflectUtils.getFieldValue(context, "mPackageInfo");
      ClassLoader classLoader = (ClassLoader)ReflectUtils.invokMethod(mPackageInfo, "getClassLoader", null, null);

      System.out.println(mPackageInfo);
      System.out.println(classLoader);

      activityThread.installSystemApplicationInfo(applicationInfo,classLoader);

      System.out.println("installSystemApplicationInfo success");

      Context packageContext = context.createPackageContext(APPOPSX_PKG, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);


      if(revicerName != null){
        Class aClass = Class.forName(revicerName, false, packageContext.getClassLoader());
        BroadcastReceiver receiver= (BroadcastReceiver) aClass.newInstance();

        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        context.registerReceiver(receiver,filter);
        System.out.println("registerReceiver --> "+receiver);
      }

    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
    }
  }

  private void destory() {
    getPI().unregisterCallback(this);
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


  private static IPackageManager getPM(){
    return ActivityThread.getPackageManager();
  }

  private static IPackageInstaller getPI(){
    return getPM().getPackageInstaller();
  }

  @Override
  public void onSessionCreated(int sessionId)  {
    System.out.println("onSessionCreated  "+sessionId);
    //SessionInfo sessionInfo = getPI().getSessionInfo(sessionId);
    readSession(sessionId);
  }

  @Override
  public void onSessionBadgingChanged(int sessionId) {
    System.out.println("onSessionBadgingChanged "+sessionId);
  }

  @Override
  public void onSessionActiveChanged(int sessionId, boolean active)  {
    System.out.println("onSessionActiveChanged "+sessionId);
  }

  @Override
  public void onSessionProgressChanged(int sessionId, float progress)  {
    System.out.println("onSessionProgressChanged "+sessionId+"   "+progress);
    readSession(sessionId);
  }

  @Override
  public void onSessionFinished(int sessionId, boolean success) {
    System.out.println("onSessionFinished "+sessionId+"   "+success);
    readSession(sessionId);
  }

  private void readSession(int sessionId){
    SessionInfo sessionInfo = getPI().getSessionInfo(sessionId);

    if(sessionInfo != null) {

      System.out.println(sessionInfo.getAppLabel() + "   " + sessionInfo.getAppPackageName()+"   "+sessionInfo.isActive());
    }

  }
}
