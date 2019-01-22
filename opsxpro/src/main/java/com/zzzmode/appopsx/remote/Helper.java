package com.zzzmode.appopsx.remote;

import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import com.zzzmode.appopsx.common.FixCompat;
import com.zzzmode.appopsx.common.ReflectUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zl on 2017/1/15.
 */
class Helper {

  static int getPackageUid(String packageName, int userId) {
    int uid = -1;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uid = ActivityThread.getPackageManager().getPackageUid(packageName,PackageManager.MATCH_UNINSTALLED_PACKAGES,userId);
      } else {
        uid = ActivityThread.getPackageManager().getPackageUid(packageName, userId);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }

    if (uid == -1) {
      try {
        ApplicationInfo applicationInfo = ActivityThread.getPackageManager()
            .getApplicationInfo(packageName, 0, userId);
        List<Class> paramsType = new ArrayList<>(2);
        paramsType.add(int.class);
        paramsType.add(int.class);
        List<Object> params = new ArrayList<>(2);
        params.add(userId);
        params.add(applicationInfo.uid);
        uid = (int) ReflectUtils.invokMethod(UserHandle.class, "getUid", paramsType, params);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    return uid;
  }

  private static Map<String, Integer> sRuntimePermToOp = null;

  static int permissionToCode(String permission) {
    if (sRuntimePermToOp == null) {
      sRuntimePermToOp = new HashMap<>();
      String[] opPerms = FixCompat.sOpPerms();
      int[] opToSwitch = FixCompat.sOpToSwitch();

      if (opPerms != null && opToSwitch != null && opPerms.length == opToSwitch.length) {
        for (int i = 0; i < opToSwitch.length; i++) {
          if (opPerms[i] != null) {
            sRuntimePermToOp.put(opPerms[i], opToSwitch[i]);
          }
        }
      }
    }
    Integer code = sRuntimePermToOp.get(permission);
    if (code != null) {
      return code;
    }
    return -1;
  }

}
