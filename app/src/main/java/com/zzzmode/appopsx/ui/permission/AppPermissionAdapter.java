package com.zzzmode.appopsx.ui.permission;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2016/11/18.
 */

public class AppPermissionAdapter extends RecyclerView.Adapter<AppPermissionAdapter.ViewHolder> {

    private List<OpEntryInfo> datas=new ArrayList<>();

    public void setDatas(List<OpEntryInfo> datas) {
        this.datas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OpEntryInfo opEntryInfo = datas.get(position);
        if(opEntryInfo != null){
            holder.textView.setText(opEntryInfo.opName);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView.findViewById(R.id.text);
        }
    }
}
