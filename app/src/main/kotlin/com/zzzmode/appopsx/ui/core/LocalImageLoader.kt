package com.zzzmode.appopsx.ui.core

import android.app.ActivityManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.v4.util.LruCache
import android.util.Log
import android.widget.ImageView
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.model.AppInfo

/**
 * Created by zl on 2017/4/18.
 */

object LocalImageLoader {

    private lateinit var sLruCache: LruCache<String, Drawable>

    private fun init(context: Context) {

        if (!::sLruCache.isInitialized) {

            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val maxSize = Math.round(am.memoryClass.toFloat() * 1024f * 1024f * 0.3f)


            sLruCache = object : LruCache<String, Drawable>(maxSize) {
                override fun sizeOf(key: String?, drawable: Drawable?): Int {
                    return if (drawable != null) {
                        (drawable as? BitmapDrawable)?.bitmap?.allocationByteCount
                                ?: drawable.intrinsicWidth * drawable.intrinsicHeight * 2
                    } else super.sizeOf(key, drawable)
                }

            }
        }
    }

    fun load(view: ImageView, appInfo: AppInfo?) {

        val drawable = getDrawable(view.context, appInfo)

        if (drawable != null) {
            view.setImageDrawable(drawable)
        } else {
            view.setImageResource(R.mipmap.ic_launcher)
        }
    }


    fun getDrawable(context: Context, appInfo: AppInfo?): Drawable? {
        if(appInfo == null){
            return null
        }
        init(context)
        var drawable: Drawable? = sLruCache.get(appInfo.packageName)

        if (drawable == null && appInfo.applicationInfo != null) {
            drawable = if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
                appInfo.applicationInfo?.loadUnbadgedIcon(context.packageManager)
            } else {
                appInfo.applicationInfo?.loadIcon(context.packageManager)
            }
            val currentUser = Users.instance.currentUser
            if (currentUser != null && currentUser.isManagedProfile) {
                drawable = context.packageManager.getUserBadgedIcon(drawable, currentUser.userHandle)
            }
            sLruCache.put(appInfo.packageName, drawable!!)
        }
        return drawable
    }

    fun initAdd(context: Context, appInfo: AppInfo) {
        init(context)
        if (sLruCache.evictionCount() == 0) {
            sLruCache
                    .put(appInfo.packageName, appInfo.applicationInfo!!.loadIcon(context.packageManager))
        }
    }

    fun clear() {
        sLruCache.evictAll()
    }

}

