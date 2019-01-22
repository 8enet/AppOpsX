package com.zzzmode.appopsx.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.LangHelper;
import com.zzzmode.appopsx.ui.core.SpHelper;

/**
 * Created by zl on 2017/1/7.
 */

public class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    LangHelper.updateLanguage(this);
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setDefaultNightMode(SpHelper.getThemeMode(this));

  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(LangHelper.attachBaseContext(newBase));
  }

  @Override
  protected void onResume() {
    super.onResume();
    ATracker.onResume(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    ATracker.onPause(this);
  }
}
