package com.zzzmode.appopsx.common;

import android.os.Parcelable;


/**
 * Created by zl on 2018/1/9.
 */

public abstract class CallerMethod implements Parcelable{

    protected Class[] cParamsType;
    protected String[] sParamsType;
    protected Object[] params;


    protected void initParams(Class[] paramsType,Object[] params){
        setParamsType(paramsType);
        this.params=params;
    }

    public void setParamsType(Class[] paramsType) {
        if(paramsType != null){
            sParamsType=new String[paramsType.length];
            for (int i = 0; i < paramsType.length; i++) {
                sParamsType[i]=paramsType[i].getName();
            }
        }
    }

    public void setParamsType(String[] paramsType) {
        this.sParamsType=paramsType;
    }


    public Class[] getParamsType() {
        if(sParamsType != null) {
            if(cParamsType == null) {
                cParamsType = ClassUtils.string2Class(sParamsType);
            }
            return cParamsType;
        }
        return null;
    }

    public Object[] getParams() {
        return params;
    }


    public CallerMethod wrapParams(){
        return ParamsFixer.wrap(this);
    }

    public CallerMethod unwrapParams(){
        return ParamsFixer.unwrap(this);
    }


    public abstract int getType();


}
