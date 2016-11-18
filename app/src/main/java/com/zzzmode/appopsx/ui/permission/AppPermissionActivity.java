package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.os.Bundle;
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
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends AppCompatActivity{

    public static final String EXTRA_APP="extra.app";

    private OpsxManager manager;
    private AppPermissionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opsx);

        AppInfo info= getIntent().getParcelableExtra(EXTRA_APP);

        setTitle(info.appName);


        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        adapter=new AppPermissionAdapter();
        recyclerView.setAdapter(adapter);

        manager=new OpsxManager(getApplicationContext());

        manager.getOpsForPackage(info.packageName).map(new Function<OpsResult, List<OpEntryInfo>>() {
            @Override
            public List<OpEntryInfo> apply(OpsResult opsResult) throws Exception {
                List<PackageOps> opses = opsResult.getList();
                //Log.e("test", "apply --> "+opsResult);
                if(opses != null){
                    List<OpEntryInfo> list=new ArrayList<OpEntryInfo>(2);
                    for (PackageOps opse : opses) {
                        List<OpEntry> ops = opse.getOps();
                        if(ops != null){
                            for (OpEntry op : ops) {
                                list.add(new OpEntryInfo(op));
                            }
                        }
                    }
                    return list;
                }
                return null;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<OpEntryInfo>>() {
            @Override
            public void accept(List<OpEntryInfo> opEntryInfos) throws Exception {
                if(opEntryInfos != null){
                    adapter.setDatas(opEntryInfos);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }




}
