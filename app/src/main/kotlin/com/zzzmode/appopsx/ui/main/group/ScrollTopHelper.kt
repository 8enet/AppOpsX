package com.zzzmode.appopsx.ui.main.group

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager

/**
 * Created by zl on 2017/7/13.
 */

internal class ScrollTopHelper(private val recyclerView: RecyclerView,
                               private val linearLayoutManager: LinearLayoutManager,
                               private val mRVExpandableItemManager: RecyclerViewExpandableItemManager, private val fab: View) : OnScrollListener() {

    private var offset = 0
    private var childPos = 0

    init {
        recyclerView.addOnScrollListener(this)

        fab.setOnClickListener { v ->
            if (childPos != RecyclerView.NO_POSITION) {

                val resources = v.context.resources
                val pad = (resources.displayMetrics.density * 10).toInt()
                val childItemHeight = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) + pad * 2
                val topMargin = (resources.displayMetrics.density * 16).toInt()

                v.isEnabled = false

                recyclerView
                        .smoothScrollBy(0, -((childPos + 2) * childItemHeight + topMargin + topMargin))
            }
        }

        fab.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                offset = (fab.parent as View).bottom - fab.top
                if (offset != 0) {
                    this@ScrollTopHelper.fab.animate().translationYBy(offset.toFloat()).start()
                    this@ScrollTopHelper.fab.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

    }


    private fun trackHeader() {

        val firstExpandablePosition = mRVExpandableItemManager
                .getExpandablePosition(linearLayoutManager.findFirstVisibleItemPosition())

        val fChildPos = RecyclerViewExpandableItemManager
                .getPackedPositionChild(firstExpandablePosition)
        val fGroupPos = RecyclerViewExpandableItemManager
                .getPackedPositionGroup(firstExpandablePosition)

        val fGroupExpanded = mRVExpandableItemManager.isGroupExpanded(fGroupPos)

        var show = false

        if (fChildPos == RecyclerView.NO_POSITION) {
            //group position
            if (fGroupExpanded) {
                val lastExpandablePosition = mRVExpandableItemManager
                        .getExpandablePosition(linearLayoutManager.findLastVisibleItemPosition())

                val lGroupPos = RecyclerViewExpandableItemManager
                        .getPackedPositionGroup(lastExpandablePosition)

                show = lGroupPos == fGroupPos && fGroupPos != 0

            } else {
                hide()
            }

        } else {
            //child position
            val lastExpandablePosition = mRVExpandableItemManager
                    .getExpandablePosition(linearLayoutManager.findLastVisibleItemPosition())
            val lGroupPos = RecyclerViewExpandableItemManager
                    .getPackedPositionGroup(lastExpandablePosition)
            show = lGroupPos == fGroupPos
        }

        if (show) {
            childPos = fChildPos
            show()
        } else {
            hide()
        }

    }


    private fun hide() {
        childPos = RecyclerView.NO_POSITION

        if (fab.alpha == 1f) {
            fab.animate().translationYBy(offset.toFloat()).alpha(0f).start()
        }

    }


    private fun show() {

        fab.isEnabled = true
        if (fab.alpha == 0f) {
            fab.animate().translationYBy((-offset).toFloat()).alpha(1f).start()
        }

    }


    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            trackHeader()
        }
    }


    fun release() {
        recyclerView.removeOnScrollListener(this)
    }

    companion object {

        private const val TAG = "ScrollTopHelper"
    }
}
