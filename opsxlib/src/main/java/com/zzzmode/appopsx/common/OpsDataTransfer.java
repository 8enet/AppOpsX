package com.zzzmode.appopsx.common;

import android.text.TextUtils;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zl on 2016/11/5.
 */

public class OpsDataTransfer {

  // length+data

  public static final String PROTOCOL_VERSION = "1.2.4";

  private DataOutputStream outputStream;
  private DataInputStream inputStream;
  private OnRecvCallback callback;

  private boolean running = true;
  private boolean async = true;

  public OpsDataTransfer(OutputStream outputStream, InputStream inputStream,
      OnRecvCallback callback) {
    this.outputStream = new DataOutputStream(outputStream);
    this.inputStream = new DataInputStream(inputStream);
    this.callback = callback;
  }

  public OpsDataTransfer(OutputStream outputStream, InputStream inputStream) {
    this(outputStream, inputStream, true);
  }

  public OpsDataTransfer(OutputStream outputStream, InputStream inputStream, boolean async) {
    this(outputStream, inputStream, null);
    this.async = async;
  }

  public void setCallback(OnRecvCallback callback) {
    this.callback = callback;
  }

  public void sendMsg(String text) throws IOException {
    if (text != null) {
      sendMsg(text.getBytes());
    }
  }

  public void sendMsg(byte[] msg) throws IOException {
    if (msg != null) {
      outputStream.writeInt(msg.length);
      outputStream.write(msg);
      outputStream.flush();
    }
  }

  private byte[] readMsg() throws IOException {
    int len = inputStream.readInt();

    byte[] bytes = new byte[len];
    inputStream.readFully(bytes, 0, len);
    return bytes;
  }

  public synchronized byte[] sendMsgAndRecv(byte[] msg) throws IOException {
    if (msg != null) {
      sendMsg(msg);
      return readMsg();
    }
    return null;
  }

  public interface OnRecvCallback {

    void onMessage(byte[] bytes);
  }



  public void shakehands(String token, boolean isServer) throws IOException {
    if (token == null) {
      return;
    }
    if (isServer) {
      FLog.log("shakehands --> start: token " + token + "  " + isServer + "  server protocol :"
          + PROTOCOL_VERSION);
    } else {
      Log.e("test", "shakehands --> start: token " + token + "  " + isServer + "  client protocol:"
          + PROTOCOL_VERSION);
    }
    if (isServer) {
      String auth = new String(readMsg());

      FLog.log("recv auth " + auth);

      String[] split = auth.split(",");

      String ver = split[0];
      String recvToken = split[1];
      if (!TextUtils.equals(ver, PROTOCOL_VERSION)) {
        throw new RuntimeException(
            "client protocol version:" + ver + "  ,server protocol version:" + PROTOCOL_VERSION);
      }
      if (TextUtils.equals(token, recvToken)) {
        //auth success,pass
        FLog.log("shakehands --> hands success ");
      } else {
        FLog.log("shakehands --> unknow token ");
        throw new RuntimeException("Unauthorized client, token:" + token);
      }
    } else {
      //client
      sendMsg(PROTOCOL_VERSION + "," + token);
    }
  }


  public void handleRecv() throws IOException {
    if (!async) {
      return;
    }
    while (running) {
//                int len = inputStream.readInt();
//                byte[] bytes = new byte[len];
//                if(inputStream.read(bytes, 0, len) == len){
//                    onRecvMsg(bytes);
//                }
      onRecvMsg(readMsg());
    }

  }

  private void onRecvMsg(byte[] bytes) {
    if (callback != null) {
      callback.onMessage(bytes);
    }
  }

  public void stop() {
    running = false;

    try {
      if (outputStream != null) {
        outputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
