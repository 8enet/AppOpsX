package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by zl on 2016/11/8.
 */

public class OpsResult implements Parcelable {

  private Throwable exception;
  private List<PackageOps> list;

  public OpsResult(List<PackageOps> list, Throwable exception) {
    this.exception = exception;
    this.list = list;
  }


  public Throwable getException() {
    return exception;
  }

  public List<PackageOps> getList() {
    return list;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(this.exception);
    dest.writeTypedList(this.list);
  }

  protected OpsResult(Parcel in) {
    this.exception = (Exception) in.readSerializable();
    this.list = in.createTypedArrayList(PackageOps.CREATOR);
  }

  public static final Parcelable.Creator<OpsResult> CREATOR = new Parcelable.Creator<OpsResult>() {
    @Override
    public OpsResult createFromParcel(Parcel source) {
      return new OpsResult(source);
    }

    @Override
    public OpsResult[] newArray(int size) {
      return new OpsResult[size];
    }
  };


  @Override
  public String toString() {
    return "OpsResult{" +
        "exception=" + exception +
        ", list=" + list +
        '}';
  }
}
