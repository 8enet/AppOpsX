package com.zzzmode.appopsx.ui.main.backup

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker

/**
 * 导入配置
 * Created by zl on 2017/5/7.
 */

class ImportFragment : BaseConfigFragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mPresenter: ConfigPresenter
    private lateinit var adapter: ImportAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById<View>(R.id.swiperefreshlayout) as SwipeRefreshLayout
        swipeRefreshLayout.isRefreshing = false
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener { reload() }

        adapter = ImportAdapter()
        recyclerView?.adapter = adapter

        adapter.setItemClickListener(object : ImportAdapter.OnItemClickListener {
            override fun onItemClick(model: RestoreModel) {
                showRestore(model)
            }
        })

        mPresenter = ConfigPresenter(context!!.applicationContext, this)
        reload()
    }


    private fun reload() {
        adapter.showData(mPresenter.restoreFiles)
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showRestore(model: RestoreModel) {
        val alertDialog = AlertDialog.Builder(context!!)
                .setTitle(R.string.perm_restore)
                .setMessage(getString(R.string.dlg_restore_msg, model.path))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mPresenter.restoreOps(model)

                    ATracker.send(AEvent.A_RESTORE_CONFIRM)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        alertDialog.show()
    }

}
