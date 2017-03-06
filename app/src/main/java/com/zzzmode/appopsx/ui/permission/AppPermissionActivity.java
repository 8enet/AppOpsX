package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends BaseActivity {

    private static final String TAG = "AppPermissionActivity";

    public static final String EXTRA_APP = "extra.app";
    public static final String EXTRA_APP_PKGNAME="extra.app.packagename";
    public static final String EXTRA_APP_NAME="extra.app.name";


    private ProgressBar mProgressBar;

    private AppPermissionAdapter adapter;
    private AppInfo appInfo;

    private TextView tvError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opsx);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appInfo = getIntent().getParcelableExtra(EXTRA_APP);
        if(appInfo == null){
            String pkgName=getIntent().getStringExtra(EXTRA_APP_PKGNAME);
            String name=getIntent().getStringExtra(EXTRA_APP_NAME);
            if(pkgName != null && name != null){
                appInfo=new AppInfo();
                appInfo.packageName=pkgName;
                appInfo.appName=name;
            }else {
                finish();
                return;
            }

        }

        setTitle(appInfo.appName);

        tvError = (TextView) findViewById(R.id.tv_error);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if(AppOpsx.getInstance(getApplicationContext()).isRunning()){
            mProgressBar.setVisibility(View.GONE);
        }else {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider_h), true));

        adapter = new AppPermissionAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setListener(new AppPermissionAdapter.OnSwitchItemClickListener() {
            @Override
            public void onSwitch(OpEntryInfo info, boolean v) {
                if(v){
                    info.mode=AppOpsManager.MODE_ALLOWED;
                }else {
                    info.mode=AppOpsManager.MODE_IGNORED;
                }
                Map<String,String> map=new HashMap<String, String>(2);
                map.put("new_mode",String.valueOf(info.mode));
                map.put("op_name",info.opName);
                ATracker.send(AEvent.C_PERM_ITEM,map);

                setMode(info);
            }
        });

        initData(appInfo.packageName);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
//            case R.id.action_reset:
//                resetMode();
//                return true;
            case R.id.action_hide_perm:
                showHidePerms();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {


        getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem menuShowAllPerm = menu.findItem(R.id.action_hide_perm);
        MenuItem menuShowOpDesc = menu.findItem(R.id.action_show_op_perm);
        MenuItem menuShowOpName = menu.findItem(R.id.action_show_op_name);
        MenuItem menuShowPremTime = menu.findItem(R.id.action_show_perm_time);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        final Map<MenuItem,String> menus=new HashMap<>();
        menus.put(menuShowAllPerm,"key_show_no_prems");
        menus.put(menuShowOpDesc,"key_show_op_desc");
        menus.put(menuShowOpName,"key_show_op_name");
        menus.put(menuShowPremTime,"key_show_perm_time");

        MenuItem.OnMenuItemClickListener itemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String s = menus.get(item);
                if(s != null) {
                    item.setChecked(!item.isChecked());
                    sp.edit().putBoolean(s, item.isChecked()).apply();
                    ActivityCompat.invalidateOptionsMenu(AppPermissionActivity.this);
                    initData(appInfo.packageName);
                }
                return true;
            }
        };


        Set<Map.Entry<MenuItem, String>> entries = menus.entrySet();
        for (Map.Entry<MenuItem, String> entry : entries) {
            entry.getKey().setChecked(sp.getBoolean(entry.getValue(),false));
            entry.getKey().setOnMenuItemClickListener(itemClickListener);
        }

        return true;
    }



    private void initData(String packageName){
        Helper.getAppPermission(getApplicationContext(),packageName,PreferenceManager.getDefaultSharedPreferences(this).getBoolean("key_show_no_prems",false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<List<OpEntryInfo>>() {

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onNext(List<OpEntryInfo> opEntryInfos) {
                        mProgressBar.setVisibility(View.GONE);

                        if (opEntryInfos != null && !opEntryInfos.isEmpty()) {

                            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            adapter.setShowConfig(sp.getBoolean("key_show_op_desc",false),
                                    sp.getBoolean("key_show_op_name",false),
                                    sp.getBoolean("key_show_perm_time",false));
                            adapter.setDatas(opEntryInfos);
                            adapter.notifyDataSetChanged();
                        } else {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText(R.string.no_perms);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(getString(R.string.error_msg,Log.getStackTraceString(e)));

                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }



    private void setMode(final OpEntryInfo info){
        Helper.setMode(getApplicationContext(),appInfo.packageName,info)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
            @Override
            public void onNext(OpsResult value) {

                if(value.getException() ==null){

                }
            }

            @Override
            public void onError(Throwable e) {

                info.mode=info.opEntry.getMode();
                adapter.updateItem(info);

                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void resetMode(){
        Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

                OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).resetAllModes(appInfo.packageName);
                if(opsForPackage != null ){
                    if(opsForPackage.getException() == null){
                        e.onNext(opsForPackage);
                    }else {
                        throw new Exception(opsForPackage.getException());
                    }
                }
                e.onComplete();

            }
        }).retry(5, new Predicate<Throwable>() {
            @Override
            public boolean test(Throwable throwable) throws Exception {
                return throwable instanceof IOException || throwable instanceof NullPointerException;
            }
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
            @Override
            public void onNext(OpsResult value) {

            }

            @Override
            public void onError(Throwable e) {

                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {
                initData(appInfo.packageName);
            }
        });
    }

    private void showHidePerms(){


    }


}
