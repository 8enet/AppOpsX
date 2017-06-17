package com.zzzmode.appopsx.server;

import android.app.AppOpsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.SparseArray;

import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.PackageOps;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zl on 2017/3/1.
 */

class PersistenceConfig {

  private static final String TABLE_NAME = "tb_block_ops";

  private SQLiteDatabase database;
  private SQLiteStatement sqLiteStatement;


  PersistenceConfig() {
    database = SQLiteDatabase.openOrCreateDatabase(new File("/data/local/config/appopsx.db"), null);
    String sql =
        "create table if not exists " + TABLE_NAME + " ( uid integer primary key ,block_ops text);";
    database.execSQL(sql);

    sqLiteStatement = database
        .compileStatement("update  OR REPLACE TABLE_NAME set block_ops =? where uid =?");
    load();


  }


  void close() {
    if (database != null) {
      database.close();
    }
  }

  private SparseArray<Set<Integer>> mBlockOps = new SparseArray<>();


  private void load() {
    Cursor cursor = null;
    try {
      cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
      while (cursor.moveToNext()) {
        int uid = cursor.getInt(0);
        String ops = cursor.getString(1);
//                mBlockCfg.put(uid,ops);

        if (!TextUtils.isEmpty(ops)) {
          String[] split = ops.split(",");
          Set<Integer> integers = mBlockOps.get(uid);

          if (integers == null) {
            integers = new HashSet<>();
            mBlockOps.put(uid, integers);
          } else {
            integers.clear();
          }
          for (String s : split) {
            try {
              if (!TextUtils.isEmpty(s)) {
                integers.add(Integer.parseInt(s));
              }
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }
          }
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  public boolean isBlocked(int uid, int opCode) {
    Set<Integer> ops = mBlockOps.get(uid);
    return ops != null && ops.contains(opCode);
  }


  public void setPerm(int uid, int op, boolean block) {
    Set<Integer> integers = mBlockOps.get(uid);
    if (integers == null) {
      integers = new HashSet<>();
      mBlockOps.put(uid, integers);
    }

    boolean needRef = false;

    if (block) {
      integers.add(op);
    } else {
      integers.remove(op);
    }

    StringBuilder sb = new StringBuilder();
    for (Integer integer : integers) {
      sb.append(integer).append(',');
    }

    save(uid, sb.toString());
  }


  void sync(PackageOps ops) {
    List<OpEntry> entryList = ops.getOps();
    if (entryList != null) {
      StringBuilder sb = new StringBuilder();
      int uid = ops.getUid();
      boolean needRef = false;

      Set<Integer> integers = mBlockOps.get(uid);
      if (integers == null) {
        integers = new HashSet<>();
        mBlockOps.put(uid, integers);
      }

      for (OpEntry opEntry : entryList) {
        if (opEntry.getMode() == AppOpsManager.MODE_IGNORED) {
          int op = opEntry.getOp();
          if (integers.add(op)) {
            if (!needRef) {
              needRef = true;
            }
          }
          sb.append(opEntry.getOp()).append(',');
        }
      }
      if (needRef) {
        save(uid, sb.toString());
      }
    }
  }


  private void save(int uid, String ops) {
    try {
      System.out.println("uid  -->  " + ops);
      sqLiteStatement.clearBindings();
      sqLiteStatement.bindString(0, ops);
      sqLiteStatement.bindLong(1, uid);
      sqLiteStatement.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
