package com.zzzmode.appopsx.ui.main.backup;

import android.content.Context;
import android.os.SystemClock;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by zl on 2017/5/7.
 */

class BFileUtils {

  private static final String DIR_NAME = "backup";
  private static final String SUFFIX = ".bak";

  static File getBackupDir(Context context) {
    File externalFilesDir = context.getExternalFilesDir(DIR_NAME);
    if (externalFilesDir != null) {
      if (externalFilesDir.exists()) {
        return externalFilesDir;
      } else {
        boolean mkdirs = externalFilesDir.mkdirs();
        if (mkdirs) {
          return externalFilesDir;
        }
      }
    }
    return context.getDir(DIR_NAME, Context.MODE_PRIVATE);
  }

  private static File generateDefaultFile(Context context) {
    File file = new File(getBackupDir(context),
        System.currentTimeMillis() + "_" + new Random().nextInt(1000) + SUFFIX);
    try {
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }

  static List<File> getBackFiles(Context context) {
    List<File> files = new ArrayList<>();

    FilenameFilter filenameFilter = new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name != null && name.endsWith(SUFFIX);
      }
    };

    File[] dirs = {context.getExternalFilesDir(DIR_NAME),
        context.getDir(DIR_NAME, Context.MODE_PRIVATE)};

    for (File dir : dirs) {
      File[] filesTmp = dirFinder(dir, filenameFilter);
      if (filesTmp != null) {
        files.addAll(Arrays.asList(filesTmp));
      }
    }
    return files;
  }

  private static File[] dirFinder(File dir, FilenameFilter filenameFilter) {
    if (dir != null && dir.exists()) {
      return dir.listFiles(filenameFilter);
    }
    return null;
  }

  static File saveBackup(Context context, String config) throws IOException {
    File file = generateDefaultFile(context);
    FileOutputStream fos = null;
    int len = 0;
    try {
      fos = new FileOutputStream(file);
      byte[] bytes = config.getBytes();
      len = bytes.length;
      fos.write(bytes);
      fos.flush();
      fos.getFD().sync();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      SystemClock.sleep(100);
    }
    long length = file.length();
    if (length == len) {
      return file;
    } else {
      throw new IOException(
          "file:" + file + ",data.len:" + len + ",file.len:" + length + ",write error!");
    }
  }

  static String read2String(File file) {
    FileInputStream fis = null;
    ByteArrayOutputStream baos = null;
    try {
      fis = new FileInputStream(file);
      baos = new ByteArrayOutputStream(4096);
      int len = -1;
      byte[] buff = new byte[4096];
      while ((len = fis.read(buff)) != -1) {
        baos.write(buff, 0, len);
      }
      return baos.toString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  static boolean deleteBackFile(String path) {
    return new File(path).delete();
  }

}
