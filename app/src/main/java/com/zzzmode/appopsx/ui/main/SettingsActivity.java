package com.zzzmode.appopsx.ui.main;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;

import com.zzzmode.appopsx.BuildConfig;
import com.zzzmode.appopsx.R;
import com.zzzmode.appopsx.ui.BaseActivity;
import com.zzzmode.appopsx.ui.core.AppOpsx;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.OpEntryInfo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.single.SingleJust;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zl on 2017/1/16.
 */

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.menu_setting);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        private Preference mPrefAppSort;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            findPreference("version").setSummary(BuildConfig.VERSION_NAME);

            findPreference("acknowledgments").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    StringBuilder sb=new StringBuilder();
                    String[] stringArray = getResources().getStringArray(R.array.acknowledgments_list);
                    for (String s : stringArray) {
                        sb.append(s).append('\n');
                    }
                    sb.deleteCharAt(sb.length()-1);
                    showTextDialog(R.string.acknowledgments_list,sb.toString());
                    return true;
                }
            });


            findPreference("ignore_premission_templete").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showPremissionTemplete();
                    return true;
                }
            });

            mPrefAppSort=findPreference("pref_app_sort_type");
            mPrefAppSort.setSummary(getString(R.string.app_sort_type_summary,getResources().getStringArray(R.array.app_sort_type)[PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(mPrefAppSort.getKey(),0)]));
            mPrefAppSort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAppSortDialog(preference);
                    return true;
                }
            });


            findPreference("show_log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showLog();
                    return true;
                }
            });
        }

        private void showPremissionTemplete(){

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.auto_ignore_premission_title);
            List<OpEntryInfo> localOpEntryInfos = Helper.getLocalOpEntryInfos(getActivity());
            int size = localOpEntryInfos.size();
            CharSequence[] items=new CharSequence[size];

            boolean[] selected = new boolean[size];

            for (int i = 0; i < size; i++) {
                OpEntryInfo opEntryInfo = localOpEntryInfos.get(i);
                items[i]=opEntryInfo.opPermsLab;
                selected[i]=false; //默认关闭
            }

            initCheckd(selected);

            final SparseBooleanArray choiceResult=new SparseBooleanArray();
            for (int i = 0; i < selected.length; i++) {
                choiceResult.put(i,selected[i]);
            }

            saveChoice(choiceResult);

            builder.setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    choiceResult.put(which,isChecked);
                }
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveChoice(choiceResult);
                }
            });
            builder.show();
        }

        private void initCheckd(boolean[] localChecked) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String result = sp.getString("auto_perm_templete", getActivity().getString(R.string.default_ignored));
            String[] split = result.split(",");
            for (String s : split) {
                try {
                    int i = Integer.parseInt(s);
                    localChecked[i] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void saveChoice(SparseBooleanArray choiceResult){
            StringBuilder sb=new StringBuilder();
            int size = choiceResult.size();
            for (int i = 0; i < size; i++) {
                if(choiceResult.get(i)){
                    sb.append(i).append(',');
                }
            }
            String s=sb.toString();
            if(!TextUtils.isEmpty(s)){
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sp.edit().putString("auto_perm_templete",s).apply();
            }
        }

        private void showTextDialog(int title, String text) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setMessage(text);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }

        private void showAppSortDialog(final Preference preference){
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_sort_type_title);

            final int[] selected=new int[1];
            selected[0]=PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(preference.getKey(),0);
            builder.setSingleChoiceItems(R.array.app_sort_type, selected[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selected[0]=which;
                }
            });

            builder.setNegativeButton(android.R.string.cancel,null);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(preference.getKey(),selected[0]).apply();
                    mPrefAppSort.setSummary(getString(R.string.app_sort_type_summary,getResources().getStringArray(R.array.app_sort_type)[selected[0]]));
                }
            });
            builder.show();
        }


        private void showLog(){
            SingleJust.create(new SingleOnSubscribe<String>() {
                @Override
                public void subscribe(SingleEmitter<String> e) throws Exception {
                    e.onSuccess(AppOpsx.readLogs(getActivity()));
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(String value) {
                            showTextDialog(R.string.show_log, value);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

        }
    }
}
