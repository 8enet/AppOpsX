package com.zzzmode.appopsx.ui.widget

import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.zzzmode.appopsx.R

class ExpandableItemIndicator : FrameLayout {

    private  val mImpl: Impl by lazy {
        ExpandableItemIndicatorImplAnim()
    }

    internal abstract class Impl {

        abstract fun onInit(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                            thiz: ExpandableItemIndicator)

        abstract fun setExpandedState(isExpanded: Boolean, animate: Boolean)
    }

    constructor(context: Context) : super(context) {
        onInit(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        onInit(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onInit(context, attrs, defStyleAttr)
    }

    protected fun onInit(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        mImpl.onInit(context, attrs, defStyleAttr, this)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        super.dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        super.dispatchThawSelfOnly(container)
    }

    fun setExpandedState(isExpanded: Boolean, animate: Boolean) {
        mImpl.setExpandedState(isExpanded, animate)
    }


    // NOTE: AnimatedVectorDrawableCompat works on API level 11+
    internal class ExpandableItemIndicatorImplAnim : ExpandableItemIndicator.Impl() {

        private lateinit var mImageView: AppCompatImageView

        override fun onInit(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                            thiz: ExpandableItemIndicator) {
            val v = LayoutInflater.from(context)
                    .inflate(R.layout.widget_expandable_item_indicator, thiz, true)
            mImageView = v.findViewById<View>(R.id.image_view) as AppCompatImageView
        }

        override fun setExpandedState(isExpanded: Boolean, animate: Boolean) {
            if (animate) {
                @DrawableRes val resId = if (isExpanded)
                    R.drawable.ic_expand_more_to_expand_less
                else
                    R.drawable.ic_expand_less_to_expand_more
                mImageView.setImageResource(resId)
                (mImageView.drawable as Animatable).start()
            } else {
                @DrawableRes val resId = if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                mImageView.setImageResource(resId)
            }
        }
    }
}