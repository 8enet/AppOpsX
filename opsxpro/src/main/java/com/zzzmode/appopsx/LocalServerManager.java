package com.zzzmode.appopsx;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.ParcelableUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zl on 2016/11/13.
 */

class LocalServerManager {

    private static final String TAG = "LocalServerManager";
    private static LocalServerManager sLocalServerManager;

    static LocalServerManager getInstance(){
        if(sLocalServerManager == null){
            synchronized (LocalServerManager.class){
                if(sLocalServerManager == null){
                    sLocalServerManager=new LocalServerManager();
                }
            }
        }
        return sLocalServerManager;
    }


    private SyncClient mClientThread = null;

    private LocalServerManager(){

    }


    public void start(){

        if(mClientThread == null || !mClientThread.isRunning()){
            mClientThread=new SyncClient();
            if(mClientThread.start()){
                Log.e(TAG, "start --> server alread start !!!!!");

            }else {
                startServer();
                mClientThread.start();
            }
        }
    }

    public void stop(){
        if(mClientThread != null && mClientThread.isRunning()){
            mClientThread.exit();
        }
    }

    public OpsResult exec(OpsCommands.Builder builder){
        start();
        return mClientThread.exec(builder);
    }


    private static void startServer(){
        BufferedWriter writer=null;
        Process exec=null;
        try {
            exec= new ProcessBuilder("sh").redirectErrorStream(true).start();
            writer = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));

            String[] cmds={"su","export LD_LIBRARY_PATH=/vendor/lib:/system/lib",
                    "export CLASSPATH="+SConfig.getClassPath(),"" +
                    "echo start",
                    "exec app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain \"$@\""};

            for (String cmd:cmds){
                writer.write(cmd);
                writer.newLine();
                writer.flush();
            }
            writer.flush();
            exec.waitFor();

            Log.e(TAG, "startServer --> server start ----- ");

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
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

    private static class RunnerServerThread extends Thread{

        private static final String TAG = "RunnerServerThread";
        private Process exec;
        private volatile boolean isRunning=false;

        private BufferedWriter writer;

        @Override
        public void run() {

            try {


                exec= new ProcessBuilder("sh").redirectErrorStream(true).start();
                writer = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));

                String[] cmds={"su","export LD_LIBRARY_PATH=/vendor/lib:/system/lib",
                        "export CLASSPATH="+SConfig.getClassPath(),"" +
                        "echo start",
                        "exec app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain \"$@\""};

                for (String cmd:cmds){
                   execWriter(cmd);
                }
                Log.e(TAG, "run --> start server");
//                isRunning=true;
//                String tmp;
//
//                while ( !"exit".equals((tmp=reader.readLine())) ){
//                    Log.e(TAG, "run -->read "+tmp);
//                }

                //exec=Runtime.getRuntime().exec(new String[]{"su","-C","export LD_LIBRARY_PATH=/vendor/lib:/system/lib;export CLASSPATH=/data/data/com.zzzmode.appopsx/app_opsx/appopsx.jar;app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain \"$@\""});
                //exec=Runtime.getRuntime().exec("su -C /data/data/com.zzzmode.appopsx/app_opsx/appopsx");
                isRunning=true;
                int waitFor = exec.waitFor();
                Log.e(TAG, "run --> server end "+waitFor);
            } catch (Exception e) {
                e.printStackTrace();
                isRunning=false;
            }

        }

        private void execWriter(String cmd){
            try{
                if(writer != null){
                    writer.write(cmd);
                    writer.newLine();
                    writer.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        void exit(){
//            Log.e(TAG, "exit --> write");
//            execWriter("echo exit");

            Log.e(TAG, "exit --> write end");
            if(exec!= null){
                exec.destroy();
            }
            Log.e(TAG, "exit --> destroy");
            isRunning=false;
        }

        boolean isRunning() {
            return isRunning;
        }
    }


    private static class RunnerClientThread extends Thread implements OpsDataTransfer.OnRecvCallback{

        private static final String TAG = "RunnerClientThread";

        private OpsDataTransfer transfer;
        private final AtomicReference<OpsResult> mRefResult=new AtomicReference<>();
        private volatile boolean isRunning=false;

        @Override
        public void run() {

            try {
                Log.e(TAG, "run --> start run connect");
                LocalSocket localSocket=new LocalSocket();
                localSocket.connect(new LocalSocketAddress("com.zzzmode.appopsx"));
                transfer=new OpsDataTransfer(localSocket.getOutputStream(),localSocket.getInputStream());
                transfer.setCallback(this);
                isRunning=true;
                transfer.handleRecv();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onMessage(byte[] bytes) {
            OpsResult result = ParcelableUtil.unmarshall(bytes, OpsResult.CREATOR);
            Log.e(TAG, "onMessage --> "+ result);
            mRefResult.set(result);
            synchronized (mRefResult){
                mRefResult.notifyAll();
            }
        }


        OpsResult exec(OpsCommands.Builder builder){
            if(!isRunning){
                return null;
            }
            try {
                mRefResult.set(null);

                transfer.sendMsg(ParcelableUtil.marshall(builder));
                if(mRefResult.get() == null) {
                    synchronized (mRefResult) {
                        try {
                            mRefResult.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return mRefResult.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        void exit(){
            isRunning=false;
            if(transfer != null){
                transfer.stop();
            }
        }

        boolean isRunning() {
            return isRunning;
        }
    }


    private static class SyncClient{
        private volatile boolean isRunning=false;
        private OpsDataTransfer transfer;

        private void connect(){
            if(!isRunning) {
                try {
                    LocalSocket localSocket = new LocalSocket();
                    localSocket.connect(new LocalSocketAddress("com.zzzmode.appopsx"));
                    transfer = new OpsDataTransfer(localSocket.getOutputStream(), localSocket.getInputStream(), false);
                    isRunning = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    startServer();
                    isRunning = false;
                }
            }
        }

        boolean start(){
            connect();
            return isRunning;
        }

        OpsResult exec(OpsCommands.Builder builder) {
            if(!isRunning){
                connect();
            }
            try {
                byte[] bytes = transfer.sendMsgAndRecv(ParcelableUtil.marshall(builder));
                return ParcelableUtil.unmarshall(bytes, OpsResult.CREATOR);
            } catch (IOException e) {
                isRunning=false;
                e.printStackTrace();
            }
            return null;
        }

        void exit(){
            isRunning=false;
            if(transfer != null){
                transfer.stop();
            }
        }

        boolean isRunning() {
            return isRunning;
        }

    }

}
