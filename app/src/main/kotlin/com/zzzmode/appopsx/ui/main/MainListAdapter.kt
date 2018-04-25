package com.zzzmode.appopsx.ui.main

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity
import java.util.ArrayList

/**
 * Created by zl on 2016/11/18.
 */

internal open class MainListAdapter : RecyclerView.Adapter<AppItemViewHolder>(), View.OnClickListener {

    var appInfos: MutableList<AppInfo> = ArrayList()


    protected open val aEventId: String
        get() = AEvent.C_APP

    fun addItem(info: AppInfo) {
        appInfos.add(info)
        notifyItemInserted(appInfos.size - 1)
    }

    fun showItems(infos: List<AppInfo>?) {
        appInfos.clear()
        if (infos != null) {
            appInfos.addAll(infos)
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        return AppItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_main_app, parent, false))
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val appInfo = appInfos[position]
        holder.bindData(appInfo)

        holder.tvName.text = processText(appInfo.appName)
        holder.itemView.tag = appInfo
        holder.itemView.setOnClickListener(this)

    }

    protected open fun processText(name: String?): CharSequence? {
        return name
    }

    override fun getItemCount(): Int {
        return appInfos.size
    }

    override fun onClick(v: View) {


        (v.tag as? AppInfo)?.let {
            val intent = Intent(v.context, AppPermissionActivity::class.java)
            intent.putExtra(AppPermissionActivity.EXTRA_APP, it)
            v.context.startActivity(intent)
            ATracker.send(aEventId)
        }
    }

}
