package com.zzzmode.appopsx.ui.core;

import android.annotation.TargetApi;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.zzzmode.appopsx.BuildConfig;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.common.OpEntry;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.common.PackageOps;
import com.zzzmode.appopsx.common.ReflectUtils;
import com.zzzmode.appopsx.ui.model.AppInfo;
import com.zzzmode.appopsx.ui.model.AppPremissions;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;
import com.zzzmode.appopsx.ui.model.PremissionChildItem;
import com.zzzmode.appopsx.ui.model.PremissionGroup;
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.operators.single.SingleJust;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/17.
 */

public class Helper {

    private static final SparseIntArray NO_PERM_OP=new SparseIntArray();

    static {
        int[] ops={2,11,12,15,22,30,31,32,33,34,35,36,37,38,39,41,42,44,45,46,47,48,49,50,58,61,63,65,69};
        for (int op : ops) {
            NO_PERM_OP.put(op,op);
        }

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
    }};


    public static void updataShortcuts(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            getInstalledApps(context, false).concatMap(new Function<List<AppInfo>, ObservableSource<AppInfo>>() {
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
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new ResourceSingleObserver<List<AppInfo>>() {
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

            shortcut.setIcon(Icon.createWithBitmap(drawableToBitmap(appInfo.icon)));

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
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Observable<List<AppInfo>> getInstalledApps(final Context context, final boolean loadSysapp) {
        return Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<AppInfo>> e) throws Exception {
                PackageManager packageManager = context.getPackageManager();
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                List<AppInfo> zhAppInfos = new ArrayList<AppInfo>();
                List<AppInfo> enAppInfos=new ArrayList<AppInfo>();
                for (PackageInfo installedPackage : installedPackages) {
                    if (loadSysapp || (installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        AppInfo info = new AppInfo();
                        info.packageName = installedPackage.packageName;
                        info.appName = BidiFormatter.getInstance().unicodeWrap(installedPackage.applicationInfo.loadLabel(packageManager)).toString();
                        info.icon = installedPackage.applicationInfo.loadIcon(packageManager);
                        info.time = Math.max(installedPackage.lastUpdateTime, installedPackage.firstInstallTime);

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
                List<AppInfo> ret=new ArrayList<AppInfo>();
                ret.addAll(enAppInfos);
                ret.addAll(zhAppInfos);
                e.onNext(ret);
                e.onComplete();
            }
        });
    }


    public static Observable<List<OpEntryInfo>> getAppPermission(final Context context, final String packageName) {
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
                                    for (OpEntry op : ops) {
                                        OpEntryInfo opEntryInfo = new OpEntryInfo(op);
                                        if (opEntryInfo.opName != null) {
                                            try {
                                                PermissionInfo permissionInfo = pm.getPermissionInfo(opEntryInfo.opPermsName, 0);
                                                opEntryInfo.opPermsLab = String.valueOf(permissionInfo.loadLabel(pm));
                                                opEntryInfo.opPermsDesc = String.valueOf(permissionInfo.loadDescription(pm));
                                            } catch (PackageManager.NameNotFoundException e) {
                                                //ignore
                                                Integer resId = sPermI18N.get(opEntryInfo.opName);
                                                if (resId != null) {
                                                    opEntryInfo.opPermsLab = context.getString(resId);
                                                    opEntryInfo.opPermsDesc = opEntryInfo.opName;
                                                }

                                            }
                                            list.add(opEntryInfo);
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
                    public List<OpEntryInfo> apply(List<OpEntryInfo> opEntryInfos) throws Exception {
                        Collections.sort(opEntryInfos, new Comparator<OpEntryInfo>() {
                            @Override
                            public int compare(OpEntryInfo o1, OpEntryInfo o2) {
                                if (o1.opPermsLab == null && o2.opPermsLab != null) {
                                    return 1;
                                }
                                if (o1.opPermsDesc == null && o2.opPermsDesc != null) {
                                    return 1;
                                }
                                if (o1.opPermsLab != null && o2.opPermsLab != null && o1.opPermsDesc != null && o2.opPermsDesc != null) {
                                    return 0;
                                }
                                if (o1.opPermsLab != null && o2.opPermsLab != null && o1.opPermsDesc == null && o2.opPermsDesc == null) {
                                    return o1.opPermsLab.compareTo(o2.opPermsLab);
                                }
                                return -1;
                            }
                        });
                        return opEntryInfos;
                    }
                });
    }

    public static Single<List<PremissionGroup>> getPremissionGroup(final Context context, final boolean loadSysapp) {
        return getInstalledApps(context, loadSysapp).concatMap(new Function<List<AppInfo>, ObservableSource<AppInfo>>() {
            @Override
            public ObservableSource<AppInfo> apply(List<AppInfo> appInfos) throws Exception {
                return Observable.fromIterable(appInfos);
            }
        }).map(new Function<AppInfo, AppPremissions>() {
            @Override
            public AppPremissions apply(AppInfo info) throws Exception {
                AppPremissions p = new AppPremissions();
                p.appInfo = info;
                p.opEntries = getAppPermission(context, info.packageName).blockingFirst();
                return p;
            }
        }).collect(new Callable<Map<String, List<AppPremissions>>>() {
            @Override
            public Map<String, List<AppPremissions>> call() throws Exception {
                return new HashMap<String, List<AppPremissions>>();
            }
        }, new BiConsumer<Map<String, List<AppPremissions>>, AppPremissions>() {
            @Override
            public void accept(Map<String, List<AppPremissions>> map, AppPremissions app) throws Exception {
                if (app.opEntries != null && app.hasPremissions()) {
                    for (OpEntryInfo opEntry : app.opEntries) {
                        if (opEntry.opName != null) {
                            List<AppPremissions> appPremissionses = map.get(opEntry.opName);
                            if (appPremissionses == null) {
                                appPremissionses = new ArrayList<AppPremissions>();
                            }
                            appPremissionses.add(app);
                            map.put(opEntry.opName, appPremissionses);
                        }
                    }
                }
            }
        }).map(new Function<Map<String, List<AppPremissions>>, Map<String, List<AppPremissions>>>() {
            @Override
            public Map<String, List<AppPremissions>> apply(Map<String, List<AppPremissions>> map) throws Exception {
                Map<String, Integer> counts = new HashMap<String, Integer>();
                Set<Map.Entry<String, List<AppPremissions>>> entries = map.entrySet();
                for (Map.Entry<String, List<AppPremissions>> entry : entries) {
                    counts.put(entry.getKey(), entry.getValue().size());
                }
                List<Map.Entry<String, Integer>> countsSort = new LinkedList<Map.Entry<String, Integer>>(counts.entrySet());
                Collections.sort(countsSort, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                Map<String, List<AppPremissions>> ret = new LinkedHashMap<String, List<AppPremissions>>();
                for (Map.Entry<String, Integer> stringIntegerEntry : countsSort) {
                    String key = stringIntegerEntry.getKey();
                    List<AppPremissions> appPremissionses = map.get(key);
                    if (appPremissionses != null) {
                        ret.put(key, appPremissionses);
                    }
                }
                return ret;
            }
        }).map(new Function<Map<String, List<AppPremissions>>, List<PremissionGroup>>() {
            @Override
            public List<PremissionGroup> apply(Map<String, List<AppPremissions>> map) throws Exception {
                List<PremissionGroup> groups = new ArrayList<PremissionGroup>();
                Set<Map.Entry<String, List<AppPremissions>>> entries = map.entrySet();
                for (Map.Entry<String, List<AppPremissions>> entry : entries) {
                    PremissionGroup group = new PremissionGroup();
                    group.opName = entry.getKey();

                    List<AppPremissions> value = entry.getValue();

                    group.count = value.size();
                    group.apps = new ArrayList<PremissionChildItem>();

                    for (AppPremissions appPremissions : value) {
                        PremissionChildItem item = new PremissionChildItem();
                        item.appInfo = appPremissions.appInfo;

                        group.apps.add(item);
                        if (appPremissions.opEntries != null) {
                            for (OpEntryInfo opEntry : appPremissions.opEntries) {
                                if (group.opName.equals(opEntry.opName)) {
                                    item.opEntryInfo = opEntry;
                                    if (opEntry.opEntry.getMode() == AppOpsManager.MODE_ALLOWED) {
                                        group.grants += 1;
                                    }
                                    group.opPermsDesc = opEntry.opPermsDesc;
                                    group.opPermsLab = opEntry.opPermsLab;
                                    break;
                                }
                            }
                        }


                    }
                    groups.add(group);
                }

                return groups;
            }
        });
    }


    public static Observable<OpsResult> setMode(final Context context, final String pkgName, final OpEntryInfo opEntryInfo) {
        return Observable.create(new ObservableOnSubscribe<OpsResult>() {
            @Override
            public void subscribe(ObservableEmitter<OpsResult> e) throws Exception {

                OpsResult opsForPackage = AppOpsx.getInstance(context).setOpsMode(pkgName, opEntryInfo.opEntry.getOp(), opEntryInfo.mode);
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


    public static Single<SparseIntArray> autoDisable(final Context context, final String pkg) {

        return SingleJust.create(new SingleOnSubscribe<SparseIntArray>() {
            @Override
            public void subscribe(SingleEmitter<SparseIntArray> e) throws Exception {
                List<OpEntryInfo> opEntryInfos = getAppPermission(context, pkg).blockingFirst();

                SparseIntArray canIgnored = new SparseIntArray();//可以忽略的op
                if (opEntryInfos != null && !opEntryInfos.isEmpty()) {
                    for (OpEntryInfo opEntryInfo : opEntryInfos) {
                        int op = opEntryInfo.opEntry.getOp();
                        canIgnored.put(op, op);
                    }
                }

                Log.e(TAG, "subscribe --> canIgnored " + canIgnored);

                SparseIntArray list = new SparseIntArray();
                SparseIntArray allowedIgnoreOps = getAllowedIgnoreOps(context);

                Log.e(TAG, "subscribe --> allowedIgnoreOps " + allowedIgnoreOps);

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
                        AppOpsx.getInstance(context).setOpsMode(pkg, op, AppOpsManager.MODE_IGNORED);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
                e.onSuccess(list);
            }
        });
    }


    private static final SparseArray<OpEntryInfo> sOpEntryInfo = new SparseArray<>();
    private static final SparseIntArray sAllOps=new SparseIntArray();
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
                sAllOps.put(entry.getOp(),entry.getOp());
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
}
