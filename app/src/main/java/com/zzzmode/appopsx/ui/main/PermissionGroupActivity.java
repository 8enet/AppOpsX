package com.zzzmode.appopsx.ui.main;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.zzzmode.appopsx.BuildConfig;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;

import java.lang.ref.SoftReference;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/17.
 */

public class PermissionGroupActivity extends BaseActivity implements RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener,
        PopupMenu.OnMenuItemClickListener,
PopupMenu.OnDismissListener{
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private static final String TAG = "PermissionGroupActivity";

    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private PermissionGroupAdapter myItemAdapter;

    private TextView tvError;

    private int contextGroupPosition=-1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prems_group);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setTitle(R.string.menu_permission_sort);

        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);
        recyclerView= (RecyclerView) findViewById(R.id.recyclerView);

        tvError = (TextView) findViewById(R.id.tv_error);

        findViewById(R.id.swiperefreshlayout).setEnabled(false);

        mLayoutManager = new LinearLayoutManager(this);

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this);
        mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(this);

        myItemAdapter = new PermissionGroupAdapter(mRecyclerViewExpandableItemManager);
        myItemAdapter.setHasStableIds(true);
        myItemAdapter.setListener(new PermissionGroupAdapter.OnSwitchItemClickListener() {
            @Override
            public void onSwitch(int groupPosition, int childPosition, PermissionChildItem info, boolean v) {
                changeMode(groupPosition, childPosition, info);
            }
        }, new PermissionGroupAdapter.OnGroupOtherClickListener() {
            @Override
            public void onOtherClick(int groupPosition, View view) {
                contextGroupPosition= groupPosition;
                ATracker.send(AEvent.C_GROUP_MENU);
                showPopMenu(groupPosition,view);
            }
        });



        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setHasFixedSize(false);

        recyclerView.addItemDecoration(new CommonDivderDecorator(getApplicationContext()));

        mRecyclerViewExpandableItemManager.attachRecyclerView(recyclerView);

        init();
    }


    private void showPopMenu(int groupPosition, View view){
        PopupMenu popupMenu=new PopupMenu(this,view);
        getMenuInflater().inflate(R.menu.group_item_menu,popupMenu.getMenu());
        popupMenu.setOnDismissListener(this);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    private void changeMode(final int groupPosition,final int childPosition,final PermissionChildItem info){


        info.opEntryInfo.changeStatus();

        Helper.setMode(getApplicationContext(),info.appInfo.packageName,info.opEntryInfo)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
            @Override
            public void onNext(OpsResult value) {

                myItemAdapter.changeTitle(groupPosition,info.opEntryInfo.isAllowed());
                mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition,childPosition);
                mRecyclerViewExpandableItemManager.notifyGroupItemChanged(groupPosition);
            }

            @Override
            public void onError(Throwable e) {
                try{
                    info.opEntryInfo.changeStatus();
                    mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
            }

            @Override
            public void onComplete() {

            }
        });
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

    private void init(){
        boolean showSysApp= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("show_sysapp",false);

        final SoftReference<PermissionGroupActivity> actRef=new SoftReference<PermissionGroupActivity>(this);
        Helper.getPermissionGroup(getApplicationContext(),showSysApp).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceSingleObserver<List<PermissionGroup>>() {
            @Override
            public void onSuccess(List<PermissionGroup> value) {

                try {
                    PermissionGroupActivity act=null;
                    if(actRef != null && (act=actRef.get()) != null){
                        act.showList(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {

                try {
                    mProgressBar.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(getString(R.string.error_msg,Log.getStackTraceString(e)));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });
    }


    private void showList(List<PermissionGroup> value){
        if(isFinishing()){
            return;
        }
        mProgressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);


        myItemAdapter.setData(value);

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);
        recyclerView.setAdapter(mWrappedAdapter);

        myItemAdapter.notifyDataSetChanged();

        if(BuildConfig.DEBUG) {
            for (PermissionGroup group : value) {
                Log.e(TAG, "onSuccess --> " + group);
            }
        }
    }

    @Override
    public void onGroupCollapse(int groupPosition, boolean fromUser) {

    }

    @Override
    public void onGroupExpand(int groupPosition, boolean fromUser) {
        if(fromUser){
            adjustScrollPositionOnGroupExpanded(groupPosition);
        }
    }


    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int pad=(int) (getResources().getDisplayMetrics().density * 10);
        int childItemHeight = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size)+pad*2;
        int topMargin = (int) (getResources().getDisplayMetrics().density * 16);
        int bottomMargin = topMargin;

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);
    }



    private void changeAll(int newMode){
        if(contextGroupPosition >= 0) {
            try {
                final int groupPosition=contextGroupPosition;
                PermissionGroup permissionGroup = myItemAdapter.getData().get(groupPosition);
                List<PermissionChildItem> apps = permissionGroup.apps;
                int size=apps.size();
                for (int i = 0; i < size; i++) {
                    PermissionChildItem info = apps.get(i);
                    if(info.opEntryInfo.mode != newMode) {
                        changeMode(groupPosition, i, info);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
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
}
