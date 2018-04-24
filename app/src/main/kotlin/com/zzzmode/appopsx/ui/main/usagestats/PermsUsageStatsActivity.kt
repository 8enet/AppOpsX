package com.zzzmode.appopsx.ui.main.usagestats

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.observers.ResourceSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_usage_stats.*
import kotlinx.android.synthetic.main.layout_appbar.*

/**
 * Created by zl on 2017/8/16.
 */

class PermsUsageStatsActivity : BaseActivity() {



    private lateinit var adapter: UsageStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage_stats)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle(R.string.menu_stats)


        swiperefreshlayout.isRefreshing = false
        swiperefreshlayout.setColorSchemeResources(R.color.colorAccent)
        swiperefreshlayout.isEnabled = false

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(CommonDivderDecorator(applicationContext))
        recyclerView.itemAnimator = RefactoredDefaultItemAnimator()

        adapter = UsageStatsAdapter()
        recyclerView.adapter = adapter


        loadData(true)
        swiperefreshlayout.setOnRefreshListener { loadData(false) }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadData(isFirst: Boolean) {
        val showSysApp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean("show_sysapp", false)

        Helper.getPermsUsageStatus(applicationContext, showSysApp).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ResourceSingleObserver<List<Pair<AppInfo, OpEntryInfo>>>() {
                    override fun onSuccess(@NonNull pairs: List<Pair<AppInfo, OpEntryInfo>>) {

                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        swiperefreshlayout.isRefreshing = false

                        if (isFirst) {
                            swiperefreshlayout.isEnabled = true
                        }

                        adapter.showItems(pairs)

                        invalidateOptionsMenu()
                    }

                    override fun onError(@NonNull e: Throwable) {
                        swiperefreshlayout.isRefreshing = false
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    }
                })
    }

}
