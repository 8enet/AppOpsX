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
    Parcel parcel = unmarshall(bytes);
    return creator.createFromParcel(parcel);
  }

  public static Parcel unmarshall(byte[] bytes) {
    Parcel parcel = Parcel.obtain();
    parcel.unmarshall(bytes, 0, bytes.length);
    parcel.setDataPosition(0);
    return parcel;
  }
}