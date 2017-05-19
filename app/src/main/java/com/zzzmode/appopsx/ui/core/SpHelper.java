package com.zzzmode.appopsx.ui.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.SparseArray;

import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

/**
 * Created by zl on 2017/4/19.
 */

public class SpHelper {


    public static final String KEY_AUTO_IGNORE_TEMPLATE_PERMS="auto_perm_template";


    public static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getThemeMode(Context context){
         if(getSharedPreferences(context).getBoolean("pref_app_daynight_mode", false)){
             return AppCompatDelegate.MODE_NIGHT_YES;
         }
         return AppCompatDelegate.MODE_NIGHT_NO;
    }

    public static String getPermTemplate(Context context) {
        return getSharedPreferences(context).getString(KEY_AUTO_IGNORE_TEMPLATE_PERMS, context.getString(R.string.default_ignored));
    }

    public static void savePermTemplate(Context context,String tmps){
        getSharedPreferences(context).edit().putString(KEY_AUTO_IGNORE_TEMPLATE_PERMS,tmps).apply();
    }
}
