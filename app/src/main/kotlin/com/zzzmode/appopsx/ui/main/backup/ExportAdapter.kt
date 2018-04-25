package com.zzzmode.appopsx.ui.main.backup

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.main.AppItemViewHolder
import com.zzzmode.appopsx.ui.model.AppInfo

/**
 * 导出
 * Created by zl on 2017/5/7.
 */

internal class ExportAdapter(private val appInfos: List<AppInfo>) : RecyclerView.Adapter<ExportAdapter.ExportViewHolder>(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    val checkedApps = SparseArrayCompat<AppInfo>()

    init {
        for ((i, appInfo) in appInfos.withIndex()) {

            checkedApps.append(i, appInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExportAdapter.ExportViewHolder {
        return ExportAdapter.ExportViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_export_app, parent, false))
    }

    override fun onBindViewHolder(holder: ExportAdapter.ExportViewHolder, position: Int) {
        holder.bindData(appInfos[position])
        holder.itemView.setOnClickListener(this)
        holder.itemView.tag = holder

        holder.checkBox.tag = holder
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = isChecked(position)
        holder.checkBox.setOnCheckedChangeListener(this)
    }

    override fun getItemCount(): Int {
        return appInfos.size
    }

    override fun onClick(v: View) {

        (v.tag as? ExportViewHolder)?.let {
            handleCheck(it, true)
        }

    }

    private fun handleCheck(holder: ExportViewHolder, change: Boolean) {
        val pos = holder.adapterPosition

        if (holder.checkBox.isChecked) {
            checkedApps.delete(pos)
        } else {
            checkedApps.put(pos, appInfos[pos])
        }
        if (change) {
            holder.checkBox.setOnCheckedChangeListener(null)
            holder.checkBox.isChecked = isChecked(pos)
            holder.checkBox.setOnCheckedChangeListener(this)
        }
    }

    private fun isChecked(pos: Int): Boolean {
        return checkedApps.indexOfKey(pos) >= 0
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {

        (buttonView.tag as? ExportViewHolder)?.let {
            handleCheck(it, false)
        }
    }


    internal class ExportViewHolder(itemView: View) : AppItemViewHolder(itemView) {

        var checkBox: CheckBox = itemView.findViewById<View>(R.id.checkbox) as CheckBox

    }
}
