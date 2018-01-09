package com.zzzmode.appopsx.ui.core;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.model.AppInfo;

/**
 * Created by zl on 2017/4/18.
 */

public class LocalImageLoader {

  private static LruCache<String, Drawable> sLruCache = null;

  private static void init(Context context) {
    if (sLruCache == null) {

      ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      int maxSize = Math.round(am.getMemoryClass() * 1024 * 1024 * 0.3f);

      sLruCache = new LruCache<String, Drawable>(maxSize) {
        @Override
        protected int sizeOf(String key, Drawable drawable) {
          if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
              return ((BitmapDrawable) drawable).getBitmap().getAllocationByteCount();
            } else {
              return drawable.getIntrinsicWidth() * drawable.getIntrinsicHeight() * 2;
            }
          }
          return super.sizeOf(key, drawable);
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Drawable oldValue,
            Drawable newValue) {
          super.entryRemoved(evicted, key, oldValue, newValue);

        }
      };
    }
  }

  public static void load(ImageView view, AppInfo appInfo) {

    Drawable drawable = getDrawable(view.getContext(), appInfo);

    if (drawable != null) {
      view.setImageDrawable(drawable);
    } else {
      view.setImageResource(R.mipmap.ic_launcher);
    }
  }


  public static Drawable getDrawable(Context context, AppInfo appInfo) {
    init(context);
    Drawable drawable = sLruCache.get(appInfo.packageName);

    if (drawable == null && appInfo.applicationInfo != null) {
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
        drawable = appInfo.applicationInfo.loadUnbadgedIcon(context.getPackageManager());
      }else {
        drawable = appInfo.applicationInfo.loadIcon(context.getPackageManager());
      }
      UserInfo currentUser = Users.getInstance().getCurrentUser();
      if(currentUser != null && currentUser.isManagedProfile()){
        drawable = context.getPackageManager().getUserBadgedIcon(drawable,currentUser.getUserHandle());
      }
      sLruCache.put(appInfo.packageName, drawable);
    }
    return drawable;
  }

  public static void initAdd(Context context, AppInfo appInfo) {
    init(context);
    if (sLruCache.evictionCount() == 0) {
      sLruCache
          .put(appInfo.packageName, appInfo.applicationInfo.loadIcon(context.getPackageManager()));
    }
  }

  public static void clear(){
    sLruCache.evictAll();
  }

}
