package com.zzzmode.appopsx.server;

import android.content.Intent;
import com.zzzmode.appopsx.common.Actions;
import java.util.Map;

/**
 * Created by zl on 2017/10/12.
 */

class LifecycleAgent {

  static volatile Map<String, String> sParams;

  static void onStarted(){
    Intent intent = makeIntent(Actions.ACTION_SERVER_STARTED);

    ApiCompat.sendBroadcast(intent);
  }



  static void onConnected(){
    Intent intent = makeIntent(Actions.ACTION_SERVER_CONNECTED);

    ApiCompat.sendBroadcast(intent);
  }

  static void onDisconnected(){
    Intent intent = makeIntent(Actions.ACTION_SERVER_DISCONNECTED);

    ApiCompat.sendBroadcast(intent);
  }

  static void onStoped(){
    Intent intent = makeIntent(Actions.ACTION_SERVER_STOPED);
    ApiCompat.sendBroadcast(intent);
  }


  private static Intent makeIntent(String action){
    Intent intent = new Intent(action);
    intent.putExtra("token",sParams.get("token"));
    intent.putExtra("type",sParams.get("type"));
    return intent;
  }

}
