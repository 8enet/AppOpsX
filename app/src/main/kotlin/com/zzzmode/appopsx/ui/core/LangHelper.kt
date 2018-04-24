package com.zzzmode.appopsx.ui.core

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import com.zzzmode.appopsx.R
import java.util.*

/**
 * Created by zl on 2017/6/16.
 */

object LangHelper {

    private val TAG = "LangHelper"

    private val sLocalMap = HashMap<String, Locale>()
    private var sDefaultLocal: Locale

    init {
        sDefaultLocal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().get(0)
        } else {
            Locale.getDefault()
        }

        sLocalMap["zh-cn"] = Locale.SIMPLIFIED_CHINESE
        sLocalMap["zh-tw"] = Locale.TRADITIONAL_CHINESE
        sLocalMap["en"] = Locale.ENGLISH
        sLocalMap["cs"] = Locale("cs", "CZ")
        sLocalMap["es"] = Locale("es")
        sLocalMap["ru"] = Locale("ru")
    }


    fun updateLanguage(context: Context) {

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(getLocaleByLanguage(context))
        val dm = resources.displayMetrics
        resources.updateConfiguration(config, dm)
    }


    fun getLocalIndex(context: Context): Int {
        var defSelected = 0
        val defKey = SpHelper
                .getSharedPreferences(context).getString("pref_app_language", null)
        val langKeys = context.resources.getStringArray(R.array.languages_key)
        if (defKey != null) {
            for (i in langKeys.indices) {
                if (TextUtils.equals(defKey, langKeys[i])) {
                    defSelected = i
                    break
                }
            }
        }
        return defSelected
    }

    private fun getLocaleByLanguage(context: Context): Locale? {
        val language = SpHelper
                .getSharedPreferences(context).getString("pref_app_language", null)
                ?: return sDefaultLocal
        return sLocalMap[language] ?: sDefaultLocal
    }


    fun attachBaseContext(context: Context): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context)
        } else {
            context
        }
    }


    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context): Context {
        val resources = context.resources
        val locale = getLocaleByLanguage(context)

        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.locales = LocaleList(locale!!)
        return context.createConfigurationContext(configuration)
    }

}
