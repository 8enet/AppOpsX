package com.zzzmode.appopsx;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbStream;
import com.zzzmode.adblib.AdbConnector;
import com.zzzmode.adblib.LineReader;
import com.zzzmode.android.opsxpro.BuildConfig;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.ParcelableUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zl on 2016/11/13.
 */

class LocalServerManager {

  private static final String TAG = "LocalServerManager";

  private static LocalServerManager sLocalServerManager;

  private OpsxManager.Config mConfig;

  static LocalServerManager getInstance(OpsxManager.Config config) {
    if (sLocalServerManager == null) {
      synchronized (LocalServerManager.class) {
        if (sLocalServerManager == null) {
          sLocalServerManager = new LocalServerManager(config);
        }
      }
    }
    return sLocalServerManager;
  }


  private SyncClient mClientThread = null;

  private LocalServerManager(OpsxManager.Config config) {
    mConfig = config;
  }

  void updateConfig(OpsxManager.Config config) {
    if (config != null) {
      mConfig = config;
    }
  }

  OpsxManager.Config getConfig() {
    return mConfig;
  }

  void start() throws Exception {
    if (mClientThread == null || !mClientThread.isRunning()) {
      mClientThread = new SyncClient();
      if (mClientThread.start(0, false)) {
        Log.e(TAG, "start --> server alread start !!!!!");
      } else {
        startServer();
        if(mClientThread != null) {
          mClientThread.start(0, true);
        }
      }
    }
  }

  public boolean isRunning() {
    return mClientThread != null && mClientThread.isRunning();
  }

  public void stop() {
    try {
      if(adbStream != null){
        adbStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (mClientThread != null) {
      mClientThread.exit();
      mClientThread = null;
    }

  }

  public OpsResult exec(OpsCommands.Builder builder) throws Exception {
    start();
    return mClientThread.exec(builder);
  }

  private List<String> getCommonds() {

    StringBuilder sb = new StringBuilder();

    sb.append("type:");
    if (mConfig.useAdb || mConfig.rootOverAdb) {
      sb.append("adb");
      sb.append(",path:" + SConfig.getPort());
    } else {
      sb.append("root");
      sb.append(",path:" + SConfig.SOCKET_PATH);
    }
    sb.append(",token:" + SConfig.getLocalToken());

    if (mConfig.allowBgRunning) {
      sb.append(",bgrun:1");
    }

    if (BuildConfig.DEBUG) {
      sb.append(",debug:1");
    }

    //if(mConfig.useAdb || mConfig.rootOverAdb){
    sb.append("   & ");
    //}

    Log.e(TAG, "getCommonds --> " + sb);

    List<String> cmds = new ArrayList<>();
    cmds.add("export CLASSPATH=" + SConfig.getClassPath());
    cmds.add("echo start");
    cmds.add("id");
    cmds.add(
        "exec  app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain $@ " + sb.toString());

    return cmds;
  }

  private AdbConnection connection;
  private AdbStream adbStream;

  private boolean useAdbStartServer() throws Exception {
    if (adbStream != null && !adbStream.isClosed()) {
      return true;
    }
    if (connection != null) {
      connection.close();
    }

    final AtomicBoolean connResult = new AtomicBoolean(false);
    connection = AdbConnector.buildConnect(mConfig.context, mConfig.adbHost, mConfig.adbPort);

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          connection.connect();
          connResult.set(true);
        } catch (Exception e) {
          connResult.set(false);
          e.printStackTrace();
          if (connection != null) {
            try {
              connection.close();
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        }
      }
    });

    try {
      Log.e(TAG, "useAdbStartServer --> start");
      thread.start();
      thread.join(10000);
      Log.e(TAG, "useAdbStartServer --> jion 10000");

      if (!connResult.get()) {
        connection.close();
      }
    } catch (InterruptedException e) {
      connResult.set(false);
      e.printStackTrace();
      if (connection != null) {
        connection.close();
      }
    }

    if (!connResult.get()) {
      throw new RuntimeException("please grant adb permission!");
    }

    adbStream = connection.open("shell:");

    if (!TextUtils.isEmpty(mConfig.logFile)) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          BufferedWriter bw = null;
          LineReader reader;
          try {
            bw = new BufferedWriter(new FileWriter(mConfig.logFile, false));
            bw.write(new Date().toString());
            bw.newLine();
            bw.write("adb start log");
            bw.newLine();

            reader = new LineReader(adbStream);
            int line = 0;
            String s = reader.readLine();
            while (!adbStream.isClosed()) {
              Log.e(TAG, "log run --> " + s);
              s = reader.readLine();
              if (s != null) {
                bw.write(s);
                bw.newLine();
              }
              line++;
              if (!mConfig.printLog && (line >= 50 || (s != null && s.startsWith("runGet")))) {
                break;
              }
            }
            bw.flush();
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            try {
              if (bw != null) {
                bw.close();
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }).start();
    }

    adbStream.write("\n\n".getBytes());
    SystemClock.sleep(100);
    adbStream.write("id\n".getBytes());
    SystemClock.sleep(100);
    List<String> cmds = getCommonds();

    StringBuilder sb = new StringBuilder();
    for (String cmd : cmds) {
      sb.append(cmd).append(';');
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("\n");
    Log.e(TAG, "useAdbStartServer --> " + sb);
    adbStream.write(sb.toString().getBytes());
    SystemClock.sleep(3000);

    Log.e(TAG, "startServer -->ADB server start ----- ");

    return true;
  }

  private boolean useRootStartServer() throws Exception {
    DataOutputStream outputStream = null;
    RootChecker checker = null;
    Process exec = null;
    try {

      Log.e(TAG, "useRootStartServer --> ");

      exec = Runtime.getRuntime().exec("su");
      checker = new RootChecker(exec);
      checker.start();

      try {
        checker.join(20000);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (checker.exit == -1) {
        throw new RuntimeException("grant root timeout");
      }

      if (checker.exit != 1) {
        throw new RuntimeException(checker.errorMsg);
      }

      outputStream = new DataOutputStream(exec.getOutputStream());

      List<String> cmds = getCommonds();

      //部分情况下selinux导致执行失败 exec  app_process
      if (mConfig.rootOverAdb) {
        cmds.clear();

        cmds.add("echo 'root over adb mode'");
        cmds.add("getenforce");
        cmds.add("setprop service.adb.tcp.port " + mConfig.adbPort);
        cmds.add("stop adbd");
        cmds.add("start adbd");
        cmds.add("echo $?");
        cmds.add("echo end");

        final OutputStream waitWriter = outputStream;
        final Process waitProcess = exec;
        new Thread(new Runnable() {
          @Override
          public void run() {
            SystemClock.sleep(1000 * 20);
            try {
              Log.e(TAG, "run --> stop adb ");

              List<String> cls=new ArrayList<String>(){
                {
                  add("echo 'stop adb!!!'");
                  add("setprop service.adb.tcp.port -1");
                  add("stop adbd");
                  add("start adbd");
                  add("getprop service.adb.tcp.port");
                }
              };

              writeCmds(cls,waitWriter);

            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              try {
                waitProcess.destroy();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

          }
        }).start();

      }

      writeCmds(cmds,outputStream);

      final BufferedReader inputStream = new BufferedReader(
          new InputStreamReader(exec.getInputStream(), "UTF-8"));

      //记录日志
      if (!TextUtils.isEmpty(mConfig.logFile)) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            BufferedWriter bw = null;
            try {
              boolean saveLog = !TextUtils.isEmpty(mConfig.logFile);
              if (saveLog) {
                bw = new BufferedWriter(new FileWriter(mConfig.logFile, false));
                bw.write(new Date().toString());
                bw.newLine();
                bw.write("root start log");
                bw.newLine();
              }

              int line = 0;
              String s = inputStream.readLine();
              while (s != null) {
                Log.e(TAG, "log run --> " + s);
                s = inputStream.readLine();
                if (saveLog && s != null) {
                  bw.write(s);
                  bw.newLine();
                }
                line++;
                if (!mConfig.printLog && (line >= 50 || (s != null && s.startsWith("runGet")))) {
                  break;
                }

              }
              if (bw != null) {
                bw.flush();
              }
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              try {
                if (bw != null) {
                  bw.close();
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }).start();
      }

      SystemClock.sleep(3000);

      if (mConfig.rootOverAdb) {
        Log.e(TAG, "startServer --- use root over adb,open adb server----");
        return useAdbStartServer();
      }

      Log.e(TAG, "startServer -->ROOT server start ----- ");

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      if (checker != null) {
        checker.interrupt();
      }
      throw e;
    } finally {
      try {
        if (exec != null && !mConfig.rootOverAdb) {
          exec.destroy();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private void writeCmds(List<String> cmds, OutputStream outputStream) throws IOException {
    for (String cmd : cmds) {
      outputStream.write((cmd + "\n").getBytes("UTF-8"));
      outputStream.flush();
    }
    outputStream.flush();
  }

  private boolean startServer() throws Exception {
    if (mConfig.useAdb) {
      return useAdbStartServer();
    } else {
      return useRootStartServer();
    }
  }


  private static class RootChecker extends Thread {

    int exit = -1;
    String errorMsg = null;
    Process process;

    private RootChecker(Process process) {
      this.process = process;
    }

    @Override
    public void run() {
      try {
        BufferedReader inputStream = new BufferedReader(
            new InputStreamReader(process.getInputStream(), "UTF-8"));
        BufferedWriter outputStream = new BufferedWriter(
            new OutputStreamWriter(this.process.getOutputStream(), "UTF-8"));

        outputStream.write("echo Started");
        outputStream.newLine();
        outputStream.flush();

        while (true) {
          String line = inputStream.readLine();
          if (line == null) {
            throw new EOFException();
          }
          if ("".equals(line)) {
            continue;
          }
          if ("Started".equals(line)) {
            this.exit = 1;
            break;
          }
          errorMsg = "unkown error occured.";
        }
      } catch (IOException e) {
        exit = -42;
        if (e.getMessage() != null) {
          errorMsg = e.getMessage();
        } else {
          errorMsg = "RootAccess denied?.";
        }
      }

    }
  }


  private class SyncClient {

    private volatile boolean isRunning = false;
    private OpsDataTransfer transfer;

    private void connect(int retryCount) throws Exception {
      if (!isRunning && retryCount >= 0) {
        try {
          OutputStream os = null;
          InputStream is = null;
          if (mConfig.useAdb || mConfig.rootOverAdb) {
            Socket socket = new Socket("127.0.0.1", SConfig.getPort());
            os = socket.getOutputStream();
            is = socket.getInputStream();
          } else {
            LocalSocket localSocket = new LocalSocket();
            localSocket.connect(new LocalSocketAddress(SConfig.SOCKET_PATH));
            os = localSocket.getOutputStream();
            is = localSocket.getInputStream();
          }
          String token = SConfig.getLocalToken();
          if (TextUtils.isEmpty(token)) {
            throw new RuntimeException("token is null !");
          }
          transfer = new OpsDataTransfer(os, is, false);
          transfer.shakehands(token, false);
          isRunning = true;
        } catch (IOException e) {
          e.printStackTrace();
          if (retryCount >= 0) {
            try {
              isRunning = false;
              startServer();

              SystemClock.sleep(1000);
              Log.e(TAG, "connect --> retry " + retryCount);
              connect(--retryCount);
            } catch (Exception e1) {
              //e1.printStackTrace();
              throw e1;
            }
          } else {
            throw new IOException(e);
          }
        }
      } else {
        throw new RuntimeException("connect fail !");
      }
    }

    boolean start(int retryCount, boolean orThrow) throws Exception {
      try {
        connect(retryCount);
      } catch (Exception e) {
        if (orThrow) {
          throw e;
        }
        //e.printStackTrace();
      }
      return isRunning;
    }

    OpsResult exec(OpsCommands.Builder builder) throws Exception {
      if (!isRunning) {
        connect(5);
      }
      try {
        return ParcelableUtil.unmarshall(execCmd(builder), OpsResult.CREATOR);
      } catch (IOException e) {
        isRunning = false;
        e.printStackTrace();
        throw e;
      }
    }


    byte[] execCmd(OpsCommands.Builder builder) throws IOException {
      return transfer.sendMsgAndRecv(ParcelableUtil.marshall(builder));
    }

    void exit() {
      isRunning = false;
      if (transfer != null) {
        transfer.stop();
      }
    }

    boolean isRunning() {
      return isRunning;
    }

  }


  public static void closeBgServer() {
    OutputStream os = null;
    InputStream is = null;
    String token = SConfig.getLocalToken();
    try {
      LocalSocket localSocket = new LocalSocket();
      localSocket.connect(new LocalSocketAddress(SConfig.SOCKET_PATH));
      os = localSocket.getOutputStream();
      is = localSocket.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (os == null || is == null) {
      try {
        Socket socket = new Socket("127.0.0.1", SConfig.getPort());
        os = socket.getOutputStream();
        is = socket.getInputStream();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (os != null && is != null) {
      OpsDataTransfer transfer = null;
      try {
        transfer = new OpsDataTransfer(os, is, false);
        transfer.shakehands(token, false);

        OpsCommands.Builder builder = new OpsCommands.Builder();
        builder.setAction(OpsCommands.ACTION_OTHER);
        builder.setPackageName("close_server");
        transfer.sendMsg(ParcelableUtil.marshall(builder));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (transfer != null) {
          transfer.stop();
        }
      }
    }
  }
}
