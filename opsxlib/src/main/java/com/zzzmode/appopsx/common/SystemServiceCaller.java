package com.zzzmode.appopsx.common;

import static com.zzzmode.appopsx.common.BaseCaller.TYPE_SYSTEM_SERVICE;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zl on 2018/1/9.
 */

public class SystemServiceCaller extends CallerMethod {
    private String serviceName;
    private String methodName;

    /**
     *
     * @param serviceName SystemService name, Like {@link android.content.Context#APP_OPS_SERVICE}
     * @param methodName service aidl methodName,
     * @param paramsType
     * @param params
     */
    public SystemServiceCaller(String serviceName, String methodName, Class[] paramsType, Object[] params) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        initParams(paramsType,params);
    }

    public SystemServiceCaller(String serviceName, String methodName) {
        this.serviceName = serviceName;
        this.methodName = methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceName);
        dest.writeString(this.methodName);
        dest.writeStringArray(this.sParamsType);
        dest.writeArray(this.params);
    }

    protected SystemServiceCaller(Parcel in) {
        this.serviceName = in.readString();
        this.methodName = in.readString();
        this.sParamsType = in.createStringArray();
        this.params = in.readArray(Object[].class.getClassLoader());
    }

    public static final Parcelable.Creator<SystemServiceCaller> CREATOR = new Parcelable.Creator<SystemServiceCaller>() {
        @Override
        public SystemServiceCaller createFromParcel(Parcel source) {
            return new SystemServiceCaller(source);
        }

        @Override
        public SystemServiceCaller[] newArray(int size) {
            return new SystemServiceCaller[size];
        }
    };

    @Override
    public int getType() {
        return TYPE_SYSTEM_SERVICE;
    }
}
