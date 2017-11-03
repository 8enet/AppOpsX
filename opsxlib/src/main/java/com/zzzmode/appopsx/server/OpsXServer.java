package com.zzzmode.appopsx.server;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.OpsDataTransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by zl on 2016/11/6.
 */

class OpsXServer {

  private boolean running = true;
  private IServer server;
  private OpsDataTransfer opsDataTransfer;
  private OpsDataTransfer.OnRecvCallback callback;

  private String token;
  boolean allowBackgroundRun = false;

  OpsXServer(String name, String token, OpsDataTransfer.OnRecvCallback callback)
      throws IOException {

    int port = -1;
    try {
      port = Integer.parseInt(name);
    } catch (Exception e) {
      //ignore
    }

    if(port != -1){
      server = new NetSocketServerImpl(port);
    }else {
      server = new LocalServerImpl(name);
    }

    this.callback = callback;
    this.token = token;
  }

  void run() throws Exception {
    while (running) {

      try {

        server.accept(); //only one connect

        opsDataTransfer = new OpsDataTransfer(server.getOutputStream(), server.getInputStream(),
            callback);
        opsDataTransfer.shakehands(token, true);

        LifecycleAgent.onConnected();

        opsDataTransfer.handleRecv();
      } catch (IOException e) {
        FLog.log(e);
        FLog.log("------- allowBackgroundRun: " + allowBackgroundRun);

        LifecycleAgent.onDisconnected();

        if (!allowBackgroundRun) {
          running = false;
          throw e;
        }
      } catch (RuntimeException e) {
        FLog.log(e);

        LifecycleAgent.onDisconnected();

        allowBackgroundRun = false;
        running = false;
        throw e;
      }
    }
  }

  public void sendResult(String str) throws IOException {
    if (running && opsDataTransfer != null) {
      opsDataTransfer.sendMsg(str.getBytes());
    }
  }

  public void sendResult(byte[] bytes) throws IOException {
    if (running && opsDataTransfer != null) {
      opsDataTransfer.sendMsg(bytes);
    }
  }

  public void setStop() {
    running = false;
    if (opsDataTransfer != null) {
      opsDataTransfer.stop();
    }
    try {
      if (server != null) {
        server.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private interface IServer {

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    void accept() throws IOException;

    void close() throws IOException;
  }

  private static class LocalServerImpl implements IServer {

    private LocalServerSocket serverSocket;
    private LocalSocket socket;

    public LocalServerImpl(String name) throws IOException {
      this.serverSocket = new LocalServerSocket(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return socket.getOutputStream();
    }

    @Override
    public void accept() throws IOException {
      socket = serverSocket.accept();
    }

    @Override
    public void close() throws IOException {
      socket.close();
      serverSocket.close();
    }
  }

  private static class NetSocketServerImpl implements IServer {

    private ServerSocket serverSocket;
    private Socket socket;

    public NetSocketServerImpl(int port) throws IOException {
      this.serverSocket = new ServerSocket(port);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return socket.getOutputStream();
    }

    @Override
    public void accept() throws IOException {
      socket = serverSocket.accept();
    }

    @Override
    public void close() throws IOException {
      socket.close();
      serverSocket.close();
    }
  }

}
