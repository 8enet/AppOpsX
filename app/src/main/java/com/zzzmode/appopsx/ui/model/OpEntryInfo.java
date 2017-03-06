package com.zzzmode.appopsx.ui.model;

import android.app.AppOpsManager;
import android.os.Parcel;

import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.ReflectUtils;

/**
 * Created by zl on 2016/11/18.
 */

public class OpEntryInfo{

    private static Integer sMaxLength=null;

    public OpEntry opEntry;
    public String opName;
    public String opPermsName;
    public String opPermsLab;
    public String opPermsDesc;
    public int mode;
    public int icon;
    public String groupName;

    public OpEntryInfo(OpEntry opEntry){
        if(opEntry != null) {
            this.opEntry = opEntry;
            this.mode = opEntry.getMode();

            if (sMaxLength == null) {
                Object sOpNames = ReflectUtils.getFieldValue(AppOpsManager.class, "sOpNames");
                if (sOpNames instanceof String[]) {
                    sMaxLength = ((String[]) sOpNames).length;
                }
            }

            if (opEntry.getOp() < sMaxLength) {

                Object sOpNames = ReflectUtils.getArrayFieldValue(AppOpsManager.class, "sOpNames", opEntry.getOp());
                if (sOpNames != null) {
                    this.opName = String.valueOf(sOpNames);
                    this.opPermsName = String.valueOf(ReflectUtils.getArrayFieldValue(AppOpsManager.class, "sOpPerms", opEntry.getOp()));
                }
            }
        }
    }

    public boolean isAllowed(){
        return this.mode == AppOpsManager.MODE_ALLOWED;
    }

    public void changeStatus(){
        if(isAllowed()){
            this.mode=AppOpsManager.MODE_IGNORED;
        }else {
            this.mode=AppOpsManager.MODE_ALLOWED;
        }
    }

    @Override
    public String toString() {
        return "OpEntryInfo{" +
                ", opName='" + opName + '\'' +
                ", opPermsName='" + opPermsName + '\'' +
                ", opPermsLab='" + opPermsLab + '\'' +
                ", mode=" + mode +
                '}';
    }
}
