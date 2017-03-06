package com.zzzmode.appopsx;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by zl on 2016/11/18.
 */

class SConfig {

    static final String SOCKET_PATH = "appopsx_zzzmode_socket";
    private static final String LOCAL_TOKEN ="l_token";

    static final String DIR_NAME="opsx";
    static final String JAR_NAME="appopsx.jar";

    private static File destJarFile;
    private static String sClassPath=null;
    private static SharedPreferences sPreferences;


    static void init(Context context){
        //sExecPrefix=context.getDir(DIR_NAME,Context.MODE_PRIVATE).getAbsolutePath();
        //destJarFile=new File(context.getDir(DIR_NAME,Context.MODE_PRIVATE),JAR_NAME);
        destJarFile=new File(context.getExternalFilesDir(DIR_NAME),JAR_NAME);
        sClassPath=destJarFile.getAbsolutePath();
        sPreferences = context.getSharedPreferences("sp_app_opsx", Context.MODE_PRIVATE);
    }


    static File getDestJarFile(){
        return destJarFile;
    }

    static String getClassPath(){
        return sClassPath;
    }


    static String getLocalToken(){
        String path= sPreferences.getString(LOCAL_TOKEN,null);
        if(TextUtils.isEmpty(path)){
            path= AssetsUtils.generateToken(16);
            sPreferences.edit().putString(LOCAL_TOKEN,path).apply();
        }
        return path;
    }

    static int getPort(){
        return 52053;
    }

}
