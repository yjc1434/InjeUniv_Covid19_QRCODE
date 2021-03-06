package com.injeuniv.yjc.qrcode;


import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class TimePreference extends PreferenceFragmentCompat {
    private Preference start,end;
    private ListPreference style;
    private MultiSelectListPreference days;
    private SwitchPreference kill;
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private AlarmManager alarmManager;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)  {
        setPreferencesFromResource(R.xml.setting_time, rootKey);
        SettingsActivity activity = (SettingsActivity)getActivity();

        activity.status = 1;

        start = findPreference("time_Start");
        end = findPreference("time_End");
        days = findPreference("time_days");

        style = findPreference("noti_style");

        kill = findPreference("touch_kill");

        start.setOnPreferenceClickListener(timePickerDialog);
        end.setOnPreferenceClickListener(timePickerDialog);

        sPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sPreferences.edit();

        alarmManager =  (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        String startS,endS;
        if(sPreferences.getString("time_Start",null) != null) {
            startS = sPreferences.getString("time_Start",null);
        }
        else {
            startS = "?????? 09??? 00???";
        }
        start.setSummary(startS);
        if(sPreferences.getString("time_End",null) != null) {
            endS = sPreferences.getString("time_End",null);
        }
        else {
            endS = "?????? 06??? 00???";
        }

        end.setSummary(endS);


        if(sPreferences.getString("noti_style","").equals("0")) {
            style.setSummary("?????? ??? ??????");
            kill.setEnabled(true);
            start.setEnabled(false);
            end.setEnabled(false);
            days.setEnabled(false);
        }
        else if(sPreferences.getString("noti_style","").equals("1")) {
            style.setSummary("?????? ???????????? ??????");
            kill.setEnabled(false);
            start.setEnabled(true);
            end.setEnabled(true);
            days.setEnabled(true);
        }
        else if (sPreferences.getString("noti_style","").equals("2")) {
            style.setSummary("?????? ??????");
            kill.setEnabled(false);
            start.setEnabled(false);
            end.setEnabled(false);
            days.setEnabled(false);
        }

        style.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putString("noti_style",(String)newValue);
                editor.commit();
                if(((String)newValue).equals("0")) {
                    preference.setSummary("?????? ??? ??????");
                    kill.setEnabled(true);
                    start.setEnabled(false);
                    end.setEnabled(false);
                    days.setEnabled(false);
                }
                else if(((String)newValue).equals("1")) {
                    preference.setSummary("?????? ???????????? ??????");
                    kill.setEnabled(false);
                    start.setEnabled(true);
                    end.setEnabled(true);
                    days.setEnabled(true);
                }
                else if(((String)newValue).equals("2")) {
                    preference.setSummary("?????? ??????");
                    kill.setEnabled(false);
                    start.setEnabled(false);
                    end.setEnabled(false);
                    days.setEnabled(false);
                }
                style.setValue((String)newValue);
                Charles.setShortcutNoti(getActivity());
                return false;
            }
        });


        days.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Set<String> ret = (Set<String>) newValue;

                editor.putStringSet("time_days",ret);
                editor.commit();

                String summary = "";
                if(!ret.isEmpty()) {
                    for (String s : ret) {
                        summary += int2day(Integer.parseInt(s)) + ", ";
                    }
                    preference.setSummary(summary.substring(0,summary.length()-2));
                }
                else {
                    preference.setSummary("????????? ?????? ??????");
                }
                days.setValues(ret);
                return false;
            }
        });

        Set<String> ret = sPreferences.getStringSet("time_days", new HashSet<String>());
        String summary = "";
        if(!ret.isEmpty()) {
            for (String s : ret) {
                summary += int2day(Integer.parseInt(s)) + ", ";
            }
            days.setSummary(summary.substring(0,summary.length()-2));
        }
        else {
            days.setSummary("????????? ?????? ??????");
        }

        kill.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putBoolean(preference.getKey(),(boolean)newValue);
                editor.commit();
                Charles.setShortcutNoti(getActivity());
                kill.setChecked((boolean)newValue);
                return false;
            }
        });
    }

    private String int2day(int day) {
        switch (day) {
            case 1:
                return "???";
            case 2:
                return "???";
            case 3:
                return "???";
            case 4:
                return "???";
            case 5:
                return "???";
            case 6:
                return "???";
            case 0:
                return "???";
        }
        return "";
    }

    Preference.OnPreferenceClickListener timePickerDialog = new Preference.OnPreferenceClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String[] date = preference.getSummary().toString().split(" ");
            int hour,min;

            //??????12:00 > 0:00 ??????12:00 > 12:00
            if(date[0].equals("??????") || date[0].equals("??????")) { //12??????
                hour = Integer.parseInt(date[1].substring(0,date[1].indexOf('???')));
                min = Integer.parseInt(date[2].substring(0,date[2].indexOf('???')));

                if (date[0].equals("??????") && hour >= 1 && hour <= 11) {
                    hour += 12;
                }
                if (date[0].equals("??????") && hour == 12) {
                    hour = 0;
                }
            }
            else { //24??????
                hour = Integer.parseInt(date[0].substring(0,date[0].indexOf('???')));
                min = Integer.parseInt(date[1].substring(0,date[1].indexOf('???')));
            }

            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    int h = 0;
                    String minute_s =  "";
                    String hour_s = "";
                    String apm;
                    String prefrence;

                    if(preference == findPreference("time_Start")) {
                        prefrence = "time_Start";
                        // regist(getActivity(),hourOfDay,minute);
                    }
                    else {
                        prefrence = "time_End";
                    }
                    if(minute < 10) {
                        minute_s = "0" + minute;
                    }
                    if (DateFormat.is24HourFormat(getActivity()) == false) {
                       if (hourOfDay > 11) {
                            h = hourOfDay - 12;
                            apm = "??????";
                        }
                       else {
                           h = hourOfDay;
                           apm = "??????";
                       }
                       if (h == 0) { //?????? 12???
                           hour_s = "12";
                       }
                       else if(h < 10) {
                           hour_s = "0" + h;
                       }
                       else {
                           hour_s = Integer.toString(h);
                       }
                        preference.setSummary(apm + " " + hour_s + "??? " + minute_s + "???");
                        editor.putString(prefrence,preference.getSummary().toString());
                    }
                    else {
                        if(h < 10) {
                            hour_s = "0" + h;
                        }
                        else {
                            hour_s = Integer.toString(h);
                        }
                        preference.setSummary(hour_s + "??? " + minute_s + "???");
                        editor.putString(prefrence,preference.getSummary().toString());
                    }
                    editor.commit();
                    if(preference == findPreference("time_Start")) {
                       regist(getActivity(),hourOfDay,minute);
                    }
                    else {
                        String[] dd = sPreferences.getString("time_Start","").split(" ");
                        int hh,mm;
                        if(dd[0].equals("??????") || dd[0].equals("??????")) { //12??????
                            hh = Integer.parseInt(dd[1].substring(0,dd[1].indexOf('???')));
                            mm = Integer.parseInt(dd[2].substring(0,dd[2].indexOf('???')));

                            if (date[0].equals("??????") && hh >= 1 && hh <= 11) {
                                hh += 12;
                            }
                            if (date[0].equals("??????") && hh == 12) {
                                hh = 0;
                            }
                        }
                        else { //24??????
                            hh = Integer.parseInt(dd[0].substring(0,dd[0].indexOf('???')));
                            mm = Integer.parseInt(dd[1].substring(0,dd[1].indexOf('???')));
                        }
                        regist(getActivity(),hh,mm);
                    }
                }
            };
            TimePickerDialog dialog = new TimePickerDialog(getActivity(),listener,hour,min, DateFormat.is24HourFormat(getActivity()));
            dialog.setTitle(preference.getTitle());
            dialog.show();
            return false;
        }
    };

    public void regist(Context context, int hour, int minute) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // ????????? ????????? ?????? ??????
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);

    }// regist()..

    public void unregist(int hour,int minute) {
        Intent intent = new Intent(getActivity(), Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        alarmManager.cancel(pIntent);
    }// unregist()..
}
