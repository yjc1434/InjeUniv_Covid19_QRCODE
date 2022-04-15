package com.injeuniv.yjc.qrcode;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //7일 간 보지않기 구현
        SharedPreferences sPrefrences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean pass = false;
        try {
            if ((sPrefrences.getString("darkmode","").equals("0"))) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if ((sPrefrences.getString("darkmode","").equals("1"))) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if ((sPrefrences.getString("darkmode","").equals("2"))) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            boolean superAgree = sPrefrences.getBoolean("superagree",false);
            if (superAgree == true) {
                pass = true;
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                String data = sPrefrences.getString("7days", null);
                if (data != null) {
                    long now = System.currentTimeMillis();
                    SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
                    Date target = simpleDate.parse(data); //동의날짜+7일 지난 날짜
                    Date today =  new Date(now); //오늘

                    if (target.after(today)) {
                        pass = true;
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else { //7일이 지난 날이 되면 데이터 삭제하기
                        SharedPreferences.Editor editor = sPrefrences.edit();
                        editor.remove("7days");
                        editor.commit();
                    }
                }
                if(!pass) {
                    Intent intent = new Intent(MainActivity.this, AgreeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } catch (Exception ex) {
            Charles.showAlertDialog("Exception",ex.getMessage(),MainActivity.this);
        }
    }
}
