package com.zzzmode.appopsx.server;

import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.SparseArray;

import com.android.internal.app.IAppOpsService;
import com.zzzmode.appopsx.common.ReflectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zl on 2017/1/15.
 */
class Helper {
    static int getPackageUid(String packageName, int flag) {
        int uid = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<Class> paramsType = new ArrayList<>();
            paramsType.add(String.class);
            paramsType.add(int.class);
            paramsType.add(int.class);
            List<Object> params = new ArrayList<>();
            params.add(packageName);
            params.add(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            params.add(flag);
            uid = (int) ReflectUtils.invokMethod(ActivityThread.getPackageManager(), "getPackageUid", paramsType, params);
        } else {
            List<Class> paramsType = new ArrayList<>();
            paramsType.add(String.class);
            paramsType.add(int.class);
            List<Object> params = new ArrayList<>();
            params.add(packageName);
            params.add(flag);
            uid = (int) ReflectUtils.invokMethod(ActivityThread.getPackageManager(), "getPackageUid", paramsType, params);
        }

        return uid;
    }

    private static Map<String,Integer> sRuntimePermToOp = null;

    static int permissionToCode(String permission) {
        if (sRuntimePermToOp == null) {
            sRuntimePermToOp = new HashMap<>();
            Object sOpPerms = ReflectUtils.getFieldValue(AppOpsManager.class, "sOpPerms");
            Object sOpToSwitch = ReflectUtils.getFieldValue(AppOpsManager.class, "sOpToSwitch");

            if (sOpPerms instanceof String[] && sOpToSwitch instanceof int[]) {
                String[] opPerms = (String[]) sOpPerms;
                int[] opToSwitch = (int[]) sOpToSwitch;

                if (opPerms.length == opToSwitch.length) {
                    for (int i = 0; i < opToSwitch.length; i++) {
                        if (opPerms[i] != null) {
                            sRuntimePermToOp.put(opPerms[i],opToSwitch[i]);
                        }
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
