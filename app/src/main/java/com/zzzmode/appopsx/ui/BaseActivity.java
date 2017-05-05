package com.zzzmode.appopsx.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.umeng.analytics.MobclickAgent;
import com.zzzmode.appopsx.ui.core.SpHelper;

/**
 * Created by zl on 2017/1/7.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeMode = SpHelper.getThemeMode(this);
        if(themeMode != AppCompatDelegate.MODE_NIGHT_AUTO) {
            AppCompatDelegate.setDefaultNightMode(themeMode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
