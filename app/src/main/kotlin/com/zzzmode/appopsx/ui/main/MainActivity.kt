package com.zzzmode.appopsx.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.UserInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.widget.Toast
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.core.AppOpsx
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.core.LocalImageLoader
import com.zzzmode.appopsx.ui.core.Users
import com.zzzmode.appopsx.ui.main.backup.BackupActivity
import com.zzzmode.appopsx.ui.main.group.PermissionGroupActivity
import com.zzzmode.appopsx.ui.main.usagestats.PermsUsageStatsActivity
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.ResourceObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_appbar.*
import java.util.*

class MainActivity : BaseActivity(), SearchView.OnQueryTextListener {

    private lateinit var adapter: MainListAdapter

    private lateinit var mSearchHandler: SearchHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSearchHandler = SearchHandler(container_search)


        setSupportActionBar(toolbar)

        setTitle(R.string.app_name)

        swiperefreshlayout.isRefreshing = false
        swiperefreshlayout.isEnabled = false
        swiperefreshlayout.setColorSchemeResources(R.color.colorAccent)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(CommonDivderDecorator(applicationContext))
        recyclerView.itemAnimator = RefactoredDefaultItemAnimator()

        adapter = MainListAdapter()
        recyclerView.adapter = adapter

        loadData(true)
        swiperefreshlayout.setOnRefreshListener { loadData(false) }
    }


    private fun loadData(isFirst: Boolean) {
        val showSysApp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean("show_sysapp", false)
        Helper.getInstalledApps(applicationContext, showSysApp)
                .map(Helper.getSortComparator(applicationContext)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : ResourceObserver<List<AppInfo>>() {

            override fun onStart() {
                super.onStart()
                if (isFirst) {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }

            override fun onNext(value: List<AppInfo>) {
                adapter.showItems(value)
                mSearchHandler.setBaseData(ArrayList(value))

                invalidateOptionsMenu()

            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                swiperefreshlayout.isRefreshing = false
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()

                invalidateOptionsMenu()
            }

            override fun onComplete() {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                swiperefreshlayout.isRefreshing = false

                if (isFirst) {
                    swiperefreshlayout.isEnabled = true
                }

                invalidateOptionsMenu()
            }
        })
        loadUsers()
    }


    private fun loadUsers() {
        Helper.getUsers(applicationContext, true).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<UserInfo>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(userInfos: List<UserInfo>) {

                        Users.instance.updateUsers(userInfos)
                        invalidateOptionsMenu()
                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_setting -> {
                ATracker.send(AEvent.C_SETTINGS)
                openSetting()
                return true
            }
            R.id.action_permission_sort -> {
                ATracker.send(AEvent.C_PERMISSION_LIST)
                openSortPermission()
                return true
            }
            R.id.action_backup -> {
                ATracker.send(AEvent.C_BACKUP)
                openConfigPerms()
                return true
            }
            R.id.action_stats -> {
                ATracker.send(AEvent.C_USAGE_STATUS)
                openUsageStats()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.ops_menu, menu)

        val searchMenu = menu.findItem(R.id.action_search)
        val settingsMenu = menu.findItem(R.id.action_setting)
        val premsMenu = menu.findItem(R.id.action_permission_sort)

        menu.findItem(R.id.action_backup).isVisible =  adapter.itemCount > 0

        val users = Users.instance
        if (users.hasMultiUser) {
            val userSub = menu.addSubMenu(R.id.action_users, Menu.NONE, Menu.NONE, R.string.action_users)

            userSub.item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            val menuItemClickListener = OnMenuItemClickListener { item ->
                item.isChecked = true

                users.users?.takeWhile { it.id == item.itemId && users.currentUid != it.id }?.firstOrNull()?.apply {
                    onSwitchUser(this)
                }

                true
            }

            users.users?.forEach {
                val add = userSub.add(R.id.action_users, it.id, Menu.NONE, it.name)
                add.isCheckable = true
                add.isChecked = it.id == users.currentUid
                add.setOnMenuItemClickListener(menuItemClickListener)
            }


            userSub.setGroupCheckable(R.id.action_users, true, true)

        }



        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchView = searchMenu.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(this)


        searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_edit_frame)?.run {
            var oldVisibility = -1
            viewTreeObserver.addOnGlobalLayoutListener {

               val currentVisibility =  visibility

                if(currentVisibility != oldVisibility){

                    if(currentVisibility == View.VISIBLE){
                        //show

                        container_app.visibility = View.GONE
                        container_search.visibility = View.VISIBLE
                    }else{
                        //hide

                        container_app.visibility = View.VISIBLE
                        container_search.visibility = View.GONE
                    }

                    oldVisibility = currentVisibility
                }

            }

        }

        return true
    }

    private fun openSetting() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openSortPermission() {
        startActivity(Intent(this, PermissionGroupActivity::class.java))
    }

    private fun openConfigPerms() {
        val intent = Intent(this, BackupActivity::class.java)
        intent.putParcelableArrayListExtra(BackupActivity.EXTRA_APPS,
                ArrayList(adapter.appInfos))
        startActivity(intent)
    }

    private fun openUsageStats() {
        startActivity(Intent(this, PermsUsageStatsActivity::class.java))
    }



    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mSearchHandler.handleWord(newText)
        return false
    }


    private fun onSwitchUser(user: UserInfo) {
        supportActionBar?.subtitle = user.name
        Users.instance.setCurrentLoadUser(user)

        AppOpsx.getInstance(applicationContext).setUserHandleId(user.id)
        LocalImageLoader.clear()
        loadData(true)
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
