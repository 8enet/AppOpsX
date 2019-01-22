package com.zzzmode.appopsx.ui.main;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.core.LocalImageLoader;
import com.zzzmode.appopsx.ui.model.AppInfo;

/**
 * Created by zl on 2017/5/7.
 */

public class AppItemViewHolder extends RecyclerView.ViewHolder {

  private ImageView imgIcon;
  TextView tvName;

  public AppItemViewHolder(View itemView) {
    super(itemView);
    imgIcon = itemView.findViewById(R.id.app_icon);
    tvName = itemView.findViewById(R.id.app_name);
  }


  public void bindData(AppInfo appInfo) {
    tvName.setText(appInfo.appName);
    LocalImageLoader.load(imgIcon, appInfo);
  }
}
