package com.zzzmode.appopsx.ui.main.usagestats;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OtherOp;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2017/8/16.
 */
 class UsageStatsAdapter extends RecyclerView.Adapter<UsageStatsAdapter.ViewHolder> implements
    OnClickListener,OnCheckedChangeListener{

  private static final String TAG = "UsageStatsAdapter";

  private List<Pair<AppInfo, OpEntryInfo>> mDatas=new ArrayList<>();

  void showItems(List<Pair<AppInfo, OpEntryInfo>> value){
    mDatas.clear();
    if(value != null) {
      mDatas.addAll(value);
    }
    notifyDataSetChanged();
  }

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_app_usagestats_item,parent,false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    Pair<AppInfo, OpEntryInfo> pair = mDatas.get(position);

    LocalImageLoader.load(holder.imgIcon, pair.first);

    holder.tvName.setText(pair.first.appName);
    holder.imgPerm.setImageResource(pair.second.icon);

    long time = pair.second.opEntry.getTime();
    if (time > 0) {
      holder.tvLastTime.setText(DateUtils
          .getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
              DateUtils.FORMAT_ABBREV_TIME));
    } else {
      holder.tvLastTime.setText(R.string.never_used);
    }
    if(OtherOp.isSupportCount()){
      holder.tvPermName.setText(holder.tvPermName.getResources().getString(R.string.perms_count,pair.second.opPermsLab,pair.second.opEntry.getAllowedCount()));
    }else {
      holder.tvPermName.setText(pair.second.opPermsLab);
    }
    holder.itemView.setTag(holder);
    holder.itemView.setOnClickListener(this);

    holder.switchCompat.setTag(holder);

    holder.switchCompat.setOnCheckedChangeListener(null);
    holder.switchCompat.setChecked(pair.second.isAllowed());
    holder.switchCompat.setOnCheckedChangeListener(this);

  }

  @Override
  public int getItemCount() {
    return mDatas.size();
  }

  @Override
  public void onClick(View view) {
    Object tag = view.getTag();
    if(tag instanceof ViewHolder){
      ViewHolder holder = ((ViewHolder) tag);
      Pair<AppInfo, OpEntryInfo> pair = mDatas.get(holder.getAdapterPosition());

      Intent intent = new Intent(view.getContext(), AppPermissionActivity.class);
      intent.putExtra(AppPermissionActivity.EXTRA_APP,pair.first);
      view.getContext().startActivity(intent);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    Object tag = compoundButton.getTag();
    if(tag instanceof ViewHolder){
      ViewHolder holder = ((ViewHolder) tag);
      Pair<AppInfo, OpEntryInfo> pair = mDatas.get(holder.getAdapterPosition());

      Helper.setMode(compoundButton.getContext(),pair.first.packageName,pair.second,b)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe();

    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder{

    ImageView imgPerm;
    ImageView imgIcon;
    TextView tvName;
    TextView tvLastTime;
    TextView tvPermName;
    SwitchCompat switchCompat;


    ViewHolder(View itemView) {
      super(itemView);
      imgIcon = itemView.findViewById(R.id.app_icon);
      tvName = itemView.findViewById(R.id.app_name);
      switchCompat = itemView.findViewById(R.id.switch_compat);
      tvLastTime = itemView.findViewById(R.id.perm_last_time);
      tvPermName = itemView.findViewById(R.id.perm_name);
      imgPerm = itemView.findViewById(R.id.img_group);
    }
  }
}
