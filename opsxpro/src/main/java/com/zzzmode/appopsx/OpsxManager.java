package com.zzzmode.appopsx;

import android.content.Context;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsResult;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

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
        AssetsUtils.copyFile(mContext,SConfig.JAR_NAME,SConfig.getDestJarFile(),false);
    }


    public Observable<OpsResult> getOpsForPackage(final String packageName){

        return Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult>  subscriber) {
                OpsResult exec = null;
                try {
                    OpsCommands.Builder builder=new OpsCommands.Builder();
                    builder.setAction(OpsCommands.ACTION_GET);
                    builder.setPackageName(packageName);
                    exec = mLocalServerManager.exec(builder);
                    subscriber.onNext(exec);
                    subscriber.onComplete();
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

}
