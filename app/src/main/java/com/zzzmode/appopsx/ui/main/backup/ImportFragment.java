package com.zzzmode.appopsx.ui.main.backup;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.analytics.AEvent;
import com.zzzmode.appopsx.ui.analytics.ATracker;

/**
 * 导入配置
 * Created by zl on 2017/5/7.
 */

public class ImportFragment extends BaseConfigFragment {

  private SwipeRefreshLayout swipeRefreshLayout;
  private ConfigPresenter mPresenter;
  ImportAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_import, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefreshlayout);
    swipeRefreshLayout.setRefreshing(false);
    swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        reload();

      }
    });

    adapter = new ImportAdapter();
    recyclerView.setAdapter(adapter);

    adapter.setItemClickListener(new ImportAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(RestoreModel model) {
        showRestore(model);
      }
    });

    mPresenter = new ConfigPresenter(getContext().getApplicationContext(), this);
    reload();
  }


  private void reload() {
    adapter.showData(mPresenter.getRestoreFiles());
    swipeRefreshLayout.setRefreshing(false);
  }

  private void showRestore(final RestoreModel model) {
    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
        .setTitle(R.string.perm_restore)
        .setMessage(getString(R.string.dlg_restore_msg, model.path))
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mPresenter.restoreOps(model);

            ATracker.send(AEvent.A_RESTORE_CONFIRM);
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create();
    alertDialog.show();
  }

}
