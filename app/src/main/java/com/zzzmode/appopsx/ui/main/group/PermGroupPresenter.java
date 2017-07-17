package com.zzzmode.appopsx.ui.main.group;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.AppPermissions;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zl on 2017/7/17.
 */

class PermGroupPresenter {

  private static final String TAG = "PermGroupPresenter";
  private IPermGroupView mView;

  private Context context;

  private Single<List<PermissionGroup>> permissionGroup;

  PermGroupPresenter(IPermGroupView mView, Context context) {
    this.mView = mView;
    this.context = context;
  }

  ResourceSingleObserver<List<PermissionGroup>> subscriber;
  void loadPerms() {
    boolean showSysApp = PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean("show_sysapp", false);

    final AtomicInteger atomicSize = new AtomicInteger();
    final AtomicInteger atomicProgress = new AtomicInteger();

    subscriber = new ResourceSingleObserver<List<PermissionGroup>>() {
      @Override
      public void onSuccess(List<PermissionGroup> value) {

        mView.showList(value);
      }

      @Override
      public void onError(Throwable e) {
        mView.showError(e);
      }

    };

    Helper
        .getPermissionGroup(context, showSysApp, new Consumer<List<AppInfo>>() {
          @Override
          public void accept(@NonNull List<AppInfo> appInfos) throws Exception {
            if (appInfos != null) {
              atomicSize.set(appInfos.size());
            }
          }
        }, new Consumer<AppPermissions>() {
          @Override
          public void accept(@NonNull AppPermissions appPermissions) throws Exception {
            int p = atomicProgress.incrementAndGet();
            mView.loading(atomicSize.get(), p, appPermissions.appInfo.appName);

          }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subscriber);

  }


  void changeMode(final int groupPosition, final int childPosition,
      final PermissionChildItem info) {

    info.opEntryInfo.changeStatus();

    Helper.setMode(context, info.appInfo.packageName, info.opEntryInfo)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new ResourceObserver<OpsResult>() {
          @Override
          public void onNext(OpsResult value) {

            mView.changeTitle(groupPosition, childPosition, info.opEntryInfo.isAllowed());
          }

          @Override
          public void onError(Throwable e) {
            try {
              info.opEntryInfo.changeStatus();
              mView.refreshItem(groupPosition, childPosition);
            } catch (Exception e2) {
              e2.printStackTrace();
            }
          }

          @Override
          public void onComplete() {

          }
        });
  }


  void destroy() {
    if(subscriber != null && !subscriber.isDisposed()) {
      subscriber.dispose();
    }
  }
}
