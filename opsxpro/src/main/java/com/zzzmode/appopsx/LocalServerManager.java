package com.zzzmode.appopsx;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbStream;
import com.zzzmode.adblib.AdbConnector;
import com.zzzmode.adblib.LineReader;
import com.zzzmode.appopsx.common.BaseCaller;
import com.zzzmode.appopsx.common.CallerMethod;
import com.zzzmode.appopsx.common.CallerResult;
import com.zzzmode.appopsx.common.OpsDataTransfer;
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


  private ClientSession mSession = null;


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

  private ClientSession getSession() throws Exception {
    if (mSession == null || !mSession.isRunning()) {
      try {
        //try to connect
        mSession = createSession();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (mSession == null) {
        //server not started
        startServer();
        mSession = createSession();
      }
    }
    return mSession;
  }

  public boolean isRunning() {
    return mSession != null && mSession.isRunning();
  }

  void stop() {
    try {
      if (adbStream != null) {
        adbStream.close();
      }
      adbStream = null;
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (mSession != null) {
      mSession.close();
      mSession = null;
    }

  }


  void start() throws Exception {
    getSession();
  }

  private byte[] execPre(byte[] params)throws Exception{
    ClientSession session = getSession();
    if(session == null){
      throw new RuntimeException("create session error ------");
    }
    OpsDataTransfer transfer = session.getTransfer();
    if(transfer == null){
      throw new RuntimeException("get transfer error -----");
    }

    return transfer.sendMsgAndRecv(params);
  }


  CallerResult execNew(CallerMethod method) throws Exception {
    byte[] result = execPre(ParcelableUtil.marshall(new BaseCaller(method.wrapParams())));

    return ParcelableUtil.unmarshall(
        result, CallerResult.CREATOR);
  }

  void closeBgServer() {
    try {

      BaseCaller baseCaller=new BaseCaller(BaseCaller.TYPE_CLOSE);
      createSession().getTransfer().sendMsgAndRecv(ParcelableUtil.marshall(baseCaller));
    } catch (Exception e) {
      Log.w(TAG, "closeBgServer: "+e.getCause()+"  "+e.getMessage());
    }
  }


  private List<String> getCommonds() {


    AssetsUtils.writeScript(mConfig);

    Log.e(TAG, "classpath --> "+SConfig.getClassPath());


    List<String> cmds = new ArrayList<>();
    cmds.add("sh /sdcard/Android/data/com.zzzmode.appopsx/opsx.sh");
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

    for (String cmd : cmds) {
      adbStream.write((cmd+"\n").getBytes());
      SystemClock.sleep(100);
    }
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

              List<String> cls = new ArrayList<String>() {
                {
                  add("echo 'stop adb!!!'");
                  add("setprop service.adb.tcp.port -1");
                  add("stop adbd");
                  add("start adbd");
                  add("getprop service.adb.tcp.port");
                }
              };

              writeCmds(cls, waitWriter);

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

      //cmds.add(0,"supolicy --live \'allow qti_init_shell zygote_exec file execute\'");
      writeCmds(cmds, outputStream);

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
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      if (checker != null) {
        checker.interrupt();
      }
      throw e;
    } finally {
      try {
        if (exec != null) {
          //exec.destroy();
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

  private void startServer() throws Exception {
    if (mConfig.useAdb) {
      useAdbStartServer();
    } else {
      useRootStartServer();
    }
    Log.e(TAG, "startServer --> end ---");
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
        outputStream.write("echo U333L\n");
        outputStream.flush();

        while (true) {
          String line = inputStream.readLine();
          if (line == null) {
            throw new EOFException();
          }
          if ("".equals(line)) {
            continue;
          }
          if ("U333L".equals(line)) {
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

  private ClientSession createSession() throws IOException {
    if(isRunning()){
      return mSession;
    }
    Socket socket = new Socket("127.0.0.1", SConfig.getPort());
    socket.setSoTimeout(1000 * 30);
    OutputStream os = socket.getOutputStream();
    InputStream is = socket.getInputStream();
    String token = SConfig.getLocalToken();
    if (TextUtils.isEmpty(token)) {
      throw new RuntimeException("token is null !");
    }
    OpsDataTransfer transfer = new OpsDataTransfer(os, is, false);
    transfer.shakehands(token, false);
    return new ClientSession(transfer);
  }

  private static class ClientSession {

    private volatile boolean isRunning = false;
    private OpsDataTransfer transfer;

    ClientSession(OpsDataTransfer transfer) {
      this.transfer = transfer;
      isRunning = true;
    }

    void close() {
      isRunning = false;
      if (transfer != null) {
        transfer.stop();
      }
      transfer = null;
    }

    boolean isRunning() {
      return isRunning && transfer != null;
    }

    OpsDataTransfer getTransfer() {
      return transfer;
    }
  }
}
