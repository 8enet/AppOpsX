package com.zzzmode.appopsx;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.ParcelableUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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

    public void updateConfig(OpsxManager.Config config) {
        if (config != null) {
            mConfig = config;
        }
    }

    public void start() throws Exception {

        if (mClientThread == null || !mClientThread.isRunning()) {
            mClientThread = new SyncClient();
            if (mClientThread.start()) {
                Log.e(TAG, "start --> server alread start !!!!!");

            } else {
                startServer();
                mClientThread.start();
            }
        }
    }

    public boolean isRunning() {
        return mClientThread != null && mClientThread.isRunning();
    }

    public void stop() {
        if (mClientThread != null && mClientThread.isRunning()) {
            mClientThread.exit();
            mClientThread = null;
        }

    }

    public OpsResult exec(OpsCommands.Builder builder) throws Exception {
        start();
        return mClientThread.exec(builder);
    }


    private boolean startServer() throws Exception {
        BufferedWriter writer = null;
        RootChecker checker = null;
        try {

            Process exec = Runtime.getRuntime().exec("su");
            checker = new RootChecker(exec);
            checker.start();

            checker.join(20000);

            if (checker.exit == -1) {
                throw new RuntimeException("grant root timeout");
            }

            if (checker.exit != 1) {
                throw new RuntimeException(checker.errorMsg);
            }

            writer = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));

            String arch = AssetsUtils.is64Bit() ? "64" : "";

            String args = "";
            if (mConfig.allowBgRunning) {
                args += " -D ";
            }

            String[] cmds = {"export LD_LIBRARY_PATH=" + String.format("/vendor/lib%1$s:/system/lib%2$s", arch, arch),
                    "export CLASSPATH=" + SConfig.getClassPath(),
                    "echo start",
                    "exec  app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain $@ " + SConfig.generateDomainName() + " " + args,
                    "echo end"};

            for (String cmd : cmds) {
                writer.write(cmd);
                writer.newLine();
                writer.flush();
            }
            writer.flush();

            final BufferedReader inputStream = new BufferedReader(new InputStreamReader(exec.getInputStream(), "UTF-8"));

            //记录日志
            if(!TextUtils.isEmpty(mConfig.logFile)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedWriter bw=null;
                        try {
                            bw=new BufferedWriter(new FileWriter(mConfig.logFile,false));
                            bw.write(new Date().toString());
                            bw.newLine();

                            int line=0;
                            String s = inputStream.readLine();
                            while (s != null) {
                                Log.e(TAG, "log run --> " + s);
                                s = inputStream.readLine();
                                bw.write(s);
                                bw.newLine();
                                line++;
                                if(line >= 20 || (s !=null && s.startsWith("runGet"))){
                                    break;
                                }
                            }
                            bw.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            try {
                                if(bw != null){
                                    bw.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            Log.e(TAG, "startServer --> server start ----- ");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (checker != null) {
                checker.interrupt();
            }
            throw e;
        } finally {
//            try {
//                if(exec != null){
//                    exec.destroy();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
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
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), "UTF-8"));

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
                    LocalSocket localSocket = new LocalSocket();
                    localSocket.connect(new LocalSocketAddress(SConfig.getLocalServerPath()));
                    transfer = new OpsDataTransfer(localSocket.getOutputStream(), localSocket.getInputStream(), false);
                    transfer.shakehands(null, false);
                    isRunning = true;
                } catch (IOException e) {
                    if (retryCount >= 0) {
                        try {
                            startServer();
                            isRunning = false;
                            SystemClock.sleep(1000);
                            Log.e(TAG, "connect --> retry " + retryCount);
                            connect(--retryCount);
                        } catch (Exception e1) {
                            e1.printStackTrace();
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

        boolean start() throws Exception {
            connect(10);
            return isRunning;
        }

        OpsResult exec(OpsCommands.Builder builder) throws Exception {
            if (!isRunning) {
                connect(10);
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

}
