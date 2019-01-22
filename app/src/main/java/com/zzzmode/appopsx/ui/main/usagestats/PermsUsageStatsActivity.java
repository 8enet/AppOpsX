package com.zzzmode.appopsx.ui.main.usagestats;

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * Created by zl on 2017/8/16.
 */

public class PermsUsageStatsActivity extends BaseActivity {

  private ProgressBar mProgressBar;
  private RecyclerView recyclerView;

  private SwipeRefreshLayout mSwipeRefreshLayout;
  private View containerApp;

  private UsageStatsAdapter adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_usage_stats);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setTitle(R.string.menu_stats);

    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

    containerApp = findViewById(R.id.container_app);

    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
    mSwipeRefreshLayout.setRefreshing(false);
    mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    mSwipeRefreshLayout.setEnabled(false);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new CommonDivderDecorator(getApplicationContext()));
    recyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());

    adapter = new UsageStatsAdapter();
    recyclerView.setAdapter(adapter);


    loadData(true);
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        loadData(false);
      }
    });


  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  private void loadData(final boolean isFirst) {
    boolean showSysApp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        .getBoolean("show_sysapp", false);

    Helper.getPermsUsageStatus(getApplicationContext(),showSysApp).
        subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new ResourceSingleObserver<List<Pair<AppInfo, OpEntryInfo>>>() {
          @Override
          public void onSuccess(@NonNull List<Pair<AppInfo, OpEntryInfo>> pairs) {


            mProgressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);

            if (isFirst) {
              mSwipeRefreshLayout.setEnabled(true);
            }


            adapter.showItems(pairs);

            invalidateOptionsMenu();
          }

          @Override
          public void onError(@NonNull Throwable e) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          }
        });
  }

}
