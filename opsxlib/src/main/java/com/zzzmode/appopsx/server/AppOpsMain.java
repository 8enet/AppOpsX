package com.zzzmode.appopsx.server;


import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.system.Os;
import android.util.Log;

import com.android.internal.app.IAppOpsService;
import com.zzzmode.appopsx.common.OpsCommands;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ParcelableUtil;
import com.zzzmode.appopsx.common.ReflectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AppOpsMain implements OpsDataTransfer.OnRecvCallback{

    private static final int MSG_TIMEOUT=1;
    private static final int DEFAULT_TIME_OUT_TIME=1000*60*1; //1min

    public static void main(String[] args){

        try {
            new AppOpsMain(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private OpsXServer server;
    private Handler handler;
    private volatile boolean isDeath=false;
    private final int timeOut=DEFAULT_TIME_OUT_TIME;

    private AppOpsMain(String[] args) throws IOException {
        server = new OpsXServer("com.zzzmode.appopsx",this);

        try {

            HandlerThread thread1=new HandlerThread("watcher-ups");
            thread1.start();
            handler=new Handler(thread1.getLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what){
                        case MSG_TIMEOUT:
                            try {
                                isDeath=true;
                                server.setStop();
                                System.out.println("timeout stop-----");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                }
            };

            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("end ---- ");
    }


    private void handleCommand(OpsCommands.Builder builder){
        String s = builder.getAction();
        if(OpsCommands.ACTION_GET.equals(s)){
                runGet(builder);
        }else if(OpsCommands.ACTION_SET.equals(s)){
                runSet(builder);
        }
    }

    private void runGet(OpsCommands.Builder  getBuilder){
        final IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(
                ServiceManager.getService(Context.APP_OPS_SERVICE));
        try {
            String packageName=getBuilder.getPackageName();

            final int uid = ActivityThread.getPackageManager().getPackageUid(packageName,0);
            System.out.println("uid "+uid);

            List opsForPackage = appOpsService.getOpsForPackage(uid, packageName, null);

            List<PackageOps> packageOpses=new ArrayList<>();
            for (Object o : opsForPackage) {
                packageOpses.add(ReflectUtils.opsConvert(o));
            }

            System.out.println(packageOpses);
            //server.sendResult(OpsCommands.toGetRestlt(packageOpses));
            server.sendResult(ParcelableUtil.marshall(new OpsResult(packageOpses,null)));
        } catch (Exception e) {
            e.printStackTrace();

            try {
                server.sendResult(ParcelableUtil.marshall(new OpsResult(null,e)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void runSet(OpsCommands.Builder setBuilder){

    }




    private static Object printField(Object obj,String fieldName){
        Object object=null;
        try {
            Field f=obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            object=f.get(obj);
            System.out.println(fieldName+":"+object+"   "+f.getType().getName());
            if("java.util.List".equals(f.getType().getName())){
                //list
                List list= (List) f.get(obj);
                for (Object o : list) {
                    System.out.println(invokStatic(AppOpsManager.class,"opToPermission",new Class[]{int.class},printField(o,"mOp")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private static Object invokStatic(Class cls,String methodName,Class[] parameterTypes,Object... args){
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName,parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null,args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getStatic(Class cls,String fieldName){
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void aaa(){
        java.lang.Process id=null;
        try {
            id = Runtime.getRuntime().exec("id");
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(id.getInputStream()));
            String line=null;
            while ( (line=bufferedReader.readLine()) != null){
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(id != null){
                id.destroy();
            }
        }
    }

    @Override
    public void onMessage(byte[] bytes) {
        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(MSG_TIMEOUT);
        if(!isDeath) {
            handler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOut);

            OpsCommands.Builder unmarshall = ParcelableUtil.unmarshall(bytes, OpsCommands.Builder.CREATOR);
            System.out.println("onMessage --->  " + unmarshall);
            handleCommand(unmarshall);
        }
    }
}
