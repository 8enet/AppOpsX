package com.zzzmode.appopsx.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.core.LangHelper
import com.zzzmode.appopsx.ui.core.SpHelper

/**
 * Created by zl on 2017/1/7.
 */

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        LangHelper.updateLanguage(this)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(SpHelper.getThemeMode(this))

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LangHelper.attachBaseContext(newBase))
    }

    override fun onResume() {
        super.onResume()
        ATracker.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        ATracker.onPause(this)
    }
}
