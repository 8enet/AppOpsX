package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.decoration.SimpleListDividerDecorator;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends BaseActivity {

    private static final String TAG = "AppPermissionActivity";

    public static final String EXTRA_APP = "extra.app";

    private AppPermissionAdapter adapter;
    private AppInfo appInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opsx);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appInfo= getIntent().getParcelableExtra(EXTRA_APP);

        setTitle(appInfo.appName);

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
            case R.id.action_reset:
                resetMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void initData(String packageName){
        getAppInfo(packageName)
                .map(new Function<List<OpEntryInfo>, List<OpEntryInfo>>() {
                    @Override
                    public List<OpEntryInfo> apply(List<OpEntryInfo> opEntryInfos) throws Exception {
                        Collections.sort(opEntryInfos, new Comparator<OpEntryInfo>() {
                            @Override
                            public int compare(OpEntryInfo o1, OpEntryInfo o2) {
                                if(o1.opPermsLab == null && o2.opPermsLab != null){
                                    return 1;
                                }
                                if(o1.opPermsDesc == null && o2.opPermsDesc != null){
                                    return 1;
                                }
                                if(o1.opPermsLab != null && o2.opPermsLab != null && o1.opPermsDesc!=null && o2.opPermsDesc != null){
                                    return 0;
                                }
                                if(o1.opPermsLab != null && o2.opPermsLab != null && o1.opPermsDesc == null && o2.opPermsDesc == null){
                                    return o1.opPermsLab.compareTo(o2.opPermsLab);
                                }
                                return -1;
                            }
                        });
                        return opEntryInfos;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceObserver<List<OpEntryInfo>>() {

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onNext(List<OpEntryInfo> opEntryInfos) {
                        if (opEntryInfos != null) {
                            adapter.setDatas(opEntryInfos);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError --> ", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<List<OpEntryInfo>> getAppInfo(final String packageName) {
        return Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

                OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).getOpsForPackage(packageName);
                if(opsForPackage != null ){
                    if(opsForPackage.getException() == null){
                        e.onNext(opsForPackage);
                    }else {
                        throw new Exception(opsForPackage.getException());
                    }
                }
                e.onComplete();

            }
        })
                .retry(5, new Predicate<Throwable>() {
                    @Override
                    public boolean test(Throwable throwable) throws Exception {
                        return throwable instanceof IOException || throwable instanceof NullPointerException;
                    }
                })
                .subscribeOn(Schedulers.io()).map(new Function<OpsResult, List<OpEntryInfo>>() {
                    @Override
                    public List<OpEntryInfo> apply(OpsResult opsResult) throws Exception {
                        List<PackageOps> opses = opsResult.getList();
                        if (opses != null) {
                            List<OpEntryInfo> list = new ArrayList<OpEntryInfo>();
                            PackageManager pm = getPackageManager();
                            for (PackageOps opse : opses) {
                                List<OpEntry> ops = opse.getOps();
                                if (ops != null) {
                                    for (OpEntry op : ops) {
                                        OpEntryInfo opEntryInfo = new OpEntryInfo(op);
                                        try {
                                            PermissionInfo permissionInfo = pm.getPermissionInfo(opEntryInfo.opPermsName, 0);
                                            opEntryInfo.opPermsLab = String.valueOf(permissionInfo.loadLabel(pm));
                                            opEntryInfo.opPermsDesc = String.valueOf(permissionInfo.loadDescription(pm));
                                        } catch (PackageManager.NameNotFoundException e) {
                                            //ignore
                                        }
                                        list.add(opEntryInfo);
                                    }
                                }

                            }
                            return list;
                        }
                        return Collections.emptyList();
                    }
                });
    }


    private void setMode(final OpEntryInfo info){
        Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

                OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).setOpsMode(appInfo.packageName,info.opEntry.getOp(),info.mode);
                if(opsForPackage != null ){
                    if(opsForPackage.getException() == null){
                        e.onNext(opsForPackage);
                    } else {
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
                if(value.getException() !=null){

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
}
