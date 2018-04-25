package com.zzzmode.appopsx.ui.permission

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.core.LocalImageLoader
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator
import kotlinx.android.synthetic.main.activity_alert_opsx.*

/**
 * Created by zl on 2017/5/1.
 */

class AlertInstalledPremActivity : BaseActivity(), IPermView {
    private var appInfo: AppInfo? = null

    private var mProgressBar: ProgressBar? = null
    private var tvError: TextView? = null

    private lateinit var mPresenter: PermPresenter
    private lateinit var adapter: AppPermissionAdapter

    private val titleView: View
        get() {
            val inflate = layoutInflater.inflate(R.layout.layout_dlg_title, null)
            val icon = inflate.findViewById<View>(R.id.app_icon) as ImageView
            val tvTitle = inflate.findViewById<View>(R.id.tv_title) as TextView
            val tvInfo = inflate.findViewById<View>(R.id.tv_info) as TextView

            appInfo?.apply {
                LocalImageLoader.load(icon, this)
                tvTitle.text = appName
                tvInfo.text = "$packageName ($versionName)"
            }


            return inflate
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appInfo = intent?.getParcelableExtra(EXTRA_APP)
        if (appInfo == null) {
            finish()
            return
        }

        initView()

        mPresenter = PermPresenter(this, appInfo!!, applicationContext)
        mPresenter.setAutoDisabled(false)
        mPresenter.setSortByMode(true)
        mPresenter.setUp()
    }

    private fun initView() {
        val inflate = layoutInflater.inflate(R.layout.activity_alert_opsx,null)
        tvError = inflate.findViewById<View>(R.id.tv_error) as TextView
        mProgressBar = inflate.findViewById<View>(R.id.progressBar) as ProgressBar

        val recyclerView = inflate.findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.addItemDecoration(CommonDivderDecorator(applicationContext))

        adapter = AppPermissionAdapter()
        recyclerView.adapter = adapter

        adapter.setListener(object : AppPermissionAdapter.OnSwitchItemClickListener {
            override fun onSwitch(info: OpEntryInfo, v: Boolean) {

                mPresenter.switchMode(info, v)
            }
        })

        val clickListener = DialogInterface.OnClickListener { _, _ -> finish() }

        val alertDialog = AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setView(inflate)
                .setPositiveButton(android.R.string.ok, clickListener)
                .create()

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        val window = alertDialog.window
        window?.setWindowAnimations(0)

        inflate.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {

                        val minHeight = applicationContext.resources
                                .getDimensionPixelOffset(R.dimen.dlg_min_height)
                        if (inflate.height < minHeight) {
                            val layoutParams = inflate.layoutParams
                            layoutParams.height = minHeight
                            inflate.layoutParams = layoutParams
                        }
                        inflate.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    }
                })

        alertDialog.setOnDismissListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.destory()
    }

    override fun showProgress(show: Boolean) {
        tvError?.visibility = View.GONE
        mProgressBar?.visibility = if (show) View.VISIBLE else View.GONE

    }

    override fun showError(text: CharSequence) {
        mProgressBar?.visibility = View.GONE
        tvError?.visibility = View.VISIBLE
        tvError?.text = text
    }


    override fun showPerms(opEntryInfos: List<OpEntryInfo>) {
        adapter.setShowConfig(false, false, false)
        adapter.datas = opEntryInfos
        adapter.notifyDataSetChanged()
    }

    override fun updateItem(info: OpEntryInfo) {
        info.mode = info.opEntry.mode
        adapter.updateItem(info)
    }

    companion object {

        const val EXTRA_APP = "extra.app"
    }
}
