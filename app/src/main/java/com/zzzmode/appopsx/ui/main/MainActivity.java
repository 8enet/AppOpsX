package com.zzzmode.appopsx.ui.main;

import android.app.AppOpsManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.zzzmode.appopsx.OpsxManager;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.AppOpEntry;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private MainListAdapter adapter;

    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);

        recyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getApplicationContext(),R.drawable.list_divider_h),true));

        adapter=new MainListAdapter();
        recyclerView.setAdapter(adapter);

        boolean showSysApp= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("show_sysapp",false);
        Helper.getInstalledApps(getApplicationContext(),showSysApp).subscribeOn(Schedulers.io())
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
                mProgressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                openSetting();
                return true;
            case R.id.action_premission_sort:
                openSortPremission();
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

    private void openSetting(){
        startActivity(new Intent(this,SettingsActivity.class));
    }

    private void openSortPremission(){
        startActivity(new Intent(this,PremissionGroupActivity.class));
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
