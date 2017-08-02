package android.content.pm;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by zl on 2017/7/31.
 */

public interface IPackageManager {
  PackageInfo getPackageInfo(String packageName, int flags, int userId);
  PermissionInfo getPermissionInfo(String name, int flags);
  PermissionGroupInfo getPermissionGroupInfo(String name, int flags);
  void grantRuntimePermission(String packageName, String permissionName, int userId);

  void revokeRuntimePermission(String packageName, String permissionName, int userId);

  void resetRuntimePermissions();

  int getPermissionFlags(String permissionName, String packageName, int userId);
  String[] getAppOpPermissionPackages(String permissionName);
  ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId);

  ApplicationInfo getApplicationInfo(String packageName, int flags ,int userId);


  int getPackageUid(String packageName,int flags , int userId);

  // for API 23 or lower
  int getPackageUid(String packageName, int userId);

}
