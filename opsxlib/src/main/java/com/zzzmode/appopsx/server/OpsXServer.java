package com.zzzmode.appopsx.server;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.zzzmode.appopsx.common.OpsDataTransfer;

import java.io.IOException;

/**
 * Created by zl on 2016/11/6.
 */

class OpsXServer implements Runnable{

    private boolean running=true;
    private LocalServerSocket serverSocket;
    private OpsDataTransfer opsDataTransfer;
    private OpsDataTransfer.OnRecvCallback callback;

    OpsXServer(String name,OpsDataTransfer.OnRecvCallback callback) throws IOException {
        serverSocket=new LocalServerSocket(name);
        this.callback=callback;
    }

    @Override
    public void run() {
        while (running){
            try {
                LocalSocket socket = serverSocket.accept(); //only one connect
                opsDataTransfer=new OpsDataTransfer(socket.getOutputStream(),socket.getInputStream(),callback);
                opsDataTransfer.handleRecv();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendResult(String str) throws IOException {
        if(running && opsDataTransfer != null){
            opsDataTransfer.sendMsg(str.getBytes());
        }
    }

    public void sendResult(byte[] bytes) throws IOException {
        if(running && opsDataTransfer != null){
            opsDataTransfer.sendMsg(bytes);
        }
    }

    public void setStop(){
        running=false;
        if(opsDataTransfer != null){
            opsDataTransfer.stop();
        }
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
