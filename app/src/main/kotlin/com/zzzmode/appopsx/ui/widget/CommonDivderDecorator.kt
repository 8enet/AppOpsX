package com.zzzmode.appopsx.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import com.zzzmode.appopsx.R

/**
 * Created by zl on 2017/4/18.
 */

class CommonDivderDecorator : DividerItemDecoration {

    constructor(context: Context, orientation: Int) : super(context, orientation) {}

    constructor(context: Context) : super(context, DividerItemDecoration.VERTICAL) {

        ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let {
            setDrawable(it)
        }
    }

}
