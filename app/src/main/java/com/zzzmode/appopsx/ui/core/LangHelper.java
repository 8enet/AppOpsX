package com.zzzmode.appopsx.ui.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.zzzmode.appopsx.R;

import java.util.Locale;

/**
 * Created by zl on 2017/6/16.
 */

public class LangHelper {

  private static final String TAG = "LangHelper";

  public static void updateLanguage(Context context) {

    Resources resources = context.getResources();
    Configuration config = resources.getConfiguration();
    config.setLocale(getLocaleByLanguage(context));
    DisplayMetrics dm = resources.getDisplayMetrics();
    resources.updateConfiguration(config, dm);
  }


  public static int getLocalIndex(Context context) {
    int defSelected = 0;
    String defKey = SpHelper.getSharedPreferences(context).getString("pref_app_language", null);
    String[] langKeys = context.getResources().getStringArray(R.array.languages_key);
    if (defKey != null) {
      for (int i = 0; i < langKeys.length; i++) {
        if (TextUtils.equals(defKey, langKeys[i])) {
          defSelected = i;
          break;
        }
      }
    }
    return defSelected;
  }

  public static Locale getLocaleByLanguage(Context context) {
    String language = SpHelper.getSharedPreferences(context).getString("pref_app_language", "");
    switch (language) {
      case "zh-cn":
        return Locale.SIMPLIFIED_CHINESE;
      case "zh-tw":
        return Locale.TRADITIONAL_CHINESE;
      case "en":
        return Locale.ENGLISH;
      default:
        return Locale.getDefault();
    }
  }


  public static Context attachBaseContext(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return updateResources(context);
    } else {
      return context;
    }
  }


  @TargetApi(Build.VERSION_CODES.N)
  private static Context updateResources(Context context) {
    Resources resources = context.getResources();
    Locale locale = getLocaleByLanguage(context);

    Configuration configuration = resources.getConfiguration();
    configuration.setLocale(locale);
    configuration.setLocales(new LocaleList(locale));
    return context.createConfigurationContext(configuration);
  }

}
