package com.injeuniv.yjc.qrcode;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SettingsActivity extends AppCompatActivity {
    public int status;
    @Override
    public void onBackPressed() {
        if(status == 1) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // 프래그먼트매니저를 통해 사용
            SettingPreference fragment1= new SettingPreference(); // 객체 생성
            transaction.replace(R.id.settings_container, fragment1); //layout, 교체될 layout
            transaction.commit(); //commit으로 저장 하지 않으면 화면 전환이 되지 않음
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 프래그먼트매니저를 통해 사용
        SettingPreference fragment1= new SettingPreference(); // 객체 생성
        transaction.replace(R.id.settings_container, fragment1); //layout, 교체될 layout
        transaction.commit(); //commit으로 저장 하지 않으면 화면 전환이 되지 않음
    }
}