package com.zzzmode.appopsx.ui.main.backup;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.PreAppInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by zl on 2017/5/7.
 */

class ConfigPresenter {

  private static final String TAG = "ConfigPresenter";

  private IConfigView mView;

  private Context context;

  ConfigPresenter(Context context, IConfigView view) {
    this.mView = view;
    this.context = context;
  }


  void export(AppInfo[] appInfos) {
    final int max = appInfos.length;

    final AtomicInteger progress = new AtomicInteger();
    mView.showProgress(true, max);
    Helper.getAppsPermission(context, appInfos)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(new Consumer<PreAppInfo>() {
          @Override
          public void accept(@NonNull PreAppInfo appInfo) throws Exception {
            mView.setProgress(progress.incrementAndGet());
          }
        })
        .collect(new Callable<List<PreAppInfo>>() {
          @Override
          public List<PreAppInfo> call() throws Exception {
            return new ArrayList<PreAppInfo>();
          }
        }, new BiConsumer<List<PreAppInfo>, PreAppInfo>() {
          @Override
          public void accept(List<PreAppInfo> preAppInfos, PreAppInfo appInfo) throws Exception {
            preAppInfos.add(appInfo);
          }
        }).observeOn(Schedulers.io()).doAfterSuccess(new Consumer<List<PreAppInfo>>() {
      @Override
      public void accept(@NonNull List<PreAppInfo> preAppInfos) throws Exception {
        save2Local(preAppInfos);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<PreAppInfo>>() {


      @Override
      public void onSubscribe(@NonNull Disposable d) {

      }

      @Override
      public void onSuccess(@NonNull List<PreAppInfo> preAppInfos) {
        mView.showProgress(false, 0);
      }

      @Override
      public void onError(@NonNull Throwable e) {
        mView.showProgress(false, 0);
      }
    });
  }

  private void save2Local(List<PreAppInfo> preAppInfos) {
    //io thread

    final StringBuilder msg = new StringBuilder();
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.putOpt("time", System.currentTimeMillis());
      jsonObject.putOpt("v", 1);
      jsonObject.putOpt("size", preAppInfos.size());
      JSONArray jsonArray = new JSONArray();
      for (PreAppInfo preAppInfo : preAppInfos) {
        String ignoredOps = preAppInfo.getIgnoredOps();
        if (!TextUtils.isEmpty(ignoredOps)) {
          JSONObject object = new JSONObject();
          object.putOpt("pkg", preAppInfo.getPackageName());
          object.putOpt("ops", ignoredOps);
          jsonArray.put(object);
        }
      }
      jsonObject.putOpt("opbacks", jsonArray);
      File file = BFileUtils.saveBackup(context, jsonObject.toString());
      msg.append(context.getString(R.string.backup_success, file.getAbsoluteFile()));
    } catch (Exception e) {
      e.printStackTrace();
      msg.append("error").append(e.getMessage());
    }
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (context != null) {
          Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  List<RestoreModel> getRestoreFiles() {
    List<File> backFiles = BFileUtils.getBackFiles(context);
    if (backFiles != null && !backFiles.isEmpty()) {
      List<RestoreModel> models = new ArrayList<>();
      for (File backFile : backFiles) {
        RestoreModel model = readModel(backFile);
        if (model != null) {
          models.add(model);
        }
      }

      Collections.sort(models, new Comparator<RestoreModel>() {
        @Override
        public int compare(RestoreModel o1, RestoreModel o2) {
          return (o1.createTime < o2.createTime) ? 1 : ((o1.createTime == o2.createTime) ? 0 : -1);
        }
      });

      return models;
    }
    return null;
  }

  void importBack(File file) {
    String s = BFileUtils.read2String(file);
    try {
      JSONObject jsonObject = new JSONObject(s);
      long time = jsonObject.optLong("time");
      int v = jsonObject.optInt("v");
      int size = jsonObject.optInt("size");
      JSONArray jsonArray = jsonObject.optJSONArray("opbacks");
      if (jsonArray != null && jsonArray.length() > 0) {
        int len = jsonArray.length();
        List<PreAppInfo> preAppInfos = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
          JSONObject jo = jsonArray.optJSONObject(i);
          if (jo != null) {
            String pkg = jo.optString("pkg");
            String ops = jo.optString("ops");
            if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(ops)) {
              preAppInfos.add(new PreAppInfo(pkg, ops));
            }
          }
        }
        //restoreOps(preAppInfos);
      }
    } catch (Exception e) {
      Toast.makeText(context, R.string.backup_file_lack, Toast.LENGTH_LONG).show();
    }
  }


  private RestoreModel readModel(File file) {
    try {
      RestoreModel model = new RestoreModel();
      model.path = file.getAbsolutePath();
      model.fileName = file.getName();
      model.fileSize = file.length();

      String s = BFileUtils.read2String(file);
      JSONObject jsonObject = new JSONObject(s);
      model.createTime = jsonObject.optLong("time");
      model.version = jsonObject.optInt("v");
      model.size = jsonObject.optInt("size");
      JSONArray jsonArray = jsonObject.optJSONArray("opbacks");
      if (jsonArray != null && jsonArray.length() > 0) {
        int len = jsonArray.length();
        List<PreAppInfo> preAppInfos = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
          JSONObject jo = jsonArray.optJSONObject(i);
          if (jo != null) {
            String pkg = jo.optString("pkg");
            String ops = jo.optString("ops");
            if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(ops)) {
              preAppInfos.add(new PreAppInfo(pkg, ops));
            }
          }
        }
        model.preAppInfos = preAppInfos;

        return model;
      }
    } catch (Exception e) {
      if (file != null) {
        file.delete();
      }
    }
    return null;
  }

  void restoreOps(final RestoreModel model) {
    final int size = model.preAppInfos.size();
    final AtomicInteger progress = new AtomicInteger();
    mView.showProgress(true, size);

    Observable.fromIterable(model.preAppInfos)
        .flatMap(new Function<PreAppInfo, ObservableSource<OpsResult>>() {
          @Override
          public ObservableSource<OpsResult> apply(@NonNull PreAppInfo appInfo) throws Exception {
            return Helper.setModes(context, appInfo.getPackageName(), AppOpsManager.MODE_IGNORED,
                appInfo.getOps());
          }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceObserver<OpsResult>() {
      @Override
      public void onNext(@NonNull OpsResult opsResult) {
        mView.setProgress(progress.incrementAndGet());
      }

      @Override
      public void onError(@NonNull Throwable e) {
        progress.incrementAndGet();
      }

      @Override
      public void onComplete() {
        mView.showProgress(false, 0);
        Toast.makeText(context, "恢复成功", Toast.LENGTH_LONG).show();
      }
    });
  }


}
