package com.zzzmode.appopsx.ui.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zl on 2017/5/7.
 */

public class Formatter {

  private static final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
      Locale.getDefault());

  public static String formatDate(long time) {
    return sdfYMD.format(new Date(time));
  }


  public static final String B = "B";
  public static final String KB = "KB";
  public static final String MB = "MB";
  public static final String GB = "GB";
  public static final String TB = "TB";
  public static final String PB = "PB";


  public static String formatFileSize(long number) {
    float result = number;
    String suffix = B;
    if (result > 900) {
      suffix = KB;
      result = result / 1024;
    }
    if (result > 900) {
      suffix = MB;
      result = result / 1024;
    }
    if (result > 900) {
      suffix = GB;
      result = result / 1024;
    }
    if (result > 900) {
      suffix = TB;
      result = result / 1024;
    }
    if (result > 900) {
      suffix = PB;
      result = result / 1024;
    }
    String value;
    if (result < 1) {
      value = String.format("%.2f", result);
    } else if (result < 10) {
      value = String.format("%.2f", result);
    } else if (result < 100) {
      value = String.format("%.2f", result);
    } else {
      value = String.format("%.0f", result);
    }
    return value + suffix;
  }


}
