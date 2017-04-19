package com.zzzmode.appopsx.ui.main;

import android.support.annotation.IntRange;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
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
import com.zzzmode.appopsx.ui.model.PremissionChildItem;
import com.zzzmode.appopsx.ui.model.PremissionGroup;
import com.zzzmode.appopsx.ui.widget.ExpandableItemIndicator;

import java.util.List;

/**
 * Created by zl on 2017/1/18.
 */

class PremissionGroupAdapter extends AbstractExpandableItemAdapter<PremissionGroupAdapter.GroupViewHolder,PremissionGroupAdapter.ChildViewHolder> implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


    private PremissionGroupAdapter.OnSwitchItemClickListener listener;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;

    private List<PremissionGroup> mData;

    public void setData(List<PremissionGroup> data) {
        this.mData = data;
    }

    public List<PremissionGroup> getData() {
        return mData;
    }

    void changeTitle(int groupPosition, boolean allowed){
        PremissionGroup premissionGroup = mData.get(groupPosition);
        if(premissionGroup != null){
            premissionGroup.grants+=(allowed?1:-1);
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
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_premission_group_item,parent,false));
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        return new ChildViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_premission_child_item,parent,false));
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        PremissionGroup premissionGroup = mData.get(groupPosition);
        if(TextUtils.isEmpty(premissionGroup.opPermsLab)){
            holder.tvPermName.setText(premissionGroup.opName);
        }else {
            holder.tvPermName.setText(premissionGroup.opPermsLab);
        }
        holder.groupIcon.setImageResource(premissionGroup.icon);

        holder.itemView.setTag(R.id.groupPosition,groupPosition);
        holder.itemView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);


        holder.tvCount.setText(holder.itemView.getResources().getString(R.string.premission_count,premissionGroup.grants,premissionGroup.count));


        final int expandState = holder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            boolean isExpanded=(expandState & ExpandableItemConstants.STATE_FLAG_IS_EXPANDED) != 0;
            boolean animateIndicator = ((expandState & ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);
            holder.indicator.setExpandedState(isExpanded, animateIndicator);
        }

    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder, int groupPosition, int childPosition, @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        PremissionChildItem appPremissions = mData.get(groupPosition).apps.get(childPosition);

        LocalImageLoader.load(holder.imgIcon,appPremissions.appInfo);

        holder.tvName.setText(appPremissions.appInfo.appName);

        holder.itemView.setOnClickListener(this);


        holder.itemView.setTag(holder);

        holder.switchCompat.setTag(appPremissions);
        holder.switchCompat.setTag(R.id.groupPosition,groupPosition);
        holder.switchCompat.setTag(R.id.childPosition,childPosition);

        holder.switchCompat.setOnCheckedChangeListener(null);
        holder.switchCompat.setChecked(appPremissions.opEntryInfo.isAllowed());
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
        if(buttonView.getTag() instanceof PremissionChildItem && listener != null){
            int groupPosition= (int) buttonView.getTag(R.id.groupPosition);
            int childPosition= (int) buttonView.getTag(R.id.childPosition);
            listener.onSwitch(groupPosition,childPosition,((PremissionChildItem) buttonView.getTag()),isChecked);
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
        void onSwitch(int groupPosition, int childPosition,PremissionChildItem item, boolean v);
    }
}
