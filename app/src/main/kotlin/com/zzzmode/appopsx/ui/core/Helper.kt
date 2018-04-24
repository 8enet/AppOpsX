package com.zzzmode.appopsx.ui.core

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.text.BidiFormatter
import android.text.TextUtils
import android.util.SparseArray
import android.util.SparseIntArray
import com.zzzmode.appopsx.BuildConfig
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.common.*
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.model.*
import com.zzzmode.appopsx.ui.permission.AppPermissionActivity
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.observers.ResourceSingleObserver
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*

/**
 * Created by zl on 2017/1/17.
 */

object Helper {

    private val RE_SORT_GROUPS = arrayOf(permission_group.GPS, Manifest.permission_group.LOCATION, Manifest.permission_group.CALENDAR, Manifest.permission_group.SMS, Manifest.permission_group.CONTACTS, Manifest.permission_group.CAMERA, permission_group.CALLPHONE, Manifest.permission_group.PHONE, Manifest.permission_group.STORAGE, Manifest.permission_group.SENSORS, permission_group.FINGERPRINT, Manifest.permission_group.MICROPHONE, permission_group.VIBRATION, permission_group.NET, permission_group.WIFI, permission_group.NFC, permission_group.SETTINGS, permission_group.NOTIFICATIONS, permission_group.CLIPBOARD, permission_group.AUDIO, permission_group.DEVICE, permission_group.OTHER)

    private val NO_PERM_OP = SparseIntArray()
    private val FAKE_PERMS_GROUP = HashMap<String, String>()

    private val PERMS_GROUPS = HashMap<String, PermGroupInfo>()

    private val OTHER_PERM_INFO = PermGroupInfo(null,
            permission_group.OTHER, R.drawable.perm_group_other)

    private val TAG = "Helper"

    private val sPermI18N = object : HashMap<String, Int>() {
        init {
            put("POST_NOTIFICATION", R.string.permlab_POST_NOTIFICATION)
            put("READ_CLIPBOARD", R.string.permlab_READ_CLIPBOARD)
            put("WRITE_CLIPBOARD", R.string.permlab_WRITE_CLIPBOARD)
            put("TURN_ON_SCREEN", R.string.permlab_TURN_ON_SCREEN)
            put("RUN_IN_BACKGROUND", R.string.permlab_RUN_IN_BACKGROUND)
            put("MONITOR_LOCATION", R.string.permlab_MONITOR_LOCATION)
            put("MONITOR_HIGH_POWER_LOCATION", R.string.permlab_MONITOR_HIGH_POWER_LOCATION)
            put("NEIGHBORING_CELLS", R.string.permlab_NEIGHBORING_CELLS)
            put("PLAY_AUDIO", R.string.permlab_PLAY_AUDIO)
            put("AUDIO_MASTER_VOLUME", R.string.permlab_AUDIO_MASTER_VOLUME)
            put("AUDIO_VOICE_VOLUME", R.string.permlab_AUDIO_VOICE_VOLUME)
            put("AUDIO_RING_VOLUME", R.string.permlab_AUDIO_RING_VOLUME)
            put("AUDIO_MEDIA_VOLUME", R.string.permlab_AUDIO_MEDIA_VOLUME)
            put("AUDIO_ALARM_VOLUME", R.string.permlab_AUDIO_ALARM_VOLUME)
            put("AUDIO_NOTIFICATION_VOLUME", R.string.permlab_AUDIO_NOTIFICATION_VOLUME)
            put("AUDIO_BLUETOOTH_VOLUME", R.string.permlab_AUDIO_BLUETOOTH_VOLUME)
            put("TOAST_WINDOW", R.string.permlab_TOAST_WINDOW)
            put("ACTIVATE_VPN", R.string.permlab_ACTIVATE_VPN)
            put("TAKE_AUDIO_FOCUS", R.string.permlab_TAKE_AUDIO_FOCUS)
            put("ACCESS_PHONE_DATA", R.string.permlab_ACCESS_MOBLIE_NETWORK_DATA)
            put("ACCESS_WIFI_NETWORK", R.string.permlab_ACCESS_WIFI_NETWORK_DATA)

        }
    }


    private val sOpEntryInfo = SparseArray<OpEntryInfo>()
    private val sAllOps = SparseIntArray()
    private val sOpEntryInfoList = ArrayList<OpEntryInfo>()

    object permission_group {

        internal const val AUDIO = "com.zzzmode.appopsx.permission-group.AUDIO"
        internal const val DEVICE = "com.zzzmode.appopsx.permission-group.DEVICE"
        internal const val OTHER = "com.zzzmode.appopsx.permission-group.OTHER"
        internal const val GPS = "com.zzzmode.appopsx.permission-group.GPS"
        internal const val CALLPHONE = "com.zzzmode.appopsx.permission-group.CALLPHONE"
        internal const val VIBRATION = "com.zzzmode.appopsx.permission-group.VIBRATION"
        internal const val NET = "com.zzzmode.appopsx.permission-group.NET"
        internal const val WIFI = "com.zzzmode.appopsx.permission-group.WIFI"
        internal const val NFC = "com.zzzmode.appopsx.permission-group.NFC"
        internal const val SETTINGS = "com.zzzmode.appopsx.permission-group.SETTINGS"
        internal const val NOTIFICATIONS = "com.zzzmode.appopsx.permission-group.NOTIFICATIONS"
        internal const val CLIPBOARD = "com.zzzmode.appopsx.permission-group.CLIPBOARD"
        internal const val FINGERPRINT = "com.zzzmode.appopsx.permission-group.FINGERPRINT"

    }

    private class PermGroupInfo internal constructor(internal var title: String?, internal var group: String, internal var icon: Int) {

        override fun toString(): String {
            return "PermGroupInfo{" +
                    "title='" + title + '\''.toString() +
                    ", group='" + group + '\''.toString() +
                    '}'.toString()
        }
    }

    init {
        val ops = intArrayOf(2, 11, 12, 15, 22, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41, 42, 44, 45, 46, 47, 48, 49, 50, 58, 61, 63, 65, 69)
        for (op in ops) {
            NO_PERM_OP.put(op, op)
        }

        FAKE_PERMS_GROUP["COARSE_LOCATION"] = Manifest.permission_group.LOCATION
        FAKE_PERMS_GROUP["FINE_LOCATION"] = permission_group.GPS
        FAKE_PERMS_GROUP["GPS"] = permission_group.GPS
        FAKE_PERMS_GROUP["NEIGHBORING_CELLS"] = Manifest.permission_group.LOCATION
        FAKE_PERMS_GROUP["MONITOR_LOCATION"] = Manifest.permission_group.LOCATION
        FAKE_PERMS_GROUP["MONITOR_HIGH_POWER_LOCATION"] = Manifest.permission_group.LOCATION

        FAKE_PERMS_GROUP["READ_CALL_LOG"] = permission_group.CALLPHONE
        FAKE_PERMS_GROUP["WRITE_CALL_LOG"] = permission_group.CALLPHONE
        FAKE_PERMS_GROUP["CALL_PHONE"] = permission_group.CALLPHONE
        FAKE_PERMS_GROUP["PROCESS_OUTGOING_CALLS"] = permission_group.CALLPHONE

        FAKE_PERMS_GROUP["READ_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["WRITE_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["RECEIVE_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["RECEIVE_EMERGECY_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["RECEIVE_MMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["RECEIVE_WAP_PUSH"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["SEND_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["READ_ICC_SMS"] = Manifest.permission_group.SMS
        FAKE_PERMS_GROUP["WRITE_ICC_SMS"] = Manifest.permission_group.SMS

        FAKE_PERMS_GROUP["PLAY_AUDIO"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["TAKE_MEDIA_BUTTONS"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["TAKE_AUDIO_FOCUS"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_MASTER_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_VOICE_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_RING_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_MEDIA_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_ALARM_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_NOTIFICATION_VOLUME"] = permission_group.AUDIO
        FAKE_PERMS_GROUP["AUDIO_BLUETOOTH_VOLUME"] = permission_group.AUDIO

        FAKE_PERMS_GROUP["MUTE_MICROPHONE"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["TOAST_WINDOW"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["PROJECT_MEDIA"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["ACTIVATE_VPN"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["WRITE_WALLPAPER"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["ASSIST_STRUCTURE"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["ASSIST_SCREENSHOT"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["MOCK_LOCATION"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["TURN_ON_SCREEN"] = permission_group.DEVICE
        FAKE_PERMS_GROUP["RUN_IN_BACKGROUND"] = permission_group.DEVICE

        FAKE_PERMS_GROUP["ACCESS_PHONE_DATA"] = permission_group.NET
        FAKE_PERMS_GROUP["ACCESS_WIFI_NETWORK"] = permission_group.NET

        FAKE_PERMS_GROUP["VIBRATE"] = permission_group.VIBRATION

        FAKE_PERMS_GROUP["WIFI_SCAN"] = permission_group.WIFI
        FAKE_PERMS_GROUP["WIFI_CHANGE"] = permission_group.WIFI

        FAKE_PERMS_GROUP["NFC_CHANGE"] = permission_group.NFC

        FAKE_PERMS_GROUP["WRITE_SETTINGS"] = permission_group.SETTINGS

        FAKE_PERMS_GROUP["ACCESS_NOTIFICATIONS"] = permission_group.NOTIFICATIONS
        FAKE_PERMS_GROUP["POST_NOTIFICATION"] = permission_group.NOTIFICATIONS

        FAKE_PERMS_GROUP["READ_CLIPBOARD"] = permission_group.CLIPBOARD
        FAKE_PERMS_GROUP["WRITE_CLIPBOARD"] = permission_group.CLIPBOARD

        FAKE_PERMS_GROUP["USE_FINGERPRINT"] = permission_group.FINGERPRINT

        PERMS_GROUPS[Manifest.permission_group.CALENDAR] = PermGroupInfo(null, Manifest.permission_group.CALENDAR,
                R.drawable.perm_group_calendar)
        PERMS_GROUPS[Manifest.permission_group.CAMERA] = PermGroupInfo(null, Manifest.permission_group.CAMERA, R.drawable.perm_group_camera)
        PERMS_GROUPS[Manifest.permission_group.CONTACTS] = PermGroupInfo(null, Manifest.permission_group.CONTACTS,
                R.drawable.perm_group_contacts)
        PERMS_GROUPS[Manifest.permission_group.LOCATION] = PermGroupInfo(null, Manifest.permission_group.LOCATION,
                R.drawable.perm_group_location)
        PERMS_GROUPS[Manifest.permission_group.MICROPHONE] = PermGroupInfo(null, Manifest.permission_group.MICROPHONE,
                R.drawable.perm_group_microphone)
        PERMS_GROUPS[Manifest.permission_group.PHONE] = PermGroupInfo(null, Manifest.permission_group.PHONE, R.drawable.ic_perm_device_info)
        PERMS_GROUPS[Manifest.permission_group.SENSORS] = PermGroupInfo(null, Manifest.permission_group.SENSORS, R.drawable.perm_group_sensors)
        PERMS_GROUPS[Manifest.permission_group.SMS] = PermGroupInfo(null, Manifest.permission_group.SMS, R.drawable.perm_group_sms)
        PERMS_GROUPS[Manifest.permission_group.STORAGE] = PermGroupInfo(null, Manifest.permission_group.STORAGE, R.drawable.perm_group_storage)

        PERMS_GROUPS[permission_group.AUDIO] = PermGroupInfo(null, permission_group.AUDIO, R.drawable.perm_group_audio)
        PERMS_GROUPS[permission_group.DEVICE] = PermGroupInfo(null, permission_group.DEVICE, R.drawable.perm_group_device)
        PERMS_GROUPS[permission_group.OTHER] = PermGroupInfo(null, permission_group.OTHER, R.drawable.perm_group_other)
        PERMS_GROUPS[permission_group.GPS] = PermGroupInfo(null, permission_group.GPS, R.drawable.perm_group_gps)
        PERMS_GROUPS[permission_group.CALLPHONE] = PermGroupInfo(null, permission_group.CALLPHONE, R.drawable.perm_group_callphone)
        PERMS_GROUPS[permission_group.VIBRATION] = PermGroupInfo(null, permission_group.VIBRATION, R.drawable.perm_group_vibration)
        PERMS_GROUPS[permission_group.NET] = PermGroupInfo(null, permission_group.NET, R.drawable.perm_group_net)
        PERMS_GROUPS[permission_group.WIFI] = PermGroupInfo(null, permission_group.WIFI, R.drawable.perm_group_wifi)
        PERMS_GROUPS[permission_group.NFC] = PermGroupInfo(null, permission_group.NFC, R.drawable.perm_group_nfc)
        PERMS_GROUPS[permission_group.SETTINGS] = PermGroupInfo(null, permission_group.SETTINGS, R.drawable.perm_group_settings)
        PERMS_GROUPS[permission_group.NOTIFICATIONS] = PermGroupInfo(null, permission_group.NOTIFICATIONS,
                R.drawable.perm_group_notifications)
        PERMS_GROUPS[permission_group.CLIPBOARD] = PermGroupInfo(null, permission_group.CLIPBOARD, R.drawable.perm_group_clipboard)

        PERMS_GROUPS[permission_group.FINGERPRINT] = PermGroupInfo(null, permission_group.FINGERPRINT, R.drawable.perm_group_fingerprint)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun updataShortcuts(context: Context) {
        getInstalledApps(context, false)
                .concatMap { appInfos -> Observable.fromIterable(appInfos) }.filter { info -> BuildConfig.APPLICATION_ID != info.packageName }.collect({ ArrayList<AppInfo>() }, { appInfos, info -> appInfos.add(info) }).map { appInfos ->
            Collections.sort(appInfos) { o1, o2 -> if (o1.time > o2.time) -1 else 1 }
            appInfos
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ResourceSingleObserver<List<AppInfo>>() {

                    override fun onSuccess(value: List<AppInfo>) {
                        try {
                            updataShortcuts(context, value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })

    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private fun updataShortcuts(context: Context, items: List<AppInfo>) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcutInfoList = ArrayList<ShortcutInfo>()
        val max = shortcutManager!!.maxShortcutCountPerActivity
        var i = 0
        while (i < max && i < items.size) {
            val appInfo = items[i]
            val shortcut = ShortcutInfo.Builder(context, appInfo.packageName)
            shortcut.setShortLabel(appInfo.appName)
            shortcut.setLongLabel(appInfo.appName)

            shortcut.setIcon(
                    Icon.createWithBitmap(drawableToBitmap(
                            LocalImageLoader.getDrawable(context, appInfo))))

            val intent = Intent(context, AppPermissionActivity::class.java)
            intent.putExtra(AppPermissionActivity.EXTRA_APP_PKGNAME,
                    appInfo.packageName)
            intent.putExtra(AppPermissionActivity.EXTRA_APP_NAME, appInfo.appName)
            intent.action = Intent.ACTION_DEFAULT
            shortcut.setIntent(intent)

            shortcutInfoList.add(shortcut.build())
            i++
        }
        shortcutManager.dynamicShortcuts = shortcutInfoList
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if(drawable == null){
            return Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        }


        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap: Bitmap by lazy {
            if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1,
                        Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888)
            }
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    fun getAppInfo(context: Context, pkgName: String): Single<AppInfo> {
        return SingleJust.just(pkgName).map { s ->
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(s, 0)


            val info = AppInfo(packageName = packageInfo.packageName)
            info.appName = BidiFormatter.getInstance()
                    .unicodeWrap(packageInfo.applicationInfo.loadLabel(packageManager)).toString()
            info.time = Math.max(packageInfo.lastUpdateTime, packageInfo.firstInstallTime)
            info.installTime = packageInfo.firstInstallTime
            info.updateTime = packageInfo.lastUpdateTime
            info.applicationInfo = packageInfo.applicationInfo

            LocalImageLoader.initAdd(context, info)
            info
        }

    }

    fun getInstalledApps(context: Context,
                         loadSysapp: Boolean): Observable<List<AppInfo>> {

        return Observable.create { e ->
            val packageManager = context.packageManager
            val uid = Users.instance.currentUid
            val installedPackages = if (uid == 0) {
                packageManager.getInstalledPackages(0)
            } else {
                AppOpsx
                        .getInstance(context).apiSupporter.getInstalledPackages(0, uid)
            }


            val zhAppInfos = ArrayList<AppInfo>()
            val enAppInfos = ArrayList<AppInfo>()
            for (installedPackage in installedPackages!!) {
                if (loadSysapp || installedPackage.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    val info = AppInfo(packageName =  installedPackage.packageName)
                    info.appName = BidiFormatter.getInstance()
                            .unicodeWrap(installedPackage.applicationInfo.loadLabel(packageManager))
                            .toString()
                    info.time = Math
                            .max(installedPackage.lastUpdateTime, installedPackage.firstInstallTime)
                    info.installTime = installedPackage.firstInstallTime
                    info.updateTime = installedPackage.lastUpdateTime
                    info.applicationInfo = installedPackage.applicationInfo

                    LocalImageLoader.initAdd(context, info)

                    //some of the app name is empty.
                    if (TextUtils.isEmpty(info.appName)) {
                        info.appName = info.packageName
                    }
                    val c = info.appName[0]
                    if (c.toInt() in 48..122) {
                        enAppInfos.add(info)
                    } else {
                        zhAppInfos.add(info)
                    }

                }
            }

            Collections.sort(enAppInfos) { o1, o2 -> o1.appName.compareTo(o2.appName, ignoreCase = true) }

            Collections.sort(zhAppInfos) { o1, o2 -> o2.appName.compareTo(o1.appName) }
            val ret = ArrayList<AppInfo>()

            val type = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("pref_app_sort_type", 0)
            if (type == 1) {
                //按名称排序[字母在后]

                ret.addAll(zhAppInfos)
                ret.addAll(enAppInfos)
            } else {
                //按名称排序[字母在前] 默认
                ret.addAll(enAppInfos)
                ret.addAll(zhAppInfos)
            }

            e.onNext(ret)
            e.onComplete()
        }
    }


    fun getAppsPermission(context: Context,
                          appInfos: Array<AppInfo>): Observable<PreAppInfo> {
        return Observable.fromArray(*appInfos).map { s ->
            val opsForPackage = AppOpsx.getInstance(context).getOpsForPackage(s.packageName)
            if (opsForPackage != null) {
                if (opsForPackage.exception != null) {
                    throw Exception(opsForPackage.exception)
                }
            } else {
                throw NullPointerException("getOpsForPackage:$s return null !")
            }
            val appInfo = PreAppInfo(s.packageName)
            val opses = opsForPackage.list
            if (opses != null) {
                val sb = StringBuilder()
                for (opse in opses) {
                    opse.ops?.filter { it.mode == AppOpsManager.MODE_IGNORED }
                            ?.forEach { sb.append(it.op).append(',') }

                }
                val len = sb.length
                if (len > 0 && sb[len - 1] == ',') {
                    sb.deleteCharAt(len - 1)
                }
                appInfo.ignoredOps = sb.toString()
                appInfo
            } else {
                throw NullPointerException("")
            }
        }
                .retry(5) { throwable -> throwable is IOException || throwable is NullPointerException }
                .subscribeOn(Schedulers.io())
    }


    @JvmOverloads
    fun getAppPermission(context: Context,
                         packageName: String, needNoPermsOp: Boolean = false): Observable<List<OpEntryInfo>> {
        return Observable.create(ObservableOnSubscribe<OpsResult> { e ->
            val opsForPackage = AppOpsx.getInstance(context).getOpsForPackage(packageName)
            if (opsForPackage != null) {
                if (opsForPackage.exception == null) {
                    e.onNext(opsForPackage)
                } else {
                    throw Exception(opsForPackage.exception)
                }
            }
            e.onComplete()
        })
                .retry(5) { throwable -> throwable is IOException || throwable is NullPointerException }
                .subscribeOn(Schedulers.io()).map(opsResult2OpEntryInfoMap(context, needNoPermsOp)).map { opEntryInfos -> sortPermsFunction(context, opEntryInfos) }
    }

    private fun opsResult2OpEntryInfoMap(context: Context, needNoPermsOp: Boolean): Function<OpsResult, List<OpEntryInfo>> {
        return Function { opsResult ->
            val opses = opsResult.list
            if (opses != null) {
                val list = ArrayList<OpEntryInfo>()
                val pm = context.packageManager
                for (opse in opses) {
                    val ops = opse.ops

                    if (ops != null) {
                        val hasOp = SparseIntArray()
                        for (op in ops) {
                            val opEntryInfo = opEntry2Info(op, context, pm)
                            if (opEntryInfo != null) {
                                hasOp.put(op.op, op.op)
                                list.add(opEntryInfo)
                            }
                        }

                        if (needNoPermsOp) {
                            val size = NO_PERM_OP.size()
                            (0 until size)
                                    .map { NO_PERM_OP.keyAt(it) }
                                    .filter { hasOp.indexOfKey(it) < 0 }
                                    .map { OpEntry(it, AppOpsManager.MODE_ALLOWED, 0, 0, 0, 0, null) }
                                    .mapNotNullTo(list) { opEntry2Info(it, context, pm) }
                        }
                    }
                }
                return@Function list
            }
            emptyList()
        }
    }

    private fun opEntry2Info(op: OpEntry, context: Context, pm: PackageManager): OpEntryInfo? {
        val opEntryInfo = OpEntryInfo(op)
        if (OtherOp.isOtherOp(op.op)) {
            opEntryInfo.opName = OtherOp.getOpName(op.op)
            opEntryInfo.opPermsName = OtherOp.getOpPermName(op.op)
        }
        if (opEntryInfo.opName != null) {
            try {
                if (!OtherOp.isOtherOp(op.op)) {
                    val permissionInfo = pm.getPermissionInfo(opEntryInfo.opPermsName, 0)
                    opEntryInfo.opPermsLab = permissionInfo.loadLabel(pm)?.toString()
                    opEntryInfo.opPermsDesc = permissionInfo.loadDescription(pm)?.toString()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                //ignore
            }

            if (opEntryInfo.opPermsLab == null && opEntryInfo.opName != null) {
                val resId = sPermI18N[opEntryInfo.opName!!]
                if (resId != null) {
                    opEntryInfo.opPermsLab = context.getString(resId)
                    opEntryInfo.opPermsDesc = opEntryInfo.opName
                }
            }

            return opEntryInfo
        }
        return null
    }


    internal fun getAllAppPermissions(context: Context,
                                      loadSysapp: Boolean, reqNet: Boolean): Observable<AppPermissions> {
        return Observable.create(ObservableOnSubscribe<OpsResult> { e ->
            val opsForPackage = AppOpsx
                    .getInstance(context).getPackagesForOps(null, reqNet)
            if (opsForPackage != null) {
                if (opsForPackage.exception == null) {
                    e.onNext(opsForPackage)
                } else {
                    throw Exception(opsForPackage.exception)
                }
            }
            e.onComplete()
        })
                .retry(5) { throwable -> throwable is IOException || throwable is NullPointerException }.map { result ->
            val map = HashMap<String, PackageOps>()
            val list = result.list
            if (list != null) {
                for (packageOps in list) {
                    map[packageOps.packageName] = packageOps
                }
            }
            map
        }.flatMap { result ->
                    getInstalledApps(context, loadSysapp).map { appInfos ->
                        val list = ArrayList<AppPermissions>()
                        val pm = context.packageManager
                            for (appInfo in appInfos) {
                                val p = AppPermissions(appInfo)

                                val packageOps = result[appInfo.packageName]
                                if (packageOps != null) {
                                    val ops = packageOps.ops
                                    if (ops != null) {
                                        val opEntryInfos = ArrayList<OpEntryInfo>()
                                        val hasOp = SparseIntArray()
                                        for (op in ops) {
                                            val opEntryInfo = opEntry2Info(op, context, pm)
                                            if (opEntryInfo != null) {
                                                hasOp.put(op.op, op.op)
                                                opEntryInfos.add(opEntryInfo)
                                            }
                                        }
                                        p.opEntries = opEntryInfos
                                        list.add(p)
                                    }
                                }

                        }
                        list
                    }
                }.flatMap { appPermissions -> Observable.fromIterable(appPermissions) }
    }


    fun getPermsUsageStatus(context: Context,
                            loadSysapp: Boolean): Single<List<Pair<AppInfo, OpEntryInfo>>> {
        return getAllAppPermissions(context, loadSysapp, false)
                .collect({ ArrayList<Pair<AppInfo, OpEntryInfo>>() }, { pairs, appPermissions ->
                    if (appPermissions.opEntries != null) {
                        for (opEntry in appPermissions.opEntries!!) {
                            //被调用过并且允许的才加入列表
                            //超过一个月的记录不显示
                            val time = opEntry.opEntry.time
                            val now = System.currentTimeMillis()

                            if (time > 0 && now - time < 60 * 60 * 24 * 31 * 1000L && opEntry.isAllowed) {
                                joinOpEntryInfo(opEntry, context)
                                pairs.add(Pair(appPermissions.appInfo, opEntry))
                            }
                        }
                    }
                }).flatMapObservable { pairs -> Observable.fromIterable(pairs) }.toSortedList { t0, t1 -> t1.second.opEntry.time.compareTo(t0.second.opEntry.time) }
    }

    fun getPermissionGroup(context: Context,
                           loadSysapp: Boolean, reqNet: Boolean, showIgnored: Boolean): Single<List<PermissionGroup>> {
        return getAllAppPermissions(context, loadSysapp, reqNet)
                .collect({ HashMap<String, MutableList<AppPermissions>>() }, { map, app ->
                    if (app.opEntries != null && app.hasPermissions()) {
                        for (opEntry in app.opEntries!!) {
                            opEntry.opName?.apply {
                                var appPermissionses: MutableList<AppPermissions>? = map[this]
                                if (appPermissionses == null) {
                                    appPermissionses = ArrayList()
                                }
                                appPermissionses.add(app)
                                map[this] = appPermissionses
                            }

                        }
                    }
                })
                .map { map ->
                    val groups = ArrayList<PermissionGroup>()
                    val entries = map.entries
                    for ((key, value) in entries) {
                        val group = PermissionGroup()
                        group.opName = key

                        group.count = value.size

                        for (appPermissions in value) {
                            val item = PermissionChildItem(appPermissions.appInfo)


                            var show = false

                            appPermissions.opEntries?.let {
                                for (opEntry in it) {
                                    if (group.opName == opEntry.opName) {
                                        item.opEntryInfo = opEntry
                                        if (opEntry.opEntry.mode == AppOpsManager.MODE_ALLOWED) {
                                            group.grants = group.grants + 1
                                            show = true
                                        } else if (!showIgnored) {
                                            show = false
                                        }
                                        group.opPermsName = opEntry.opPermsName
                                        group.opPermsDesc = opEntry.opPermsDesc
                                        group.opPermsLab = opEntry.opPermsLab
                                        break
                                    }
                                }
                            }

                            if (show) {
                                group.apps.add(item)
                            }

                        }


                        group.apps.sortWith(Comparator { o1, o2 ->
                            o2.opEntryInfo.opEntry.time.compareTo(o1.opEntryInfo.opEntry.time)
                        })

                        groups.add(group)
                    }

                    groups
                }.map { permissionGroups ->
                    val groups = HashMap<String, MutableList<PermissionGroup>>()
                    val pm = context.packageManager
                    for (permissionGroup in permissionGroups) {

                        var groupS: String? = FAKE_PERMS_GROUP[permissionGroup.opName]

                        if (groupS == null && permissionGroup.opPermsName != null) {
                            try {
                                val permissionInfo = pm
                                        .getPermissionInfo(permissionGroup.opPermsName, PackageManager.GET_META_DATA)
                                groupS = permissionInfo.group
                            } catch (e: Exception) {
                                //ignore
                            }

                        }

                        var permGroupInfo: PermGroupInfo? = null
                        if (groupS != null) {
                            permGroupInfo = PERMS_GROUPS[groupS]
                        }
                        if (permGroupInfo == null) {
                            permGroupInfo = OTHER_PERM_INFO
                        }
                        permissionGroup.icon = permGroupInfo.icon
                        permissionGroup.group = permGroupInfo.group

                        var value: MutableList<PermissionGroup>? = groups[permissionGroup.group]
                        if (value == null) {
                            value = ArrayList()
                        }
                        value.add(permissionGroup)

                        groups[permissionGroup.group] = value
                    }

                    reSort(RE_SORT_GROUPS, groups)
                }
    }


    private fun reSort(groupNames: Array<String>,
                       groups: Map<String, List<PermissionGroup>>): List<PermissionGroup> {
        val ret = LinkedList<PermissionGroup>()
        groupNames
                .mapNotNull { groups[it] }
                .forEach { ret.addAll(it) }
        return ret
    }


    fun setMode(context: Context, pkgName: String,
                opEntryInfo: OpEntryInfo, isAllow: Boolean): Observable<OpsResult> {
        if (isAllow) {
            opEntryInfo.mode = AppOpsManager.MODE_ALLOWED
        } else {
            opEntryInfo.mode = AppOpsManager.MODE_IGNORED
        }
        val map = HashMap<String, String>(2)
        map["new_mode"] = opEntryInfo.mode.toString()
        map["op_name"] = opEntryInfo.opName!!
        ATracker.send(AEvent.C_PERM_ITEM, map)
        return setMode(context, pkgName, opEntryInfo)
    }

    fun setMode(context: Context, pkgName: String,
                opEntryInfo: OpEntryInfo): Observable<OpsResult> {

        return Observable.create(ObservableOnSubscribe<OpsResult> { e ->
            val opsForPackage = AppOpsx.getInstance(context)
                    .setOpsMode(pkgName, opEntryInfo.opEntry.op, opEntryInfo.mode)
            if (opsForPackage != null) {
                if (opsForPackage.exception == null) {
                    e.onNext(opsForPackage)
                } else {
                    throw Exception(opsForPackage.exception)
                }
            }
            e.onComplete()
        }).retry(5) { throwable -> throwable is IOException || throwable is NullPointerException }
    }


    fun setModes(context: Context, pkgName: String,
                 opMode: Int, ops: List<Int>): Observable<OpsResult> {
        return Observable.fromIterable(ops)
                .flatMap { integer ->
                    Observable.just(integer).map { opCode ->
                        val opsForPackage = AppOpsx.getInstance(context)
                                .setOpsMode(pkgName, opCode, opMode)
                        if (opsForPackage != null) {
                            if (opsForPackage.exception == null) {
                                opsForPackage
                            } else {
                                throw Exception(opsForPackage.exception)
                            }
                        } else throw Exception("setOpsMode error")
                    }.retry(5) { throwable -> throwable is IOException || throwable is NullPointerException }
                }
    }

    fun resetMode(context: Context, pkgName: String): Single<OpsResult> {
        return SingleJust.just(pkgName).map {
            val opsForPackage = AppOpsx.getInstance(context).resetAllModes(pkgName)
            if (opsForPackage != null && opsForPackage.exception != null) {
                throw Exception(opsForPackage.exception)
            }
            opsForPackage
        }
    }

    fun autoDisable(context: Context, pkg: String): Single<SparseIntArray> {

        return SingleJust.just(pkg).map { s ->
            val opEntryInfos = getAppPermission(context, s).blockingFirst()

            val canIgnored = SparseIntArray()//可以忽略的op

            opEntryInfos?.map { it.opEntry.op }?.forEach { canIgnored.put(it, it) }

            val list = SparseIntArray()
            val allowedIgnoreOps = getAllowedIgnoreOps(context)

            if (allowedIgnoreOps.size() > 0) {
                val size = allowedIgnoreOps.size()
                (0 until size)
                        .map { allowedIgnoreOps.keyAt(it) }
                        .filter { canIgnored.indexOfKey(it) >= 0 || NO_PERM_OP.indexOfKey(it) >= 0 }
                        .forEach {
                            //
                            list.put(it, it)
                        }
            }
            for (i in 0 until list.size()) {
                try {
                    val op = list.keyAt(i)
                    AppOpsx.getInstance(context).setOpsMode(s, op, AppOpsManager.MODE_IGNORED)
                } catch (ee: Exception) {
                    ee.printStackTrace()
                }

            }
            list
        }
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

    fun getLocalOpEntryInfos(context: Context): List<OpEntryInfo> {
        if (sOpEntryInfoList.isEmpty()) {
            val sOpToSwitch = ReflectUtils.getFieldValue(AppOpsManager::class.java, "sOpToSwitch") as IntArray
            val sOpNames = ReflectUtils.getFieldValue(AppOpsManager::class.java, "sOpNames") as Array<String>
            val sOpPerms = ReflectUtils.getFieldValue(AppOpsManager::class.java, "sOpPerms") as Array<String>
            val len = sOpPerms.size
            val pm = context.packageManager
            for (i in 0 until len) {
                val entry = OpEntry(sOpToSwitch[i], AppOpsManager.MODE_ALLOWED, 0, 0, 0, 0, null)
                val opEntryInfo = OpEntryInfo(entry)
                opEntryInfo.opName = sOpNames[i]
                try {
                    val permissionInfo = pm.getPermissionInfo(sOpPerms[i], 0)
                    opEntryInfo.opPermsLab = permissionInfo.loadLabel(pm)?.toString()
                    opEntryInfo.opPermsDesc = permissionInfo.loadDescription(pm)?.toString()
                } catch (e: Throwable) {
                    //ignore
                    val resId = sPermI18N[opEntryInfo.opName!!]
                    if (resId != null) {
                        opEntryInfo.opPermsLab = context.getString(resId)
                        opEntryInfo.opPermsDesc = opEntryInfo.opName
                    } else {
                        opEntryInfo.opPermsLab = opEntryInfo.opName
                    }
                }

                sOpEntryInfo.put(entry.op, opEntryInfo)
                sAllOps.put(entry.op, entry.op)
                sOpEntryInfoList.add(opEntryInfo)
            }
        }
        return ArrayList(sOpEntryInfoList)
    }


    fun getAllowedIgnoreOps(context: Context): SparseIntArray {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val result = sp.getString("auto_perm_templete", context.getString(R.string.default_ignored))
        val ret = SparseIntArray()
        val split = result!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in split) {
            try {
                val op = Integer.parseInt(s)
                ret.put(op, op)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return ret
    }

    fun closeBgServer(context: Context): Single<Boolean> {
        return SingleJust.just(context).map { context ->
            AppOpsx.getInstance(context).closeBgServer()
            true
        }
    }

    fun restartServer(context: Context): Single<Boolean> {
        return SingleJust.just(context).map { context ->
            AppOpsx.getInstance(context).apiSupporter.restartServer(context)
            true
        }
    }

    fun getSortComparator(context: Context): Function<List<AppInfo>, List<AppInfo>> {
        return Function { appInfos ->
            val type = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("pref_app_sort_type", 0)
            var comparator: Comparator<AppInfo>? = null
            if (type == 0) {
                //按名称排序
            } else if (type == 2) {

                //按安装时间排序
                comparator = Comparator { o1, o2 -> o2.installTime.compareTo(o1.installTime) }
            } else if (type == 3) {
                //按最后更新时间排序

                comparator = Comparator { o1, o2 ->
                    Math.max(o2.installTime, o2.updateTime).compareTo(Math.max(o1.installTime, o1.updateTime))
                }
            }

            if (comparator != null) {
                Collections.sort(appInfos, comparator)
            }

            appInfos
        }
    }


    fun sortPermsFunction(context: Context,
                          opEntryInfos: List<OpEntryInfo>): List<OpEntryInfo> {
        //resort
        //val groupS: String? = null
        //val pm = context.packageManager

        val sMap = HashMap<String, MutableList<OpEntryInfo>>()

        for (opEntryInfo in opEntryInfos) {

            joinOpEntryInfo(opEntryInfo, context)

            var infos: MutableList<OpEntryInfo>? = sMap.get(opEntryInfo.groupName)
            if (infos == null && opEntryInfo.groupName != null) {
                infos = ArrayList()
                sMap[opEntryInfo.groupName!!] = infos
            }
            infos?.add(opEntryInfo)

        }

        val infoList = ArrayList<OpEntryInfo>()
        RE_SORT_GROUPS
                .mapNotNull { sMap[it] }
                .forEach { infoList.addAll(it) }

        return infoList
    }

    private fun joinOpEntryInfo(opEntryInfo: OpEntryInfo, context: Context) {
        var groupS: String? = FAKE_PERMS_GROUP[opEntryInfo.opName]

        try {
            if (groupS == null && opEntryInfo.opPermsName != null) {
                val permissionInfo = context.packageManager
                        .getPermissionInfo(opEntryInfo.opPermsName, PackageManager.GET_META_DATA)
                groupS = permissionInfo.group
            }
        } catch (e: Exception) {
            //ignore
        }

        var permGroupInfo: PermGroupInfo? = null
        if (groupS != null) {
            permGroupInfo = PERMS_GROUPS[groupS]
        }

        if (permGroupInfo == null) {
            permGroupInfo = OTHER_PERM_INFO
        }

        opEntryInfo.icon = permGroupInfo.icon
        opEntryInfo.groupName = permGroupInfo.group

    }

    fun groupByMode(context: Context,
                    list: List<OpEntryInfo>): Single<MutableList<OpEntryInfo>> {

        return Observable.fromIterable(list).collect({ arrayOfNulls<MutableList<OpEntryInfo>>(2) }) { lists, opEntryInfo ->
            if (opEntryInfo != null) {
                val idx = if (opEntryInfo.mode == AppOpsManager.MODE_ALLOWED) 0 else 1
                var list: MutableList<OpEntryInfo>? = lists[idx]
                if (list == null) {
                    list = ArrayList()
                    lists[idx] = list
                }

                list.add(opEntryInfo)
            }
        }
                .map { lists ->
                    val ret = ArrayList<OpEntryInfo>()
                    lists.forEach { list ->
                        if (list != null) {
                            ret.addAll(Helper.sortPermsFunction(context, list))
                        }
                    }
                    ret
                }
    }


    fun getUsers(context: Context, excludeDying: Boolean): Single<List<UserInfo>> {
        return Single.create { emitter -> emitter.onSuccess(AppOpsx.getInstance(context).apiSupporter.getUsers(excludeDying)) }
    }

}
