package com.zzzmode.appopsx.ui.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log

import com.zzzmode.appopsx.ui.analytics.ATracker

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by zl on 2017/1/7.
 */

class AppGlobal : Application(), Application.ActivityLifecycleCallbacks {

    private val mAliveActivity = AtomicInteger(0)

    override fun onCreate() {
        super.onCreate()
        LangHelper.updateLanguage(this)
        registerActivityLifecycleCallbacks(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Helper.updataShortcuts(this)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            registerReceiver(AppInstalledRevicer(), IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
            })
        }
        ATracker.init(this)
        AppOpsx.getInstance(applicationContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LangHelper.updateLanguage(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LangHelper.attachBaseContext(base))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mAliveActivity.getAndIncrement()
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        val i = mAliveActivity.decrementAndGet()
        if (i <= 0) {
            Log.e("test", "onActivityDestroyed --> ")
            Helper.closeBgServer(applicationContext)
        }
    }
}
