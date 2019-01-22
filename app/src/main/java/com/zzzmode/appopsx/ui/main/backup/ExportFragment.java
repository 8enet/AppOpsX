package com.zzzmode.appopsx.ui.main.backup;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;
import com.zzzmode.appopsx.ui.model.AppInfo;
import java.util.ArrayList;

/**
 * 导出配置
 * Created by zl on 2017/5/7.
 */

public class ExportFragment extends BaseConfigFragment implements View.OnClickListener {

  private static final String TAG = "ExportFragment";

  private ExportAdapter adapter;

  private ConfigPresenter mPresenter;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_export, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ArrayList<AppInfo> appInfos = getArguments().getParcelableArrayList(BackupActivity.EXTRA_APPS);
    adapter = new ExportAdapter(appInfos);
    recyclerView.setAdapter(adapter);

    view.findViewById(R.id.fab).setOnClickListener(this);

    mPresenter = new ConfigPresenter(getContext().getApplicationContext(), this);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.fab) {
      //导出
      export();
      ATracker.send(AEvent.A_EXPORT);
    }
  }


  private void export() {
    SparseArrayCompat<AppInfo> checkedApps = adapter.getCheckedApps();
    final int size = checkedApps.size();
    AppInfo[] appInfos = new AppInfo[size];
    for (int i = 0; i < size; i++) {
      appInfos[i] = checkedApps.valueAt(i);
    }

    mPresenter.export(appInfos);
  }

}
