package com.zzzmode.appopsx.ui.permission;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import java.util.List;

/**
 * Created by zl on 2017/5/1.
 */

public class AlertInstalledPremActivity extends BaseActivity implements IPermView {

  public static final String EXTRA_APP = "extra.app";
  private AppInfo appInfo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    appInfo = getIntent().getParcelableExtra(EXTRA_APP);
    if (appInfo == null) {
      finish();
      return;
    }

    initView();

    mPresenter = new PermPresenter(this, appInfo, getApplicationContext());
    mPresenter.setAutoDisabled(false);
    mPresenter.setSortByMode(true);
    mPresenter.setUp();
  }

  private ProgressBar mProgressBar;
  private TextView tvError;
  private PermPresenter mPresenter;
  private AppPermissionAdapter adapter;

  private void initView() {
    final View inflate = getLayoutInflater().inflate(R.layout.activity_opsx, null);

    inflate.findViewById(R.id.appBar).setVisibility(View.GONE);

    tvError = (TextView) inflate.findViewById(R.id.tv_error);
    mProgressBar = (ProgressBar) inflate.findViewById(R.id.progressBar);

    RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.recyclerView);
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

    DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    };

    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setCustomTitle(getTitleView())
        .setView(inflate)
        .setPositiveButton(android.R.string.ok, clickListener)
        .create();

    alertDialog.setCanceledOnTouchOutside(false);
    alertDialog.show();
    Window window = alertDialog.getWindow();
    if (window != null) {
      window.setWindowAnimations(0);
    }

    inflate.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {

            int minHeight = getApplicationContext().getResources()
                .getDimensionPixelOffset(R.dimen.dlg_min_height);
            if (inflate.getHeight() < minHeight) {
              ViewGroup.LayoutParams layoutParams = inflate.getLayoutParams();
              layoutParams.height = minHeight;
              inflate.setLayoutParams(layoutParams);
            }
            inflate.getViewTreeObserver().removeOnGlobalLayoutListener(this);

          }
        });

    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        finish();
      }
    });
  }

  private View getTitleView() {
    View inflate = getLayoutInflater().inflate(R.layout.layout_dlg_title, null);
    ImageView icon = (ImageView) inflate.findViewById(R.id.app_icon);
    TextView tvTitle = (TextView) inflate.findViewById(R.id.tv_title);
    TextView tvInfo = (TextView) inflate.findViewById(R.id.tv_info);

    LocalImageLoader.load(icon, appInfo);
    tvTitle.setText(appInfo.appName);
    tvInfo.setText(appInfo.packageName);

    return inflate;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(mPresenter != null) {
      mPresenter.destory();
    }
  }

  @Override
  public void showProgress(boolean show) {
    tvError.setVisibility(View.GONE);
    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);

  }

  @Override
  public void showError(CharSequence text) {
    mProgressBar.setVisibility(View.GONE);
    tvError.setVisibility(View.VISIBLE);
    tvError.setText(text);
  }

  @Override
  public void showPerms(final List<OpEntryInfo> opEntryInfos) {
    adapter.setShowConfig(false, false, false);
    adapter.setDatas(opEntryInfos);
    adapter.notifyDataSetChanged();
  }

  @Override
  public void updateItem(OpEntryInfo info) {
    info.mode = info.opEntry.getMode();
    adapter.updateItem(info);
  }
}
