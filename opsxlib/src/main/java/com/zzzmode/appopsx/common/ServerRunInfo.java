package com.zzzmode.appopsx.common;

import android.os.Parcel;

public class ServerRunInfo implements android.os.Parcelable {

  public String protocolVersion = OpsDataTransfer.PROTOCOL_VERSION;

  public String startArgs;
  public long startTime;
  public long startRealTime;
  public long recvBytes;
  public long sentBytes;

  public long successCount;
  public long errorCount;


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.protocolVersion);
    dest.writeString(this.startArgs);
    dest.writeLong(this.startTime);
    dest.writeLong(this.startRealTime);
    dest.writeLong(this.recvBytes);
    dest.writeLong(this.sentBytes);
    dest.writeLong(this.successCount);
    dest.writeLong(this.errorCount);
  }

  public ServerRunInfo() {
  }

  protected ServerRunInfo(Parcel in) {
    this.protocolVersion = in.readString();
    this.startArgs = in.readString();
    this.startTime = in.readLong();
    this.startRealTime = in.readLong();
    this.recvBytes = in.readLong();
    this.sentBytes = in.readLong();
    this.successCount = in.readLong();
    this.errorCount = in.readLong();
  }

  public static final Creator<ServerRunInfo> CREATOR = new Creator<ServerRunInfo>() {
    @Override
    public ServerRunInfo createFromParcel(Parcel source) {
      return new ServerRunInfo(source);
    }

    @Override
    public ServerRunInfo[] newArray(int size) {
      return new ServerRunInfo[size];
    }
  };


  @Override
  public String toString() {
    return "ServerRunInfo{" +
        "protocolVersion='" + protocolVersion + '\'' +
        ", startArgs='" + startArgs + '\'' +
        ", startTime=" + startTime +
        ", startRealTime=" + startRealTime +
        ", recvBytes=" + recvBytes +
        ", sentBytes=" + sentBytes +
        ", successCount=" + successCount +
        ", errorCount=" + errorCount +
        '}';
  }
}
