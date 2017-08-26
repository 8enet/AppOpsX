package com.zzzmode.appopsx.common;

import android.Manifest;
import android.util.SparseArray;

/**
 * Created by zl on 2017/2/24.
 */

public final class OtherOp {

  public static final int OP_ACCESS_PHONE_DATA = 51001;

  public static final int OP_ACCESS_WIFI_NETWORK = 52002;


  public static final String OP_NAME_ACCESS_PHONE_DATA = "ACCESS_PHONE_DATA";
  public static final String OP_NAME_ACCESS_WIFI_NETWORK = "ACCESS_WIFI_NETWORK";

  private static Boolean sSupportCount = null;

  private static final SparseArray<String> mData = new SparseArray<>();
  private static final SparseArray<String> mPerms = new SparseArray<>();

  static {
    mData.put(OP_ACCESS_PHONE_DATA, OP_NAME_ACCESS_PHONE_DATA);
    mData.put(OP_ACCESS_WIFI_NETWORK, OP_NAME_ACCESS_WIFI_NETWORK);

    mPerms.put(OP_ACCESS_PHONE_DATA, Manifest.permission.INTERNET);
    mPerms.put(OP_ACCESS_WIFI_NETWORK, Manifest.permission.INTERNET);
  }

  public static boolean isOtherOp(int op) {
    return op == OP_ACCESS_PHONE_DATA || op == OP_ACCESS_WIFI_NETWORK;
  }

  public static boolean isOtherOp(String opName) {
    return OP_NAME_ACCESS_PHONE_DATA.equals(opName) || OP_NAME_ACCESS_WIFI_NETWORK.equals(opName);
  }


  public static String getOpName(int op) {
    return mData.get(op);
  }

  public static String getOpPermName(int op) {
    return mPerms.get(op);
  }

  public static boolean isSupportCount(){
    if(sSupportCount == null){
      try {
        Class<?> aClass = Class.forName("android.app.AppOpsManager$OpEntry", false,
            ClassLoader.getSystemClassLoader());
        sSupportCount = ReflectUtils.hasField(aClass,"mAllowedCount");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return sSupportCount;
  }


}
