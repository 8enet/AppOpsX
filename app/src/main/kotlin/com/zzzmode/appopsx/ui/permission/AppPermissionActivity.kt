package com.zzzmode.appopsx.ui.permission

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_opsx.*
import kotlinx.android.synthetic.main.layout_appbar.*
import java.util.*

/**
 * am start -n com.zzzmode.appopsx/.ui.permission.AppPermissionActivity --es pkgName "com.zzzmode.appopsx"
 * am start "appops://details?id=com.zzzmode.appopsx"
 * Created by zl on 2016/11/18.
 */

class AppPermissionActivity : BaseActivity(), IPermView {


    private var mPresenter: PermPresenter?=null
    private lateinit var adapter: AppPermissionAdapter

    private var pkgName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opsx)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val appInfo = handleIntent(intent)
        if (appInfo == null) {
            finish()
            return
        }

        if (TextUtils.isEmpty(appInfo.appName)) {
            loadAppinfo(appInfo.packageName)
        } else {
            title = appInfo.appName
        }


        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.addItemDecoration(CommonDivderDecorator(applicationContext))

        adapter = AppPermissionAdapter()
        recyclerView.adapter = adapter

        adapter.setListener(object : AppPermissionAdapter.OnSwitchItemClickListener {
            override fun onSwitch(info: OpEntryInfo, v: Boolean) {

                mPresenter?.switchMode(info, v)
            }
        })

        pkgName = appInfo.packageName
        mPresenter = PermPresenter(this, appInfo, applicationContext)
        mPresenter!!.setUp()
    }


    private fun handleIntent(intent: Intent): AppInfo? {
        var appInfo: AppInfo? = intent.getParcelableExtra(EXTRA_APP)
        if (appInfo == null) {
            //find from extra
            var pkgName = intent.getStringExtra(EXTRA_APP_PKGNAME)
            if (TextUtils.isEmpty(pkgName) && intent.data != null) {
                pkgName = intent.data!!.getQueryParameter("id")
            }
            if (!TextUtils.isEmpty(pkgName)) {
                appInfo = AppInfo(packageName = pkgName)

            }

        }
        return appInfo
    }

    private fun loadAppinfo(pkgName: String) {
        Helper.getAppInfo(applicationContext, pkgName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<AppInfo> {
                    override fun onSubscribe(@NonNull d: Disposable) {

                    }

                    override fun onSuccess(@NonNull appInfo: AppInfo) {
                        title = appInfo.appName
                    }

                    override fun onError(@NonNull e: Throwable) {

                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.destory()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_reset -> {
                mPresenter?.reset()
                return true
            }
            R.id.action_hide_perm -> {
                showHidePerms()
                return true
            }
            R.id.action_open_all -> {
                changeAll(AppOpsManager.MODE_ALLOWED)
                ATracker.send(AEvent.C_APP_OPEN_ALL)
            }
            R.id.action_close_all -> {
                changeAll(AppOpsManager.MODE_IGNORED)
                ATracker.send(AEvent.C_APP_IGNOR_ALL)
            }
            R.id.action_app_info -> startAppinfo()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mPresenter?.apply {
            if(!isLoadSuccess){
                return false
            }
        }

        menuInflater.inflate(R.menu.app_menu, menu)

        val menuShowAllPerm = menu.findItem(R.id.action_hide_perm)
        val menuShowOpDesc = menu.findItem(R.id.action_show_op_perm)
        val menuShowOpName = menu.findItem(R.id.action_show_op_name)
        val menuShowPremTime = menu.findItem(R.id.action_show_perm_time)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        val menus = HashMap<MenuItem, String>()
        menus[menuShowAllPerm] = "key_show_no_prems"
        menus[menuShowOpDesc] = "key_show_op_desc"
        menus[menuShowOpName] = "key_show_op_name"
        menus[menuShowPremTime] = "key_show_perm_time"

        val itemClickListener = MenuItem.OnMenuItemClickListener { item ->
            val s = menus[item]
            if (s != null) {
                item.isChecked = !item.isChecked
                sp.edit().putBoolean(s, item.isChecked).apply()
                invalidateOptionsMenu()
                mPresenter?.load()
            }
            true
        }

        val entries = menus.entries
        for ((key, value) in entries) {
            key.isChecked = sp.getBoolean(value, false)
            key.setOnMenuItemClickListener(itemClickListener)
        }

        return true
    }


    private fun showHidePerms() {

    }

    private fun changeAll(newMode: Int) {
        adapter.datas.forEach {data ->
            data.mode = newMode
            mPresenter?.setMode(data)
            adapter.updateItem(data)
        }

    }


    override fun showProgress(show: Boolean) {
        tv_error.visibility = View.GONE
        progressBar.visibility = if (show) View.VISIBLE else View.GONE

        invalidateOptionsMenu()
    }

    override fun showError(text: CharSequence) {
        progressBar.visibility = View.GONE
        tv_error.visibility = View.VISIBLE
        tv_error.text = text

        invalidateOptionsMenu()
    }

    override fun showPerms(opEntryInfos: List<OpEntryInfo>) {
        val sp = PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
        adapter.setShowConfig(sp.getBoolean("key_show_op_desc", false),
                sp.getBoolean("key_show_op_name", false),
                sp.getBoolean("key_show_perm_time", false))
        adapter.datas = opEntryInfos
        adapter.notifyDataSetChanged()

        invalidateOptionsMenu()
    }

    override fun updateItem(info: OpEntryInfo) {
        info.mode = info.opEntry.mode
        adapter.updateItem(info)

        //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
    }


    private fun startAppinfo() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", pkgName, null)
        startActivity(intent)
    }

    companion object {

        private const val TAG = "AppPermissionActivity"

        const val EXTRA_APP = "extra.app"
        const val EXTRA_APP_PKGNAME = "pkgName"
        const val EXTRA_APP_NAME = "appName"
    }
}
