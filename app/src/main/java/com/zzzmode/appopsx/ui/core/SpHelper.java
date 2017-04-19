package com.zzzmode.appopsx.ui.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by zl on 2017/4/19.
 */

public class SpHelper {

    public static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getThemeMode(Context context){
         if(getSharedPreferences(context).getBoolean("pref_app_daynight_mode", false)){
             return AppCompatDelegate.MODE_NIGHT_YES;
         }
         return AppCompatDelegate.MODE_NIGHT_AUTO;
    }

}
