package com.zzzmode.appopsx.common;

public class OpEntry {
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
}