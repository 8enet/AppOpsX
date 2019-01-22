package com.zzzmode.appopsx.ui.widget;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.zzzmode.appopsx.R;

public class ExpandableItemIndicator extends FrameLayout {

  static abstract class Impl {

    public abstract void onInit(Context context, AttributeSet attrs, int defStyleAttr,
        ExpandableItemIndicator thiz);

    public abstract void setExpandedState(boolean isExpanded, boolean animate);
  }

  private Impl mImpl;

  public ExpandableItemIndicator(Context context) {
    super(context);
    onInit(context, null, 0);
  }

  public ExpandableItemIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);
    onInit(context, attrs, 0);
  }

  public ExpandableItemIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    onInit(context, attrs, defStyleAttr);
  }

  protected boolean shouldUseAnimatedIndicator(Context context, AttributeSet attrs,
      int defStyleAttr) {
    // NOTE: AnimatedVectorDrawableCompat works on API level 11+,
    // but I prefer to use it on API level 16+ only due to performance reason of
    // both hardware and Android platform.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  protected void onInit(Context context, AttributeSet attrs, int defStyleAttr) {
    mImpl = new ExpandableItemIndicatorImplAnim();
    mImpl.onInit(context, attrs, defStyleAttr, this);
  }

  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    super.dispatchFreezeSelfOnly(container);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    super.dispatchThawSelfOnly(container);
  }

  public void setExpandedState(boolean isExpanded, boolean animate) {
    mImpl.setExpandedState(isExpanded, animate);
  }


  // NOTE: AnimatedVectorDrawableCompat works on API level 11+
  static class ExpandableItemIndicatorImplAnim extends ExpandableItemIndicator.Impl {

    private AppCompatImageView mImageView;

    @Override
    public void onInit(Context context, AttributeSet attrs, int defStyleAttr,
        ExpandableItemIndicator thiz) {
      View v = LayoutInflater.from(context)
          .inflate(R.layout.widget_expandable_item_indicator, thiz, true);
      mImageView = (AppCompatImageView) v.findViewById(R.id.image_view);
    }

    @Override
    public void setExpandedState(boolean isExpanded, boolean animate) {
      if (animate) {
        @DrawableRes int resId = isExpanded ? R.drawable.ic_expand_more_to_expand_less
            : R.drawable.ic_expand_less_to_expand_more;
        mImageView.setImageResource(resId);
        ((Animatable) mImageView.getDrawable()).start();
      } else {
        @DrawableRes int resId = isExpanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more;
        mImageView.setImageResource(resId);
      }
    }
  }
}