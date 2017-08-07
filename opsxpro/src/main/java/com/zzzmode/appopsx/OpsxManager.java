package com.zzzmode.appopsx;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;

import java.util.List;

/**
 * Created by zl on 2016/11/13.
 */

public class OpsxManager {

  private static final String TAG = "OpsxManager";

  private Context mContext;

  private LocalServerManager mLocalServerManager;

  private int mUserHandleId;

  public OpsxManager(Context context) {
    this(context, new Config());
  }

  public OpsxManager(Context context, Config config) {
    mContext = context;
    config.context = mContext;
    mUserHandleId = Process.myUid() / 100000; //android.os.UserHandle.myUserId()
    SConfig.init(context, mUserHandleId);
    mLocalServerManager = LocalServerManager.getInstance(config);
    checkFile();
  }

  public void updateConfig(Config config) {
    mLocalServerManager.updateConfig(config);
  }

  public Config getConfig() {
    return mLocalServerManager.getConfig();
  }

  private void checkFile() {
    //AssetsUtils.copyFile(mContext,"appopsx",new File(mContext.getDir(DIR_NAME,Context.MODE_PRIVATE),"appopsx"),false);
    AssetsUtils.copyFile(mContext, SConfig.JAR_NAME, SConfig.getDestJarFile(), true);
  }

  private synchronized void checkConnect() throws Exception {
    mLocalServerManager.start();
  }

  public OpsResult getOpsForPackage(final String packageName) throws Exception {
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_GET);
    builder.setPackageName(packageName);
    builder.setUserHandleId(mUserHandleId);
    return mLocalServerManager.exec(builder);
  }

  public OpsResult getPackagesForOps(int[] ops,boolean reqNet)throws Exception{
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_GET_FOR_OPS);
    builder.setOps(ops);
    builder.setReqNet(reqNet);
    builder.setUserHandleId(mUserHandleId);
    return mLocalServerManager.exec(builder);
  }

  public OpsResult setOpsMode(String packageName, int opInt, int modeInt) throws Exception {
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_SET);
    builder.setPackageName(packageName);
    builder.setOpInt(opInt);
    builder.setModeInt(modeInt);
    builder.setUserHandleId(mUserHandleId);
    return mLocalServerManager.exec(builder);
  }

  public OpsResult resetAllModes(String packageName) throws Exception {
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_RESET);
    builder.setPackageName(packageName);
    builder.setUserHandleId(mUserHandleId);
    return mLocalServerManager.exec(builder);
  }

  public void destory() {
    if (mLocalServerManager != null) {
      mLocalServerManager.stop();
    }
  }

  public boolean isRunning() {
    return mLocalServerManager != null && mLocalServerManager.isRunning();
  }

  public OpsResult disableAllPermission(final String packageName) throws Exception {
    OpsResult opsForPackage = getOpsForPackage(packageName);
    if (opsForPackage != null) {
      if (opsForPackage.getException() == null) {
        List<PackageOps> list = opsForPackage.getList();
        if (list != null && !list.isEmpty()) {
          for (PackageOps packageOps : list) {
            List<OpEntry> ops = packageOps.getOps();
            if (ops != null) {
              for (OpEntry op : ops) {
                if (op.getMode() != AppOpsManager.MODE_IGNORED) {
                  setOpsMode(packageName, op.getOp(), AppOpsManager.MODE_IGNORED);
                }
              }
            }
          }
        }
      } else {
        throw new Exception(opsForPackage.getException());
      }
    }
    return opsForPackage;
  }


  public void closeBgServer() {
    if(mLocalServerManager != null){
      mLocalServerManager.stop();
    }
    LocalServerManager.closeBgServer();
  }

  public static boolean isEnableSELinux() {
    return AssetsUtils.isEnableSELinux();
  }

  public static class Config {

    public boolean allowBgRunning = false;
    public String logFile;
    public boolean printLog = false;
    public boolean useAdb = false;
    public boolean rootOverAdb = false;
    public String adbHost = "127.0.0.1";
    public int adbPort = 5555;
    Context context;
  }
}
