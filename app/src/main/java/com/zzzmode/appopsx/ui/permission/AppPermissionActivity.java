package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.zzzmode.appopsx.OpsxManager;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ReflectUtils;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends AppCompatActivity {

    private static final String TAG = "AppPermissionActivity";

    public static final String EXTRA_APP = "extra.app";

    private AppPermissionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opsx);

        final AppInfo info = getIntent().getParcelableExtra(EXTRA_APP);

        setTitle(info.appName);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        adapter = new AppPermissionAdapter();
        recyclerView.setAdapter(adapter);


        getAppInfo(info.packageName)
                .subscribeOn(Schedulers.io())
                .retry(10, new Predicate<Throwable>() {
                    @Override
                    public boolean test(Throwable throwable) throws Exception {
                        Log.e(TAG, "test --> retry "+throwable+"   "+Thread.currentThread());
                        return throwable instanceof RemoteException;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new Consumer<List<OpEntryInfo>>() {
                    @Override
                    public void accept(List<OpEntryInfo> opEntryInfos) throws Exception {
                        if (opEntryInfos != null) {
                            adapter.setDatas(opEntryInfos);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

    }


    private Observable<List<OpEntryInfo>> getAppInfo(final String packageName) {
        return Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

                Log.e(TAG, "subscribe --> ");
                OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).getOpsForPackage(packageName);
                e.onNext(opsForPackage);
                e.onComplete();

            }
        }).retry(5).subscribeOn(Schedulers.io()).map(new Function<OpsResult, List<OpEntryInfo>>() {
            @Override
            public List<OpEntryInfo> apply(OpsResult opsResult) throws Exception {
                List<PackageOps> opses = opsResult.getList();
                Log.e("test", "apply --> " + opsResult);
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
                                } catch (PackageManager.NameNotFoundException e) {
                                    //e.printStackTrace();
                                }
                                list.add(opEntryInfo);
                            }
                        }

                        PackageInfo packageInfo = pm.getPackageInfo(opse.getPackageName(), PackageManager.GET_PERMISSIONS);
                        Log.e("test", "apply --> " + Arrays.toString(packageInfo.requestedPermissions));


                    }
                    return list;
                }
                return null;
            }
        });
    }


}
