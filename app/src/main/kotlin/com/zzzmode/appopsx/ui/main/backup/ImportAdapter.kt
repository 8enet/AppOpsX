package com.zzzmode.appopsx.ui.main.backup

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.util.Formatter
import java.util.ArrayList

/**
 * Created by zl on 2017/5/7.
 */

internal class ImportAdapter : RecyclerView.Adapter<ImportAdapter.ViewHolder>(), View.OnClickListener {

    private val mDatas = ArrayList<RestoreModel>()
    private var itemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ImportAdapter.ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_import_layout, parent, false))
    }

    fun showData(datas: List<RestoreModel>?) {
        mDatas.clear()
        if (datas != null) {
            mDatas.addAll(datas)
        }
        notifyDataSetChanged()
    }

    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(mDatas[position])
        holder.itemView.tag = holder
        holder.delete.tag = holder

        holder.itemView.setOnClickListener(this)
        holder.delete.setOnClickListener(this)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onClick(v: View) {
        val id = v.id
        val tag = v.tag
        if (id == R.id.fl_delete) {
            //delete
            if (tag is ViewHolder) {
                showDelete(tag)
            }
            ATracker.send(AEvent.A_DELETE_BACKFILE)
        } else {
            //item
            if (tag is ViewHolder) {
                if (itemClickListener != null) {
                    val position = tag.adapterPosition
                    val model = mDatas[position]
                    itemClickListener!!.onItemClick(model)
                }

                ATracker.send(AEvent.A_RESTORE)
            }

        }
    }


    private fun showDelete(holder: ViewHolder) {
        val context = holder.itemView.context
        val position = holder.adapterPosition
        val model = mDatas[position]
        val alertDialog = AlertDialog.Builder(context)
                .setTitle(R.string.delete)
                .setMessage(context.getString(R.string.dlg_delete_msg, model.path))
                .setPositiveButton(R.string.delete) { _, _ ->
                    ATracker.send(AEvent.A_DELETE_CONFIRM)
                    val ret = BFileUtils.deleteBackFile(model.path!!)
                    if (ret) {
                        mDatas.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        Toast.makeText(context, "delete error", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        alertDialog.show()
    }


    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvName: TextView
        var tvTime: TextView
        var tvBackCount: TextView
        var tvFileSize: TextView
        var delete: View

        init {
            tvName = itemView.findViewById<View>(R.id.title) as TextView
            tvTime = itemView.findViewById<View>(R.id.tv_time) as TextView
            tvBackCount = itemView.findViewById<View>(R.id.tv_back_count) as TextView
            tvFileSize = itemView.findViewById<View>(R.id.tv_file_len) as TextView
            delete = itemView.findViewById(R.id.fl_delete)
        }

        fun bindData(model: RestoreModel) {
            tvName.text = model.fileName
            tvTime.text = Formatter.formatDate(model.createTime)

            tvBackCount.text = tvBackCount.resources.getString(R.string.backup_count,
                    model.size)
            tvFileSize.text = tvFileSize.resources
                    .getString(R.string.backup_file_size, Formatter.formatFileSize(
                            model.fileSize))
        }
    }

    internal interface OnItemClickListener {

        fun onItemClick(model: RestoreModel)
    }

}
