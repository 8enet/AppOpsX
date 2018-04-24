package com.zzzmode.appopsx.ui.main.group

import android.support.annotation.IntRange
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.core.LocalImageLoader
import com.zzzmode.appopsx.ui.model.PermissionChildItem
import com.zzzmode.appopsx.ui.model.PermissionGroup
import com.zzzmode.appopsx.ui.widget.ExpandableItemIndicator

/**
 * Created by zl on 2017/1/18.
 */

internal class PermissionGroupAdapter(expandableItemManager: RecyclerViewExpandableItemManager) : AbstractExpandableItemAdapter<PermissionGroupAdapter.GroupViewHolder, PermissionGroupAdapter.ChildViewHolder>(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private var listener: PermissionGroupAdapter.OnSwitchItemClickListener? = null
    private var mOnGroupOtherClickListener: OnGroupOtherClickListener? = null

    private var mExpandableItemManager: RecyclerViewExpandableItemManager =  expandableItemManager

    var data: List<PermissionGroup> = ArrayList<PermissionGroup>()
        set(value) {

            if(field is MutableList){
                (field as MutableList<PermissionGroup>).clear()

                (field as MutableList<PermissionGroup>).addAll(value)
            }
        }





    fun changeTitle(groupPosition: Int, allowed: Boolean) {
        val permissionGroup = data[groupPosition]
        permissionGroup.grants = permissionGroup.grants + if (allowed) 1 else -1
    }

    fun setListener(listener: OnSwitchItemClickListener,
                    onGroupOtherClickListener: OnGroupOtherClickListener) {
        this.listener = listener
        this.mOnGroupOtherClickListener = onGroupOtherClickListener
    }


    override fun getGroupCount(): Int {
        return data.size
    }

    override fun getChildCount(groupPosition: Int): Int {
        return data[groupPosition].apps.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup,
                                         @IntRange(from = -8388608L, to = 8388607L) viewType: Int): GroupViewHolder {
        return GroupViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_permission_group_item, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup,
                                         @IntRange(from = -8388608L, to = 8388607L) viewType: Int): ChildViewHolder {
        return ChildViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_permission_child_item, parent, false))
    }

    override fun onBindGroupViewHolder(holder: GroupViewHolder, groupPosition: Int,
                                       @IntRange(from = -8388608L, to = 8388607L) viewType: Int) {
        val permissionGroup = data[groupPosition]

        holder.tvPermName.text = if (TextUtils.isEmpty(permissionGroup.opPermsLab)) permissionGroup.opName else permissionGroup.opPermsLab

        holder.groupIcon.setImageResource(permissionGroup.icon)

        holder.itemView.setTag(R.id.groupPosition, groupPosition)

        holder.itemView.tag = holder
        holder.itemView.setOnClickListener(this)

        holder.tvCount.text = holder.itemView.resources
                .getString(R.string.permission_count, permissionGroup.grants,
                        permissionGroup.count)

        holder.imgMenu.tag = holder
        holder.imgMenu.setOnClickListener(this)

        val expandState = holder.expandStateFlags

        if (expandState and ExpandableItemConstants.STATE_FLAG_IS_UPDATED != 0) {
            val isExpanded = expandState and ExpandableItemConstants.STATE_FLAG_IS_EXPANDED != 0
            val animateIndicator = expandState and ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED != 0
            holder.indicator.setExpandedState(isExpanded, animateIndicator)

            holder.imgMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE
        }

    }

    override fun onBindChildViewHolder(holder: ChildViewHolder, groupPosition: Int, childPosition: Int,
                                       @IntRange(from = -8388608L, to = 8388607L) viewType: Int) {
        val appPermissions = data[groupPosition].apps[childPosition]

        LocalImageLoader.load(holder.imgIcon, appPermissions.appInfo)

        holder.tvName.text = appPermissions.appInfo.appName

        val time = appPermissions.opEntryInfo.opEntry.time
        if (time > 0) {
            holder.tvLastTime.text = DateUtils
                    .getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_TIME)
        } else {
            holder.tvLastTime.setText(R.string.never_used)
        }


        holder.itemView.setOnClickListener(this)
        holder.itemView.tag = holder

        holder.switchCompat.tag = appPermissions
        holder.switchCompat.setTag(R.id.groupPosition, groupPosition)
        holder.switchCompat.setTag(R.id.childPosition, childPosition)

        holder.switchCompat.setOnCheckedChangeListener(null)
        holder.switchCompat.isChecked = appPermissions.opEntryInfo.isAllowed
        holder.switchCompat.setOnCheckedChangeListener(this)


    }

    override fun onCheckCanExpandOrCollapseGroup(holder: GroupViewHolder, groupPosition: Int, x: Int,
                                                 y: Int, expand: Boolean): Boolean {
        return false
    }

    override fun onClick(v: View) {

        val tag = v.tag
        if (tag is RecyclerView.ViewHolder) {
            val flatPosition = (v.tag as RecyclerView.ViewHolder).adapterPosition

            if (flatPosition == RecyclerView.NO_POSITION) {
                return
            }

            val expandablePosition = mExpandableItemManager.getExpandablePosition(flatPosition)
            val groupPosition = RecyclerViewExpandableItemManager
                    .getPackedPositionGroup(expandablePosition)
//            val childPosition = RecyclerViewExpandableItemManager
//                    .getPackedPositionChild(expandablePosition)

            when (v.id) {
                R.id.layout_group_item ->
                    // toggle expanded/collapsed
                    if (mExpandableItemManager.isGroupExpanded(groupPosition)) {
                        mExpandableItemManager.collapseGroup(groupPosition)
                    } else {
                        mExpandableItemManager.expandGroup(groupPosition)
                    }
                R.id.img_menu_ups -> if (mOnGroupOtherClickListener != null) {
                    mOnGroupOtherClickListener?.onOtherClick(groupPosition, v)
                }
            }
        }

        if (v.tag is ChildViewHolder) {
            (v.tag as ChildViewHolder).switchCompat.toggle()
        } else if (v.id == R.id.img_menu_ups) {

        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.tag is PermissionChildItem) {
            val groupPosition = buttonView.getTag(R.id.groupPosition) as Int
            val childPosition = buttonView.getTag(R.id.childPosition) as Int
            listener?.onSwitch(groupPosition, childPosition, buttonView.tag as PermissionChildItem,
                    isChecked)
        }
    }


    internal class GroupViewHolder(itemView: View) : AbstractExpandableItemViewHolder(itemView) {

        var tvPermName: TextView
        var tvCount: TextView
        var groupIcon: ImageView
        var indicator: ExpandableItemIndicator
        var imgMenu: ImageView

        init {
            tvPermName = itemView.findViewById<View>(R.id.tv_permission_name) as TextView
            tvCount = itemView.findViewById<View>(R.id.tv_permission_count) as TextView
            indicator = itemView.findViewById<View>(R.id.indicator) as ExpandableItemIndicator
            groupIcon = itemView.findViewById<View>(R.id.img_group) as ImageView
            imgMenu = itemView.findViewById<View>(R.id.img_menu_ups) as ImageView
        }
    }

    internal class ChildViewHolder(itemView: View) : AbstractExpandableItemViewHolder(itemView) {

        var imgIcon: ImageView
        var tvName: TextView
        var tvLastTime: TextView
        var switchCompat: SwitchCompat

        init {
            imgIcon = itemView.findViewById<View>(R.id.app_icon) as ImageView
            tvName = itemView.findViewById<View>(R.id.app_name) as TextView
            switchCompat = itemView.findViewById<View>(R.id.switch_compat) as SwitchCompat
            tvLastTime = itemView.findViewById<View>(R.id.perm_last_time) as TextView
        }
    }

    internal interface OnSwitchItemClickListener {

        fun onSwitch(groupPosition: Int, childPosition: Int, item: PermissionChildItem, v: Boolean)
    }

    internal interface OnGroupOtherClickListener {

        fun onOtherClick(groupPosition: Int, view: View)
    }
}
