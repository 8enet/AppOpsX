package com.zzzmode.appopsx.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;


/**
 * Created by zl on 2018/1/9.
 */
public abstract class ClassCallerProcessor {
    private Context mPackageContext;
    private Context mSystemContext;

    public ClassCallerProcessor() {
    }

    public void setPackageContext(Context packageContext) {
        this.mPackageContext = packageContext;
    }

    public void setSystemContext(Context systemContext) {
        this.mSystemContext = systemContext;
    }

    protected Context getSystemContext(){
        return mSystemContext;
    }

    /**
     * get current package context
     * @return
     */
    protected Context getPackageContext() {
        return mPackageContext;
    }

    /**
     * Processes a method invocation on a proxy instance and returns
     * the result. This method will be invoked on an invocation handler
     * when a method is invoked on a proxy instance that it is
     * associated with.
     * <p>
     * <strong>Note:</strong>This method invoke on ROOT process, DON'T across processes access object !
     *
     * @param args
     * @return put your result in Bundle
     * @throws Throwable
     */
    public abstract Bundle proxyInvoke(Bundle args) throws Throwable;
}
