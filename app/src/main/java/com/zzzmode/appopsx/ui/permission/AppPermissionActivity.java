package com.zzzmode.appopsx.ui.permission;

import android.app.AppOpsManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionActivity extends BaseActivity implements IPermView {

  private static final String TAG = "AppPermissionActivity";

  public static final String EXTRA_APP = "extra.app";
  public static final String EXTRA_APP_PKGNAME = "extra.app.packagename";
  public static final String EXTRA_APP_NAME = "extra.app.name";


  private ProgressBar mProgressBar;
  private TextView tvError;
  private PermPresenter mPresenter;
  private AppPermissionAdapter adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_opsx);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    AppInfo appInfo = getIntent().getParcelableExtra(EXTRA_APP);
    if (appInfo == null) {
      String pkgName = getIntent().getStringExtra(EXTRA_APP_PKGNAME);
      String name = getIntent().getStringExtra(EXTRA_APP_NAME);
      if (pkgName != null && name != null) {
        appInfo = new AppInfo();
        appInfo.packageName = pkgName;
        appInfo.appName = name;
      } else {
        finish();
        return;
      }

    }

    setTitle(appInfo.appName);

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

    mPresenter = new PermPresenter(this, appInfo, getApplicationContext());
    mPresenter.setUp();
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
//            case R.id.action_reset:
//                resetMode();
//                return true;
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
}
