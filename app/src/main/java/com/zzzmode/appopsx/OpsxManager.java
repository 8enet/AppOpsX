package com.zzzmode.appopsx;

import android.content.Context;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by zl on 2016/11/13.
 */

public class OpsxManager {

    public static final String DIR_NAME="opsx";
    public static final String JAR_NAME="appopsx.jar";

    private static File destJarFile;
    private Context mContext;

    private LocalServerManager mLocalServerManager;

    public OpsxManager(Context context){
        mContext=context;
        destJarFile=getDestJarFile(context);
        mLocalServerManager=LocalServerManager.getInstance();
        checkFile();
    }

    private void checkFile(){
        AssetsUtils.copyFile(mContext,"appopsx",new File(mContext.getDir(DIR_NAME,Context.MODE_PRIVATE),"appopsx"),false);
        AssetsUtils.copyFile(mContext,JAR_NAME,destJarFile,false);
    }


    public Observable<OpsResult> getOpsForPackage(final String packageName){



        return Observable.create(new Observable.OnSubscribe<OpsResult>() {
            @Override
            public void call(Subscriber<? super OpsResult> subscriber) {
                OpsResult exec = null;
                try {
                    subscriber.onStart();
                    OpsCommands.Builder builder=new OpsCommands.Builder();
                    builder.setAction(OpsCommands.ACTION_GET);
                    builder.setPackageName(packageName);
                    exec = mLocalServerManager.exec(builder);
                    subscriber.onNext(exec);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        }).subscribeOn(Schedulers.computation());
    }

    public void destory(){
        if(mLocalServerManager != null){
            mLocalServerManager.stop();
        }
    }

    public static File getDestJarFile(Context context){
        if(destJarFile == null){
            destJarFile=new File(context.getDir(DIR_NAME,Context.MODE_PRIVATE),JAR_NAME);
        }
        return destJarFile;
    }

}
