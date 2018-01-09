package com.zzzmode.appopsx;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import com.zzzmode.appopsx.common.CallerResult;
import com.zzzmode.appopsx.common.ClassCaller;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.SystemServiceCaller;
import com.zzzmode.appopsx.remote.AppOpsHandler;
import java.io.File;
import java.util.List;

/**
 * Created by zl on 2016/11/13.
 */

public class OpsxManager {

  private static final String TAG = "OpsxManager";

  private Context mContext;

  private LocalServerManager mLocalServerManager;

  private int mUserHandleId;

  private int userId;

  private static String pkgName;

  private ApiSupporter apiSupporter;

  public OpsxManager(Context context) {
    this(context, new Config());
  }

  public OpsxManager(Context context, Config config) {
    mContext = context;
    config.context = mContext;
    mUserHandleId = Process.myUid() / 100000; //android.os.UserHandle.myUserId()
    SConfig.init(context, mUserHandleId);
    userId = mUserHandleId;
    mLocalServerManager = LocalServerManager.getInstance(config);
    apiSupporter = new ApiSupporter(mLocalServerManager);
    pkgName = context.getPackageName();
    checkFile();
  }

  public void setUserHandleId(int uid) {
    this.userId = uid;
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
    AssetsUtils.copyFile(mContext.getApplicationInfo().nativeLibraryDir+ File.separator+"libopsxstart.so",SConfig.getDestExecuableFile(),true);
    AssetsUtils.writeScript(getConfig());
  }

  private synchronized void checkConnect() throws Exception {
    mLocalServerManager.start();
  }

  public OpsResult getOpsForPackage(final String packageName) throws Exception {
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_GET);
    builder.setPackageName(packageName);
    builder.setUserHandleId(userId);


    return wrapOps(builder);
  }


  private OpsResult wrapOps(OpsCommands.Builder builder) throws Exception {
    Bundle bundle = new Bundle();
    bundle.putParcelable("args",builder);
    ClassCaller classCaller = new ClassCaller(pkgName,AppOpsHandler.class.getName(),bundle);
    CallerResult result = mLocalServerManager.execNew(classCaller);
    Bundle replyBundle = result.getReplyBundle();
    return replyBundle.getParcelable("return");
  }

  public OpsResult getPackagesForOps(int[] ops,boolean reqNet)throws Exception{
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_GET_FOR_OPS);
    builder.setOps(ops);
    builder.setReqNet(reqNet);
    builder.setUserHandleId(userId);
    return wrapOps(builder);
  }

  public OpsResult setOpsMode(String packageName, int opInt, int modeInt) throws Exception {
    checkConnect();
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_SET);
    builder.setPackageName(packageName);
    builder.setOpInt(opInt);
    builder.setModeInt(modeInt);
    builder.setUserHandleId(userId);
    return wrapOps(builder);
  }

  public OpsResult resetAllModes(String packageName) throws Exception {
    OpsCommands.Builder builder = new OpsCommands.Builder();
    builder.setAction(OpsCommands.ACTION_RESET);
    builder.setPackageName(packageName);
    builder.setUserHandleId(userId);
    return wrapOps(builder);
  }



  public ApiSupporter getApiSupporter() {
    return apiSupporter;
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
      mLocalServerManager.closeBgServer();
      mLocalServerManager.stop();
    }

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
