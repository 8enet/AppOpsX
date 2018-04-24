package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

import android.os.Parcelable.Creator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zl on 2016/11/6.
 */

public class OpsCommands implements Parcelable {

  public static final String ACTION_GET = "get";
  public static final String ACTION_SET = "set";
  public static final String ACTION_RESET = "reset";
  public static final String ACTION_GET_FOR_OPS ="get_f_ops";
  public static final String ACTION_GET_APPS = "get_apps";
  public static final String ACTION_OTHER = "other";


  private String action = ACTION_GET;
  private String packageName;
  private int userHandleId;
  private int opInt;
  private int modeInt;
  private int[] ops;
  private boolean reqNet;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public int getUserHandleId() {
    return userHandleId;
  }

  public void setUserHandleId(int userHandleId) {
    this.userHandleId = userHandleId;
  }

  public int getOpInt() {
    return opInt;
  }

  public void setOpInt(int opInt) {
    this.opInt = opInt;
  }

  public int getModeInt() {
    return modeInt;
  }

  public void setModeInt(int modeInt) {
    this.modeInt = modeInt;
  }

  public int[] getOps() {
    return ops;
  }

  public void setOps(int[] ops) {
    this.ops = ops;
  }

  public boolean isReqNet() {
    return reqNet;
  }

  public void setReqNet(boolean reqNet) {
    this.reqNet = reqNet;
  }

  @Override
  public String toString() {
    return "OpsCommands{" +
        "action='" + action + '\'' +
        ", packageName='" + packageName + '\'' +
        ", userHandleId=" + userHandleId +
        ", opInt=" + opInt +
        ", modeInt=" + modeInt +
        '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.action);
    dest.writeString(this.packageName);
    dest.writeInt(this.userHandleId);
    dest.writeInt(this.opInt);
    dest.writeInt(this.modeInt);
    dest.writeIntArray(this.ops);
    dest.writeByte(this.reqNet ? (byte) 1 : (byte) 0);
  }

  public OpsCommands() {
  }

  protected OpsCommands(Parcel in) {
    this.action = in.readString();
    this.packageName = in.readString();
    this.userHandleId = in.readInt();
    this.opInt = in.readInt();
    this.modeInt = in.readInt();
    this.ops = in.createIntArray();
    this.reqNet = in.readByte() != 0;
  }

  public static final Creator<OpsCommands> CREATOR = new Creator<OpsCommands>() {
    @Override
    public OpsCommands createFromParcel(Parcel source) {
      return new OpsCommands(source);
    }

    @Override
    public OpsCommands[] newArray(int size) {
      return new OpsCommands[size];
    }
  };
}
