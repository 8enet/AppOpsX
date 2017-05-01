package com.zzzmode.appopsx.ui.main;

import android.support.annotation.IntRange;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import com.zzzmode.appopsx.ui.widget.ExpandableItemIndicator;

import java.util.List;

/**
 * Created by zl on 2017/1/18.
 */

class PermissionGroupAdapter extends AbstractExpandableItemAdapter<PermissionGroupAdapter.GroupViewHolder,PermissionGroupAdapter.ChildViewHolder> implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


    private PermissionGroupAdapter.OnSwitchItemClickListener listener;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;

    private List<PermissionGroup> mData;

    public void setData(List<PermissionGroup> data) {
        this.mData = data;
    }

    public List<PermissionGroup> getData() {
        return mData;
    }

    void changeTitle(int groupPosition, boolean allowed){
        PermissionGroup permissionGroup = mData.get(groupPosition);
        if(permissionGroup != null){
            permissionGroup.grants+=(allowed?1:-1);
        }
    }

    void setListener(OnSwitchItemClickListener listener,View.OnCreateContextMenuListener onCreateContextMenuListener) {
        this.listener = listener;
        this.mOnCreateContextMenuListener=onCreateContextMenuListener;
    }



    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mData.get(groupPosition).apps.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_permission_group_item,parent,false));
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        return new ChildViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_permission_child_item,parent,false));
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        PermissionGroup permissionGroup = mData.get(groupPosition);
        if(TextUtils.isEmpty(permissionGroup.opPermsLab)){
            holder.tvPermName.setText(permissionGroup.opName);
        }else {
            holder.tvPermName.setText(permissionGroup.opPermsLab);
        }
        holder.groupIcon.setImageResource(permissionGroup.icon);

        holder.itemView.setTag(R.id.groupPosition,groupPosition);
        holder.itemView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);


        holder.tvCount.setText(holder.itemView.getResources().getString(R.string.permission_count,permissionGroup.grants,permissionGroup.count));


        final int expandState = holder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            boolean isExpanded=(expandState & ExpandableItemConstants.STATE_FLAG_IS_EXPANDED) != 0;
            boolean animateIndicator = ((expandState & ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);
            holder.indicator.setExpandedState(isExpanded, animateIndicator);
        }

    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder, int groupPosition, int childPosition, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        PermissionChildItem appPermissions = mData.get(groupPosition).apps.get(childPosition);

        LocalImageLoader.load(holder.imgIcon,appPermissions.appInfo);

        holder.tvName.setText(appPermissions.appInfo.appName);

        holder.itemView.setOnClickListener(this);


        holder.itemView.setTag(holder);

        holder.switchCompat.setTag(appPermissions);
        holder.switchCompat.setTag(R.id.groupPosition,groupPosition);
        holder.switchCompat.setTag(R.id.childPosition,childPosition);

        holder.switchCompat.setOnCheckedChangeListener(null);
        holder.switchCompat.setChecked(appPermissions.opEntryInfo.isAllowed());
        holder.switchCompat.setOnCheckedChangeListener(this);


    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof ChildViewHolder){
            ((ChildViewHolder) v.getTag()).switchCompat.toggle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getTag() instanceof PermissionChildItem && listener != null){
            int groupPosition= (int) buttonView.getTag(R.id.groupPosition);
            int childPosition= (int) buttonView.getTag(R.id.childPosition);
            listener.onSwitch(groupPosition,childPosition,((PermissionChildItem) buttonView.getTag()),isChecked);
        }
    }


    static class GroupViewHolder extends AbstractExpandableItemViewHolder {

        TextView tvPermName;
        TextView tvCount;
        ImageView groupIcon;
        ExpandableItemIndicator indicator;

        public GroupViewHolder(View itemView) {
            super(itemView);
            tvPermName= (TextView) itemView.findViewById(R.id.tv_permission_name);
            tvCount= (TextView) itemView.findViewById(R.id.tv_permission_count);
            indicator= (ExpandableItemIndicator) itemView.findViewById(R.id.indicator);
            groupIcon= (ImageView) itemView.findViewById(R.id.img_group);
        }
    }

    static class ChildViewHolder extends AbstractExpandableItemViewHolder{

        ImageView imgIcon;
        TextView tvName;
        SwitchCompat switchCompat;

        public ChildViewHolder(View itemView) {
            super(itemView);
            imgIcon= (ImageView) itemView.findViewById(R.id.app_icon);
            tvName= (TextView) itemView.findViewById(R.id.app_name);
            switchCompat= (SwitchCompat) itemView.findViewById(R.id.switch_compat);
        }
    }

    public interface OnSwitchItemClickListener{
        void onSwitch(int groupPosition, int childPosition, PermissionChildItem item, boolean v);
    }
}
