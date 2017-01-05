package com.zzzmode.appopsx;

import android.content.Context;
import android.os.RemoteException;

import com.zzzmode.android.opsxpro.BuildConfig;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;

import java.io.IOException;

/**
 * Created by zl on 2016/11/13.
 */

public class OpsxManager {

    private Context mContext;

    private LocalServerManager mLocalServerManager;

    public OpsxManager(Context context){
        mContext=context;
        mLocalServerManager=LocalServerManager.getInstance();
        SConfig.init(context);
        checkFile();
    }

    private void checkFile(){
        //AssetsUtils.copyFile(mContext,"appopsx",new File(mContext.getDir(DIR_NAME,Context.MODE_PRIVATE),"appopsx"),false);
        AssetsUtils.copyFile(mContext,SConfig.JAR_NAME,SConfig.getDestJarFile(), BuildConfig.DEBUG);
    }


    public OpsResult getOpsForPackage(final String packageName) throws Exception{
        OpsCommands.Builder builder=new OpsCommands.Builder();
        builder.setAction(OpsCommands.ACTION_GET);
        builder.setPackageName(packageName);
        return mLocalServerManager.exec(builder);
    }

    public OpsResult setOpsMode(String packageName,int opInt,int modeInt)throws Exception{
        OpsCommands.Builder builder=new OpsCommands.Builder();
        builder.setAction(OpsCommands.ACTION_SET);
        builder.setPackageName(packageName);
        builder.setOpInt(opInt);
        builder.setModeInt(modeInt);
        return mLocalServerManager.exec(builder);
    }

    public OpsResult resetAllModes(String packageName)throws Exception{
        OpsCommands.Builder builder=new OpsCommands.Builder();
        builder.setAction(OpsCommands.ACTION_RESET);
        builder.setPackageName(packageName);
        return mLocalServerManager.exec(builder);
    }

    public void destory(){
        if(mLocalServerManager != null){
            mLocalServerManager.stop();
        }
    }

}
