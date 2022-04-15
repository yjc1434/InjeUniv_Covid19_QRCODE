package com.injeuniv.yjc.qrcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class SettingPreference extends PreferenceFragmentCompat {

    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private AlertDialog dialog0, dialog1, dialog2, alertDialog;
    private Preference loginInfo, logout, appInfo, setTime;
    private ListPreference displayMode;
    private SwitchPreference autoSelect, quickQR, notiOn;
    private Toast toast;
    private int count;
    private String getVersion,nowVersion,getURL;

    public void updateInfo() {
        getVersion = sPreferences.getString("newVersion","");
        getURL = sPreferences.getString("newLink","");
        nowVersion = sPreferences.getString("nowVersion","");

        if (getVersion.equals(nowVersion)) {
            appInfo.setSummary("최신버전입니다.");
        } else {
            appInfo.setSummary("다운로드 가능한 업데이트가 있습니다.");
            appInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("애플리케이션 정보");
                    if(!getVersion.equals(nowVersion)) {
                        builder.setMessage("* 최신 버전 : " + getVersion + "\n* 현재 버전 : " + nowVersion + "\n\n최신 버전으로 업데이트 하시겠습니까?");
                        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL));
                                startActivity(browserIntent);
                            }
                        });
                        builder.setNegativeButton("아니오",null);
                    }else {
                        builder.setMessage("최신 버전입니다.");
                        builder.setPositiveButton("확인",null);
                    }
                    builder.show();
                    return false;
                }
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SettingsActivity activity = (SettingsActivity) getActivity();
        activity.status = 0;

        setPreferencesFromResource(R.xml.setting_preference, rootKey);
        sPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        editor = sPreferences.edit();

        loginInfo = findPreference("IDinfo");
        logout = findPreference("logout");
        displayMode = findPreference("darkmode");
        autoSelect = findPreference("autocheck");
        quickQR = findPreference("quickqr");
        displayMode = findPreference("darkmode");
        appInfo = findPreference("Appinfo");
        notiOn = findPreference("notion");
        setTime = findPreference("alarm_time");

        count = 0;

        loginInfo.setSummary(sPreferences.getString("ID", null));

        updateInfo();

        String group = sPreferences.getString("group", "");
        String name = sPreferences.getString("name", "");
        Boolean Auto = sPreferences.getBoolean("Auto", false);
        String auto = "사용안함";
        if (Auto)
            auto = "사용";
        builder.setTitle("로그인 정보");
        builder.setMessage("* 소속 : " + group + "\n" + "* 개인번호 : " + loginInfo.getSummary() + "\n* 성명 : " + name + "\n* 자동 로그인 여부 : " + auto);
        builder.setPositiveButton("닫기", null);
        dialog0 = builder.create();

        if ((sPreferences.getString("darkmode", "").equals("0"))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            displayMode.setSummary("기기 설정");
        } else if ((sPreferences.getString("darkmode", "").equals("1"))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            displayMode.setSummary("라이트 모드");
        } else if ((sPreferences.getString("darkmode", "").equals("2"))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            displayMode.setSummary("다크 모드");
        }

        loginInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dialog0.show();
                return false;
            }
        });

        if (sPreferences.getBoolean("Auto", false) == true) {
            quickQR.setEnabled(true);
            quickQR.setSummary("로그인 없이 바로 QR코드를 표시합니다.");
            autoSelect.setEnabled(true);
            autoSelect.setSummary("자가진단 시 자동으로 '모두 해당 없음'이 선택됩니다.");
            notiOn.setEnabled(true);
            notiOn.setSummary("알림창에서 애플리케이션을 켤 수 있습니다.");
        }

        builder.setTitle("증상 자동 선택");
        builder.setMessage("거짓 확인시 「감염병의 예방 및 관리에 관한 법률」에 의해 처벌 받을 수 있으며, 이에 대한 책임은 본인에게 있습니다.");
        builder.setPositiveButton("동의", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                autoSelect.setChecked(true);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                autoSelect.setChecked(false);
            }
        });
        dialog1 = builder.create();

        builder = new AlertDialog.Builder(getActivity());
        if (sPreferences.getBoolean("Auto", false) == true) {
            builder.setTitle("로그아웃").setMessage("로그아웃 시 자동로그인이 해제됩니다. 로그아웃하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    editor.remove("Auto");
                    editor.remove("Password");
                    editor.remove("autocheck");
                    editor.remove("notion");
                    editor.remove("alwaysnoti");
                    editor.commit();
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(getActivity(), "성공적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        } else {
            builder.setTitle("로그아웃").setMessage("로그아웃하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(getActivity(), "성공적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }
        builder.setNegativeButton("아니오", null);
        dialog2 = builder.create();

        autoSelect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (sPreferences.getBoolean("autocheck", false) != true)
                    dialog1.show();
                else {
                    autoSelect.setChecked(false);
                }
                return false;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dialog2.show();
                return false;
            }
        });

        displayMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putString("darkmode", (String) newValue);
                editor.commit();
                if (((String) newValue).equals("0")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    preference.setSummary("기기 설정");
                } else if (((String) newValue).equals("1")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    preference.setSummary("라이트 모드");
                } else if (((String) newValue).equals("2")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    preference.setSummary("다크 모드");
                }

                return true;
            }
        });

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("테스트 모드").setMessage("데이터를 초기화시키겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                editor.clear();
                editor.commit();

                if (toast != null) toast.cancel();
                toast = Toast.makeText(getActivity(), "데이터 초기화를 완료하였습니다.", Toast.LENGTH_LONG);
                toast.show();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });
        builder.setNegativeButton("아니오", null);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);

        notiOn.setOnPreferenceClickListener(preferenceClickListener);

        setTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                TimePreference fragment2 = new TimePreference();
                transaction.replace(R.id.settings_container, fragment2);
                transaction.commit();
                return false;
            }
        });
    }

    Preference.OnPreferenceClickListener preferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            boolean show = sPreferences.getBoolean("notion", false);
            if (show) {
                Charles.setShortcutNoti(getActivity());
            } else {
                Charles.cancelShortcutNoti(getActivity());
            }
            return false;
        }
    };
}