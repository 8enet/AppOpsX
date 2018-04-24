package com.zzzmode.appopsx.ui.main.group

import android.app.AppOpsManager
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.model.PermissionChildItem
import com.zzzmode.appopsx.ui.model.PermissionGroup
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import kotlinx.android.synthetic.main.activity_prems_group.*
import kotlinx.android.synthetic.main.layout_appbar.*

/**
 * Created by zl on 2017/1/17.
 */

class PermissionGroupActivity : BaseActivity(), RecyclerViewExpandableItemManager.OnGroupCollapseListener, RecyclerViewExpandableItemManager.OnGroupExpandListener, PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener, IPermGroupView {


    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mRecyclerViewExpandableItemManager: RecyclerViewExpandableItemManager
    private lateinit var myItemAdapter: PermissionGroupAdapter

    private var mWrappedAdapter: RecyclerView.Adapter<*>? = null

    private var contextGroupPosition = -1

    private lateinit var mPresenter: PermGroupPresenter
    private lateinit var stickyHelper: ScrollTopHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prems_group)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle(R.string.menu_permission_sort)

        mLayoutManager = LinearLayoutManager(this)

        val eimSavedState = savedInstanceState?.getParcelable<Parcelable>(SAVED_STATE_EXPANDABLE_ITEM_MANAGER)
        mRecyclerViewExpandableItemManager = RecyclerViewExpandableItemManager(eimSavedState)
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this)
        mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(this)



        myItemAdapter = PermissionGroupAdapter(mRecyclerViewExpandableItemManager)
        myItemAdapter.setHasStableIds(true)
        myItemAdapter.setListener(object : PermissionGroupAdapter.OnSwitchItemClickListener {
            override fun onSwitch(groupPosition: Int, childPosition: Int, item: PermissionChildItem,
                                  v: Boolean) {
                mPresenter.changeMode(groupPosition, childPosition, item)
            }
        }, object : PermissionGroupAdapter.OnGroupOtherClickListener {
            override fun onOtherClick(groupPosition: Int, view: View) {
                contextGroupPosition = groupPosition
                ATracker.send(AEvent.C_GROUP_MENU)
                showPopMenu(groupPosition, view)
            }
        })


        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(CommonDivderDecorator(applicationContext))

        mRecyclerViewExpandableItemManager.attachRecyclerView(recyclerView)

        stickyHelper = ScrollTopHelper(recyclerView, mLayoutManager as LinearLayoutManager,
                mRecyclerViewExpandableItemManager, findViewById(R.id.fab))


        mPresenter = PermGroupPresenter(this, applicationContext)
        mPresenter.loadPerms()
    }


    private fun showPopMenu(groupPosition: Int, view: View) {
        val popupMenu = PopupMenu(this, view)
        menuInflater.inflate(R.menu.group_item_menu, popupMenu.menu)
        popupMenu.setOnDismissListener(this)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
                SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                mRecyclerViewExpandableItemManager.savedState)
    }

    override fun onDestroy() {
        super.onDestroy()

        mPresenter.destroy()
        stickyHelper.release()
        mRecyclerViewExpandableItemManager.release()


        recyclerView.itemAnimator = null
        recyclerView.adapter = null

        WrapperAdapterUtils.releaseAll(mWrappedAdapter)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_with_net -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mPresenter.isLoadSuccess) {
            menuInflater.inflate(R.menu.group_menu, menu)

            val menuShowNet = menu.findItem(R.id.action_with_net)
            val mendShowHided = menu.findItem(R.id.action_show_ignored)

            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            val itemClickListener = MenuItem.OnMenuItemClickListener { item ->
                item.isChecked = !item.isChecked
                when (item.itemId) {
                    R.id.action_with_net -> sp.edit().putBoolean("key_g_show_net", item.isChecked).apply()
                    R.id.action_show_ignored -> sp.edit().putBoolean("key_g_show_ignored", item.isChecked).apply()
                }

                invalidateOptionsMenu()
                true
            }

            menuShowNet.isChecked = sp.getBoolean("key_g_show_net", false)
            menuShowNet.setOnMenuItemClickListener(itemClickListener)

            mendShowHided.isChecked = sp.getBoolean("key_g_show_ignored", false)
            mendShowHided.setOnMenuItemClickListener(itemClickListener)
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun changeTitle(groupPosition: Int, childPosition: Int, allowed: Boolean) {
        myItemAdapter.changeTitle(groupPosition, allowed)
        mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition)
        mRecyclerViewExpandableItemManager.notifyGroupItemChanged(groupPosition)
    }

    override fun refreshItem(groupPosition: Int, childPosition: Int) {
        mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition)
    }

    override fun showList(value: List<PermissionGroup>) {
        if (isFinishing) {
            return
        }
        progressBar.visibility = View.GONE
        coordinator_layout.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE

        myItemAdapter.data = value

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter)
        recyclerView.adapter = mWrappedAdapter
        invalidateOptionsMenu()
    }

    override fun showError(e: Throwable) {
        try {
            progressBar.visibility = View.GONE
            tv_error.visibility = View.VISIBLE
            tv_error.text = getString(R.string.error_msg, "", Log.getStackTraceString(e))
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

    }


    private fun changeAll(newMode: Int) {
        if (contextGroupPosition >= 0) {
            try {
                val groupPosition = contextGroupPosition
                val permissionGroup = myItemAdapter.data[groupPosition]
                val apps = permissionGroup.apps
                val size = apps.size
                for (i in 0 until size) {
                    val info = apps[i]
                    if (info.opEntryInfo.mode != newMode) {
                        //changeMode(groupPosition, i, info);
                        mPresenter.changeMode(groupPosition, i, info)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close_all -> {
                changeAll(AppOpsManager.MODE_IGNORED)
                ATracker.send(AEvent.C_GROUP_IGNORE_ALL)
                return true
            }
            R.id.action_open_all -> {
                changeAll(AppOpsManager.MODE_ALLOWED)
                ATracker.send(AEvent.C_GROUP_OPEN_ALL)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onDismiss(menu: PopupMenu) {
        contextGroupPosition = -1
    }

    override fun onGroupCollapse(groupPosition: Int, fromUser: Boolean, o: Any?) {

    }

    override fun onGroupExpand(groupPosition: Int, fromUser: Boolean, o: Any?) {
        if (fromUser) {
            adjustScrollPositionOnGroupExpanded(groupPosition)
        }
    }

    private fun adjustScrollPositionOnGroupExpanded(groupPosition: Int) {
        val pad = (resources.displayMetrics.density * 10).toInt()
        val childItemHeight = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) + pad * 2
        val topMargin = (resources.displayMetrics.density * 16).toInt()

        mRecyclerViewExpandableItemManager
                .scrollToGroup(groupPosition, childItemHeight, topMargin, topMargin)

        recyclerView.smoothScrollBy(0, -100)
    }

    companion object {

        private val SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager"

        private val TAG = "PermissionGroupActivity"
    }

}
