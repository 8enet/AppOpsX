package com.zzzmode.appopsx.ui.main.backup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.zzzmode.appopsx.R;

/**
 * 导入配置
 * Created by zl on 2017/5/7.
 */

public class ImportFragment extends BaseConfigFragment {

  private SwipeRefreshLayout swipeRefreshLayout;
  private ConfigPresenter mPresenter;
  private ImportAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_import, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
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
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create();
    alertDialog.show();
  }

}
