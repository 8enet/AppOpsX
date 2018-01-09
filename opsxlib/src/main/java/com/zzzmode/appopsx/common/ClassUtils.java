package com.zzzmode.appopsx.common;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zl on 2018/1/9.
 */

public class ClassUtils {

    private static final Map<String,Class> sDefaultClassMap =new HashMap<>();
    private static final LruCache<String,Class> sClassCache=new LruCache<>(128);
    static {
        defCacheClass(byte.class);
        defCacheClass(boolean.class);
        defCacheClass(short.class);
        defCacheClass(char.class);
        defCacheClass(int.class);
        defCacheClass(float.class);
        defCacheClass(long.class);
        defCacheClass(double.class);

        defCacheClass(String.class);
        defCacheClass(Bundle.class);
        defCacheClass(ComponentName.class);
        defCacheClass(Message.class);
        defCacheClass(ParcelFileDescriptor.class);
        defCacheClass(ResultReceiver.class);
        defCacheClass(WorkSource.class);
        defCacheClass(Intent.class);
        defCacheClass(IntentFilter.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defCacheClass(UserHandle.class);
        }


        defCacheClass(byte[].class);
        defCacheClass(int[].class);
        defCacheClass(String[].class);
        defCacheClass(Intent[].class);
    }

    private static void defCacheClass(Class cls){
        sDefaultClassMap.put(cls.getName(),cls);
    }

    public static Class[] string2Class(String... names){
        if(names != null){
            Class[] ret=new Class[names.length];
            for (int i = 0; i < names.length; i++) {
                ret[i]=string2Class(names[i]);
            }
            return ret;
        }
        return null;
    }

    public static Class string2Class(String name){
        try {
            Class cls= sDefaultClassMap.get(name);
            if(cls == null){
                cls=sClassCache.get(name);
            }
            if(cls == null) {
                cls = Class.forName(name, false, null);
                sClassCache.put(name,cls);
            }
            return cls;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
