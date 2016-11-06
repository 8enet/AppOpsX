package com.zzzmode.appopsx.common;

import java.util.List;

public class PackageOps {
    private final String mPackageName;
    private final int mUid;
    private final List<OpEntry> mEntries;

    public PackageOps(String packageName, int uid, List<OpEntry> entries) {
        mPackageName = packageName;
        mUid = uid;
        mEntries = entries;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getUid() {
        return mUid;
    }

    public List<OpEntry> getOps() {
        return mEntries;
    }

    @Override
    public String toString() {
        return "PackageOps{" +
                "mPackageName='" + mPackageName + '\'' +
                ", mUid=" + mUid +
                ", mEntries=" + mEntries +
                '}';
    }
}