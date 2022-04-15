package com.injeuniv.yjc.qrcode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

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
import android.view.View;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AgreeActivity extends AppCompatActivity {
    private long backKeyPressedTime = 0;
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private Toast toast;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            if(toast != null) toast.cancel();
            toast = Toast.makeText(this, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            ActivityCompat.finishAffinity(AgreeActivity.this);
            if(toast != null) toast.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);
        Button agree = findViewById(R.id.agree_button1);
        Button disagree = findViewById(R.id.agree_button2);
        CheckBox checkBox = findViewById(R.id.agree_check); //7일 간

        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sPreferences.edit();

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(checkBox.isChecked()) {
                        if(toast != null) toast.cancel();
                        toast = Toast.makeText(AgreeActivity.this,"7일 간 동의 화면을 표시하지 않습니다.", Toast.LENGTH_SHORT);
                        toast.show();

                        long now = System.currentTimeMillis();
                        Date mDate = new Date(now);
                        Calendar cal = Calendar.getInstance(); //Java의 Calendar
                        cal.setTime(mDate);
                        cal.add(Calendar.DATE, 7); //7일 더하기
                        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd"); //dd(일)만 저장
                        String getTime = simpleDate.format(cal.getTime());

                        editor.putString("7days",getTime); // 끝나는 날을 저장
                        editor.commit();
                    }
                    Intent intent = new Intent(AgreeActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                catch (Exception ex) {
                    Charles.showAlertDialog("Exception",ex.getMessage(),AgreeActivity.this);
                }
            }
        });

        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AgreeActivity.this);
                builder.setTitle("인제대학교 자가진단").setMessage("자가진단을 하지 않으면 시설에 출입 할 수 없습니다. 정말로 취소하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)  {
                        ActivityCompat.finishAffinity(AgreeActivity.this);
                    }
                });
                builder.setNegativeButton("아니오", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}
