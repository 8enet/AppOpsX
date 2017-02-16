package com.zzzmode.appopsx.ui.main;

import android.app.AppOpsManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = "MainActivity";

    private MainListAdapter adapter;

    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;

    private RecyclerView mSearchResult;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private SearchHandler mSearchHandler;

    private View containerApp,containerSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchHandler=new SearchHandler();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mSearchResult= (RecyclerView) findViewById(R.id.search_result_recyclerView);

        containerApp=findViewById(R.id.container_app);
        containerSearch=findViewById(R.id.container_search);

        mSearchHandler.initView(mSearchResult);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setEnabled(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider_h), true));
        recyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());

        adapter = new MainListAdapter();
        recyclerView.setAdapter(adapter);

        loadData(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(false);
            }
        });
    }


    private void loadData(final boolean isFirst) {
        boolean showSysApp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("show_sysapp", false);
        Helper.getInstalledApps(getApplicationContext(), showSysApp).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<List<AppInfo>>() {
            @Override
            public void onNext(List<AppInfo> value) {
                adapter.showItems(value);
                mSearchHandler.setBaseData(new ArrayList<AppInfo>(value));
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {
                mProgressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);

                if(isFirst){
                    mSwipeRefreshLayout.setEnabled(true);
                }
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

        final MenuItem searchMenu=menu.findItem(R.id.action_search);
        final MenuItem settingsMenu=menu.findItem(R.id.action_setting);
        final MenuItem premsMenu=menu.findItem(R.id.action_premission_sort);

        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                containerApp.setVisibility(View.GONE);
                containerSearch.setVisibility(View.VISIBLE);

                settingsMenu.setVisible(false);
                premsMenu.setVisible(false);

                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                containerApp.setVisibility(View.VISIBLE);
                containerSearch.setVisibility(View.GONE);


                settingsMenu.setVisible(true);
                premsMenu.setVisible(true);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);

                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    private void openSetting() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void openSortPremission() {
        startActivity(new Intent(this, PremissionGroupActivity.class));
    }

    private void resetAll() {
        if (adapter != null && adapter.appInfos != null) {
            final List<AppInfo> appInfos = adapter.appInfos;
            Observable.fromIterable(appInfos).concatMap(new Function<AppInfo, ObservableSource<AppOpEntry>>() {
                @Override
                public ObservableSource<AppOpEntry> apply(AppInfo info) throws Exception {

                    return Observable.just(info).map(new Function<AppInfo, AppOpEntry>() {
                        @Override
                        public AppOpEntry apply(AppInfo info) throws Exception {
                            OpsResult opsForPackage = AppOpsx.getInstance(getApplicationContext()).getOpsForPackage(info.packageName);
                            if (opsForPackage != null) {
                                if (opsForPackage.getException() == null) {
                                    return new AppOpEntry(info, opsForPackage);
                                } else {
                                    throw new Exception(opsForPackage.getException());
                                }
                            }
                            throw new RuntimeException("getOpsForPackage fail: " + info);
                        }
                    });
                }
            }).filter(new Predicate<AppOpEntry>() {
                @Override
                public boolean test(AppOpEntry appOpEntry) throws Exception {

                    List<PackageOps> list = appOpEntry.opsResult.getList();
                    if (list != null) {
                        for (PackageOps packageOps : list) {
                            List<OpEntry> ops = packageOps.getOps();
                            if (ops != null) {
                                for (OpEntry op : ops) {

                                    if (op.getMode() == AppOpsManager.MODE_ERRORED) {
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
                            if (list != null) {
                                OpsxManager opsxManager = AppOpsx.getInstance(getApplicationContext());
                                for (PackageOps packageOps : list) {
                                    List<OpEntry> ops = packageOps.getOps();
                                    if (ops != null) {
                                        for (OpEntry op : ops) {

                                            if (op.getMode() == AppOpsManager.MODE_ERRORED) {
                                                //Log.e(TAG, "test --> "+op);
                                                appOpEntry.modifyResult = opsxManager.setOpsMode(appOpEntry.appInfo.packageName, op.getOp(), AppOpsManager.MODE_IGNORED);
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
                            Log.e(TAG, "onNext --> " + value);

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


    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchHandler.handleWord(newText);
        return true;
    }
}
