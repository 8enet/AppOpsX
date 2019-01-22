package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * am start -n com.zzzmode.appopsx/.ui.permission.AppPermissionActivity --es pkgName "com.zzzmode.appopsx"
 * am start "appops://details?id=com.zzzmode.appopsx"
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends BaseActivity implements IPermView {

  private static final String TAG = "AppPermissionActivity";

  public static final String EXTRA_APP = "extra.app";
  public static final String EXTRA_APP_PKGNAME = "pkgName";
  public static final String EXTRA_APP_NAME = "appName";


  private ProgressBar mProgressBar;
  private TextView tvError;
  private PermPresenter mPresenter;
  private AppPermissionAdapter adapter;

  private String pkgName;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_opsx);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    AppInfo appInfo = handleIntent(getIntent());
    if(appInfo == null){
      finish();
      return;
    }

    if(TextUtils.isEmpty(appInfo.appName)){
      loadAppinfo(appInfo.packageName);
    }else {
      setTitle(appInfo.appName);
    }


    tvError = (TextView) findViewById(R.id.tv_error);
    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    recyclerView.addItemDecoration(new CommonDivderDecorator(getApplicationContext()));

    adapter = new AppPermissionAdapter();
    recyclerView.setAdapter(adapter);

    adapter.setListener(new AppPermissionAdapter.OnSwitchItemClickListener() {
      @Override
      public void onSwitch(OpEntryInfo info, boolean v) {

        mPresenter.switchMode(info, v);
      }
    });

    pkgName = appInfo.packageName;
    mPresenter = new PermPresenter(this, appInfo, getApplicationContext());
    mPresenter.setUp();
  }


  private AppInfo handleIntent(Intent intent){
    AppInfo appInfo = intent.getParcelableExtra(EXTRA_APP);
    if(appInfo == null){
      //find from extra
      String pkgName = intent.getStringExtra(EXTRA_APP_PKGNAME);
      if(TextUtils.isEmpty(pkgName) && intent.getData() != null){
        pkgName = intent.getData().getQueryParameter("id");
      }
      if(!TextUtils.isEmpty(pkgName)){
        appInfo = new AppInfo();
        appInfo.packageName = pkgName;
      }

    }
    return appInfo;
  }

  private void loadAppinfo(String pkgName){
    Helper.getAppInfo(getApplicationContext(),pkgName)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<AppInfo>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {

          }

          @Override
          public void onSuccess(@NonNull AppInfo appInfo) {
            setTitle(appInfo.appName);
          }

          @Override
          public void onError(@NonNull Throwable e) {

          }
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mPresenter != null) {
      mPresenter.destory();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_reset:
        mPresenter.reset();
        return true;
      case R.id.action_hide_perm:
        showHidePerms();
        return true;
      case R.id.action_open_all:
        changeAll(AppOpsManager.MODE_ALLOWED);
        ATracker.send(AEvent.C_APP_OPEN_ALL);
        break;
      case R.id.action_close_all:
        changeAll(AppOpsManager.MODE_IGNORED);
        ATracker.send(AEvent.C_APP_IGNOR_ALL);
        break;
      case R.id.action_app_info:
        startAppinfo();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    if (!mPresenter.isLoadSuccess()) {
      return false;
    }

    getMenuInflater().inflate(R.menu.app_menu, menu);

    MenuItem menuShowAllPerm = menu.findItem(R.id.action_hide_perm);
    MenuItem menuShowOpDesc = menu.findItem(R.id.action_show_op_perm);
    MenuItem menuShowOpName = menu.findItem(R.id.action_show_op_name);
    MenuItem menuShowPremTime = menu.findItem(R.id.action_show_perm_time);

    final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

    final Map<MenuItem, String> menus = new HashMap<>();
    menus.put(menuShowAllPerm, "key_show_no_prems");
    menus.put(menuShowOpDesc, "key_show_op_desc");
    menus.put(menuShowOpName, "key_show_op_name");
    menus.put(menuShowPremTime, "key_show_perm_time");

    MenuItem.OnMenuItemClickListener itemClickListener = new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        String s = menus.get(item);
        if (s != null) {
          item.setChecked(!item.isChecked());
          sp.edit().putBoolean(s, item.isChecked()).apply();
          ActivityCompat.invalidateOptionsMenu(AppPermissionActivity.this);
          mPresenter.load();
        }
        return true;
      }
    };

    Set<Map.Entry<MenuItem, String>> entries = menus.entrySet();
    for (Map.Entry<MenuItem, String> entry : entries) {
      entry.getKey().setChecked(sp.getBoolean(entry.getValue(), false));
      entry.getKey().setOnMenuItemClickListener(itemClickListener);
    }

    return true;
  }


  private void showHidePerms() {

  }

  private void changeAll(int newMode) {
    final List<OpEntryInfo> datas = adapter.getDatas();
    if (datas != null) {
      for (OpEntryInfo data : datas) {
        data.mode = newMode;
        mPresenter.setMode(data);
        adapter.updateItem(data);
      }
    }
  }


  @Override
  public void showProgress(boolean show) {
    tvError.setVisibility(View.GONE);
    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);

    ActivityCompat.invalidateOptionsMenu(AppPermissionActivity.this);
  }

  @Override
  public void showError(CharSequence text) {
    mProgressBar.setVisibility(View.GONE);
    tvError.setVisibility(View.VISIBLE);
    tvError.setText(text);

    ActivityCompat.invalidateOptionsMenu(AppPermissionActivity.this);
  }

  @Override
  public void showPerms(List<OpEntryInfo> opEntryInfos) {
    final SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());
    adapter.setShowConfig(sp.getBoolean("key_show_op_desc", false),
        sp.getBoolean("key_show_op_name", false),
        sp.getBoolean("key_show_perm_time", false));
    adapter.setDatas(opEntryInfos);
    adapter.notifyDataSetChanged();

    ActivityCompat.invalidateOptionsMenu(AppPermissionActivity.this);
  }

  @Override
  public void updateItem(OpEntryInfo info) {
    info.mode = info.opEntry.getMode();
    adapter.updateItem(info);

    //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
  }


  private void startAppinfo(){
    Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    intent.setData(Uri.fromParts("package",pkgName,null));
    startActivity(intent);
  }
}
