package com.zzzmode.appopsx.remote;

import android.Manifest;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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

  @Override
  public Bundle proxyInvoke(Bundle args) throws Throwable {

    OpsResult result = null;

    try {
      OpsCommands.Builder builder = args.getParcelable("args");
      args.clear();
      FLog.log(" appops "+builder);
      result = handleCommand(builder);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      result = new OpsResult(null,throwable);
    }
    args.putParcelable("return",result);
    return args;
  }

  private OpsResult handleCommand(OpsCommands.Builder builder) throws Throwable {
    String s = builder.getAction();
    OpsResult result = null;
    if (OpsCommands.ACTION_GET.equals(s)) {
      result = runGet(builder);
    } else if (OpsCommands.ACTION_SET.equals(s)) {
      runSet(builder);
    } else if (OpsCommands.ACTION_RESET.equals(s)) {
      runReset(builder);
    } else if (OpsCommands.ACTION_GET_FOR_OPS.equals(s)) {
      result = runGetForOps(builder);
    }
    return result;
  }


  private OpsResult runGet(OpsCommands.Builder getBuilder) throws Throwable {


    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));
    String packageName = getBuilder.getPackageName();

    int uid = Helper.getPackageUid(packageName, getBuilder.getUserHandleId());

    List opsForPackage = appOpsService.getOpsForPackage(uid, packageName, null);
    ArrayList<PackageOps> packageOpses = new ArrayList<>();
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

  private void runReset(OpsCommands.Builder builder) throws Throwable {
    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    appOpsService.resetAllModes(builder.getUserHandleId(), builder.getPackageName());

  }

  private OpsResult runGetForOps(OpsCommands.Builder builder) throws Throwable {

    final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
        ServiceManager.getService(Context.APP_OPS_SERVICE));

    List opsForPackage = appOpsService.getPackagesForOps(builder.getOps());
    ArrayList<PackageOps> packageOpses = new ArrayList<>();

    if (opsForPackage != null) {
      for (Object o : opsForPackage) {
        PackageOps packageOps = ReflectUtils.opsConvert(o);
        addSupport(appOpsService, packageOps, builder.getUserHandleId(), builder.isReqNet());
        packageOpses.add(packageOps);
      }

    }

    return new OpsResult(packageOpses, null);
  }


}
