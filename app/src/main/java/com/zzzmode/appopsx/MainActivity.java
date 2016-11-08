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
import com.zzzmode.appopsx.common.ParcelableUtil;

import java.io.IOException;

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
    }

    public void onClickServer(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {

                //copy file


            }
        }).start();
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

}
