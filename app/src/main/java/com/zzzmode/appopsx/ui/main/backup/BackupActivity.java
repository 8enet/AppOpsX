package com.zzzmode.appopsx.ui.main.backup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;

/**
 * Created by zl on 2017/5/7.
 */

public class BackupActivity extends BaseActivity {

  public static final String EXTRA_APPS = "extra.list.app";

  private TabLayout tabLayout;
  private ViewPager viewPager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_backup);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    setTitle(R.string.menu_backup);
    tabLayout = (TabLayout) findViewById(R.id.tabs);
    viewPager = (ViewPager) findViewById(R.id.viewpager);
    initView();
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  private void initView() {

    final CharSequence[] titles = {getString(R.string.perm_export),
        getString(R.string.perm_restore)};

    viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

      private SparseArrayCompat<Fragment> mFragments = new SparseArrayCompat<Fragment>();

      @Override
      public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (fragment == null) {
          switch (position) {
            case 0:
              fragment = new ExportFragment();
              fragment.setArguments(new Bundle(getIntent().getExtras()));
              break;
            case 1:
              fragment = new ImportFragment();
              break;
          }
          if (fragment != null) {
            mFragments.put(position, fragment);
          }
        }
        return fragment;
      }

      @Override
      public int getCount() {
        return 2;
      }

      @Override
      public CharSequence getPageTitle(int position) {
        return titles[position];
      }
    });

    tabLayout.setupWithViewPager(viewPager);
  }
}
