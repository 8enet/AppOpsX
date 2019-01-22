package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class OpEntry implements Parcelable {

  private final int mOp;
  private final int mMode;
  private final long[] mTimes;
  private final long[] mRejectTimes;
  private final int mDuration;
  private final int mProxyUid;
  private final boolean mRunning;
  private final String mProxyPackageName;

  /**
   * {@see https://github.com/LineageOS/android_frameworks_base/blob/lineage-16.0/core/java/android/app/AppOpsManager.java}
   */
  private final int mAllowedCount;
  private final int mIgnoredCount;

  // Metrics about an op when its uid is persistent.
  public static final int UID_STATE_PERSISTENT = 0;
  // Metrics about an op when its uid is at the top.
  public static final int UID_STATE_TOP = 1;
  // Metrics about an op when its uid is running a foreground service.
  public static final int UID_STATE_FOREGROUND_SERVICE = 2;
  // Last UID state in which we don't restrict what an op can do.
  public static final int UID_STATE_LAST_NON_RESTRICTED = UID_STATE_FOREGROUND_SERVICE;
  // Metrics about an op when its uid is in the foreground for any other reasons.
  public static final int UID_STATE_FOREGROUND = 3;
  // Metrics about an op when its uid is in the background for any reason.
  public static final int UID_STATE_BACKGROUND = 4;
  // Metrics about an op when its uid is cached.
  public static final int UID_STATE_CACHED = 5;
  // Number of uid states we track.
  public static final int _NUM_UID_STATE = 6;

  public OpEntry(int op, int mode, long time, long rejectTime, int duration,
      int proxyUid, String proxyPackage,int allowedCount,int ignoredCount) {
    mOp = op;
    mMode = mode;
    mTimes = new long[_NUM_UID_STATE];
    mRejectTimes = new long[_NUM_UID_STATE];
    mTimes[0] = time;
    mRejectTimes[0] = rejectTime;
    mDuration = duration;
    mRunning = duration == -1;
    mProxyUid = proxyUid;
    mProxyPackageName = proxyPackage;
    mAllowedCount = allowedCount;
    mIgnoredCount = ignoredCount;
  }

  public OpEntry(int op, int mode, long[] times, long[] rejectTimes, int duration,
                 boolean running, int proxyUid, String proxyPackage,
                 int allowedCount, int ignoredCount) {
    mOp = op;
    mMode = mode;
    mTimes = new long[_NUM_UID_STATE];
    mRejectTimes = new long[_NUM_UID_STATE];
    System.arraycopy(times, 0, mTimes, 0, _NUM_UID_STATE);
    System.arraycopy(rejectTimes, 0, mRejectTimes, 0, _NUM_UID_STATE);
    mDuration = duration;
    mRunning = running;
    mProxyUid = proxyUid;
    mProxyPackageName = proxyPackage;
    mAllowedCount = allowedCount;
    mIgnoredCount = ignoredCount;
  }

  public OpEntry(int op, int mode, long[] times, long[] rejectTimes, int duration,
                 int proxyUid, String proxyPackage, int allowedCount, int ignoredCount) {
    this(op, mode, times, rejectTimes, duration, duration == -1, proxyUid, proxyPackage,
            allowedCount, ignoredCount);
  }

  public OpEntry(int op, int mode, long time, long rejectTime, int duration,
                 int proxyUid, String proxyPackage) {
    this(op, mode, time, rejectTime, duration, proxyUid, proxyPackage,
            0, 0);
  }


  public int getOp() {
    return mOp;
  }

  public int getMode() {
    return mMode;
  }

  public long getTime() {
    return maxTime(mTimes, 0, _NUM_UID_STATE);
  }

  public long getRejectTime() {
    return maxTime(mRejectTimes, 0, _NUM_UID_STATE);
  }

  public boolean isRunning() {
    return mDuration == -1;
  }

  public int getDuration() {
    return mDuration;
  }

  public int getProxyUid() {
    return mProxyUid;
  }

  public String getProxyPackageName() {
    return mProxyPackageName;
  }

  public int getAllowedCount() {
    return mAllowedCount;
  }

  public int getIgnoredCount() {
    return mIgnoredCount;
  }

  @Override
  public String toString() {
    return "OpEntry{" +
        "mOp=" + mOp +
        ", mMode=" + mMode +
        ", mTime=" + Arrays.toString(mTimes) +
        ", mRejectTime=" + Arrays.toString(mRejectTimes) +
        ", mDuration=" + mDuration +
        ", mProxyUid=" + mProxyUid +
        ", mProxyPackageName='" + mProxyPackageName + '\'' +
        '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.mOp);
    dest.writeInt(this.mMode);
    dest.writeLongArray(this.mTimes);
    dest.writeLongArray(this.mRejectTimes);
    dest.writeInt(this.mDuration);
    dest.writeByte((byte) (this.mRunning ? 1 : 0));
    dest.writeInt(this.mProxyUid);
    dest.writeString(this.mProxyPackageName);
    dest.writeInt(this.mAllowedCount);
    dest.writeInt(this.mIgnoredCount);
  }

  protected OpEntry(Parcel in) {
    this.mOp = in.readInt();
    this.mMode = in.readInt();
    this.mTimes = in.createLongArray();
    this.mRejectTimes = in.createLongArray();
    this.mDuration = in.readInt();
    this.mRunning = in.readByte() != 0;
    this.mProxyUid = in.readInt();
    this.mProxyPackageName = in.readString();
    this.mAllowedCount = in.readInt();
    this.mIgnoredCount = in.readInt();
  }

  public static long maxTime(long[] times, int start, int end) {
    long time = 0;
    for (int i = start; i < end; i++) {
      if (times[i] > time) {
        time = times[i];
      }
    }
    return time;
  }

  public static final Creator<OpEntry> CREATOR = new Creator<OpEntry>() {
    @Override
    public OpEntry createFromParcel(Parcel source) {
      return new OpEntry(source);
    }

    @Override
    public OpEntry[] newArray(int size) {
      return new OpEntry[size];
    }
  };
}