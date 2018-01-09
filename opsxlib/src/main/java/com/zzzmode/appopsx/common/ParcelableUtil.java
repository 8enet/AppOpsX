package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUtil {

  public static byte[] marshall(Parcelable parceable) {

    Parcel parcel = Parcel.obtain();
    parceable.writeToParcel(parcel, 0);
    byte[] bytes = parcel.marshall();
    parcel.recycle();
    return bytes;
  }

  public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
    if(bytes == null){
      return null;
    }
    Parcel parcel = unmarshall(bytes);
    return creator.createFromParcel(parcel);
  }

  public static Parcel unmarshall(byte[] bytes) {
    if(bytes == null){
      return null;
    }
    Parcel parcel = Parcel.obtain();
    parcel.unmarshall(bytes, 0, bytes.length);
    parcel.setDataPosition(0);
    return parcel;
  }


  public static Object readValue(byte[] bytes){
    if(bytes == null){
      return null;
    }
    Parcel unmarshall = unmarshall(bytes);
    Object o = unmarshall.readValue(ParcelableUtil.class.getClassLoader());
    unmarshall.recycle();
    return o;
  }
}