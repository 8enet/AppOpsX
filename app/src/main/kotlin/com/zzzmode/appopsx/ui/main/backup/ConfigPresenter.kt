package com.zzzmode.appopsx.ui.main.backup

import android.app.AppOpsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.common.OpsResult
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.model.AppInfo
import com.zzzmode.appopsx.ui.model.PreAppInfo
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.observers.ResourceObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by zl on 2017/5/7.
 */

internal class ConfigPresenter(private val context: Context, private val mView: IConfigView) {

    val restoreFiles: List<RestoreModel>?
        get() {
            val backFiles = BFileUtils.getBackFiles(context)
            if (!backFiles.isEmpty()) {
                val models = backFiles.mapNotNull { readModel(it) }
                Collections.sort(models) { o1, o2 ->
                    when {
                        o1.createTime < o2.createTime -> 1
                        o1.createTime == o2
                                .createTime -> 0
                        else -> -1
                    }
                }

                return models
            }
            return null
        }


    fun export(appInfos: Array<AppInfo>) {
        val max = appInfos.size

        val progress = AtomicInteger()
        mView.showProgress(true, max)
        Helper.getAppsPermission(context, appInfos)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { mView.setProgress(progress.incrementAndGet()) }
                .collect({ ArrayList<PreAppInfo>() }, { preAppInfos, appInfo -> preAppInfos.add(appInfo) }).observeOn(Schedulers.io()).doAfterSuccess { preAppInfos -> save2Local(preAppInfos) }.observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<List<PreAppInfo>> {


            override fun onSubscribe(@NonNull d: Disposable) {

            }

            override fun onSuccess(@NonNull preAppInfos: List<PreAppInfo>) {
                mView.showProgress(false, 0)
            }

            override fun onError(@NonNull e: Throwable) {
                mView.showProgress(false, 0)
            }
        })
    }

    private fun save2Local(preAppInfos: List<PreAppInfo>) {
        //io thread

        val msg = StringBuilder()
        try {
            val jsonObject = JSONObject()
            jsonObject.putOpt("time", System.currentTimeMillis())
            jsonObject.putOpt("v", 1)
            jsonObject.putOpt("size", preAppInfos.size)
            val jsonArray = JSONArray()
            for (preAppInfo in preAppInfos) {
                val ignoredOps = preAppInfo.ignoredOps
                if (!TextUtils.isEmpty(ignoredOps)) {
                    val `object` = JSONObject()
                    `object`.putOpt("pkg", preAppInfo.packageName)
                    `object`.putOpt("ops", ignoredOps)
                    jsonArray.put(`object`)
                }
            }
            jsonObject.putOpt("opbacks", jsonArray)
            val file = BFileUtils.saveBackup(context, jsonObject.toString())
            msg.append(context.getString(R.string.backup_success, file.absoluteFile))
        } catch (e: Exception) {
            e.printStackTrace()
            msg.append("error").append(e.message)
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(context, msg.toString(), Toast.LENGTH_LONG).show()
        }
    }

//    fun importBack(file: File) {
//        val s = BFileUtils.read2String(file)
//        try {
//            val jsonObject = JSONObject(s)
//            val time = jsonObject.optLong("time")
//            val v = jsonObject.optInt("v")
//            val size = jsonObject.optInt("size")
//            val jsonArray = jsonObject.optJSONArray("opbacks")
//            if (jsonArray != null && jsonArray.length() > 0) {
//                val len = jsonArray.length()
//                val preAppInfos = ArrayList<PreAppInfo>(len)
//                for (i in 0 until len) {
//                    val jo = jsonArray.optJSONObject(i)
//                    if (jo != null) {
//                        val pkg = jo.optString("pkg")
//                        val ops = jo.optString("ops")
//                        if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(ops)) {
//                            preAppInfos.add(PreAppInfo(pkg, ops))
//                        }
//                    }
//                }
//                //restoreOps(preAppInfos);
//            }
//        } catch (e: Exception) {
//            Toast.makeText(context, R.string.backup_file_lack, Toast.LENGTH_LONG).show()
//        }
//
//    }


    private fun readModel(file: File?): RestoreModel? {
        try {
            val model = RestoreModel()
            model.path = file!!.absolutePath
            model.fileName = file.name
            model.fileSize = file.length()

            val s = BFileUtils.read2String(file)
            val jsonObject = JSONObject(s)
            model.createTime = jsonObject.optLong("time")
            model.version = jsonObject.optInt("v")
            model.size = jsonObject.optInt("size")
            val jsonArray = jsonObject.optJSONArray("opbacks")
            if (jsonArray != null && jsonArray.length() > 0) {
                val len = jsonArray.length()
                val preAppInfos = ArrayList<PreAppInfo>(len)
                for (i in 0 until len) {
                    val jo = jsonArray.optJSONObject(i)
                    if (jo != null) {
                        val pkg = jo.optString("pkg")
                        val ops = jo.optString("ops")
                        if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(ops)) {
                            preAppInfos.add(PreAppInfo(pkg, ops))
                        }
                    }
                }
                model.preAppInfos = preAppInfos

                return model
            }
        } catch (e: Exception) {
            file?.delete()
        }

        return null
    }

    fun restoreOps(model: RestoreModel) {
        val size = model.preAppInfos!!.size
        val progress = AtomicInteger()
        mView.showProgress(true, size)

        Observable.fromIterable(model.preAppInfos!!)
                .flatMap { appInfo ->
                    Helper.setModes(context, appInfo.packageName, AppOpsManager.MODE_IGNORED,
                            appInfo.getOps())
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : ResourceObserver<OpsResult>() {
            override fun onNext(@NonNull opsResult: OpsResult) {
                mView.setProgress(progress.incrementAndGet())
            }

            override fun onError(@NonNull e: Throwable) {
                progress.incrementAndGet()
            }

            override fun onComplete() {
                mView.showProgress(false, 0)
                Toast.makeText(context, "恢复成功", Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {

        private const val TAG = "ConfigPresenter"
    }


}
