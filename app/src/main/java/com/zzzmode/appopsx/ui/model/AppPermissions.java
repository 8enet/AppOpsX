package com.zzzmode.appopsx.ui.model;

import java.util.List;

public class AppPermissions {

  public AppInfo appInfo;
  public List<OpEntryInfo> opEntries;

  public boolean hasPermissions() {
    return opEntries != null && !opEntries.isEmpty();
  }

  @Override
  public String toString() {
    return "AppPermissions{" +
        "appInfo=" + appInfo +
        ", opEntries=" + opEntries +
        '}';
  }
}