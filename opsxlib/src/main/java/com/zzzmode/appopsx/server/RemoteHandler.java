package com.zzzmode.appopsx.server;

import android.Manifest;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.IAppOpsService;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.OtherOp;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ParcelableUtil;
import com.zzzmode.appopsx.common.ReflectUtils;
import com.zzzmode.appopsx.common.Shell;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zl on 2017/7/27.
 */

class RemoteHandler implements OpsDataTransfer.OnRecvCallback {

  private static final int MSG_TIMEOUT = 1;
  private static final int DEFAULT_TIME_OUT_TIME = 1000 * 60 * 1; //1min
  private static final int BG_TIME_OUT = DEFAULT_TIME_OUT_TIME * 10; //10min

  private OpsXServer server;
  private Handler handler;
  private volatile boolean isDeath = false;
  private int timeOut = DEFAULT_TIME_OUT_TIME;
  private volatile boolean allowBg = false;

  private Shell mShell;
  private IptablesController mIptablesController;

  RemoteHandler(Map<String, String> params) throws IOException {
    System.out.println("params --> " + params);
    boolean isRoot = TextUtils.equals(params.get("type"), "root");
    String path = params.get("path");
    String token = params.get("token");
    boolean allowBg = TextUtils.equals(params.get("bgrun"), "1");
    boolean debug = TextUtils.equals(params.get("debug"), "1");

    server = new OpsXServer(path, token, this);
    server.allowBackgroundRun = this.allowBg = allowBg;

    try {

      if (isRoot) {
        mShell = Shell.getShell();
        mIptablesController = new IptablesController(mShell);
      }
    } catch (Exception e) {
      e.printStackTrace();
      mIptablesController = null;
    }

    if (!allowBg) {
      HandlerThread thread1 = new HandlerThread("watcher-ups");
      thread1.start();
      handler = new Handler(thread1.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          switch (msg.what) {
            case MSG_TIMEOUT:
              destory();
              break;
          }
        }
      };
      handler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOut);
    }

  }


  void start() throws Exception {
    server.run();
  }

  void destory() {
    try {
      if (!allowBg && handler != null) {
        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(MSG_TIMEOUT);
        handler.getLooper().quit();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (mShell != null) {
      try {
        mShell.close();
        mShell.destroyShell();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      isDeath = true;
      server.setStop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendOpResult(OpsResult result) {
    try {
      server.sendResult(ParcelableUtil.marshall(result));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleCommand(OpsCommands.Builder builder) throws Throwable {
    String s = builder.getAction();
    if (OpsCommands.ACTION_GET.equals(s)) {
      runGet(builder);
    } else if (OpsCommands.ACTION_SET.equals(s)) {
      runSet(builder);
    } else if (OpsCommands.ACTION_RESET.equals(s)) {
      runReset(builder);
    } else if (OpsCommands.ACTION_GET_FOR_OPS.equals(s)) {
      runGetForOps(builder);
    } else {
      runOther(builder);
    }
  }

  private void runGet(OpsCommands.Builder getBuilder) throws Throwable {

    FLog.log("runGet sdk:" + Build.VERSION.SDK_INT);

    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));
    String packageName = getBuilder.getPackageName();

    int uid = Helper.getPackageUid(packageName, getBuilder.getUserHandleId());

    List opsForPackage = appOpsService.getOpsForPackage(uid, packageName, null);
    List<PackageOps> packageOpses = new ArrayList<>();
    if (opsForPackage != null) {
      for (Object o : opsForPackage) {
        PackageOps packageOps = ReflectUtils.opsConvert(o);
        addSupport(appOpsService, packageOps, getBuilder.getUserHandleId());
        packageOpses.add(packageOps);
      }
    } else {
      PackageOps packageOps = new PackageOps(packageName, uid, new ArrayList<OpEntry>());
      addSupport(appOpsService, packageOps, getBuilder.getUserHandleId());
      packageOpses.add(packageOps);
    }

    sendOpResult(new OpsResult(packageOpses, null));

  }


  private void addSupport(IAppOpsService appOpsService, PackageOps ops, int userHandleId) {
    addSupport(appOpsService, ops, userHandleId, true);
  }

  private void addSupport(IAppOpsService appOpsService, PackageOps ops, int userHandleId, boolean checkNet) {
    try {
      if (checkNet && mIptablesController != null) {
        int mode = mIptablesController.isMobileDataEnable(ops.getUid()) ? AppOpsManager.MODE_ALLOWED
            : AppOpsManager.MODE_IGNORED;
        OpEntry opEntry = new OpEntry(OtherOp.OP_ACCESS_PHONE_DATA, mode, 0, 0, 0, 0, null);
        ops.getOps().add(opEntry);

        mode = mIptablesController.isWifiDataEnable(ops.getUid()) ? AppOpsManager.MODE_ALLOWED
            : AppOpsManager.MODE_IGNORED;
        opEntry = new OpEntry(OtherOp.OP_ACCESS_WIFI_NETWORK, mode, 0, 0, 0, 0, null);
        ops.getOps().add(opEntry);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(Log.getStackTraceString(e));
    }
    try {
      PackageInfo packageInfo = ActivityThread.getPackageManager()
          .getPackageInfo(ops.getPackageName(), PackageManager.GET_PERMISSIONS, userHandleId);
      if (packageInfo != null && packageInfo.requestedPermissions != null) {
        for (String permission : packageInfo.requestedPermissions) {
          int code = Helper.permissionToCode(permission);

          if (code <= 0) {
            //correct OP_WIFI_SCAN code.
            if (Manifest.permission.ACCESS_WIFI_STATE.equals(permission)) {
              code = OtherOp.getWifiScanOp();
            }
          }
          
          if (code > 0 && !ops.hasOp(code)) {
            int mode = appOpsService.checkOperation(code, ops.getUid(), ops.getPackageName());
            if (mode != AppOpsManager.MODE_ERRORED) {
              //
              ops.getOps().add(new OpEntry(code, mode, 0, 0, 0, 0, null));
            }
          }
        }
      }

    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void runSet(OpsCommands.Builder builder) throws Throwable {

    final int uid = Helper.getPackageUid(builder.getPackageName(), builder.getUserHandleId());
    if (OtherOp.isOtherOp(builder.getOpInt())) {
      setOther(builder, uid);
    } else {
      final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
          ServiceManager.getService(Context.APP_OPS_SERVICE));
      appOpsService
          .setMode(builder.getOpInt(), uid, builder.getPackageName(), builder.getModeInt());
    }

    sendOpResult(new OpsResult(null, null));

  }

  private void setOther(OpsCommands.Builder builder, int uid) {
    if (mIptablesController != null) {
      boolean enable = builder.getModeInt() == AppOpsManager.MODE_ALLOWED;
      switch (builder.getOpInt()) {
        case OtherOp.OP_ACCESS_PHONE_DATA:
          mIptablesController.setMobileData(uid, enable);
          break;
        case OtherOp.OP_ACCESS_WIFI_NETWORK:
          mIptablesController.setWifiData(uid, enable);
          break;
      }
    }
  }

  private void runOther(OpsCommands.Builder builder) {
    String packageName = builder.getPackageName();
    if ("close_server".equals(packageName)) {
      destory();
    }
  }

  private void runReset(OpsCommands.Builder builder) throws Throwable {
    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    appOpsService.resetAllModes(builder.getUserHandleId(), builder.getPackageName());
    sendOpResult(new OpsResult(null, null));
  }

  private void runGetForOps(OpsCommands.Builder builder) throws Throwable {

    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    List opsForPackage = appOpsService.getPackagesForOps(builder.getOps());
    List<PackageOps> packageOpses = new ArrayList<>();

    if (opsForPackage != null) {
      for (Object o : opsForPackage) {
        PackageOps packageOps = ReflectUtils.opsConvert(o);
        addSupport(appOpsService, packageOps, builder.getUserHandleId(), builder.isReqNet());
        packageOpses.add(packageOps);
      }

      FLog.log("runGetForOps ---- "+packageOpses.size());
    }

    sendOpResult(new OpsResult(packageOpses, null));

  }

  @Override
  public void onMessage(byte[] bytes) {
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler.removeMessages(MSG_TIMEOUT);
    }

    if (!isDeath) {
      if (!allowBg && handler != null) {
        handler.sendEmptyMessageDelayed(MSG_TIMEOUT, BG_TIME_OUT);
      }

      OpsCommands.Builder unmarshall = ParcelableUtil
          .unmarshall(bytes, OpsCommands.Builder.CREATOR);

      FLog.log("onMessage ---> !!!! " + unmarshall);

      try {
        handleCommand(unmarshall);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
        sendOpResult(new OpsResult(null, throwable));
      }

    }
  }

}
