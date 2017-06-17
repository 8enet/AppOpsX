package com.zzzmode.appopsx.common;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zl on 2017/2/26.
 */
public class FLog {

  public static boolean writeLog = false;
  private static FileOutputStream fos;
  private static AtomicInteger sBufferSize = new AtomicInteger();
  private static AtomicInteger sErrorCount = new AtomicInteger();

  private static void openFile() {
    try {
      if (writeLog && fos == null && sErrorCount.get() < 5) {
        fos = new FileOutputStream("/data/local/tmp/opsx.txt");
        fos.write("\n\n\n--------------------".getBytes());
        fos.write(new Date().toString().getBytes());
        fos.write("\n\n".getBytes());
      }
    } catch (Exception e) {
      e.printStackTrace();
      sErrorCount.incrementAndGet();
      fos = null;
    }
  }

  public static void log(String log) {
    if (writeLog) {
      System.out.println(log);
    } else {
      Log.e("appopsx", "Flog --> " + log);
    }

    try {
      if (writeLog) {
        openFile();
        if (fos != null) {
          fos.write(log.getBytes());
          fos.write("\n".getBytes());

          if (sBufferSize.incrementAndGet() > 10) {
            fos.getFD().sync();
            fos.flush();
            sBufferSize.set(0);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void log(Throwable e) {
    log(Log.getStackTraceString(e));
  }

  public static void close() {
    try {
      if (writeLog && fos != null) {
        fos.getFD().sync();
        fos.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

