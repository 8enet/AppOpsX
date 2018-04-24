package com.zzzmode.appopsx.ui.main.backup

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator

/**
 * Created by zl on 2017/5/7.
 */

open class BaseConfigFragment : Fragment(), IConfigView {

    internal var recyclerView: RecyclerView? = null

    private var progressDialog: ProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(CommonDivderDecorator(context))
            itemAnimator = RefactoredDefaultItemAnimator()
        }

    }

    override fun showProgress(show: Boolean, max: Int) {
        progressDialog?.let {
            if(it.isShowing){
                it.dismiss()
            }
        }
        progressDialog = null

        if (show) {
            progressDialog = ProgressDialog(activity)

            progressDialog?.apply {
                setTitle(R.string.dlg_title)
                setCanceledOnTouchOutside(false)
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                this.max = max
                show()
            }


        }
    }

    override fun setProgress(progress: Int) {
        progressDialog?.progress = progress
    }
}
