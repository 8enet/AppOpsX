package com.zzzmode.appopsx;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by zl on 2016/11/18.
 */

class SConfig {

    //每次动态生成socket path，避免越权访问
    private static final String SOCKET_PATH_TEMP = "appopsx_socket_";
    private static final String LOCAL_SERVER_PATH="l_server";

    static final String DIR_NAME="opsx";
    static final String JAR_NAME="appopsx.jar";

    private static String sExecPrefix=null;
    private static File destJarFile;
    private static String sClassPath=null;
    private static SharedPreferences sPreferences;


    static void init(Context context){
        sExecPrefix=context.getDir(DIR_NAME,Context.MODE_PRIVATE).getAbsolutePath();
        destJarFile=new File(context.getDir(DIR_NAME,Context.MODE_PRIVATE),JAR_NAME);
        sClassPath=destJarFile.getAbsolutePath();
        sPreferences = context.getSharedPreferences("sp_app_opsx", Context.MODE_PRIVATE);
    }

    static String getExecPrefix(){
        return sExecPrefix;
    }

    static File getDestJarFile(){
        return destJarFile;
    }

    static String getClassPath(){
        return sClassPath;
    }


    static String generateDomainName(){
        String path=SOCKET_PATH_TEMP+AssetsUtils.generateToken(16);
        sPreferences.edit().putString(LOCAL_SERVER_PATH,path).apply();
        return path;
    }


    static String getLocalServerPath(){
        String path= sPreferences.getString(LOCAL_SERVER_PATH,null);
        if(TextUtils.isEmpty(path)){
            path= generateDomainName();
        }
        return path;
    }

}
