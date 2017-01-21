package com.zzzmode.appopsx.ui.model;

import java.util.List;

public class AppPremissions {
    public AppInfo appInfo;
    public List<OpEntryInfo> opEntries;

    public boolean hasPremissions() {
        return opEntries != null && !opEntries.isEmpty();
    }

    @Override
    public String toString() {
        return "AppPremissions{" +
                "appInfo=" + appInfo +
                ", opEntries=" + opEntries +
                '}';
    }
}