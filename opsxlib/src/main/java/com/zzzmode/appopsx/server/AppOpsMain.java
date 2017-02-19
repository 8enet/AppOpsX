package com.zzzmode.appopsx.server;


import android.Manifest;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.app.IAppOpsService;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ParcelableUtil;
import com.zzzmode.appopsx.common.ReflectUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppOpsMain implements OpsDataTransfer.OnRecvCallback {

    private static final int MSG_TIMEOUT = 1;
    private static final int DEFAULT_TIME_OUT_TIME = 1000 * 60 * 1; //1min
    private static final int BG_TIME_OUT=DEFAULT_TIME_OUT_TIME*10; //10min

    private static FileOutputStream fos;
    private static boolean writeLog=false;

    public static void main(String[] args) {
        try {
            fos=new FileOutputStream("/data/local/tmp/opsx.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            new AppOpsMain(args);
        } catch (Exception e) {
            e.printStackTrace();
            log(Log.getStackTraceString(e));
        }finally {
            try {
                fos.flush();
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void log(String log){
        try {
            if(writeLog) {
                fos.write(log.getBytes());
                fos.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OpsXServer server;
    private Handler handler;
    private volatile boolean isDeath = false;
    private int timeOut = DEFAULT_TIME_OUT_TIME;
    private volatile boolean allowBg=false;

    private AppOpsMain(String[] args) throws IOException {
        System.out.println("start ops server args:"+ Arrays.toString(args));
        log("start ops server args:"+ Arrays.toString(args));
        if(args == null || args.length < 1){
            return;
        }

//        List<Class> paramsType=new ArrayList<>(1);
//        paramsType.add(String.class);
//        List<Object> params=new ArrayList<>(1);
//        params.add("appopsx_local_server");
//        ReflectUtils.invokMethod(Process.class,"setArgV0",paramsType,params);

        String socketName=args[0]; //"com.zzzmode.appopsx.socket"
        String allowBgArg=args.length>1?args[1]:null;


        server = new OpsXServer(socketName,null,this);
        server.allowBackgroundRun=this.allowBg="-D".equalsIgnoreCase(allowBgArg);
        try {

            HandlerThread thread1 = new HandlerThread("watcher-ups");
            thread1.start();
            handler = new Handler(thread1.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_TIMEOUT:
                            destory();
                            break;
                    }
                }
            };
            handler.sendEmptyMessageDelayed(MSG_TIMEOUT,timeOut);
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
            log(Log.getStackTraceString(e));
            destory();
        }

        System.out.println("end ---- ");
    }

    private void destory(){
        try {
            if(!allowBg) {
                handler.removeCallbacksAndMessages(null);
                handler.removeMessages(MSG_TIMEOUT);
                handler.getLooper().quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            isDeath = true;
            server.setStop();

            System.out.println("timeout stop----- "+Process.myPid());

            Runtime.getRuntime().exec("kill -9 "+Process.myPid()); //kill self

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void handleCommand(OpsCommands.Builder builder) {
        String s = builder.getAction();
        if (OpsCommands.ACTION_GET.equals(s)) {
            runGet(builder);
        } else if (OpsCommands.ACTION_SET.equals(s)) {
            runSet(builder);
        } else if (OpsCommands.ACTION_RESET.equals(s)) {
            runReset(builder);
        }else {
            runOther(builder);
        }
    }

    private void runGet(OpsCommands.Builder getBuilder) {

        try {
            System.out.println("runGet sdk:"+Build.VERSION.SDK_INT);

            log("runGet sdk:"+Build.VERSION.SDK_INT);

            final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
                    ServiceManager.getService(Context.APP_OPS_SERVICE));
            String packageName = getBuilder.getPackageName();

            int uid = Helper.getPackageUid(packageName,0);

            List opsForPackage = appOpsService.getOpsForPackage(uid, packageName, null);
            List<PackageOps> packageOpses = new ArrayList<>();
            if (opsForPackage != null) {
                for (Object o : opsForPackage) {
                    PackageOps packageOps = ReflectUtils.opsConvert(o);
                    addSuport(appOpsService,packageOps);
                    packageOpses.add(packageOps);
                }
            }else {
                PackageOps packageOps=new PackageOps(packageName,uid,new ArrayList<OpEntry>());
                addSuport(appOpsService,packageOps);
                packageOpses.add(packageOps);
            }

            server.sendResult(ParcelableUtil.marshall(new OpsResult(packageOpses, null)));
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(Log.getStackTraceString(e));
            try {
                server.sendResult(ParcelableUtil.marshall(new OpsResult(null, e)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    private void addSuport(IAppOpsService appOpsService,PackageOps ops){
        try {
            PackageInfo packageInfo = ActivityThread.getPackageManager().getPackageInfo(ops.getPackageName(), PackageManager.GET_PERMISSIONS, 0);
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    int code = Helper.permissionToCode(permission);

                    if(code > 0 && !ops.hasOp(code)){
                        int mode = appOpsService.checkOperation(code, ops.getUid(), ops.getPackageName());
                        if(mode != AppOpsManager.MODE_ERRORED){
                            //
                            ops.getOps().add(new OpEntry(code,mode,0,0,0,0,null));
                        }
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void runSet(OpsCommands.Builder builder) {

        try {
            final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
                    ServiceManager.getService(Context.APP_OPS_SERVICE));
            final int uid = Helper.getPackageUid(builder.getPackageName(), 0);


            appOpsService.setMode(builder.getOpInt(), uid, builder.getPackageName(), builder.getModeInt());
            server.sendResult(ParcelableUtil.marshall(new OpsResult(null, null)));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                server.sendResult(ParcelableUtil.marshall(new OpsResult(null, e)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void runOther(OpsCommands.Builder builder){


    }

    private void runReset(OpsCommands.Builder builder) {
        try {
            final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
                    ServiceManager.getService(Context.APP_OPS_SERVICE));
            final int uid =  Helper.getPackageUid(builder.getPackageName(), 0);

            appOpsService.resetAllModes(uid,builder.getPackageName());
            server.sendResult(ParcelableUtil.marshall(new OpsResult(null, null)));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                server.sendResult(ParcelableUtil.marshall(new OpsResult(null, e)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(byte[] bytes) {
        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(MSG_TIMEOUT);

        if (!isDeath) {
            if (!allowBg) {
                handler.sendEmptyMessageDelayed(MSG_TIMEOUT, BG_TIME_OUT);
            }

            OpsCommands.Builder unmarshall = ParcelableUtil.unmarshall(bytes, OpsCommands.Builder.CREATOR);
            System.out.println("onMessage ---> !!!! " + unmarshall);

            log("onMessage ---> !!!! " + unmarshall);
            handleCommand(unmarshall);
        }
    }



}
