package com.zzzmode.appopsx.ui.main;

import android.app.AppOpsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zzzmode.appopsx.OpsxManager;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.decoration.SimpleListDividerDecorator;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.AppOpEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private MainListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getApplicationContext(),R.drawable.list_divider_h),true));

        adapter=new MainListAdapter();
        recyclerView.setAdapter(adapter);

        Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<AppInfo>> e) throws Exception {
                PackageManager packageManager = getPackageManager();
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                List<AppInfo> appInfos=new ArrayList<AppInfo>();
                for (PackageInfo installedPackage : installedPackages) {
                    if( (installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        AppInfo info = new AppInfo();
                        info.packageName = installedPackage.packageName;
                        info.appName = String.valueOf(installedPackage.applicationInfo.loadLabel(packageManager));
                        info.icon = installedPackage.applicationInfo.loadIcon(packageManager);
                        appInfos.add(info);
                    }
                }
                Collections.sort(appInfos, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo o1, AppInfo o2) {
                        return o1.appName.compareTo(o2.appName);
                    }
                });
                e.onNext(appInfos);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<List<AppInfo>>() {
            @Override
            public void onNext(List<AppInfo> value) {
                adapter.showItems(value);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                resetAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ops_menu, menu);
        return true;
    }

    private void resetAll(){
        if(adapter!= null && adapter.appInfos!=null){
            final List<AppInfo> appInfos = adapter.appInfos;
            Observable.fromIterable(appInfos).concatMap(new Function<AppInfo, ObservableSource<AppOpEntry>>() {
                @Override
                public ObservableSource<AppOpEntry> apply(AppInfo info) throws Exception {

                    return Observable.just(info).map(new Function<AppInfo, AppOpEntry>() {
                        @Override
                        public AppOpEntry apply(AppInfo info) throws Exception {
                            OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).getOpsForPackage(info.packageName);
                            if(opsForPackage != null ){
                                if(opsForPackage.getException() == null){
                                    return new AppOpEntry(info,opsForPackage);
                                }else {
                                    throw new Exception(opsForPackage.getException());
                                }
                            }
                            throw new RuntimeException("getOpsForPackage fail: "+info);
                        }
                    });
                }
            }).filter(new Predicate<AppOpEntry>() {
                @Override
                public boolean test(AppOpEntry appOpEntry) throws Exception {

                    List<PackageOps> list = appOpEntry.opsResult.getList();
                    if(list != null){
                        for (PackageOps packageOps : list) {
                            List<OpEntry> ops = packageOps.getOps();
                            if(ops !=null){
                                for (OpEntry op : ops) {

                                    if(op.getMode() == AppOpsManager.MODE_ERRORED){
                                        //Log.e(TAG, "test --> "+op);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            }).concatMap(new Function<AppOpEntry, ObservableSource<AppOpEntry>>() {
                @Override
                public ObservableSource<AppOpEntry> apply(AppOpEntry appOpEntry) throws Exception {
                    return Observable.just(appOpEntry).map(new Function<AppOpEntry, AppOpEntry>() {
                        @Override
                        public AppOpEntry apply(AppOpEntry appOpEntry) throws Exception {
                            List<PackageOps> list = appOpEntry.opsResult.getList();
                            if(list != null){
                                OpsxManager opsxManager = AppOpsx.getInstance(getApplicationContext());
                                for (PackageOps packageOps : list) {
                                    List<OpEntry> ops = packageOps.getOps();
                                    if(ops !=null){
                                        for (OpEntry op : ops) {

                                            if(op.getMode() == AppOpsManager.MODE_ERRORED){
                                                //Log.e(TAG, "test --> "+op);
                                                appOpEntry.modifyResult=opsxManager.setOpsMode(appOpEntry.appInfo.packageName,op.getOp(),AppOpsManager.MODE_IGNORED);
                                            }
                                        }
                                    }
                                }
                            }

                            return appOpEntry;
                        }
                    });
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResourceObserver<AppOpEntry>() {
                        @Override
                        public void onNext(AppOpEntry value) {
                            Log.e(TAG, "onNext --> "+value);

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
