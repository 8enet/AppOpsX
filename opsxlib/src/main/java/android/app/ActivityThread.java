package android.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;

/**
 * Created by zl on 2016/11/5.
 */

public class ActivityThread {

  public static IPackageManager getPackageManager() {
    return null;
  }

  public static ActivityThread systemMain() {
    return null;
  }

  public static ActivityThread currentActivityThread() {
    return null;
  }


  public static Application currentApplication() {
   return null;
  }

  public static String currentProcessName() {
    return null;
  }

  public ContextImpl getSystemContext() {
    return null;
  }

  public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {

  }
}

