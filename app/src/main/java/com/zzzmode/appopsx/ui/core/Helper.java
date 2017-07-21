package com.zzzmode.appopsx.ui.core;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.text.BidiFormatter;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.zzzmode.appopsx.BuildConfig;
import com.zzzmode.appopsx.OpsxManager;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.OtherOp;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ReflectUtils;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.AppPermissions;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import com.zzzmode.appopsx.ui.model.PreAppInfo;
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.operators.single.SingleJust;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/17.
 */

public class Helper {

  public static final class permission_group {

    public static final String AUDIO = "com.zzzmode.appopsx.permission-group.AUDIO";
    public static final String DEVICE = "com.zzzmode.appopsx.permission-group.DEVICE";
    public static final String OTHER = "com.zzzmode.appopsx.permission-group.OTHER";
    public static final String GPS = "com.zzzmode.appopsx.permission-group.GPS";
    public static final String CALLPHONE = "com.zzzmode.appopsx.permission-group.CALLPHONE";
    public static final String VIBRATION = "com.zzzmode.appopsx.permission-group.VIBRATION";
    public static final String NET = "com.zzzmode.appopsx.permission-group.NET";
    public static final String WIFI = "com.zzzmode.appopsx.permission-group.WIFI";
    public static final String NFC = "com.zzzmode.appopsx.permission-group.NFC";
    public static final String SETTINGS = "com.zzzmode.appopsx.permission-group.SETTINGS";
    public static final String NOTIFICATIONS = "com.zzzmode.appopsx.permission-group.NOTIFICATIONS";
    public static final String CLIPBOARD = "com.zzzmode.appopsx.permission-group.CLIPBOARD";
    public static final String FINGERPRINT = "com.zzzmode.appopsx.permission-group.FINGERPRINT";

  }

  private static final String[] RE_SORT_GROUPS =
      {
          permission_group.GPS,
          Manifest.permission_group.LOCATION,
          Manifest.permission_group.CALENDAR,
          Manifest.permission_group.SMS,
          Manifest.permission_group.CONTACTS,
          Manifest.permission_group.CAMERA,
          permission_group.CALLPHONE,
          Manifest.permission_group.PHONE,
          Manifest.permission_group.STORAGE,
          Manifest.permission_group.SENSORS,
          permission_group.FINGERPRINT,
          Manifest.permission_group.MICROPHONE,
          permission_group.VIBRATION,
          permission_group.NET,
          permission_group.WIFI,
          permission_group.NFC,
          permission_group.SETTINGS,
          permission_group.NOTIFICATIONS,
          permission_group.CLIPBOARD,
          permission_group.AUDIO,
          permission_group.DEVICE,
          permission_group.OTHER,

      };

  private static class PermGroupInfo {

    String title;
    String group;
    int icon;

    @Override
    public String toString() {
      return "PermGroupInfo{" +
          "title='" + title + '\'' +
          ", group='" + group + '\'' +
          '}';
    }

    public PermGroupInfo(String title, String group, int icon) {
      this.title = title;
      this.group = group;
      this.icon = icon;
    }
  }

  private static final SparseIntArray NO_PERM_OP = new SparseIntArray();
  private static final Map<String, String> FAKE_PERMS_GROUP = new HashMap<>();

  private static final Map<String, PermGroupInfo> PERMS_GROUPS = new HashMap<>();

  private static final PermGroupInfo OTHER_PERM_INFO = new PermGroupInfo(null,
      permission_group.OTHER, R.drawable.perm_group_other);

  static {
    int[] ops = {2, 11, 12, 15, 22, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41, 42, 44, 45,
        46, 47, 48, 49, 50, 58, 61, 63, 65, 69};
    for (int op : ops) {
      NO_PERM_OP.put(op, op);
    }

    FAKE_PERMS_GROUP.put("COARSE_LOCATION", Manifest.permission_group.LOCATION);
    FAKE_PERMS_GROUP.put("FINE_LOCATION", permission_group.GPS);
    FAKE_PERMS_GROUP.put("GPS", permission_group.GPS);
    FAKE_PERMS_GROUP.put("NEIGHBORING_CELLS", Manifest.permission_group.LOCATION);
    FAKE_PERMS_GROUP.put("MONITOR_LOCATION", Manifest.permission_group.LOCATION);
    FAKE_PERMS_GROUP.put("MONITOR_HIGH_POWER_LOCATION", Manifest.permission_group.LOCATION);

    FAKE_PERMS_GROUP.put("READ_CALL_LOG", permission_group.CALLPHONE);
    FAKE_PERMS_GROUP.put("WRITE_CALL_LOG", permission_group.CALLPHONE);
    FAKE_PERMS_GROUP.put("CALL_PHONE", permission_group.CALLPHONE);
    FAKE_PERMS_GROUP.put("PROCESS_OUTGOING_CALLS", permission_group.CALLPHONE);

    FAKE_PERMS_GROUP.put("READ_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("WRITE_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("RECEIVE_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("RECEIVE_EMERGECY_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("RECEIVE_MMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("RECEIVE_WAP_PUSH", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("SEND_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("READ_ICC_SMS", Manifest.permission_group.SMS);
    FAKE_PERMS_GROUP.put("WRITE_ICC_SMS", Manifest.permission_group.SMS);

    FAKE_PERMS_GROUP.put("PLAY_AUDIO", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("TAKE_MEDIA_BUTTONS", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("TAKE_AUDIO_FOCUS", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_MASTER_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_VOICE_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_RING_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_MEDIA_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_ALARM_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_NOTIFICATION_VOLUME", permission_group.AUDIO);
    FAKE_PERMS_GROUP.put("AUDIO_BLUETOOTH_VOLUME", permission_group.AUDIO);

    FAKE_PERMS_GROUP.put("VIBRATE", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("MUTE_MICROPHONE", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("TOAST_WINDOW", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("PROJECT_MEDIA", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("ACTIVATE_VPN", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("WRITE_WALLPAPER", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("ASSIST_STRUCTURE", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("ASSIST_SCREENSHOT", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("MOCK_LOCATION", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("TURN_ON_SCREEN", permission_group.DEVICE);
    FAKE_PERMS_GROUP.put("RUN_IN_BACKGROUND", permission_group.DEVICE);

    FAKE_PERMS_GROUP.put("ACCESS_PHONE_DATA", permission_group.NET);
    FAKE_PERMS_GROUP.put("ACCESS_WIFI_NETWORK", permission_group.NET);

    FAKE_PERMS_GROUP.put("VIBRATE", permission_group.VIBRATION);

    FAKE_PERMS_GROUP.put("WIFI_SCAN", permission_group.WIFI);
    FAKE_PERMS_GROUP.put("WIFI_CHANGE", permission_group.WIFI);

    FAKE_PERMS_GROUP.put("NFC_CHANGE", permission_group.NFC);

    FAKE_PERMS_GROUP.put("WRITE_SETTINGS", permission_group.SETTINGS);

    FAKE_PERMS_GROUP.put("ACCESS_NOTIFICATIONS", permission_group.NOTIFICATIONS);
    FAKE_PERMS_GROUP.put("POST_NOTIFICATION", permission_group.NOTIFICATIONS);

    FAKE_PERMS_GROUP.put("READ_CLIPBOARD", permission_group.CLIPBOARD);
    FAKE_PERMS_GROUP.put("WRITE_CLIPBOARD", permission_group.CLIPBOARD);

    FAKE_PERMS_GROUP.put("USE_FINGERPRINT", permission_group.FINGERPRINT);

    PERMS_GROUPS.put(Manifest.permission_group.CALENDAR,
        new PermGroupInfo(null, Manifest.permission_group.CALENDAR,
            R.drawable.perm_group_calendar));
    PERMS_GROUPS.put(Manifest.permission_group.CAMERA,
        new PermGroupInfo(null, Manifest.permission_group.CAMERA, R.drawable.perm_group_camera));
    PERMS_GROUPS.put(Manifest.permission_group.CONTACTS,
        new PermGroupInfo(null, Manifest.permission_group.CONTACTS,
            R.drawable.perm_group_contacts));
    PERMS_GROUPS.put(Manifest.permission_group.LOCATION,
        new PermGroupInfo(null, Manifest.permission_group.LOCATION,
            R.drawable.perm_group_location));
    PERMS_GROUPS.put(Manifest.permission_group.MICROPHONE,
        new PermGroupInfo(null, Manifest.permission_group.MICROPHONE,
            R.drawable.perm_group_microphone));
    PERMS_GROUPS.put(Manifest.permission_group.PHONE,
        new PermGroupInfo(null, Manifest.permission_group.PHONE, R.drawable.ic_perm_device_info));
    PERMS_GROUPS.put(Manifest.permission_group.SENSORS,
        new PermGroupInfo(null, Manifest.permission_group.SENSORS, R.drawable.perm_group_sensors));
    PERMS_GROUPS.put(Manifest.permission_group.SMS,
        new PermGroupInfo(null, Manifest.permission_group.SMS, R.drawable.perm_group_sms));
    PERMS_GROUPS.put(Manifest.permission_group.STORAGE,
        new PermGroupInfo(null, Manifest.permission_group.STORAGE, R.drawable.perm_group_storage));

    PERMS_GROUPS.put(permission_group.AUDIO,
        new PermGroupInfo(null, permission_group.AUDIO, R.drawable.perm_group_audio));
    PERMS_GROUPS.put(permission_group.DEVICE,
        new PermGroupInfo(null, permission_group.DEVICE, R.drawable.perm_group_device));
    PERMS_GROUPS.put(permission_group.OTHER,
        new PermGroupInfo(null, permission_group.OTHER, R.drawable.perm_group_other));
    PERMS_GROUPS.put(permission_group.GPS,
        new PermGroupInfo(null, permission_group.GPS, R.drawable.perm_group_gps));
    PERMS_GROUPS.put(permission_group.CALLPHONE,
        new PermGroupInfo(null, permission_group.CALLPHONE, R.drawable.perm_group_callphone));
    PERMS_GROUPS.put(permission_group.VIBRATION,
        new PermGroupInfo(null, permission_group.VIBRATION, R.drawable.perm_group_vibration));
    PERMS_GROUPS.put(permission_group.NET,
        new PermGroupInfo(null, permission_group.NET, R.drawable.perm_group_net));
    PERMS_GROUPS.put(permission_group.WIFI,
        new PermGroupInfo(null, permission_group.WIFI, R.drawable.perm_group_wifi));
    PERMS_GROUPS.put(permission_group.NFC,
        new PermGroupInfo(null, permission_group.NFC, R.drawable.perm_group_nfc));
    PERMS_GROUPS.put(permission_group.SETTINGS,
        new PermGroupInfo(null, permission_group.SETTINGS, R.drawable.perm_group_settings));
    PERMS_GROUPS.put(permission_group.NOTIFICATIONS,
        new PermGroupInfo(null, permission_group.NOTIFICATIONS,
            R.drawable.perm_group_notifications));
    PERMS_GROUPS.put(permission_group.CLIPBOARD,
        new PermGroupInfo(null, permission_group.CLIPBOARD, R.drawable.perm_group_clipboard));

    PERMS_GROUPS.put(permission_group.FINGERPRINT,
        new PermGroupInfo(null, permission_group.FINGERPRINT, R.drawable.perm_group_fingerprint));
  }

  private static final String TAG = "Helper";

  private static final Map<String, Integer> sPermI18N = new HashMap<String, Integer>() {{
    put("POST_NOTIFICATION", R.string.permlab_POST_NOTIFICATION);
    put("READ_CLIPBOARD", R.string.permlab_READ_CLIPBOARD);
    put("WRITE_CLIPBOARD", R.string.permlab_WRITE_CLIPBOARD);
    put("TURN_ON_SCREEN", R.string.permlab_TURN_ON_SCREEN);
    put("RUN_IN_BACKGROUND", R.string.permlab_RUN_IN_BACKGROUND);
    put("MONITOR_LOCATION", R.string.permlab_MONITOR_LOCATION);
    put("MONITOR_HIGH_POWER_LOCATION", R.string.permlab_MONITOR_HIGH_POWER_LOCATION);
    put("NEIGHBORING_CELLS", R.string.permlab_NEIGHBORING_CELLS);
    put("PLAY_AUDIO", R.string.permlab_PLAY_AUDIO);
    put("AUDIO_MASTER_VOLUME", R.string.permlab_AUDIO_MASTER_VOLUME);
    put("AUDIO_VOICE_VOLUME", R.string.permlab_AUDIO_VOICE_VOLUME);
    put("AUDIO_RING_VOLUME", R.string.permlab_AUDIO_RING_VOLUME);
    put("AUDIO_MEDIA_VOLUME", R.string.permlab_AUDIO_MEDIA_VOLUME);
    put("AUDIO_ALARM_VOLUME", R.string.permlab_AUDIO_ALARM_VOLUME);
    put("AUDIO_NOTIFICATION_VOLUME", R.string.permlab_AUDIO_NOTIFICATION_VOLUME);
    put("AUDIO_BLUETOOTH_VOLUME", R.string.permlab_AUDIO_BLUETOOTH_VOLUME);
    put("TOAST_WINDOW", R.string.permlab_TOAST_WINDOW);
    put("ACTIVATE_VPN", R.string.permlab_ACTIVATE_VPN);
    put("TAKE_AUDIO_FOCUS", R.string.permlab_TAKE_AUDIO_FOCUS);
    put("ACCESS_PHONE_DATA", R.string.permlab_ACCESS_MOBLIE_NETWORK_DATA);
    put("ACCESS_WIFI_NETWORK", R.string.permlab_ACCESS_WIFI_NETWORK_DATA);

  }};


  public static void updataShortcuts(final Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
      getInstalledApps(context, false)
          .concatMap(new Function<List<AppInfo>, ObservableSource<AppInfo>>() {
            @Override
            public ObservableSource<AppInfo> apply(List<AppInfo> appInfos) throws Exception {
              return Observable.fromIterable(appInfos);
            }
          }).filter(new Predicate<AppInfo>() {
        @Override
        public boolean test(AppInfo info) throws Exception {
          return !BuildConfig.APPLICATION_ID.equals(info.packageName);
        }
      }).collect(new Callable<List<AppInfo>>() {
        @Override
        public List<AppInfo> call() throws Exception {
          return new ArrayList<AppInfo>();
        }
      }, new BiConsumer<List<AppInfo>, AppInfo>() {
        @Override
        public void accept(List<AppInfo> appInfos, AppInfo info) throws Exception {
          appInfos.add(info);
        }
      }).map(new Function<List<AppInfo>, List<AppInfo>>() {
        @Override
        public List<AppInfo> apply(List<AppInfo> appInfos) throws Exception {
          Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
              return o1.time > o2.time ? -1 : 1;
            }
          });
          return appInfos;
        }
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
          .subscribe(new ResourceSingleObserver<List<AppInfo>>() {
            @Override
            public void onSuccess(List<AppInfo> value) {
              try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                  updataShortcuts(context, value);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onError(Throwable e) {

            }
          });

    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N_MR1)
  private static void updataShortcuts(Context context, List<AppInfo> items) {
    ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
    List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
    int max = shortcutManager.getMaxShortcutCountPerActivity();
    for (int i = 0; i < max && i < items.size(); i++) {
      AppInfo appInfo = items.get(i);
      ShortcutInfo.Builder shortcut = new ShortcutInfo.Builder(context, appInfo.packageName);
      shortcut.setShortLabel(appInfo.appName);
      shortcut.setLongLabel(appInfo.appName);

      shortcut.setIcon(
          Icon.createWithBitmap(drawableToBitmap(LocalImageLoader.getDrawable(context, appInfo))));

      Intent intent = new Intent(context, AppPermissionActivity.class);
      intent.putExtra(AppPermissionActivity.EXTRA_APP_PKGNAME, appInfo.packageName);
      intent.putExtra(AppPermissionActivity.EXTRA_APP_NAME, appInfo.appName);
      intent.setAction(Intent.ACTION_DEFAULT);
      shortcut.setIntent(intent);

      shortcutInfoList.add(shortcut.build());
    }
    shortcutManager.setDynamicShortcuts(shortcutInfoList);
  }

  private static Bitmap drawableToBitmap(Drawable drawable) {
    Bitmap bitmap = null;

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if (bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1,
          Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
          Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }


  public static Single<AppInfo> getAppInfo(final Context context, String pkgName) {
    return SingleJust.just(pkgName).map(new Function<String, AppInfo>() {
      @Override
      public AppInfo apply(String s) throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(s, 0);

        AppInfo info = new AppInfo();
        info.packageName = packageInfo.packageName;
        info.appName = BidiFormatter.getInstance()
            .unicodeWrap(packageInfo.applicationInfo.loadLabel(packageManager)).toString();
        info.time = Math.max(packageInfo.lastUpdateTime, packageInfo.firstInstallTime);
        info.installTime = packageInfo.firstInstallTime;
        info.updateTime = packageInfo.lastUpdateTime;
        info.applicationInfo = packageInfo.applicationInfo;

        LocalImageLoader.initAdd(context, info);
        return info;
      }
    });

  }

  public static Observable<List<AppInfo>> getInstalledApps(final Context context,
      final boolean loadSysapp) {

    return Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
      @Override
      public void subscribe(final ObservableEmitter<List<AppInfo>> e) throws Exception {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        List<AppInfo> zhAppInfos = new ArrayList<AppInfo>();
        List<AppInfo> enAppInfos = new ArrayList<AppInfo>();
        for (PackageInfo installedPackage : installedPackages) {
          if (loadSysapp
              || (installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            AppInfo info = new AppInfo();
            info.packageName = installedPackage.packageName;
            info.appName = BidiFormatter.getInstance()
                .unicodeWrap(installedPackage.applicationInfo.loadLabel(packageManager)).toString();
            info.time = Math
                .max(installedPackage.lastUpdateTime, installedPackage.firstInstallTime);
            info.installTime = installedPackage.firstInstallTime;
            info.updateTime = installedPackage.lastUpdateTime;
            info.applicationInfo = installedPackage.applicationInfo;

            LocalImageLoader.initAdd(context, info);

            final char c = info.appName.charAt(0);
            if (c >= 48 && c <= 122) {
              enAppInfos.add(info);
            } else {
              zhAppInfos.add(info);
            }

          }
        }

        Collections.sort(enAppInfos, new Comparator<AppInfo>() {
          @Override
          public int compare(AppInfo o1, AppInfo o2) {
            return o1.appName.compareTo(o2.appName);
          }
        });

        Collections.sort(zhAppInfos, new Comparator<AppInfo>() {
          @Override
          public int compare(AppInfo o1, AppInfo o2) {
            return o2.appName.compareTo(o1.appName);
          }
        });
        List<AppInfo> ret = new ArrayList<AppInfo>();

        int type = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("pref_app_sort_type", 0);
        if (type == 1) {
          //按名称排序[字母在后]

          ret.addAll(zhAppInfos);
          ret.addAll(enAppInfos);
        } else {
          //按名称排序[字母在前] 默认
          ret.addAll(enAppInfos);
          ret.addAll(zhAppInfos);
        }

        e.onNext(ret);
        e.onComplete();
      }
    });
  }


  public static Observable<PreAppInfo> getAppsPermission(final Context context,
      AppInfo[] appInfos) {
    return Observable.fromArray(appInfos).map(new Function<AppInfo, PreAppInfo>() {
      @Override
      public PreAppInfo apply(@NonNull AppInfo s) throws Exception {
        OpsResult opsForPackage = AppOpsx.getInstance(context).getOpsForPackage(s.packageName);
        if (opsForPackage != null) {
          if (opsForPackage.getException() != null) {
            throw new Exception(opsForPackage.getException());
          }
        } else {
          throw new NullPointerException("getOpsForPackage:" + s + " return null !");
        }
        PreAppInfo appInfo = new PreAppInfo(s.packageName);
        List<PackageOps> opses = opsForPackage.getList();
        if (opses != null) {
          StringBuilder sb = new StringBuilder();
          for (PackageOps opse : opses) {
            List<OpEntry> ops = opse.getOps();

            if (ops != null) {
              for (OpEntry op : ops) {
                if (op.getMode() == AppOpsManager.MODE_IGNORED) {
                  sb.append(op.getOp()).append(',');
                }
              }
            }
          }
          int len = sb.length();
          if (len > 0 && sb.charAt(len - 1) == ',') {
            sb.deleteCharAt(len - 1);
          }
          appInfo.setIgnoredOps(sb.toString());
          return appInfo;
        } else {
          throw new NullPointerException("");
        }
      }
    })
        .retry(5, new Predicate<Throwable>() {
          @Override
          public boolean test(Throwable throwable) throws Exception {
            return throwable instanceof IOException || throwable instanceof NullPointerException;
          }
        })
        .subscribeOn(Schedulers.io());
  }

  public static Observable<List<OpEntryInfo>> getAppPermission(final Context context,
      final String packageName) {
    return getAppPermission(context, packageName, false);
  }


  public static Observable<List<OpEntryInfo>> getAppPermission(final Context context,
      final String packageName, final boolean needNoPermsOp) {
    return Observable.create(new ObservableOnSubscribe<OpsResult>() {
      @Override
      public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

        OpsResult opsForPackage = AppOpsx.getInstance(context).getOpsForPackage(packageName);
        if (opsForPackage != null) {
          if (opsForPackage.getException() == null) {
            e.onNext(opsForPackage);
          } else {
            throw new Exception(opsForPackage.getException());
          }
        }
        e.onComplete();

      }
    })
        .retry(5, new Predicate<Throwable>() {
          @Override
          public boolean test(Throwable throwable) throws Exception {
            return throwable instanceof IOException || throwable instanceof NullPointerException;
          }
        })
        .subscribeOn(Schedulers.io()).map(new Function<OpsResult, List<OpEntryInfo>>() {
          @Override
          public List<OpEntryInfo> apply(OpsResult opsResult) throws Exception {
            List<PackageOps> opses = opsResult.getList();
            if (opses != null) {
              List<OpEntryInfo> list = new ArrayList<OpEntryInfo>();
              PackageManager pm = context.getPackageManager();
              for (PackageOps opse : opses) {
                List<OpEntry> ops = opse.getOps();

                if (ops != null) {
                  SparseIntArray hasOp = new SparseIntArray();
                  for (OpEntry op : ops) {
                    OpEntryInfo opEntryInfo = opEntry2Info(op, context, pm);
                    if (opEntryInfo != null) {
                      hasOp.put(op.getOp(), op.getOp());
                      list.add(opEntryInfo);
                    }
                  }

                  if (needNoPermsOp) {
                    int size = NO_PERM_OP.size();
                    for (int i = 0; i < size; i++) {
                      int opk = NO_PERM_OP.keyAt(i);
                      if (hasOp.indexOfKey(opk) < 0) {
                        OpEntry op = new OpEntry(opk, AppOpsManager.MODE_ALLOWED, 0, 0, 0, 0, null);
                        OpEntryInfo opEntryInfo = opEntry2Info(op, context, pm);
                        if (opEntryInfo != null) {
                          list.add(opEntryInfo);
                        }
                      }
                    }
                  }
                }
              }

              return list;
            }
            return Collections.emptyList();
          }
        }).map(new Function<List<OpEntryInfo>, List<OpEntryInfo>>() {
          @Override
          public List<OpEntryInfo> apply(@NonNull List<OpEntryInfo> opEntryInfos) throws Exception {
            return sortPermsFunction(context, opEntryInfos);
          }
        });
  }


  private static OpEntryInfo opEntry2Info(OpEntry op, Context context, PackageManager pm) {
    OpEntryInfo opEntryInfo = new OpEntryInfo(op);
    if (OtherOp.isOtherOp(op.getOp())) {
      opEntryInfo.opName = OtherOp.getOpName(op.getOp());
      opEntryInfo.opPermsName = OtherOp.getOpPermName(op.getOp());
    }
    if (opEntryInfo.opName != null) {
      try {
        if (!OtherOp.isOtherOp(op.getOp())) {
          PermissionInfo permissionInfo = pm.getPermissionInfo(opEntryInfo.opPermsName, 0);
          opEntryInfo.opPermsLab = String.valueOf(permissionInfo.loadLabel(pm));
          opEntryInfo.opPermsDesc = String.valueOf(permissionInfo.loadDescription(pm));
        }
      } catch (PackageManager.NameNotFoundException e) {
        //ignore
      }

      if (opEntryInfo.opPermsLab == null) {
        Integer resId = sPermI18N.get(opEntryInfo.opName);
        if (resId != null) {
          opEntryInfo.opPermsLab = context.getString(resId);
          opEntryInfo.opPermsDesc = opEntryInfo.opName;
        }
      }

      return opEntryInfo;
    }
    return null;
  }

  public static Single<List<PermissionGroup>> getPermissionGroup(final Context context,
      final boolean loadSysapp,@NonNull final Consumer<List<AppInfo>> doAfterNextApps,final Consumer<AppPermissions> onNext) {
    return getInstalledApps(context, loadSysapp)
        .observeOn(AndroidSchedulers.mainThread())
        .doAfterNext(doAfterNextApps).map(getSortComparator(context))
        .concatMap(new Function<List<AppInfo>, ObservableSource<AppInfo>>() {
          @Override
          public ObservableSource<AppInfo> apply(List<AppInfo> appInfos) throws Exception {
            return Observable.fromIterable(appInfos);
          }
        }).observeOn(Schedulers.io()).map(new Function<AppInfo, AppPermissions>() {
          @Override
          public AppPermissions apply(AppInfo info) throws Exception {
            AppPermissions p = new AppPermissions();
            p.appInfo = info;
            p.opEntries = getAppPermission(context, info.packageName).blockingFirst();
            return p;
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(onNext)
        .observeOn(Schedulers.io())
        .collect(new Callable<Map<String, List<AppPermissions>>>() {
          @Override
          public Map<String, List<AppPermissions>> call() throws Exception {
            return new HashMap<String, List<AppPermissions>>();
          }
        }, new BiConsumer<Map<String, List<AppPermissions>>, AppPermissions>() {
          @Override
          public void accept(Map<String, List<AppPermissions>> map, AppPermissions app)
              throws Exception {
            if (app.opEntries != null && app.hasPermissions()) {
              for (OpEntryInfo opEntry : app.opEntries) {
                if (opEntry.opName != null) {
                  List<AppPermissions> appPermissionses = map.get(opEntry.opName);
                  if (appPermissionses == null) {
                    appPermissionses = new ArrayList<AppPermissions>();
                  }
                  appPermissionses.add(app);
                  map.put(opEntry.opName, appPermissionses);
                }
              }
            }
          }
        })
        .map(new Function<Map<String, List<AppPermissions>>, List<PermissionGroup>>() {
          @Override
          public List<PermissionGroup> apply(Map<String, List<AppPermissions>> map)
              throws Exception {
            List<PermissionGroup> groups = new ArrayList<PermissionGroup>();
            Set<Entry<String, List<AppPermissions>>> entries = map.entrySet();
            for (Entry<String, List<AppPermissions>> entry : entries) {
              PermissionGroup group = new PermissionGroup();
              group.opName = entry.getKey();

              List<AppPermissions> value = entry.getValue();

              group.count = value.size();
              group.apps = new ArrayList<PermissionChildItem>();

              for (AppPermissions appPermissions : value) {
                PermissionChildItem item = new PermissionChildItem();
                item.appInfo = appPermissions.appInfo;

                group.apps.add(item);
                if (appPermissions.opEntries != null) {
                  for (OpEntryInfo opEntry : appPermissions.opEntries) {
                    if (group.opName.equals(opEntry.opName)) {
                      item.opEntryInfo = opEntry;
                      if (opEntry.opEntry.getMode() == AppOpsManager.MODE_ALLOWED) {
                        group.grants += 1;
                      }
                      group.opPermsName = opEntry.opPermsName;
                      group.opPermsDesc = opEntry.opPermsDesc;
                      group.opPermsLab = opEntry.opPermsLab;
                      break;
                    }
                  }
                }


              }


              Collections.sort(group.apps, new Comparator<PermissionChildItem>() {
                @Override
                public int compare(PermissionChildItem o1, PermissionChildItem o2) {
                  return Long.compare(o2.opEntryInfo.opEntry.getTime(),o1.opEntryInfo.opEntry.getTime());
                }
              });

              groups.add(group);
            }

            return groups;
          }
        }).map(new Function<List<PermissionGroup>, List<PermissionGroup>>() {
          @Override
          public List<PermissionGroup> apply(List<PermissionGroup> permissionGroups)
              throws Exception {
            Map<String, List<PermissionGroup>> groups = new HashMap<String, List<PermissionGroup>>();
            PackageManager pm = context.getPackageManager();
            for (PermissionGroup permissionGroup : permissionGroups) {

              String groupS = FAKE_PERMS_GROUP.get(permissionGroup.opName);

              if (groupS == null && permissionGroup.opPermsName != null) {
                try {
                  PermissionInfo permissionInfo = pm
                      .getPermissionInfo(permissionGroup.opPermsName, PackageManager.GET_META_DATA);
                  groupS = permissionInfo.group;
                } catch (Exception e) {
                  //ignore
                }
              }

              PermGroupInfo permGroupInfo = null;
              if (groupS != null) {
                permGroupInfo = PERMS_GROUPS.get(groupS);
              }
              if (permGroupInfo == null) {
                permGroupInfo = OTHER_PERM_INFO;
              }
              permissionGroup.icon = permGroupInfo.icon;
              permissionGroup.group = permGroupInfo.group;

              List<PermissionGroup> value = groups.get(permissionGroup.group);
              if (value == null) {
                value = new ArrayList<PermissionGroup>();
              }
              value.add(permissionGroup);

              groups.put(permissionGroup.group, value);
            }

            return reSort(RE_SORT_GROUPS, groups);
          }
        });
  }


  private static List<PermissionGroup> reSort(String[] groupNames,
      Map<String, List<PermissionGroup>> groups) {
    List<PermissionGroup> ret = new LinkedList<PermissionGroup>();
    for (String groupName : groupNames) {
      List<PermissionGroup> permissionGroups = groups.get(groupName);
      if (permissionGroups != null) {
        ret.addAll(permissionGroups);
      }
    }
    return ret;
  }

  public static Observable<OpsResult> setMode(final Context context, final String pkgName,
      final OpEntryInfo opEntryInfo) {

    return Observable.create(new ObservableOnSubscribe<OpsResult>() {
      @Override
      public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

        OpsResult opsForPackage = AppOpsx.getInstance(context)
            .setOpsMode(pkgName, opEntryInfo.opEntry.getOp(), opEntryInfo.mode);
        if (opsForPackage != null) {
          if (opsForPackage.getException() == null) {
            e.onNext(opsForPackage);
          } else {
            throw new Exception(opsForPackage.getException());
          }
        }
        e.onComplete();

      }
    }).retry(5, new Predicate<Throwable>() {
      @Override
      public boolean test(Throwable throwable) throws Exception {
        return throwable instanceof IOException || throwable instanceof NullPointerException;
      }
    });
  }


  public static Observable<OpsResult> setModes(final Context context, final String pkgName,
      final int opMode, List<Integer> ops) {
    return Observable.fromIterable(ops)
        .flatMap(new Function<Integer, ObservableSource<OpsResult>>() {
          @Override
          public ObservableSource<OpsResult> apply(@NonNull Integer integer) throws Exception {
            return Observable.just(integer).map(new Function<Integer, OpsResult>() {
              @Override
              public OpsResult apply(@NonNull Integer integer) throws Exception {
                OpsResult opsForPackage = AppOpsx.getInstance(context)
                    .setOpsMode(pkgName, integer, opMode);
                if (opsForPackage != null) {
                  if (opsForPackage.getException() == null) {
                    return opsForPackage;
                  } else {
                    throw new Exception(opsForPackage.getException());
                  }
                }
                return null;
              }
            }).retry(5, new Predicate<Throwable>() {
              @Override
              public boolean test(Throwable throwable) throws Exception {
                return throwable instanceof IOException
                    || throwable instanceof NullPointerException;
              }
            });
          }
        });
  }


  public static Single<SparseIntArray> autoDisable(final Context context, String pkg) {

    return SingleJust.just(pkg).map(new Function<String, SparseIntArray>() {
      @Override
      public SparseIntArray apply(String s) throws Exception {

        List<OpEntryInfo> opEntryInfos = getAppPermission(context, s).blockingFirst();

        SparseIntArray canIgnored = new SparseIntArray();//可以忽略的op
        if (opEntryInfos != null && !opEntryInfos.isEmpty()) {
          for (OpEntryInfo opEntryInfo : opEntryInfos) {
            int op = opEntryInfo.opEntry.getOp();
            canIgnored.put(op, op);
          }
        }

        SparseIntArray list = new SparseIntArray();
        SparseIntArray allowedIgnoreOps = getAllowedIgnoreOps(context);

        if (allowedIgnoreOps != null && allowedIgnoreOps.size() > 0) {
          int size = allowedIgnoreOps.size();
          for (int i = 0; i < size; i++) {
            int op = allowedIgnoreOps.keyAt(i);
            if (canIgnored.indexOfKey(op) >= 0 || NO_PERM_OP.indexOfKey(op) >= 0) {
              //
              list.put(op, op);
            }
          }
        }
        for (int i = 0; i < list.size(); i++) {
          try {
            int op = list.keyAt(i);
            AppOpsx.getInstance(context).setOpsMode(s, op, AppOpsManager.MODE_IGNORED);
          } catch (Exception ee) {
            ee.printStackTrace();
          }
        }
        return list;
      }
    });
//        return SingleJust.create(new SingleOnSubscribe<SparseIntArray>() {
//            @Override
//            public void subscribe(SingleEmitter<SparseIntArray> e) throws Exception {
//                List<OpEntryInfo> opEntryInfos = getAppPermission(context, pkg).blockingFirst();
//
//                SparseIntArray canIgnored = new SparseIntArray();//可以忽略的op
//                if (opEntryInfos != null && !opEntryInfos.isEmpty()) {
//                    for (OpEntryInfo opEntryInfo : opEntryInfos) {
//                        int op = opEntryInfo.opEntry.getOp();
//                        canIgnored.put(op, op);
//                    }
//                }
//
//
//                SparseIntArray list = new SparseIntArray();
//                SparseIntArray allowedIgnoreOps = getAllowedIgnoreOps(context);
//
//                if (allowedIgnoreOps != null && allowedIgnoreOps.size() > 0) {
//                    int size = allowedIgnoreOps.size();
//                    for (int i = 0; i < size; i++) {
//                        int op = allowedIgnoreOps.keyAt(i);
//                        if (canIgnored.indexOfKey(op) >= 0 || NO_PERM_OP.indexOfKey(op) >= 0) {
//                            //
//                            list.put(op, op);
//                        }
//                    }
//                }
//                for (int i = 0; i < list.size(); i++) {
//                    try {
//                        int op = list.keyAt(i);
//                        AppOpsx.getInstance(context).setOpsMode(pkg, op, AppOpsManager.MODE_IGNORED);
//                    } catch (Exception ee) {
//                        ee.printStackTrace();
//                    }
//                }
//                e.onSuccess(list);
//            }
//        });
  }


  private static final SparseArray<OpEntryInfo> sOpEntryInfo = new SparseArray<>();
  private static final SparseIntArray sAllOps = new SparseIntArray();
  private static final List<OpEntryInfo> sOpEntryInfoList = new ArrayList<>();

  public static List<OpEntryInfo> getLocalOpEntryInfos(Context context) {
    if (sOpEntryInfoList.isEmpty()) {
      int[] sOpToSwitch = (int[]) ReflectUtils.getFieldValue(AppOpsManager.class, "sOpToSwitch");
      String[] sOpNames = (String[]) ReflectUtils.getFieldValue(AppOpsManager.class, "sOpNames");
      String[] sOpPerms = (String[]) ReflectUtils.getFieldValue(AppOpsManager.class, "sOpPerms");
      int len = sOpPerms.length;
      PackageManager pm = context.getPackageManager();
      for (int i = 0; i < len; i++) {
        OpEntry entry = new OpEntry(sOpToSwitch[i], AppOpsManager.MODE_ALLOWED, 0, 0, 0, 0, null);
        OpEntryInfo opEntryInfo = new OpEntryInfo(entry);
        opEntryInfo.opName = sOpNames[i];
        try {
          PermissionInfo permissionInfo = pm.getPermissionInfo(sOpPerms[i], 0);
          opEntryInfo.opPermsLab = String.valueOf(permissionInfo.loadLabel(pm));
          opEntryInfo.opPermsDesc = String.valueOf(permissionInfo.loadDescription(pm));
        } catch (PackageManager.NameNotFoundException e) {
          //ignore
          Integer resId = sPermI18N.get(opEntryInfo.opName);
          if (resId != null) {
            opEntryInfo.opPermsLab = context.getString(resId);
            opEntryInfo.opPermsDesc = opEntryInfo.opName;
          } else {
            opEntryInfo.opPermsLab = opEntryInfo.opName;
          }
        }
        sOpEntryInfo.put(entry.getOp(), opEntryInfo);
        sAllOps.put(entry.getOp(), entry.getOp());
        sOpEntryInfoList.add(opEntryInfo);
      }
    }
    return new ArrayList<OpEntryInfo>(sOpEntryInfoList);
  }

//    public static SparseIntArray getAllowedIgnoreOps(Context context){
//        getLocalOpEntryInfos(context);
//        SparseIntArray denyIgnoreOps = getAllowedIgnoreOps(context);
//        Log.e(TAG, "getAllowedIgnoreOps -->denyIgnoreOps  "+denyIgnoreOps);
//
//        SparseIntArray clone = sAllOps.clone();
//
//        Log.e(TAG, "getAllowedIgnoreOps --> clone "+clone);
//
//        int size = denyIgnoreOps.size();
//        for (int i = 0; i < size; i++) {
//            clone.delete(denyIgnoreOps.keyAt(i));
//        }
//        return clone;
//    }

  public static SparseIntArray getAllowedIgnoreOps(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    String result = sp.getString("auto_perm_templete", context.getString(R.string.default_ignored));
    SparseIntArray ret = new SparseIntArray();
    String[] split = result.split(",");
    for (String s : split) {
      try {
        int op = Integer.parseInt(s);
        ret.put(op, op);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

//    public static boolean isPermOp(int op){
//        return NO_PERM_OP.indexOfKey(op) < 0;
//    }
//
//    public static void aaa(final Context context){
//        SingleJust.create(new SingleOnSubscribe<String>() {
//            @Override
//            public void subscribe(SingleEmitter<String> emitter) throws Exception {
//                PermissionInfo permissionInfo = context.getPackageManager().getPermissionInfo(Manifest.permission.READ_CONTACTS, PackageManager.GET_META_DATA);
//
//                PermissionGroupInfo permissionGroupInfo = context.getPackageManager().getPermissionGroupInfo(Manifest.permission_group.LOCATION, PackageManager.GET_META_DATA);
//                Log.e(TAG, "subscribe -->permissionInfo "+permissionInfo.name+"  "+permissionInfo.packageName+"  "+permissionInfo.icon+"   "+permissionInfo.group);
//
//                Log.e(TAG, "subscribe --> permissionGroupInfo "+permissionGroupInfo.name+"  "+permissionGroupInfo.packageName+"  "+permissionGroupInfo.icon+"  "+context.getResources().getResourceName(permissionGroupInfo.icon));
//
//                Drawable d=loadDrawable(context.getPackageManager(),permissionInfo.packageName,permissionInfo.icon);
//                Log.e(TAG, "subscribe --> "+d);
//                if (d == null) {
//                    d = context.getDrawable(R.drawable.ic_perm_device_info);
//                }
//
//            }
//        }).subscribeOn(Schedulers.io()).subscribe();
//    }
//
//    public static Drawable loadDrawable(PackageManager pm, String pkg, int resId) {
//        try {
//            return pm.getResourcesForApplication(pkg).getDrawable(resId, null);
//        } catch (Resources.NotFoundException | PackageManager.NameNotFoundException e) {
//            Log.d(TAG, "Couldn't get resource", e);
//            return null;
//        }
//    }


  public static Single<Boolean> closeBgServer(Context context) {
    return SingleJust.just(context).map(new Function<Context, Boolean>() {
      @Override
      public Boolean apply(@NonNull Context context) throws Exception {
        AppOpsx.getInstance(context).closeBgServer();
        return true;
      }
    });
  }

  public static Function<List<AppInfo>, List<AppInfo>> getSortComparator(final Context context) {
    return new Function<List<AppInfo>, List<AppInfo>>() {
      @Override
      public List<AppInfo> apply(List<AppInfo> appInfos) throws Exception {
        int type = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("pref_app_sort_type", 0);
        Comparator<AppInfo> comparator = null;
        if (type == 0) {
          //按名称排序
        } else if (type == 2) {
          //按安装时间排序
          comparator = new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
              return Long.compare(o2.installTime, o1.installTime);
            }
          };
        } else if (type == 3) {
          //按最后更新时间排序
          comparator = new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
              return Long.compare(Math.max(o2.installTime, o2.updateTime),
                  Math.max(o1.installTime, o1.updateTime));
            }
          };
        }

        if (comparator != null) {
          Collections.sort(appInfos, comparator);
        }

        return appInfos;
      }
    };
  }


  public static List<OpEntryInfo> sortPermsFunction(Context context,
      List<OpEntryInfo> opEntryInfos) {
    //resort
    String groupS = null;
    PackageManager pm = context.getPackageManager();

    Map<String, List<OpEntryInfo>> sMap = new HashMap<String, List<OpEntryInfo>>();

    for (OpEntryInfo opEntryInfo : opEntryInfos) {
      if (opEntryInfo != null) {

        groupS = FAKE_PERMS_GROUP.get(opEntryInfo.opName);

        try {
          if (groupS == null && opEntryInfo.opPermsName != null) {
            PermissionInfo permissionInfo = pm
                .getPermissionInfo(opEntryInfo.opPermsName, PackageManager.GET_META_DATA);
            groupS = permissionInfo.group;
          }
        } catch (Exception e) {
          //ignore
        }

        PermGroupInfo permGroupInfo = null;
        if (groupS != null) {
          permGroupInfo = PERMS_GROUPS.get(groupS);
        }

        if (permGroupInfo == null) {
          permGroupInfo = OTHER_PERM_INFO;
        }

        opEntryInfo.icon = permGroupInfo.icon;
        opEntryInfo.groupName = permGroupInfo.group;

        List<OpEntryInfo> infos = sMap.get(opEntryInfo.groupName);
        if (infos == null) {
          infos = new ArrayList<OpEntryInfo>();
          sMap.put(opEntryInfo.groupName, infos);
        }
        infos.add(opEntryInfo);

      }
    }

    List<OpEntryInfo> infoList = new ArrayList<OpEntryInfo>();
    for (String string : RE_SORT_GROUPS) {
      List<OpEntryInfo> infos = sMap.get(string);
      if (infos != null) {
        infoList.addAll(infos);
      }
    }

    return infoList;
  }


  public static Single<List<OpEntryInfo>> groupByMode(final Context context,
      List<OpEntryInfo> list) {

    return Observable.fromIterable(list).collect(new Callable<List<OpEntryInfo>[]>() {
      @Override
      public List<OpEntryInfo>[] call() throws Exception {
        return new List[2];
      }
    }, new BiConsumer<List<OpEntryInfo>[], OpEntryInfo>() {
      @Override
      public void accept(List<OpEntryInfo>[] lists, OpEntryInfo opEntryInfo) throws Exception {
        if (opEntryInfo != null) {
          int idx = opEntryInfo.mode == AppOpsManager.MODE_ALLOWED ? 0 : 1;
          List<OpEntryInfo> list = lists[idx];
          if (list == null) {
            list = new ArrayList<OpEntryInfo>();
            lists[idx] = list;
          }
          list.add(opEntryInfo);
        }
      }
    }).map(new Function<List<OpEntryInfo>[], List<OpEntryInfo>>() {
      @Override
      public List<OpEntryInfo> apply(@NonNull List<OpEntryInfo>[] lists) throws Exception {

        List<OpEntryInfo> ret = new ArrayList<OpEntryInfo>();
        if (lists != null) {
          for (List<OpEntryInfo> list : lists) {
            if (list != null) {
              ret.addAll(Helper.sortPermsFunction(context, list));
            }
          }
        }
        return ret;
      }
    });
  }

}
