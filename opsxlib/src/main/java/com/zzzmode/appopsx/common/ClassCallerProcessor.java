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
    private ServerRunInfo mServerRunInfo;

  public ClassCallerProcessor(Context mPackageContext, Context mSystemContext,
      byte[] bytes) {
    this.mPackageContext = mPackageContext;
    this.mSystemContext = mSystemContext;
    this.mServerRunInfo = ParcelableUtil.unmarshall(bytes,ServerRunInfo.CREATOR);
  }

  protected ServerRunInfo getServerRunInfo() {
        return mServerRunInfo;
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
