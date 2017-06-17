package com.zzzmode.appopsx.ui.model;

import com.zzzmode.appopsx.common.OpsResult;

/**
 * Created by zl on 2016/11/24.
 */

public class AppOpEntry {

  public AppInfo appInfo;
  public OpsResult opsResult;

  public OpsResult modifyResult;

  public AppOpEntry(AppInfo appInfo, OpsResult opsResult) {
    this.appInfo = appInfo;
    this.opsResult = opsResult;
  }

  @Override
  public String toString() {
    return "AppOpEntry{" +
        "appInfo=" + appInfo +
        ", opsResult=" + opsResult +
        ", modifyResult=" + modifyResult +
        '}';
  }
}
