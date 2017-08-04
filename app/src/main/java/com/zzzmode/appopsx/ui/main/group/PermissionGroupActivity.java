package com.zzzmode.appopsx.ui.main.group;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import java.util.List;

/**
 * Created by zl on 2017/1/17.
 */

public class PermissionGroupActivity extends BaseActivity implements
    RecyclerViewExpandableItemManager.OnGroupCollapseListener,
    RecyclerViewExpandableItemManager.OnGroupExpandListener,
    PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnDismissListener,IPermGroupView {

  private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

  private static final String TAG = "PermissionGroupActivity";


  private ProgressBar progressBar;

  private RecyclerView recyclerView;
  private View coordinatorLayout;

  private RecyclerView.LayoutManager mLayoutManager;
  private RecyclerView.Adapter mWrappedAdapter;
  private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
  private PermissionGroupAdapter myItemAdapter;

  private TextView tvError;

  private int contextGroupPosition = -1;

  private PermGroupPresenter mPresenter;
  private ScrollTopHelper stickyHelper;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prems_group);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setTitle(R.string.menu_permission_sort);

    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    coordinatorLayout = findViewById(R.id.coordinator_layout);
    tvError = (TextView) findViewById(R.id.tv_error);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    mLayoutManager = new LinearLayoutManager(this);

    final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState
        .getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
    mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
    mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this);
    mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(this);

    recyclerView.setHasFixedSize(true);

    myItemAdapter = new PermissionGroupAdapter(mRecyclerViewExpandableItemManager);
    myItemAdapter.setHasStableIds(true);
    myItemAdapter.setListener(new PermissionGroupAdapter.OnSwitchItemClickListener() {
      @Override
      public void onSwitch(int groupPosition, int childPosition, PermissionChildItem info,
          boolean v) {
        mPresenter.changeMode(groupPosition, childPosition, info);
      }
    }, new PermissionGroupAdapter.OnGroupOtherClickListener() {
      @Override
      public void onOtherClick(int groupPosition, View view) {
        contextGroupPosition = groupPosition;
        ATracker.send(AEvent.C_GROUP_MENU);
        showPopMenu(groupPosition, view);
      }
    });

    recyclerView.setLayoutManager(mLayoutManager);

    recyclerView.addItemDecoration(new CommonDivderDecorator(getApplicationContext()));

    mRecyclerViewExpandableItemManager.attachRecyclerView(recyclerView);

    stickyHelper = new ScrollTopHelper(recyclerView, (LinearLayoutManager) mLayoutManager,
        mRecyclerViewExpandableItemManager,findViewById(R.id.fab));


    mPresenter = new PermGroupPresenter(this,getApplicationContext());
    mPresenter.loadPerms();
  }


  private void showPopMenu(int groupPosition, View view) {
    PopupMenu popupMenu = new PopupMenu(this, view);
    getMenuInflater().inflate(R.menu.group_item_menu, popupMenu.getMenu());
    popupMenu.setOnDismissListener(this);
    popupMenu.setOnMenuItemClickListener(this);
    popupMenu.show();
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mRecyclerViewExpandableItemManager != null) {
      outState.putParcelable(
          SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
          mRecyclerViewExpandableItemManager.getSavedState());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(mPresenter != null){
      mPresenter.destroy();
    }

    if(stickyHelper != null) {
      stickyHelper.release();
    }

    if (mRecyclerViewExpandableItemManager != null) {
      mRecyclerViewExpandableItemManager.release();
      mRecyclerViewExpandableItemManager = null;
    }

    if (recyclerView != null) {
      recyclerView.setItemAnimator(null);
      recyclerView.setAdapter(null);
      recyclerView = null;
    }

    if (mWrappedAdapter != null) {
      WrapperAdapterUtils.releaseAll(mWrappedAdapter);
      mWrappedAdapter = null;
    }
    mLayoutManager = null;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }


  @Override
  public void loading(int count, int progress, String name) {


  }

  @Override
  public void changeTitle(int groupPosition, int childPosition, boolean allowed) {
    myItemAdapter.changeTitle(groupPosition, allowed);
    mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition);
    mRecyclerViewExpandableItemManager.notifyGroupItemChanged(groupPosition);
  }

  @Override
  public void refreshItem(int groupPosition, int childPosition) {
    mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition);
  }

  @Override
  public void showList(List<PermissionGroup> value) {
    if (isFinishing()) {
      return;
    }
    progressBar.setVisibility(View.GONE);
    coordinatorLayout.setVisibility(View.VISIBLE);
    recyclerView.setVisibility(View.VISIBLE);

    myItemAdapter.setData(value);

    mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);
    recyclerView.setAdapter(mWrappedAdapter);

  }

  @Override
  public void showError(Throwable e) {
    try {
      progressBar.setVisibility(View.GONE);
      tvError.setVisibility(View.VISIBLE);
      tvError.setText(getString(R.string.error_msg,"", Log.getStackTraceString(e)));
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }




  private void changeAll(int newMode) {
    if (contextGroupPosition >= 0) {
      try {
        final int groupPosition = contextGroupPosition;
        PermissionGroup permissionGroup = myItemAdapter.getData().get(groupPosition);
        List<PermissionChildItem> apps = permissionGroup.apps;
        int size = apps.size();
        for (int i = 0; i < size; i++) {
          PermissionChildItem info = apps.get(i);
          if (info.opEntryInfo.mode != newMode) {
            //changeMode(groupPosition, i, info);
            mPresenter.changeMode(groupPosition,i,info);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_close_all:
        changeAll(AppOpsManager.MODE_IGNORED);
        ATracker.send(AEvent.C_GROUP_IGNORE_ALL);
        return true;
      case R.id.action_open_all:
        changeAll(AppOpsManager.MODE_ALLOWED);
        ATracker.send(AEvent.C_GROUP_OPEN_ALL);
        return true;
    }
    return super.onContextItemSelected(item);
  }

  @Override
  public void onDismiss(PopupMenu menu) {
    contextGroupPosition = -1;
  }

  @Override
  public void onGroupCollapse(int groupPosition, boolean fromUser, Object o) {

  }

  @Override
  public void onGroupExpand(int groupPosition, boolean fromUser, Object o) {
    if (fromUser) {
      adjustScrollPositionOnGroupExpanded(groupPosition);
    }
  }

  private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
    int pad = (int) (getResources().getDisplayMetrics().density * 10);
    int childItemHeight =
        getResources().getDimensionPixelSize(android.R.dimen.app_icon_size) + pad * 2;
    int topMargin = (int) (getResources().getDisplayMetrics().density * 16);
    int bottomMargin = topMargin;

    mRecyclerViewExpandableItemManager
        .scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);

    recyclerView.smoothScrollBy(0,-100);
  }

}
