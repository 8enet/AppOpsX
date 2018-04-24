package com.zzzmode.appopsx.ui.main

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.core.LocalImageLoader
import com.zzzmode.appopsx.ui.model.AppInfo

/**
 * Created by zl on 2017/5/7.
 */

open class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal var imgIcon: ImageView = itemView.findViewById<View>(R.id.app_icon) as ImageView
    internal var tvName: TextView = itemView.findViewById<View>(R.id.app_name) as TextView


    fun bindData(appInfo: AppInfo) {
        tvName.text = appInfo.appName
        LocalImageLoader.load(imgIcon, appInfo)
    }
}
