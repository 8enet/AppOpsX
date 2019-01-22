package com.zzzmode.appopsx.ui.main.group;

import android.content.res.Resources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

/**
 * Created by zl on 2017/7/13.
 */

class ScrollTopHelper extends OnScrollListener {

  private static final String TAG = "ScrollTopHelper";

  private RecyclerView recyclerView;
  private LinearLayoutManager linearLayoutManager;
  private RecyclerViewExpandableItemManager mRVExpandableItemManager;

  private View fab;

  private int offset = 0;
  private int childPos = 0;

  ScrollTopHelper(final RecyclerView recyclerView,
      LinearLayoutManager linearLayoutManager,
      RecyclerViewExpandableItemManager rVExpandableItemManager, final View fab) {
    this.recyclerView = recyclerView;
    this.linearLayoutManager = linearLayoutManager;
    this.mRVExpandableItemManager = rVExpandableItemManager;
    this.fab = fab;
    recyclerView.addOnScrollListener(this);

    fab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (childPos != RecyclerView.NO_POSITION) {

          Resources resources = v.getContext().getResources();
          int pad = (int) (resources.getDisplayMetrics().density * 10);
          int childItemHeight =
              resources.getDimensionPixelSize(android.R.dimen.app_icon_size) + pad * 2;
          int topMargin = (int) (resources.getDisplayMetrics().density * 16);
          int bottomMargin = topMargin;

          v.setEnabled(false);

          recyclerView
              .smoothScrollBy(0, -((childPos + 2) * childItemHeight + topMargin + bottomMargin));
        }

      }
    });

    fab.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        offset = ((View) fab.getParent()).getBottom() - fab.getTop();
        if (offset != 0) {
          ScrollTopHelper.this.fab.animate().translationYBy(offset).start();
          ScrollTopHelper.this.fab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
      }
    });

  }


  private void trackHeader() {

    long firstExpandablePosition = mRVExpandableItemManager
        .getExpandablePosition(linearLayoutManager.findFirstVisibleItemPosition());

    int fChildPos = RecyclerViewExpandableItemManager
        .getPackedPositionChild(firstExpandablePosition);
    int fGroupPos = RecyclerViewExpandableItemManager
        .getPackedPositionGroup(firstExpandablePosition);

    boolean fGroupExpanded = mRVExpandableItemManager.isGroupExpanded(fGroupPos);

    boolean show = false;

    if (fChildPos == RecyclerView.NO_POSITION) {
      //group position
      if (fGroupExpanded) {
        long lastExpandablePosition = mRVExpandableItemManager
            .getExpandablePosition(linearLayoutManager.findLastVisibleItemPosition());

        int lGroupPos = RecyclerViewExpandableItemManager
            .getPackedPositionGroup(lastExpandablePosition);

        show = (lGroupPos == fGroupPos && fGroupPos != 0);

      } else {
        hide();
      }

    } else {
      //child position
      long lastExpandablePosition = mRVExpandableItemManager
          .getExpandablePosition(linearLayoutManager.findLastVisibleItemPosition());
      int lGroupPos = RecyclerViewExpandableItemManager
          .getPackedPositionGroup(lastExpandablePosition);
      show = (lGroupPos == fGroupPos);
    }

    if (show) {
      childPos = fChildPos;
      show();
    } else {
      hide();
    }

  }


  private void hide() {
    childPos = RecyclerView.NO_POSITION;

    if (fab.getAlpha() == 1) {
      fab.animate().translationYBy(offset).alpha(0).start();
    }

  }


  private void show() {

    fab.setEnabled(true);
    if (fab.getAlpha() == 0) {
      fab.animate().translationYBy(-offset).alpha(1).start();
    }

  }


  @Override
  public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    super.onScrollStateChanged(recyclerView, newState);

    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
      trackHeader();
    }
  }


  void release() {
    recyclerView.removeOnScrollListener(this);
  }
}
