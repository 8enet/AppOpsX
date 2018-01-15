package com.zzzmode.appopsx;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.util.Log;
import com.zzzmode.appopsx.common.CallerResult;
import com.zzzmode.appopsx.common.ClassCaller;
import com.zzzmode.appopsx.common.SystemServiceCaller;
import com.zzzmode.appopsx.remote.RestartHandler;
import java.util.List;

public class ApiSupporter {

  private static final String TAG = "ApiSupporter";

  private LocalServerManager localServerManager;

  ApiSupporter(LocalServerManager localServerManager) {
    this.localServerManager = localServerManager;
  }

  private void checkConnection() throws Exception {
    localServerManager.start();
  }

  public List<PackageInfo> getInstalledPackages(int flags,int uid) throws Exception {
    checkConnection();
    SystemServiceCaller caller = new SystemServiceCaller("package","getInstalledPackages",new Class[]{int.class,int.class},new Object[]{flags,uid});
    CallerResult callerResult = localServerManager.execNew(caller);
    callerResult.getReplyObj();
    if(callerResult.getThrowable() != null){
      throw new Exception(callerResult.getThrowable());
    }else {
      Object replyObj = callerResult.getReplyObj();
      if(replyObj instanceof List){
        return ((List<PackageInfo>) replyObj);
      }
    }

    return null;
  }

  public List<UserInfo> getUsers(boolean excludeDying) throws Exception {
    checkConnection();
    SystemServiceCaller caller = new SystemServiceCaller(
        Context.USER_SERVICE,"getUsers",new Class[]{boolean.class},new Object[]{excludeDying});
    CallerResult callerResult = localServerManager.execNew(caller);
    callerResult.getReplyObj();
    if(callerResult.getThrowable() != null){
      throw new Exception(callerResult.getThrowable());
    } else {
      Object replyObj = callerResult.getReplyObj();
      if(replyObj instanceof List){
        return ((List<UserInfo>) replyObj);
      }
    }
    return null;
  }


  public void restartServer(Context context) throws Exception {
    ClassCaller classCaller = new ClassCaller(context.getPackageName(), RestartHandler.class.getName(),new Bundle());
    CallerResult result = localServerManager.execNew(classCaller);
    Log.e(TAG, "restartServer --> "+result);
  }

}
