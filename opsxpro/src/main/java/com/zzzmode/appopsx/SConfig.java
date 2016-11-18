package com.zzzmode.appopsx;

import android.content.Context;

import java.io.File;

/**
 * Created by zl on 2016/11/18.
 */

class SConfig {

    static final String DIR_NAME="opsx";
    static final String JAR_NAME="appopsx.jar";

    private static String sExecPrefix=null;
    private static File destJarFile;
    private static String sClassPath=null;

    static void init(Context context){
        sExecPrefix=context.getDir(DIR_NAME,Context.MODE_PRIVATE).getAbsolutePath();
        destJarFile=new File(context.getDir(DIR_NAME,Context.MODE_PRIVATE),JAR_NAME);
        sClassPath=destJarFile.getAbsolutePath();
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

}
