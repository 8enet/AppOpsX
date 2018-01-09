package android.app;

import android.annotation.TargetApi;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.RemoteException;

/**
 * Created by zl on 2017/10/12.
 */

public interface IActivityManager {

  int broadcastIntent(IApplicationThread caller, Intent intent,
      String resolvedType, IIntentReceiver resultTo, int resultCode,
      String resultData, Bundle map, String requiredPermissions,
      int appOp, boolean serialized, boolean sticky, int userId)
      throws RemoteException;

  @TargetApi(VERSION_CODES.M)
  int broadcastIntent(IApplicationThread caller, Intent intent,
      String resolvedType, IIntentReceiver resultTo, int resultCode,
      String resultData, Bundle map, String[] requiredPermissions,
      int appOp, Bundle options, boolean serialized, boolean sticky, int userId)
      throws RemoteException;

}
