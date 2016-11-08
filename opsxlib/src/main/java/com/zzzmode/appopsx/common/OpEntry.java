package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

public class OpEntry implements Parcelable {
    private final int mOp;
    private final int mMode;
    private final long mTime;
    private final long mRejectTime;
    private final int mDuration;
    private final int mProxyUid;
    private final String mProxyPackageName;

    public OpEntry(int op, int mode, long time, long rejectTime, int duration,
                   int proxyUid, String proxyPackage) {
        mOp = op;
        mMode = mode;
        mTime = time;
        mRejectTime = rejectTime;
        mDuration = duration;
        mProxyUid = proxyUid;
        mProxyPackageName = proxyPackage;
    }

    public int getOp() {
        return mOp;
    }

    public int getMode() {
        return mMode;
    }

    public long getTime() {
        return mTime;
    }

    public long getRejectTime() {
        return mRejectTime;
    }

    public boolean isRunning() {
        return mDuration == -1;
    }

    public int getDuration() {
        return mDuration == -1 ? (int) (System.currentTimeMillis() - mTime) : mDuration;
    }

    public int getProxyUid() {
        return mProxyUid;
    }

    public String getProxyPackageName() {
        return mProxyPackageName;
    }


    @Override
    public String toString() {
        return "OpEntry{" +
                "mOp=" + mOp +
                ", mMode=" + mMode +
                ", mTime=" + mTime +
                ", mRejectTime=" + mRejectTime +
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
        dest.writeLong(this.mTime);
        dest.writeLong(this.mRejectTime);
        dest.writeInt(this.mDuration);
        dest.writeInt(this.mProxyUid);
        dest.writeString(this.mProxyPackageName);
    }

    protected OpEntry(Parcel in) {
        this.mOp = in.readInt();
        this.mMode = in.readInt();
        this.mTime = in.readLong();
        this.mRejectTime = in.readLong();
        this.mDuration = in.readInt();
        this.mProxyUid = in.readInt();
        this.mProxyPackageName = in.readString();
    }

    public static final Parcelable.Creator<OpEntry> CREATOR = new Parcelable.Creator<OpEntry>() {
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