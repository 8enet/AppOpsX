package com.zzzmode.appopsx.ui.permission

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.common.OtherOp
import com.zzzmode.appopsx.ui.core.SpHelper
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import java.util.ArrayList

/**
 * Created by zl on 2016/11/18.
 */
internal class AppPermissionAdapter : RecyclerView.Adapter<AppPermissionAdapter.ViewHolder>(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    var datas: List<OpEntryInfo> = ArrayList()

    private var listener: OnSwitchItemClickListener? = null

    private var showPermDesc: Boolean = false
    private var showOpName: Boolean = false
    private var showPermTime: Boolean = false

    fun setShowConfig(showPermDesc: Boolean, showOpName: Boolean, showPermTime: Boolean) {
        this.showPermDesc = showPermDesc
        this.showOpName = showOpName
        this.showPermTime = showPermTime
    }

    fun updateItem(info: OpEntryInfo?) {
        if (info != null) {
            val i = datas.indexOf(info)
            if (i != -1 && i < datas.size) {
                notifyItemChanged(i)
            }
        }
    }

    fun setListener(listener: OnSwitchItemClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_permission_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val opEntryInfo = datas[position]
        holder.itemView.setOnClickListener(this)
        holder.itemView.tag = holder

        holder.switchCompat.tag = opEntryInfo

            holder.icon.setImageResource(opEntryInfo.icon)
            if (opEntryInfo.opPermsLab != null) {
                holder.title.text = opEntryInfo.opPermsLab
            } else {
                holder.title.text = opEntryInfo.opName
            }

            if (showOpName && opEntryInfo.opName != null) {
                holder.summary.visibility = View.VISIBLE
                holder.summary.text = opEntryInfo.opName
            } else {
                holder.summary.visibility = View.GONE
            }

            if (showPermDesc && opEntryInfo.opPermsDesc != null) {
                holder.summary.visibility = View.VISIBLE
                holder.summary.text = opEntryInfo.opPermsDesc
            } else {
                if (!showOpName) {
                    holder.summary.visibility = View.GONE
                }
            }

            if (showPermTime) {


                if (opEntryInfo.opEntry.time > 0) {
                    holder.lastTime.text = DateUtils
                            .getRelativeTimeSpanString(opEntryInfo.opEntry.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_TIME)
                } else {
                    holder.lastTime.setText(R.string.never_used)
                }
                holder.lastTime.visibility = View.VISIBLE

            } else {
                holder.lastTime.visibility = View.GONE
            }

            holder.switchCompat.setOnCheckedChangeListener(null)
            holder.switchCompat.isChecked = opEntryInfo.isAllowed
            holder.switchCompat.setOnCheckedChangeListener(this)

    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onClick(v: View) {
        if (v.tag is ViewHolder) {
            val position = (v.tag as ViewHolder).adapterPosition
            val opEntryInfo = datas[position]
            if (OtherOp.isOtherOp(opEntryInfo.opEntry.op) && !SpHelper.isIgnoredNetOps(v.context, opEntryInfo.opEntry.op)) {


                val runnable = Runnable { (v.tag as ViewHolder).switchCompat.toggle() }
                showWarning(v.context, opEntryInfo, runnable)

            } else {
                (v.tag as ViewHolder).switchCompat.toggle()
            }

        }
    }

    private fun showWarning(context: Context, opEntryInfo: OpEntryInfo, runnable: Runnable) {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.other_op_hint)
                .setPositiveButton(android.R.string.ok) { _, _ -> runnable.run() }
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.other_op_stop_show) { _, _ -> SpHelper.ignoredNetOps(context, opEntryInfo.opEntry.op) }
                .create()
        alertDialog.show()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.tag is OpEntryInfo ) {
            listener?.onSwitch(buttonView.tag as OpEntryInfo, isChecked)
        }
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icon: ImageView
        var title: TextView
        var summary: TextView
        var lastTime: TextView
        var switchCompat: SwitchCompat

        init {
            icon = itemView.findViewById<View>(R.id.img_group) as ImageView
            title = itemView.findViewById<View>(android.R.id.title) as TextView
            summary = itemView.findViewById<View>(android.R.id.summary) as TextView
            lastTime = itemView.findViewById<View>(R.id.last_time) as TextView
            switchCompat = itemView.findViewById<View>(R.id.switch_compat) as SwitchCompat

        }
    }

    interface OnSwitchItemClickListener {

        fun onSwitch(info: OpEntryInfo, v: Boolean)
    }

}
