package com.zzzmode.appopsx.ui.main.backup;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.main.AppItemViewHolder;
import com.zzzmode.appopsx.ui.model.AppInfo;
import java.util.List;

/**
 * 导出
 * Created by zl on 2017/5/7.
 */

class ExportAdapter extends RecyclerView.Adapter<ExportAdapter.ExportViewHolder> implements
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private List<AppInfo> appInfos;

  private SparseArrayCompat<AppInfo> mCheckedApps = new SparseArrayCompat<>();

  ExportAdapter(List<AppInfo> appInfos) {
    this.appInfos = appInfos;
    if (appInfos != null) {
      int i = 0;
      for (AppInfo appInfo : appInfos) {
        mCheckedApps.append(i, appInfo);
        i++;
      }
    }
  }

  SparseArrayCompat<AppInfo> getCheckedApps() {
    return mCheckedApps;
  }

  @Override
  @NonNull
  public ExportAdapter.ExportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ExportAdapter.ExportViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_export_app, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ExportAdapter.ExportViewHolder holder, int position) {
    holder.bindData(appInfos.get(position));
    holder.itemView.setOnClickListener(this);
    holder.itemView.setTag(holder);

    holder.checkBox.setTag(holder);
    holder.checkBox.setOnCheckedChangeListener(null);
    holder.checkBox.setChecked(isChecked(position));
    holder.checkBox.setOnCheckedChangeListener(this);
  }

  @Override
  public int getItemCount() {
    return appInfos.size();
  }

  @Override
  public void onClick(View v) {
    Object tag = v.getTag();
    if (tag instanceof ExportViewHolder) {
      ExportViewHolder holder = (ExportViewHolder) tag;
      handleCheck(holder, true);
    }
  }

  private void handleCheck(ExportViewHolder holder, boolean change) {
    int pos = holder.getAdapterPosition();

    if (holder.checkBox.isChecked()) {
      mCheckedApps.delete(pos);
    } else {
      mCheckedApps.put(pos, appInfos.get(pos));
    }
    if (change) {
      holder.checkBox.setOnCheckedChangeListener(null);
      holder.checkBox.setChecked(isChecked(pos));
      holder.checkBox.setOnCheckedChangeListener(this);
    }
  }

  private boolean isChecked(int pos) {
    return !(mCheckedApps.indexOfKey(pos) < 0);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Object tag = buttonView.getTag();
    if (tag instanceof ExportViewHolder) {
      ExportViewHolder holder = (ExportViewHolder) tag;
      handleCheck(holder, false);
    }
  }


  static class ExportViewHolder extends AppItemViewHolder {

    CheckBox checkBox;

    ExportViewHolder(View itemView) {
      super(itemView);
      checkBox = itemView.findViewById(R.id.checkbox);
    }
  }
}
