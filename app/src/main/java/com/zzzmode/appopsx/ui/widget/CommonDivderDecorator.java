package com.zzzmode.appopsx.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.zzzmode.appopsx.R;

/**
 * Created by zl on 2017/4/18.
 */

public class CommonDivderDecorator extends DividerItemDecoration {

  private static Drawable sDefDrawable;

  public CommonDivderDecorator(Context context, int orientation) {
    super(context, orientation);
  }

  public CommonDivderDecorator(Context context) {
    super(context, VERTICAL);

    Drawable divider = ContextCompat.getDrawable(context, R.drawable.list_divider_h);
    setDrawable(divider);
  }
}
