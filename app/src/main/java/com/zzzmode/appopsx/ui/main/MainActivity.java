package com.zzzmode.appopsx.ui.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.core.Users;
import com.zzzmode.appopsx.ui.main.backup.BackupActivity;
import com.zzzmode.appopsx.ui.main.group.PermissionGroupActivity;
import com.zzzmode.appopsx.ui.main.usagestats.PermsUsageStatsActivity;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener {

  private static final String TAG = "MainActivity";

  private MainListAdapter adapter;

  private ProgressBar mProgressBar;
  private RecyclerView recyclerView;

  private SwipeRefreshLayout mSwipeRefreshLayout;

  private SearchHandler mSearchHandler;

  private View containerApp, containerSearch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle(R.string.app_name);

    Log.e(TAG, "onCreate --> ");
    mSearchHandler = new SearchHandler();

    mProgressBar = findViewById(R.id.progressBar);

    containerApp = findViewById(R.id.container_app);
    containerSearch = findViewById(R.id.container_search);

    mSearchHandler.initView(containerSearch);

    recyclerView =  findViewById(R.id.recyclerView);
    mSwipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
    mSwipeRefreshLayout.setRefreshing(false);
    mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    mSwipeRefreshLayout.setEnabled(false);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new CommonDivderDecorator(getApplicationContext()));
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
    boolean showSysApp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        .getBoolean("show_sysapp", false);
    Helper.getInstalledApps(getApplicationContext(), showSysApp)
        .map(Helper.getSortComparator(getApplicationContext())).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<List<AppInfo>>() {

      @Override
      protected void onStart() {
        super.onStart();
        if(isFirst){
          mProgressBar.setVisibility(View.VISIBLE);
          recyclerView.setVisibility(View.GONE);
        }
      }

      @Override
      public void onNext(List<AppInfo> value) {
        adapter.showItems(value);
        mSearchHandler.setBaseData(new ArrayList<>(value));

        invalidateOptionsMenu();

      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

        invalidateOptionsMenu();
      }

      @Override
      public void onComplete() {
        mProgressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);

        if (isFirst) {
          mSwipeRefreshLayout.setEnabled(true);
        }

        invalidateOptionsMenu();
      }
    });
    loadUsers();
  }


  private void loadUsers(){
    Helper.getUsers(getApplicationContext(),true).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<List<UserInfo>>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(List<UserInfo> userInfos) {

            Users.getInstance().updateUsers(userInfos);
            invalidateOptionsMenu();
          }

          @Override
          public void onError(Throwable e) {

          }
        });
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_setting:
        openSetting();
        return true;
      case R.id.action_permission_sort:
        openSortPermission();
        return true;
      case R.id.action_backup:
        openConfigPerms();
        return true;
      case R.id.action_stats:
        openUsageStats();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {

    getMenuInflater().inflate(R.menu.ops_menu, menu);

    final MenuItem searchMenu = menu.findItem(R.id.action_search);
    final MenuItem settingsMenu = menu.findItem(R.id.action_setting);
    final MenuItem premsMenu = menu.findItem(R.id.action_permission_sort);

    menu.findItem(R.id.action_backup).setVisible(adapter != null && adapter.getItemCount() > 0);

    final Users users = Users.getInstance();
    if(users.isLoaded() && !users.getUsers().isEmpty()){
      SubMenu userSub = menu.addSubMenu(R.id.action_users,Menu.NONE,Menu.NONE,R.string.action_users);

      userSub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
      OnMenuItemClickListener menuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          item.setChecked(true);
          List<UserInfo> userInfos = Users.getInstance().getUsers();
          for (UserInfo user : userInfos) {
            if(user.id == item.getItemId() && users.getCurrentUid() != user.id){
              onSwitchUser(user);
              break;
            }
          }

          return true;
        }
      };

      List<UserInfo> userInfos = users.getUsers();
      for (UserInfo user : userInfos) {
        MenuItem add = userSub.add(R.id.action_users,user.id,Menu.NONE,user.name);

        add.setCheckable(true);

        add.setChecked(user.id == users.getCurrentUid());

        add.setOnMenuItemClickListener(menuItemClickListener);
      }

      userSub.setGroupCheckable(R.id.action_users,true,true);

    }

    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) searchMenu.getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(this);

    final View searchFrame = searchView.findViewById(androidx.appcompat.R.id.search_edit_frame);

    final int[] oldVisibility = {-1};

    searchFrame.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        int currentVisibility = searchFrame.getVisibility();

        if (currentVisibility != oldVisibility[0]){
          if (currentVisibility == View.VISIBLE){
            containerApp.setVisibility(View.GONE);
            containerSearch.setVisibility(View.VISIBLE);
          }else {
            containerApp.setVisibility(View.VISIBLE);
            containerSearch.setVisibility(View.GONE);
          }
          oldVisibility[0] = currentVisibility;
        }

      }
    });

    return true;
  }

  private void openSetting() {
    startActivity(new Intent(this, SettingsActivity.class));
  }

  private void openSortPermission() {
    startActivity(new Intent(this, PermissionGroupActivity.class));
  }

  private void openConfigPerms() {
    Intent intent = new Intent(this, BackupActivity.class);
    intent.putParcelableArrayListExtra(BackupActivity.EXTRA_APPS,
        new ArrayList<>(adapter.getAppInfos()));
    startActivity(intent);
  }

  private void openUsageStats(){
    startActivity(new Intent(this, PermsUsageStatsActivity.class));
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


  private void onSwitchUser(UserInfo user){
    getSupportActionBar().setSubtitle(user.name);
    Users.getInstance().setCurrentLoadUser(user);

    AppOpsx.getInstance(getApplicationContext()).setUserHandleId(user.id);
    LocalImageLoader.clear();
    loadData(true);
  }
}
