package com.zzzmode.appopsx.remote;

import android.Manifest;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.IAppOpsService;
import com.zzzmode.appopsx.common.ClassCallerProcessor;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.OtherOp;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ReflectUtils;
import com.zzzmode.appopsx.common.Shell;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppOpsHandler extends ClassCallerProcessor {

  private static IptablesController mIptablesController = null;
  static {
    try {
      if(Process.myUid() == 0) {
        mIptablesController = new IptablesController(Shell.getRootShell());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public AppOpsHandler(Context mPackageContext, Context mSystemContext,
      byte[] bytes) {
    super(mPackageContext, mSystemContext, bytes);
  }

  @Override
  public Bundle proxyInvoke(Bundle args) throws Throwable {

    OpsResult result = null;

    try {
      OpsCommands commands = args.getParcelable("args");
      args.clear();
      FLog.log(" appops "+commands);
      result = handleCommand(commands);
      if(result == null){
        result = new OpsResult(null,null);
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      result = new OpsResult(null,throwable);
    }
    args.putParcelable("return",result);
    return args;
  }

  private OpsResult handleCommand(OpsCommands commands) throws Throwable {
    String s = commands.getAction();
    OpsResult result = null;
    if (OpsCommands.ACTION_GET.equals(s)) {
      result = runGet(commands);
    } else if (OpsCommands.ACTION_SET.equals(s)) {
      runSet(commands);
    } else if (OpsCommands.ACTION_RESET.equals(s)) {
      runReset(commands);
    } else if (OpsCommands.ACTION_GET_FOR_OPS.equals(s)) {
      result = runGetForOps(commands);
    }
    return result;
  }


  private OpsResult runGet(OpsCommands commands) throws Throwable {


    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));
    String packageName = commands.getPackageName();

    int uid = Helper.getPackageUid(packageName, commands.getUserHandleId());

    List opsForPackage = appOpsService.getOpsForPackage(uid, packageName, null);
    ArrayList<PackageOps> packageOpses = new ArrayList<>();
    if (opsForPackage != null) {
      for (Object o : opsForPackage) {
        PackageOps packageOps = ReflectUtils.opsConvert(o);
        addSupport(appOpsService, packageOps, commands.getUserHandleId());
        packageOpses.add(packageOps);
      }
    } else {
      PackageOps packageOps = new PackageOps(packageName, uid, new ArrayList<OpEntry>());
      addSupport(appOpsService, packageOps, commands.getUserHandleId());
      packageOpses.add(packageOps);
    }

    return new OpsResult(packageOpses,null);

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

  private void runSet(OpsCommands commands) throws Throwable {

    final int uid = Helper.getPackageUid(commands.getPackageName(), commands.getUserHandleId());
    if (OtherOp.isOtherOp(commands.getOpInt())) {
      setOther(commands, uid);
    } else {
      final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
          ServiceManager.getService(Context.APP_OPS_SERVICE));
      appOpsService
          .setMode(commands.getOpInt(), uid, commands.getPackageName(), commands.getModeInt());
    }


  }

  private void setOther(OpsCommands commands, int uid) {
    if (mIptablesController != null) {
      boolean enable = commands.getModeInt() == AppOpsManager.MODE_ALLOWED;
      switch (commands.getOpInt()) {
        case OtherOp.OP_ACCESS_PHONE_DATA:
          mIptablesController.setMobileData(uid, enable);
          break;
        case OtherOp.OP_ACCESS_WIFI_NETWORK:
          mIptablesController.setWifiData(uid, enable);
          break;
      }
    }
  }

  private void runReset(OpsCommands commands) throws Throwable {
    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    appOpsService.resetAllModes(commands.getUserHandleId(), commands.getPackageName());

  }

  private OpsResult runGetForOps(OpsCommands commands) throws Throwable {

    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    List opsForPackage = appOpsService.getPackagesForOps(commands.getOps());
    ArrayList<PackageOps> packageOpses = new ArrayList<>();

    if (opsForPackage != null) {
      for (Object o : opsForPackage) {
        PackageOps packageOps = ReflectUtils.opsConvert(o);
        addSupport(appOpsService, packageOps, commands.getUserHandleId(), commands.isReqNet());
        packageOpses.add(packageOps);
      }

    }

    return new OpsResult(packageOpses, null);
  }


}
