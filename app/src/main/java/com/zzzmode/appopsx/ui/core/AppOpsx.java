package com.zzzmode.appopsx.ui.core;

import android.content.Context;

import com.zzzmode.appopsx.OpsxManager;

/**
 * Created by zl on 2016/11/19.
 */

public class AppOpsx {

    private static OpsxManager sManager;

    public static OpsxManager getInstance(Context context) {
        if(sManager == null){
            synchronized (AppOpsx.class){
                if(sManager == null){
                    sManager=new OpsxManager(context.getApplicationContext());
                }
            }
        }
        return sManager;
    }
}
