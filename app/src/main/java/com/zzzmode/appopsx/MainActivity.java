package com.zzzmode.appopsx;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ParcelableUtil;

import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private volatile boolean running=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        running=true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running=false;
        if(manager!= null){
            manager.destory();
        }
    }

    public void onClickServer(View view){
       LocalServerManager.getInstance().stop();
    }

    public void onClickClient(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final LocalSocket localSocket=new LocalSocket();
                    localSocket.connect(new LocalSocketAddress("com.zzzmode.appopsx"));

                     OpsDataTransfer transfer=new OpsDataTransfer(localSocket.getOutputStream(),localSocket.getInputStream());
                    transfer.setCallback(new OpsDataTransfer.OnRecvCallback() {
                        @Override
                        public void onMessage(byte[] bytes) {
                            Log.e(TAG, "onMessage --> "+ParcelableUtil.unmarshall(bytes, OpsResult.CREATOR));
                        }
                    });

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SystemClock.sleep(10000);
                            try {
                                localSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    transfer.sendMsg(ParcelableUtil.marshall(new OpsCommands.Builder().setAction("get").setPackageName("com.taobao.trip")));
                    transfer.handleRecv();

                    Log.e(TAG, "run --> end");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    OpsxManager manager;
    public void onManagerClient(View view){
        if(manager == null){

            manager=new OpsxManager(getApplicationContext());
        }
        manager.getOpsForPackage("com.taobao.trip").observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
            @Override
            public void onNext(OpsResult value) {
                Log.e(TAG, "onManagerClient --> "+value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            protected void onStart() {
                super.onStart();
                Log.e(TAG, "onStart --> ");
            }
        });

    }



}
