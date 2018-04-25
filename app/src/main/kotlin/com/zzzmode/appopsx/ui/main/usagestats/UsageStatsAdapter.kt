package com.zzzmode.appopsx.ui.main.usagestats

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.ImageView
import android.widget.TextView
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.common.OtherOp
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.core.LocalImageLoader
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.OpEntryInfo
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by zl on 2017/8/16.
 */
internal class UsageStatsAdapter : RecyclerView.Adapter<UsageStatsAdapter.ViewHolder>(), OnClickListener, OnCheckedChangeListener {

    private val mDatas = ArrayList<Pair<AppInfo, OpEntryInfo>>()

    fun showItems(value: List<Pair<AppInfo, OpEntryInfo>>?) {
        mDatas.clear()
        if (value != null) {
            mDatas.addAll(value)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_app_usagestats_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pair = mDatas[position]

        LocalImageLoader.load(holder.imgIcon, pair.first)

        holder.tvName.text = pair.first.appName
        holder.imgPerm.setImageResource(pair.second.icon)

        val time = pair.second.opEntry.time
        if (time > 0) {
            holder.tvLastTime.text = DateUtils
                    .getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_TIME)
        } else {
            holder.tvLastTime.setText(R.string.never_used)
        }
        if (OtherOp.isSupportCount()) {
            holder.tvPermName.text = holder.tvPermName.resources.getString(R.string.perms_count,
                    pair.second.opPermsLab, pair.second.opEntry.allowedCount)
        } else {
            holder.tvPermName.text = pair.second.opPermsLab
        }
        holder.itemView.tag = holder
        holder.itemView.setOnClickListener(this)

        holder.switchCompat.tag = holder

        holder.switchCompat.setOnCheckedChangeListener(null)
        holder.switchCompat.isChecked = pair.second.isAllowed
        holder.switchCompat.setOnCheckedChangeListener(this)

    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onClick(view: View) {

        (view.tag as? ViewHolder)?.let {
            val pair = mDatas[it.adapterPosition]

            val intent = Intent(view.context, AppPermissionActivity::class.java)
            intent.putExtra(AppPermissionActivity.EXTRA_APP, pair.first)
            view.context.startActivity(intent)
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        val tag = compoundButton.tag
        if (tag is ViewHolder) {
            val pair = mDatas[tag.adapterPosition]

            Helper.setMode(compoundButton.context, pair.first.packageName, pair.second, b)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()

        }
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgPerm: ImageView
        var imgIcon: ImageView
        var tvName: TextView
        var tvLastTime: TextView
        var tvPermName: TextView
        var switchCompat: SwitchCompat


        init {
            imgIcon = itemView.findViewById<View>(R.id.app_icon) as ImageView
            tvName = itemView.findViewById<View>(R.id.app_name) as TextView
            switchCompat = itemView.findViewById<View>(R.id.switch_compat) as SwitchCompat
            tvLastTime = itemView.findViewById<View>(R.id.perm_last_time) as TextView
            tvPermName = itemView.findViewById<View>(R.id.perm_name) as TextView
            imgPerm = itemView.findViewById<View>(R.id.img_group) as ImageView
        }
    }

    companion object {

        private const val TAG = "UsageStatsAdapter"
    }
}
