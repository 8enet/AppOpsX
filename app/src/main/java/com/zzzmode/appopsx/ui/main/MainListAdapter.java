package com.zzzmode.appopsx.ui.main;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2016/11/18.
 */

class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> implements View.OnClickListener{

    protected List<AppInfo> appInfos=new ArrayList<>();

    void addItem(AppInfo info){
        appInfos.add(info);
        notifyItemInserted(appInfos.size()-1);
    }

    void showItems(List<AppInfo> infos){
        appInfos.clear();
        if(infos != null){
            appInfos.addAll(infos);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_app,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo appInfo = appInfos.get(position);
        holder.tvName.setText(processText(appInfo.appName));

        holder.itemView.setTag(appInfo);
        holder.itemView.setOnClickListener(this);

        LocalImageLoader.load(holder.imgIcon,appInfo);

    }

    protected CharSequence processText(String name){
        return name;
    }

    @Override
    public int getItemCount() {
        return appInfos.size();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof AppInfo){

            Intent intent = new Intent(v.getContext(), AppPermissionActivity.class);
            intent.putExtra(AppPermissionActivity.EXTRA_APP, ((AppInfo) v.getTag()));
            v.getContext().startActivity(intent);
            ATracker.send(getAEventId());
        }
    }


    protected String getAEventId(){
        return AEvent.C_APP;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgIcon;
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            imgIcon= (ImageView) itemView.findViewById(R.id.app_icon);
            tvName= (TextView) itemView.findViewById(R.id.app_name);
        }
    }
}
