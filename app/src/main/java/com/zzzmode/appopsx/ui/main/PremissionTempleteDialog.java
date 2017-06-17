package com.zzzmode.appopsx.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

/**
 * Created by zl on 2017/1/20.
 */

public class PremissionTempleteDialog extends AlertDialog {

  protected PremissionTempleteDialog(@NonNull Context context) {

    super(context);
  }

  protected PremissionTempleteDialog(@NonNull Context context, @StyleRes int themeResId) {
    super(context, themeResId);
  }


  public static PremissionTempleteDialog create(Context context) {
    TypedValue outValue = new TypedValue();
    context.getTheme()
        .resolveAttribute(android.support.v7.appcompat.R.attr.alertDialogTheme, outValue, true);
    return new PremissionTempleteDialog(context, outValue.resourceId);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    RecyclerView recyclerView = new RecyclerView(getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    setContentView(recyclerView);
  }
}
