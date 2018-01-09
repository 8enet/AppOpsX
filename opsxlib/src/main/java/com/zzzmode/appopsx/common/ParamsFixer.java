package com.zzzmode.appopsx.common;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by zl on 2018/1/9.
 */
public class ParamsFixer {


    public static CallerMethod wrap(CallerMethod caller) {
        Object[] params = caller.getParams();
        if (caller.getParamsType() != null && params != null) {
            Class[] paramsType=caller.getParamsType();
            for (int i = 0; i < params.length; i++) {
                params[i] = marshallParamater(paramsType[i],params[i]);
            }
        }
        return caller;
    }

    public static CallerMethod unwrap(CallerMethod caller) {
        Object[] params = caller.getParams();
        if (caller.getParamsType() != null && params != null) {
            Class[] paramsType=caller.getParamsType();
            for (int i = 0; i < params.length; i++) {
                params[i] = unmarshallParamater(paramsType[i],params[i]);
            }
        }
        return caller;
    }

    private static Object marshallParamater(Class type,Object obj) {
        if (FileDescriptor.class.equals(type)  && obj instanceof FileDescriptor) {
            try {
                return ParcelFileDescriptor.dup(((FileDescriptor) obj));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }


    private static Object unmarshallParamater(Class type,Object obj) {
        if (FileDescriptor.class.equals(type)  && obj instanceof ParcelFileDescriptor) {
            return ((ParcelFileDescriptor) obj).getFileDescriptor();
        }
        return obj;
    }
}
