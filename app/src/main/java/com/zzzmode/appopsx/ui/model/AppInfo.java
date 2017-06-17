package com.zzzmode.appopsx.ui.model;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zl on 2016/11/18.
 */

public class AppInfo implements Parcelable {

  public String appName;
  public String packageName;
  public long time;
  public long installTime;
  public long updateTime;
  public String pinyin;
  public ApplicationInfo applicationInfo;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AppInfo appInfo = (AppInfo) o;

    return packageName != null ? packageName.equals(appInfo.packageName)
        : appInfo.packageName == null;

  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.appName);
    dest.writeString(this.packageName);
  }

  public AppInfo() {
  }

  protected AppInfo(Parcel in) {
    this.appName = in.readString();
    this.packageName = in.readString();
  }

  public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
    @Override
    public AppInfo createFromParcel(Parcel source) {
      return new AppInfo(source);
    }

    @Override
    public AppInfo[] newArray(int size) {
      return new AppInfo[size];
    }
  };


  @Override
  public String toString() {
    return "AppInfo{" +
        "appName='" + appName + '\'' +
        ", packageName='" + packageName + '\'' +
        '}';
  }
}
