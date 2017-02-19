package com.zzzmode.appopsx;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.RemoteException;

import com.zzzmode.android.opsxpro.BuildConfig;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;

import java.io.IOException;
import java.util.List;

/**
 * Created by zl on 2016/11/13.
 */

public class OpsxManager {

    private Context mContext;

    private LocalServerManager mLocalServerManager;


    public OpsxManager(Context context){
        this(context,new Config());
    }

    public OpsxManager(Context context,Config config){
        mContext=context;
        config.context=mContext;
        mLocalServerManager=LocalServerManager.getInstance(config);
        SConfig.init(context);
        checkFile();
    }

    public void updateConfig(Config config){
        mLocalServerManager.updateConfig(config);
    }

    private void checkFile(){
        //AssetsUtils.copyFile(mContext,"appopsx",new File(mContext.getDir(DIR_NAME,Context.MODE_PRIVATE),"appopsx"),false);
        AssetsUtils.copyFile(mContext,SConfig.JAR_NAME,SConfig.getDestJarFile(), true);
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

    public boolean isRunning(){
        return mLocalServerManager!=null&& mLocalServerManager.isRunning();
    }

    public OpsResult disableAllPermission(final String packageName)throws Exception{
        OpsResult opsForPackage = getOpsForPackage(packageName);
        if(opsForPackage != null ){
            if(opsForPackage.getException() == null){
                List<PackageOps> list = opsForPackage.getList();
                if(list != null && !list.isEmpty()){
                    for (PackageOps packageOps : list) {
                        List<OpEntry> ops = packageOps.getOps();
                        if(ops !=null){
                            for (OpEntry op : ops) {
                                if(op.getMode() != AppOpsManager.MODE_IGNORED){
                                    setOpsMode(packageName,op.getOp(),AppOpsManager.MODE_IGNORED);
                                }
                            }
                        }
                    }
                }
            }else {
                throw new Exception(opsForPackage.getException());
            }
        }
        return opsForPackage;
    }


    public static class Config{
        public boolean allowBgRunning=false;
        public String logFile;
        public boolean useAdb=false;
        public String adbHost="127.0.0.1";
        public int adbPort=5555;
        Context context;
    }
}
