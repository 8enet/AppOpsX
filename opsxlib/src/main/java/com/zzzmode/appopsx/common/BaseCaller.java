package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by zl on 2018/1/9.
 */

public class BaseCaller implements Parcelable {

    public static final int TYPE_CLOSE = -10;
    public static final int TYPE_SYSTEM_SERVICE=1;
    public static final int TYPE_STATIC_METHOD=2;
    public static final int TYPE_CLASS=3;


    private int type;
    private byte[] rawBytes;

    public BaseCaller(CallerMethod method) {
        this.type = method.getType();
        this.rawBytes = ParcelableUtil.marshall(method);
    }

    public BaseCaller(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeByteArray(this.rawBytes);
    }


    protected BaseCaller(Parcel in) {
        this.type = in.readInt();
        this.rawBytes = in.createByteArray();
    }

    public static final Parcelable.Creator<BaseCaller> CREATOR = new Parcelable.Creator<BaseCaller>() {
        @Override
        public BaseCaller createFromParcel(Parcel source) {
            return new BaseCaller(source);
        }

        @Override
        public BaseCaller[] newArray(int size) {
            return new BaseCaller[size];
        }
    };
}
