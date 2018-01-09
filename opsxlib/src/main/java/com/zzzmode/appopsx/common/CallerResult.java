package com.zzzmode.appopsx.common;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zl on 2018/1/9.
 */

public class CallerResult implements Parcelable {
    private byte[] reply;
    private Throwable throwable;
    private Class returnType;

    private Object replyObj;

    public byte[] getReply() {
        return reply;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Class getReturnType() {
        return returnType;
    }

    public Object getReplyObj() {
        if(replyObj == null && reply != null){
            replyObj=ParcelableUtil.readValue(reply);
        }
        return replyObj;
    }

    public Bundle getReplyBundle() {
        Object replyObj = getReplyObj();
        if(replyObj instanceof Bundle){
            return ((Bundle) replyObj);
        }
        return null;
    }

    public void setReply(byte[] reply) {
        this.reply = reply;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void setReturnType(Class returnType) {
        this.returnType = returnType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.reply);
        dest.writeSerializable(this.throwable);
        dest.writeSerializable(this.returnType);
    }

    public CallerResult() {
    }

    protected CallerResult(Parcel in) {
        this.reply = in.createByteArray();
        this.throwable = (Throwable) in.readSerializable();
        this.returnType = (Class) in.readSerializable();
    }

    public static final Creator<CallerResult> CREATOR = new Creator<CallerResult>() {
        @Override
        public CallerResult createFromParcel(Parcel source) {
            return new CallerResult(source);
        }

        @Override
        public CallerResult[] newArray(int size) {
            return new CallerResult[size];
        }
    };


    @Override
    public String toString() {
        return "CallerResult{" +
                "reply=" + getReplyObj() +
                ", throwable=" + throwable +
                ", returnType=" + returnType +
                '}';
    }
}
