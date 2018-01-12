package com.zzzmode.appopsx.server;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import com.zzzmode.appopsx.common.BaseCaller;
import com.zzzmode.appopsx.common.CallerMethod;
import com.zzzmode.appopsx.common.CallerResult;
import com.zzzmode.appopsx.common.ClassCaller;
import com.zzzmode.appopsx.common.ClassCallerProcessor;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.MethodUtils;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import com.zzzmode.appopsx.common.ParcelableUtil;
import com.zzzmode.appopsx.common.ServerRunInfo;
import com.zzzmode.appopsx.common.SystemServiceCaller;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by zl on 2017/7/27.
 */

class RemoteHandler implements OpsDataTransfer.OnRecvCallback {

  private static final int MSG_TIMEOUT = 1;
  private static final int DEFAULT_TIME_OUT_TIME = 1000 * 60 * 1; //1min
  private static final int BG_TIME_OUT = DEFAULT_TIME_OUT_TIME * 10; //10min

  private OpsXServer server;
  private Handler handler;
  private volatile boolean isDeath = false;
  private int timeOut = DEFAULT_TIME_OUT_TIME;
  private volatile boolean allowBg = false;


  RemoteHandler(Map<String, String> params) throws IOException {
    System.out.println("params --> " + params);
    boolean isRoot = TextUtils.equals(params.get("type"), "root");
    String path = params.get("path");
    String token = params.get("token");
    boolean allowBg = TextUtils.equals(params.get("bgrun"), "1");
    boolean debug = TextUtils.equals(params.get("debug"), "1");

    server = new OpsXServer(path, token, this);
    server.allowBackgroundRun = this.allowBg = allowBg;


    if (!allowBg) {
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
      handler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOut);
    }
  }


  void start() throws Exception {
    server.run();
  }

  void destory() {
    try {
      if (!allowBg && handler != null) {
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendOpResult(Parcelable result) {
    try {
      server.sendResult(ParcelableUtil.marshall(result));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }




  @Override
  public void onMessage(byte[] bytes) {
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler.removeMessages(MSG_TIMEOUT);
    }

    if (!isDeath) {
      if (!allowBg && handler != null) {
        handler.sendEmptyMessageDelayed(MSG_TIMEOUT, BG_TIME_OUT);
      }


      LifecycleAgent.serverRunInfo.recvBytes += bytes.length;

      CallerResult result = null;
      try {
        BaseCaller baseCaller = ParcelableUtil.unmarshall(bytes, BaseCaller.CREATOR);

        int type = baseCaller.getType();

        if(type == BaseCaller.TYPE_CLOSE){
          destory();
          return;
        }

        if(type == BaseCaller.TYPE_SYSTEM_SERVICE){
          SystemServiceCaller callerMethod = ParcelableUtil.unmarshall(baseCaller.getRawBytes(),SystemServiceCaller.CREATOR);
          callerMethod.unwrapParams();
          result = callServiceMethod(callerMethod);
        }else if(type == BaseCaller.TYPE_CLASS){
          ClassCaller callerMethod = ParcelableUtil.unmarshall(baseCaller.getRawBytes(),ClassCaller.CREATOR);
          callerMethod.unwrapParams();

          result = callClass(callerMethod);
        }


        LifecycleAgent.serverRunInfo.successCount++;
      }catch (Throwable e){
        FLog.log(e);
        result = new CallerResult();
        result.setThrowable(e);

        LifecycleAgent.serverRunInfo.errorCount++;

      }finally {
        if (result == null) {
          result = new CallerResult();
        }
        sendOpResult(result);
      }
    }
  }


  private static class FindValue {
    private Object receiver;
    private Method method;

    void put(Object receiver, Method method) {
      this.method = method;
      this.receiver = receiver;
    }

    boolean founded() {
      return method != null && receiver != null;
    }

    void recycle() {
      receiver = null;
      method = null;
    }
  }

  private static final FindValue sFindValue = new FindValue();


  private void findFromService(SystemServiceCaller caller) {
    try {
      IBinder service = ServiceManager.getService(caller.getServiceName());
      String aidl = service.getInterfaceDescriptor();

      Class aClass = sClassCache.get(aidl);
      if (aClass == null) {
        aClass = Class.forName(aidl + "$Stub", false, null);
        sClassCache.put(aidl, aClass);
      }
      Object asInterface = MethodUtils.invokeStaticMethod(aClass, "asInterface", new Object[]{service}, new Class[]{IBinder.class});
      Method method = MethodUtils.getAccessibleMethod(aClass, caller.getMethodName(), caller.getParamsType());
      if (method != null && asInterface != null) {
        sFindValue.recycle();
        sFindValue.put(asInterface, method);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
    }
  }

  private CallerResult callServiceMethod(SystemServiceCaller caller) {
    CallerResult callerResult = new CallerResult();
    try {
      sFindValue.recycle();
      findFromService(caller);

      if (sFindValue.founded()) {
        callMethod(sFindValue.receiver, sFindValue.method, caller, callerResult);
        sFindValue.recycle();
      } else {
        throw new NoSuchMethodException("not found service " + caller.getServiceName() + "  method " + caller.getMethodName() + " params: " + Arrays
            .toString(caller.getParamsType()));
      }
    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
      if(callerResult.getThrowable() != null) {
        callerResult.setThrowable(e);
      }
    }
    return callerResult;
  }

  private void callMethod(Object obj, Method method, CallerMethod caller, CallerResult result) {
    try {

      result.setReturnType(method.getReturnType());
      Object ret = method.invoke(obj, caller.getParams());
      writeResult(result, ret);
    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log("callMethod --> "+Log.getStackTraceString(e));
      result.setThrowable(e);
    }
  }

  private static final LruCache<String, Class> sClassCache = new LruCache<>(16);
  private static final LruCache<String, Constructor> sConstructorCache = new LruCache<>(16);
  private static final LruCache<String, WeakReference<Context>> sLocalContext = new LruCache<>(16);

  private CallerResult callClass(ClassCaller caller){
    CallerResult result = new CallerResult();
    try {
      ActivityThread activityThread = ActivityThread.currentActivityThread();
      Context context = activityThread.getSystemContext();
      Context packageContext = null;

      //create or from cache get context
      WeakReference<Context> contextWeakReference = sLocalContext.get(caller.getPackageName());
      if (contextWeakReference != null && contextWeakReference.get() != null) {
        packageContext = contextWeakReference.get();
      }
      if (packageContext == null) {
        packageContext = context.createPackageContext(caller.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        sLocalContext.put(caller.getPackageName(), new WeakReference<Context>(packageContext));
      }

      //load class
      Class<?> aClass = sClassCache.get(caller.getClassName());
      Constructor<?> aConstructor = sConstructorCache.get(caller.getClassName());
      if (aClass == null || aConstructor == null) {

        aClass = Class.forName(caller.getClassName(), false, packageContext.getClassLoader());
        Class<?> processer=Class.forName(ClassCallerProcessor.class.getName(),false,packageContext.getClassLoader());

        if (processer.isAssignableFrom(aClass)) {
          sClassCache.put(caller.getClassName(), aClass);
          sConstructorCache.put(caller.getClassName(),aClass.getConstructor(Context.class,Context.class,byte[].class));
        }else {
          throw new ClassCastException("class "+aClass.getName()+"  need extends ClassCallerProcessor !");
        }
      }

      //if found class,invoke proxyInvoke method
      if (aClass != null) {

        Object o = null;
        if(aConstructor != null){

          o = aConstructor.newInstance(packageContext,context,ParcelableUtil.marshall(LifecycleAgent.serverRunInfo));
        }

        Object[] params = caller.getParams();
        if(params != null){
          for (Object param : params) {
            if(param instanceof Bundle){
              ((Bundle) param).setClassLoader(packageContext.getClassLoader());
            }
          }
        }

        FLog.log("------new object "+o+"  params "+Arrays.toString(params)+"    "+aClass);

        Object ret = MethodUtils.invokeExactMethod(o, "proxyInvoke", params,new Class[]{Bundle.class});
        if (ret != null && ret instanceof Bundle) {
          writeResult(result, ret);
        } else {
          writeResult(result, Bundle.EMPTY);
        }

      } else {
        throw new ClassNotFoundException("not found class " + caller.getClassName() + "  in package: " + caller.getPackageName());
      }

    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
      result.setThrowable(e);
    }

    return result;
  }

  private  void writeResult(CallerResult result, Object object) {
    Parcel parcel = Parcel.obtain();


    if(object instanceof ParceledListSlice){
      parcel.writeValue(((ParceledListSlice) object).getList());
    }else {
      parcel.writeValue(object);
    }
    result.setReply(parcel.marshall());

    parcel.recycle();
  }

}
