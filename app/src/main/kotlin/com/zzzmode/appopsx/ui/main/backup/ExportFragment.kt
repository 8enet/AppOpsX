package com.zzzmode.appopsx.ui.main.backup

import android.os.Bundle
import android.support.v4.util.SparseArrayCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.model.AppInfo
import java.util.ArrayList

/**
 * 导出配置
 * Created by zl on 2017/5/7.
 */

class ExportFragment : BaseConfigFragment(), View.OnClickListener {

    private lateinit var adapter: ExportAdapter

    private lateinit var mPresenter: ConfigPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelableArrayList<AppInfo>(BackupActivity.EXTRA_APPS)?.apply {
            adapter = ExportAdapter(this)
        }

        recyclerView?.adapter = adapter

        view.findViewById<View>(R.id.fab).setOnClickListener(this)

        mPresenter = ConfigPresenter(view.context, this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.fab) {
            //导出
            export()
            ATracker.send(AEvent.A_EXPORT)
        }
    }


    private fun export() {

        adapter.checkedApps.apply {
            val size = size()

            val appInfos = (0 until size).mapTo(ArrayList<AppInfo>()) { valueAt(it) }

            mPresenter.export(appInfos.toTypedArray())
        }

    }

    companion object {

        private const val TAG = "ExportFragment"
    }

}
