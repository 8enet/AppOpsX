package com.zzzmode.appopsx;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;

/**
 * Created by zl on 2017/7/10.
 */

public class OpsxConfigProvider extends ContentProvider {

  private static final UriMatcher uriMatcher;
  private static final String AUTHORITY = "com.zzzmode.appopsx.provider.local";

  private static final int TYPE_TOKEN = 1;
  private static final int TYPE_PORT = 2;
  private static final int TYPE_CLASS_PATH = 3;
  private static final int TYPE_SOCKET_PATH = 4;

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(AUTHORITY, "token", TYPE_TOKEN);
    uriMatcher.addURI(AUTHORITY, "adbPort", TYPE_PORT);
    uriMatcher.addURI(AUTHORITY, "classpath", TYPE_CLASS_PATH);
    uriMatcher.addURI(AUTHORITY, "socketPath", TYPE_SOCKET_PATH);
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    checkCalling();

    SConfig.init(getContext(), Process.myUid() / 100000);
    MatrixCursor cursor = null;
    switch (uriMatcher.match(uri)) {
      case TYPE_TOKEN:
        cursor = new MatrixCursor(new String[]{"token"}, 1);
        cursor.addRow(new String[]{SConfig.getLocalToken()});
        break;
      case TYPE_PORT:
        cursor = new MatrixCursor(new String[]{"port"}, 1);
        cursor.addRow(new String[]{String.valueOf(SConfig.getPort())});
        break;
      case TYPE_CLASS_PATH:
        cursor = new MatrixCursor(new String[]{"classpath"}, 1);
        cursor.addRow(new String[]{SConfig.getClassPath()});
        break;
      case TYPE_SOCKET_PATH:
        cursor = new MatrixCursor(new String[]{"socketPath"},1 );
        cursor.addRow(new String[]{SConfig.SOCKET_PATH});
        break;
    }
    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }


  private void checkCalling() {
    int callingUid = Binder.getCallingUid();
    boolean allow = callingUid == Process.myUid() || callingUid == 0 || callingUid == 1000
        || callingUid == 2000;
    if (!allow) {
      throw new SecurityException("Illegal uid " + callingUid);
    }
  }
}
