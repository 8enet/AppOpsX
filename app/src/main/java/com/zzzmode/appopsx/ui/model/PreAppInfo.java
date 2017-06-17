package com.zzzmode.appopsx.ui.model;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2017/4/30.
 */

public class PreAppInfo {

  private String packageName;
  private String ignoredOps;
  private List<Integer> ops;

  public PreAppInfo(String packageName, String ignoredOps) {
    this.packageName = packageName;
    this.ignoredOps = ignoredOps;
  }

  public PreAppInfo(String packageName) {
    this.packageName = packageName;
  }

  public void setIgnoredOps(String ignoredOps) {
    this.ignoredOps = ignoredOps;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getIgnoredOps() {
    return ignoredOps;
  }

  public List<Integer> getOps() {
    if (ops == null) {
      ops = new ArrayList<>();
      if (!TextUtils.isEmpty(ignoredOps)) {
        String[] split = ignoredOps.split(",");
        for (String s : split) {
          try {
            ops.add(Integer.valueOf(s));
          } catch (NumberFormatException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return ops;
  }


  @Override
  public String toString() {
    return "PreAppInfo{" +
        "packageName='" + packageName + '\'' +
        ", ignoredOps='" + ignoredOps + '\'' +
        '}';
  }
}
