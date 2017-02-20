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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private String[] getCommonds(){
        String arch = AssetsUtils.is64Bit() ? "64" : "";

        String args = "";
        if (mConfig.allowBgRunning) {
            args += " -D &";
        }else {
            args += " -T ";
        }

        return new String[] {"export LD_LIBRARY_PATH=" + String.format("/vendor/lib%1$s:/system/lib%2$s", arch, arch),
                "export CLASSPATH=" + SConfig.getClassPath(),
                "echo start",
                "exec  app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain $@ " + (mConfig.useAdb?SConfig.getPort():SConfig.generateDomainName()) + " " + args,
                };

    }
    private AdbConnection connection;
    private AdbStream adbStream;
    private boolean useAdbStartServer() throws Exception{
        if(adbStream != null && !adbStream.isClosed()){
            return true;
        }
        if(connection != null){
            connection.close();
        }

        final AtomicBoolean connResult=new AtomicBoolean(false);
        connection=AdbConnector.buildConnect(mConfig.context, mConfig.adbHost, mConfig.adbPort);

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    connResult.set(true);
                } catch (Exception e) {
                    connResult.set(false);
                    e.printStackTrace();
                    if (connection != null){
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

            if(!connResult.get()){
                connection.close();
            }
        } catch (InterruptedException e) {
            connResult.set(false);
            e.printStackTrace();
            if(connection != null){
                connection.close();
            }
        }


        if(!connResult.get()){
            throw new RuntimeException("please grant adb premission!");
        }

        adbStream = connection.open("shell:");

        if(!TextUtils.isEmpty(mConfig.logFile)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedWriter bw=null;
                    LineReader reader;
                    try {
                        bw=new BufferedWriter(new FileWriter(mConfig.logFile,false));
                        bw.write(new Date().toString());
                        bw.newLine();
                        bw.write("adb start log");
                        bw.newLine();

                        reader=new LineReader(adbStream);
                        int line=0;
                        String s = reader.readLine();
                        while (!adbStream.isClosed()) {
                            Log.e(TAG, "log run --> " + s);
                            s = reader.readLine();
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

        adbStream.write("\n\n".getBytes());
        SystemClock.sleep(100);
        adbStream.write("id\n".getBytes());
        SystemClock.sleep(100);
        String[] cmds = getCommonds();

        StringBuilder sb=new StringBuilder();
        for (String cmd : cmds) {
            sb.append(cmd).append(';');
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("\n");
        Log.e(TAG, "useAdbStartServer --> "+sb);
        adbStream.write(sb.toString().getBytes());
        SystemClock.sleep(3000);

        Log.e(TAG, "startServer -->ADB server start ----- ");

        return true;
    }

    private boolean useRootStartServer() throws Exception{
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

            String[] cmds = getCommonds();

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
                            bw.write("root start log");
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

            SystemClock.sleep(3000);

            Log.e(TAG, "startServer -->ROOT server start ----- ");

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

    private boolean startServer() throws Exception {
        if(mConfig.useAdb){
            return useAdbStartServer();
        }else {
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
                    OutputStream os=null;
                    InputStream is=null;
                    if(mConfig.useAdb){
                        Socket socket=new Socket("127.0.0.1",SConfig.getPort());
                        os=socket.getOutputStream();
                        is=socket.getInputStream();
                    }else {
                        LocalSocket localSocket = new LocalSocket();
                        localSocket.connect(new LocalSocketAddress(SConfig.getLocalServerPath()));
                        os=localSocket.getOutputStream();
                        is=localSocket.getInputStream();
                    }
                    String token=mConfig.useAdb?String.valueOf(SConfig.getPort()):SConfig.getLocalServerPath();
                    transfer = new OpsDataTransfer(os, is, false);
                    transfer.shakehands(token, false);
                    isRunning = true;
                } catch (IOException e) {
                    e.printStackTrace();
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
