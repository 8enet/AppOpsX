package com.zzzmode.appopsx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zzzmode.appopsx.common.Actions;

/**
 * Created by zl on 2017/10/12.
 */

public class ServerStatusChangeReceiver extends BroadcastReceiver {

  private static final String TAG = "ServerStatusChangeRecei";

  @Override
  public void onReceive(Context context, Intent intent) {

    String token = intent.getStringExtra("token");

    if(SConfig.getLocalToken().equals(token)) {
      String action = intent.getAction();
      Log.e(TAG, "onReceive --> " + action + "   " + token + "  " + intent
          .getStringExtra("type"));

      if (Actions.ACTION_SERVER_STARTED.equals(action)) {

      } else if (Actions.ACTION_SERVER_CONNECTED.equals(action)) {

      } else if (Actions.ACTION_SERVER_DISCONNECTED.equals(action)) {

      } else if (Actions.ACTION_SERVER_STOPED.equals(action)) {

      }
    }
  }
}
