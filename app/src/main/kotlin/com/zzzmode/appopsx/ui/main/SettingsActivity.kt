package com.zzzmode.appopsx.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.MenuItem
import android.widget.Toast
import com.zzzmode.appopsx.BuildConfig
import com.zzzmode.appopsx.R
import com.zzzmode.appopsx.ui.BaseActivity
import com.zzzmode.appopsx.ui.analytics.AEvent
import com.zzzmode.appopsx.ui.analytics.ATracker
import com.zzzmode.appopsx.ui.core.AppOpsx
import com.zzzmode.appopsx.ui.core.Helper
import com.zzzmode.appopsx.ui.core.LangHelper
import com.zzzmode.appopsx.ui.widget.NumberPickerPreference
import com.zzzmode.appopsx.ui.widget.NumberPickerPreference.NumberPickerPreferenceDialogFragmentCompat
import io.reactivex.SingleObserver
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.layout_appbar.*
/**
 * Created by zl on 2017/1/16.
 */

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.menu_setting)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, MyPreferenceFragment()).commit()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        AppOpsx.updateConfig(applicationContext)
    }


    class MyPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

        private var mPrefAppSort: Preference? = null
        private var mUseAdb: Preference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)

            findPreference("ignore_premission").onPreferenceClickListener = this
            findPreference("show_sysapp").onPreferenceClickListener = this

            mUseAdb = findPreference("use_adb")
            mUseAdb?.onPreferenceClickListener = this

            findPreference("allow_bg_remote").onPreferenceClickListener = this
            findPreference("project").onPreferenceClickListener = this

            findPreference("opensource_licenses").onPreferenceClickListener = this
            findPreference("help").onPreferenceClickListener = this
            findPreference("translate").onPreferenceClickListener = this

            findPreference("shell_start").onPreferenceClickListener = this

            val version = findPreference("version")
            version.summary = BuildConfig.VERSION_NAME
            version.onPreferenceClickListener = this

            val appLanguage = findPreference("pref_app_language")
            appLanguage.onPreferenceClickListener = this
            appLanguage.summary = resources.getStringArray(R.array.languages)[LangHelper.getLocalIndex(context!!)]

            findPreference("acknowledgments").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ATracker.send(AEvent.C_SETTING_KNOWLEDGMENTS)
                val sb = StringBuilder()
                val stringArray = resources.getStringArray(R.array.acknowledgments_list)
                for (s in stringArray) {
                    sb.append(s).append('\n')
                }
                sb.deleteCharAt(sb.length - 1)
                showTextDialog(R.string.acknowledgments_list, sb.toString())
                true
            }

            findPreference("ignore_premission_templete").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ATracker.send(AEvent.C_SETTING_IGNORE_TEMPLETE)
                showPremissionTemplete()
                true
            }

            mPrefAppSort = findPreference("pref_app_sort_type")
            mPrefAppSort?.summary = getString(R.string.app_sort_type_summary,
                    resources.getStringArray(R.array.app_sort_type)[PreferenceManager
                            .getDefaultSharedPreferences(activity).getInt(mPrefAppSort!!.key, 0)])
            mPrefAppSort?.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                ATracker.send(AEvent.C_SETTING_APP_SORE)
                showAppSortDialog(preference)
                true
            }

            findPreference("show_log").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ATracker.send(AEvent.C_SETTING_SHOW_LOG)
                showLog()
                true
            }

            findPreference("close_server").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ATracker.send(AEvent.C_SETTING_CLOSE_SERVER)
                closeServer()
                true
            }

            findPreference("pref_app_daynight_mode").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                ATracker.send(AEvent.C_SETTING_SWITCH_THEME)
                true
            }

            val adbPortPreference = findPreference(
                    "use_adb_port") as NumberPickerPreference
            adbPortPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                if (newValue is Int) {
                    mUseAdb?.summary = getString(R.string.use_adb_mode_summary, newValue.toInt())
                }
                true
            }

            mUseAdb?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                if (newValue is Boolean) {
                    adbPortPreference.isVisible = newValue
                }
                true
            }

            mUseAdb?.summary = getString(R.string.use_adb_mode_summary, adbPortPreference.value)

            adbPortPreference.isVisible = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("use_adb", false)
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (preference is NumberPickerPreference) {
                val fragment = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey())
                fragment.setTargetFragment(this, 0)
                fragment.show(fragmentManager!!,
                        "NumberPickerPreferenceDialogFragment")
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun closeServer() {
            Helper.closeBgServer(activity!!.applicationContext).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<Boolean> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(value: Boolean) {
                    val activity = activity
                    if (activity != null) {
                        Toast.makeText(activity, R.string.bg_closed, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(e: Throwable) {

                }
            })
        }

        private fun showPremissionTemplete() {

            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.auto_ignore_permission_title)
            val localOpEntryInfos = Helper.getLocalOpEntryInfos(context!!)
            val size = localOpEntryInfos.size
            val items = arrayOfNulls<CharSequence>(size)

            val selected = BooleanArray(size)

            for (i in 0 until size) {
                val opEntryInfo = localOpEntryInfos[i]
                items[i] = opEntryInfo.opPermsLab
                selected[i] = false //默认关闭
            }

            initCheckd(selected)

            val choiceResult = SparseBooleanArray()
            for (i in selected.indices) {
                choiceResult.put(i, selected[i])
            }

            saveChoice(choiceResult)

            builder
                    .setMultiChoiceItems(items, selected) { _, which, isChecked -> choiceResult.put(which, isChecked) }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { _, _ -> saveChoice(choiceResult) }
            builder.show()
        }

        private fun initCheckd(localChecked: BooleanArray) {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)
            val result = sp
                    .getString("auto_perm_templete", activity!!.getString(R.string.default_ignored))
            val split = result!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (s in split) {
                try {
                    val i = Integer.parseInt(s)
                    localChecked[i] = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        private fun saveChoice(choiceResult: SparseBooleanArray) {
            val sb = StringBuilder()
            val size = choiceResult.size()
            for (i in 0 until size) {
                if (choiceResult.get(i)) {
                    sb.append(i).append(',')
                }
            }
            val s = sb.toString()
            if (!TextUtils.isEmpty(s)) {
                val sp = PreferenceManager.getDefaultSharedPreferences(activity)
                sp.edit().putString("auto_perm_templete", s).apply()
            }
        }

        private fun showTextDialog(title: Int, text: String) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(title)
            builder.setMessage(text)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.show()
        }

        private fun showAppSortDialog(preference: Preference) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.app_sort_type_title)

            val selected = IntArray(1)
            selected[0] = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getInt(preference.key, 0)
            builder.setSingleChoiceItems(R.array.app_sort_type, selected[0]
            ) { _, which -> selected[0] = which }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                        .putInt(preference.key, selected[0]).apply()
                mPrefAppSort!!.summary = getString(R.string.app_sort_type_summary,
                        resources.getStringArray(R.array.app_sort_type)[selected[0]])
            }
            builder.show()
        }

        private fun showLanguageDialog(preference: Preference) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.app_language)

            val selected = IntArray(1)
            val defSelected = LangHelper.getLocalIndex(context!!)

            builder.setSingleChoiceItems(R.array.languages, defSelected
            ) { dialog, which -> selected[0] = which }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                val index = selected[0]
                var language: String? = null
                if (index == 0) {
                    PreferenceManager.getDefaultSharedPreferences(activity).edit()
                            .remove(preference.key).apply()
                    language = "auto"
                } else {
                    language = resources.getStringArray(R.array.languages_key)[index]
                    PreferenceManager.getDefaultSharedPreferences(activity).edit()
                            .putString(preference.key, language).apply()
                }
                preference.summary = resources.getStringArray(R.array.languages)[index]
                ATracker.send(AEvent.C_LANG, Collections.singletonMap<String, String>("lang", language))
                switchLanguage()
            }
            builder.show()
        }


        private fun switchLanguage() {
            LangHelper.updateLanguage(context!!)
            LangHelper.updateLanguage(context!!.applicationContext)

            val it = Intent(activity, MainActivity::class.java)
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity!!.startActivity(it)
            activity!!.finish()
        }

        private fun showLog() {
            SingleJust.create(SingleOnSubscribe<String> { e -> e.onSuccess(AppOpsx.readLogs(activity!!)) }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<String> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(value: String) {
                            showTextDialog(R.string.show_log, value)
                        }

                        override fun onError(e: Throwable) {

                        }
                    })

        }

        private fun showVersion() {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.zzzmode.appopsx")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (!context!!.packageManager.queryIntentActivities(intent, 0).isEmpty()) {
                startActivity(intent)
            } else {
                intent.data = Uri.parse("https://github.com/8enet/AppOpsX")
                startActivity(intent)
            }
        }

        private fun showShellStart() {
            val builder = AlertDialog.Builder(activity!!)
            builder.setMessage(getString(R.string.shell_cmd_help, getString(R.string.shell_cmd)))
            builder.setPositiveButton(android.R.string.copy) { dialog, which ->
                val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.primaryClip = ClipData.newPlainText(null, getString(R.string.shell_cmd))
                dialog.dismiss()
                Toast.makeText(context, R.string.copied_hint, Toast.LENGTH_SHORT).show()
            }
            builder.create().show()
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            val key = preference.key
            var id: String? = null
            if ("ignore_premission" == key) {
                id = AEvent.C_SETTING_AUTO_IGNORE
            } else if ("show_sysapp" == key) {
                id = AEvent.C_SETTING_SHOW_SYS
            } else if ("use_adb" == key) {
                id = AEvent.C_SETTING_USE_ADB
            } else if ("allow_bg_remote" == key) {
                id = AEvent.C_SETTING_ALLOW_BG
            } else if ("version" == key) {
                showVersion()
                id = AEvent.C_SETTING_VERSION
            } else if ("project" == key) {
                id = AEvent.C_SETTING_GITHUB
            } else if ("opensource_licenses" == key) {
                id = AEvent.C_SETTING_OPENSOURCE
                val intent = Intent(context, HtmlActionActivity::class.java)
                intent.putExtra(Intent.EXTRA_TITLE, preference.title)
                intent.putExtra(HtmlActionActivity.EXTRA_URL, "file:///android_res/raw/licenses.html")
                activity!!.startActivity(intent)
            } else if ("help" == key) {
                id = AEvent.C_SETTING_HELP
                val intent = Intent(context, HtmlActionActivity::class.java)
                intent.putExtra(Intent.EXTRA_TITLE, preference.title)
                intent.putExtra(HtmlActionActivity.EXTRA_URL, "file:///android_res/raw/help.html")
                activity!!.startActivity(intent)
            } else if ("translate" == key) {
                id = AEvent.C_SETTING_TRANSLATE
            } else if ("pref_app_language" == key) {
                id = AEvent.C_SETTING_LANGUAGE
                showLanguageDialog(preference)
            } else if ("shell_start" == key) {
                id = AEvent.C_SETTING_SHELL_START
                showShellStart()
            }
            if (id != null) {
                ATracker.send(id)
            }
            return false
        }
    }
}
