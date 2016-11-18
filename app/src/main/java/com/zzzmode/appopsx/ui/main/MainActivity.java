package com.zzzmode.appopsx.ui.main;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.model.AppInfo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private volatile boolean running=false;

    private MainListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        running=true;

        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getApplicationContext(),R.drawable.list_divider_h),true));

        adapter=new MainListAdapter();
        recyclerView.setAdapter(adapter);

        Observable.create(new ObservableOnSubscribe<AppInfo>() {
            @Override
            public void subscribe(ObservableEmitter<AppInfo> e) throws Exception {
                PackageManager packageManager = getPackageManager();
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                for (PackageInfo installedPackage : installedPackages) {
                    if( (installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        AppInfo info = new AppInfo();
                        info.packageName = installedPackage.packageName;
                        info.appName = String.valueOf(installedPackage.applicationInfo.loadLabel(packageManager));
                        info.icon = installedPackage.applicationInfo.loadIcon(packageManager);
                        e.onNext(info);
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<AppInfo>() {
            @Override
            public void onNext(AppInfo value) {
                adapter.addItem(value);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running=false;
//        if(manager!= null){
//            manager.destory();
//        }
    }

//    public void onClickServer(View view){
//
//    }
//
//    public void onClickClient(View view){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final LocalSocket localSocket=new LocalSocket();
//                    localSocket.connect(new LocalSocketAddress("com.zzzmode.appopsx"));
//
//                     OpsDataTransfer transfer=new OpsDataTransfer(localSocket.getOutputStream(),localSocket.getInputStream());
//                    transfer.setCallback(new OpsDataTransfer.OnRecvCallback() {
//                        @Override
//                        public void onMessage(byte[] bytes) {
//                            Log.e(TAG, "onMessage --> "+ParcelableUtil.unmarshall(bytes, OpsResult.CREATOR));
//                        }
//                    });
//
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            SystemClock.sleep(10000);
//                            try {
//                                localSocket.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//
//                    transfer.sendMsg(ParcelableUtil.marshall(new OpsCommands.Builder().setAction("get").setPackageName("com.taobao.trip")));
//                    transfer.handleRecv();
//
//                    Log.e(TAG, "run --> end");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//
//
//    OpsxManager manager;
//    public void onManagerClient(View view){
//        if(manager == null){
//
//            manager=new OpsxManager(getApplicationContext());
//        }
//        manager.getOpsForPackage("com.taobao.trip").observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
//            @Override
//            public void onNext(OpsResult value) {
//                Log.e(TAG, "onManagerClient --> "+value);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//
//            @Override
//            protected void onStart() {
//                super.onStart();
//                Log.e(TAG, "onStart --> ");
//            }
//        });
//
//    }



}
