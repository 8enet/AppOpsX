package com.zzzmode.appopsx.ui.model;

import android.app.AppOpsManager;
import android.os.Parcel;

import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.ReflectUtils;

/**
 * Created by zl on 2016/11/18.
 */

public class OpEntryInfo {
    public OpEntry opEntry;
    public String opName;
    public String opPermsName;
    public String opPermsLab;

    public OpEntryInfo(OpEntry opEntry){
        this.opEntry=opEntry;
        this.opName= String.valueOf(ReflectUtils.getArrayFieldValue(AppOpsManager.class,"sOpNames",opEntry.getOp()));
        this.opPermsName=String.valueOf(ReflectUtils.getArrayFieldValue(AppOpsManager.class,"sOpPerms",opEntry.getOp()));
    }

}
