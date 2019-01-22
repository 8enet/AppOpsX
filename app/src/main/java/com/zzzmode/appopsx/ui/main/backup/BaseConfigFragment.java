package com.zzzmode.appopsx.ui.main.backup;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.widget.CommonDivderDecorator;

/**
 * Created by zl on 2017/5/7.
 */

public class BaseConfigFragment extends Fragment implements IConfigView {

  RecyclerView recyclerView;

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    if (recyclerView != null) {
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      recyclerView.addItemDecoration(new CommonDivderDecorator(getContext()));
      recyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());
    }
  }


  private ProgressDialog progressDialog;

  @Override
  public void showProgress(boolean show, int max) {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
      progressDialog = null;
    }
    if (show) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setTitle(R.string.dlg_title);
      progressDialog.setCanceledOnTouchOutside(false);
      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressDialog.setMax(max);
      progressDialog.show();
    }
  }

  @Override
  public void setProgress(int progress) {
    if (progressDialog != null) {
      progressDialog.setProgress(progress);
    }
  }
}
