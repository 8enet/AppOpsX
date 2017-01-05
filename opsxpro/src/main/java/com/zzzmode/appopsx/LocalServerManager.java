package com.zzzmode.appopsx;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemClock;
import android.util.Log;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.ParcelableUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by zl on 2016/11/13.
 */

class LocalServerManager {

    private static final String TAG = "LocalServerManager";
    private static LocalServerManager sLocalServerManager;

    static LocalServerManager getInstance() {
        if (sLocalServerManager == null) {
            synchronized (LocalServerManager.class) {
                if (sLocalServerManager == null) {
                    sLocalServerManager = new LocalServerManager();
                }
            }
        }
        return sLocalServerManager;
    }


    private SyncClient mClientThread = null;

    private LocalServerManager() {

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

    public void stop() {
        if (mClientThread != null && mClientThread.isRunning()) {
            mClientThread.exit();
        }
    }

    public OpsResult exec(OpsCommands.Builder builder) throws Exception {
        start();
        return mClientThread.exec(builder);
    }


    private static boolean startServer() throws Exception{
        BufferedWriter writer = null;
        Process exec = null;
        RootChecker checker=null;
        try {

            exec = Runtime.getRuntime().exec("su");
            checker=new RootChecker(exec);
            checker.start();

            checker.join(20000);

            if(checker.exit == -1){
                throw new RuntimeException("grant root timeout");
            }

            if(checker.exit != 1){
                throw new RuntimeException(checker.errorMsg);
            }

            writer = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));

            String[] cmds = {"export LD_LIBRARY_PATH=/vendor/lib:/system/lib",
                    "export CLASSPATH=" + SConfig.getClassPath(),
                    "echo start",
                    "exec app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain \"$@\""};

            for (String cmd : cmds) {
                writer.write(cmd);
                writer.newLine();
                writer.flush();
            }
            writer.flush();

            Log.e(TAG, "startServer --> server start ----- ");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(checker != null){
                checker.interrupt();
            }
            throw e;
        } finally {
//            try {
//                if(writer != null){
//                    writer.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
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
        String errorMsg=null;
        Process process;

        private RootChecker(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                BufferedWriter outputStream =new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), "UTF-8"));

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


    private static class SyncClient {
        private volatile boolean isRunning = false;
        private OpsDataTransfer transfer;

        private void connect(int retryCount) throws Exception {
            if (!isRunning) {
                try {
                    LocalSocket localSocket = new LocalSocket();
                    localSocket.connect(new LocalSocketAddress("com.zzzmode.appopsx.socket"));
                    transfer = new OpsDataTransfer(localSocket.getOutputStream(), localSocket.getInputStream(), false);
                    isRunning = true;
                } catch (IOException e) {
                    if(retryCount >= 0) {
                        try {
                            startServer();
                            isRunning = false;
                            SystemClock.sleep(1000);
                            Log.e(TAG, "connect --> retry "+retryCount);
                            connect(--retryCount);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            throw e1;
                        }
                    }else {
                        throw new IOException(e);
                    }
                }
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
                byte[] bytes = transfer.sendMsgAndRecv(ParcelableUtil.marshall(builder));
                return ParcelableUtil.unmarshall(bytes, OpsResult.CREATOR);
            } catch (IOException e) {
                isRunning = false;
                e.printStackTrace();
                throw e;
            }
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
