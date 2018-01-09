package com.zzzmode.appopsx.common;

import static com.zzzmode.appopsx.common.BaseCaller.TYPE_CLASS;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

/**
 * Created by zl on 2018/1/9.
 */

public class ClassCaller extends CallerMethod {

    private static final Class[] paramsType=new Class[]{Bundle.class};

    private String packageName;
    private String className;

    /**
     *
     * @param packageName package name
     * @param className
     * @param bundle
     */
    public ClassCaller(String packageName, String className, Bundle bundle) {
        this.packageName = packageName;
        this.className = className;
        initParams(paramsType,new Object[]{bundle});
    }

    public ClassCaller(String packageName, String className) {
       this(packageName,className,null);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.className);
        dest.writeStringArray(this.sParamsType);
        dest.writeArray(this.params);
    }

    protected ClassCaller(Parcel in) {
        this.packageName = in.readString();
        this.className = in.readString();
        this.sParamsType = in.createStringArray();
        this.params = in.readArray(Object[].class.getClassLoader());
    }

    public static final Parcelable.Creator<ClassCaller> CREATOR = new Parcelable.Creator<ClassCaller>() {
        @Override
        public ClassCaller createFromParcel(Parcel source) {
            return new ClassCaller(source);
        }

        @Override
        public ClassCaller[] newArray(int size) {
            return new ClassCaller[size];
        }
    };

    @Override
    public int getType() {
        return TYPE_CLASS;
    }


    @Override
    public String toString() {
        return "ClassCaller{" +
            "packageName='" + packageName + '\'' +
            ", className='" + className + '\'' +
            ", cParamsType=" + Arrays.toString(cParamsType) +
            ", sParamsType=" + Arrays.toString(sParamsType) +
            ", params=" + Arrays.toString(params) +
            '}';
    }
}
