package com.injeuniv.yjc.qrcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private Button login;
    private TextView Search, Error;
    private EditText ID, PW;
    private CheckBox cb1, cb2;
    private LinearLayout linearLayout;
    private Map<String, String> FormData, PageData;
    private String id, pw, urlString;
    private Document document;
    private CrawlingWeb thread;
    private ValueHandler handler;
    //private TimerHandler tHandler;
    private long backKeyPressedTime;
    private ProgressDialog dialog;
    private Toast toast;
    private InputMethodManager imm;
    private Thread t,timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            //init Value
            sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            editor = sPreferences.edit();
            FormData = new HashMap<String, String>();
            PageData = new HashMap<String, String>();
            document = null;
            urlString = "https://ijis.inje.ac.kr/covid19/main.aspx";
            backKeyPressedTime = 0;
            thread = new CrawlingWeb();


            //findViewID
            login = findViewById(R.id.login_button);
            ID = findViewById(R.id.editText_ID);
            PW = findViewById(R.id.editText_PW);
            cb1 = findViewById(R.id.login_check1);
            cb2 = findViewById(R.id.login_check2);
            Search = findViewById(R.id.login_text4);
            Error = findViewById(R.id.login_text5);
            linearLayout = findViewById(R.id.login_error);

            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            handler = new ValueHandler();
            //tHandler = new TimerHandler();

            cb2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cb2.isChecked())
                        cb1.setChecked(true);
                }
            });

            PW.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_DONE:
                            login.callOnClick();
                            break;
                    }
                    return true;
                }
            });

            PW.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER) {
                        login.callOnClick();
                    }
                    return false;
                }
            });

            //setOnClickListener
            login.setOnClickListener(LoginButton);
            Search.setOnClickListener(searchIDPW);

            //loadValue
            setRememberLoginCheck();
            //
        } catch (Exception ex) {
            Charles.showAlertDialog("Exception",ex.getMessage(),this);
            imm.hideSoftInputFromWindow(ID.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(PW.getWindowToken(), 0);
        }
    }



    public void setRememberLoginCheck() {
        //Remember ID
        String temp;
        boolean rememberID = sPreferences.getBoolean("rID",false);
        if (rememberID == true) {
            temp = sPreferences.getString("ID", null);
            ID.setText(temp);
            cb1.setChecked(true);
            //Auto Login
            boolean autologin = sPreferences.getBoolean("Auto",false);
            if (autologin == true) {
                cb2.setChecked(true);
                temp = sPreferences.getString("Password", null);
                if (temp != null) { //
                    PW.setText(temp);
                    login.callOnClick();
                } else {
                    if(toast != null) toast.cancel();
                    toast = Toast.makeText(LoginActivity.this, "로그인 에러", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } else {
                cb2.setChecked(false);
                PW.requestFocus();

            }
        } else {
            cb1.setChecked(false);
            ID.requestFocus();
        }
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    void errorTextView(String errorMessage) {
        try {
            linearLayout.setVisibility(View.VISIBLE);
            Error.setText(errorMessage);
            PW.setText("");
            PW.setSelected(true);
            imm.showSoftInput(PW, 0);
            cb2.setChecked(false);
            editor.putBoolean("Auto",false);
            editor.commit();
        } catch (Exception ex) {
            Charles.showAlertDialog("Exception", ex.getMessage(),this);
        }
    }

    View.OnClickListener searchIDPW = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(toast != null) toast.cancel();
            toast = Toast.makeText(LoginActivity.this, "웹으로 이동합니다.", Toast.LENGTH_SHORT);
            toast.show();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stud.inje.ac.kr/SYS/SYS04040WS.aspx"));
            startActivity(browserIntent);
        }
    };

    View.OnClickListener LoginButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Boolean QR = false;
            id = ID.getText().toString();
            pw = PW.getText().toString();
            imm.hideSoftInputFromWindow(ID.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(PW.getWindowToken(), 0);
            if (!id.isEmpty() && !pw.isEmpty()) {
                String text;
                boolean autologin = sPreferences.getBoolean("Auto", false);
                if (autologin == true) {
                    autologin = sPreferences.getBoolean("quickqr",false);
                    if(autologin == true) {
                        text = sPreferences.getString("URLDATE", null);
                        if (text != null) {
                            if (text.equals(Charles.getTime("yyyy.MM.dd"))) {
                                QR = true;
                                text = sPreferences.getString("URL", null);
                                Intent intent = new Intent(LoginActivity.this, QRActivity.class);
                                intent.putExtra("QR", text);
                                text = sPreferences.getString("URLVALUE", null);
                                intent.putExtra("data", text);
                                text = sPreferences.getString("isPass", null);
                                intent.putExtra("isPass", text);
                                startActivity(intent);
                                finish();
                            } else {
                                editor.remove("URL");
                                editor.remove("URLDATE");
                                editor.remove("URLVALUE");
                                editor.remove("__VIEWSTATE");
                                editor.remove("__EVENTVALIDATION");
                                editor.remove("__VIEWSTATEGENERATOR");
                            }
                        }
                    }
                }
                if (!QR) {
                    //showProgressDialog();
                    Charles.showProgressDialog(LoginActivity.this,0);
                    text = ID.getText().toString(); // 사용자가 입력한 저장할 데이터
                    editor.putString("ID", text); // key, value를 이용하여 저장하는 형태
                    if (cb1.isChecked()) {
                        editor.putBoolean("rID", true); // key, value를 이용하여 저장하는 형태
                    } else {
                        editor.remove("rID");
                    }
                    if (cb2.isChecked()) {
                        text = PW.getText().toString(); // 사용자가 입력한 저장할 데이터
                        editor.putString("Password", text); // key, value를 이용하여 저장하는 형태
                        editor.putBoolean("Auto", true); // key, value를 이용하여 저장하는 형태
                    } else {
                        editor.remove("Password");
                        editor.remove("Auto");
                    }
                    editor.commit();

                    //앞에서 로그인 안됬을 때!!!
                    t = new Thread(thread);
                    t.start();
                }
            } else {
                String message = "";
                if (id.equals("")) {
                    message += "* 사용자ID는 입력되여야 합니다.";
                }
                if (pw.equals("")) {
                    if (message != "") message += "\n* 비밀번호는 입력되여야 합니다.";
                    else message += "* 비밀번호는 입력되여야 합니다.";
                }
                errorTextView(message);
            }
        }
    };

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
            ActivityCompat.finishAffinity(LoginActivity.this);
            if(toast != null) toast.cancel();
        }
    }


    class CrawlingWeb implements Runnable {
        @Override
        public void run() {
            try {
                //System.out.println("Thread Run!");
                String viewstate,viewstate2 = "";
                String eventvalidaton,eventvalidaton2 = "";
                String viewstategggenerator,viewstategggenerator2 = "";
                boolean loginPass = false;
                FormData.clear();
                PageData.clear();
                for (int i = 0; i < 5; i++) {
                    document = Jsoup.connect(urlString)
                            .data(FormData).data(PageData).postDataCharset("UTF-8").post();
                    viewstate = document.select("#__VIEWSTATE").attr("value");
                    eventvalidaton = document.select("#__EVENTVALIDATION").attr("value");
                    viewstategggenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");

                    FormData.clear();
                    FormData.put("__EVENTTARGET", "");
                    FormData.put("__EVENTARGUMENT", "");
                    FormData.put("__VIEWSTATE", viewstate);
                    FormData.put("__EVENTVALIDATION", eventvalidaton);
                    FormData.put("__VIEWSTATEGENERATOR", viewstategggenerator);

                    PageData.clear();
                    if (i == 0) {
                        PageData.put("ubtn동의", "동의");
                    } else if (i == 1) {
                        PageData.put("ubtn사용자_인제정보시스템", "인제정보시스템 사용자");
                    } else if (i == 2) {
                        PageData.put("utxtID", id);
                        PageData.put("utxtPassword", pw);
                        PageData.put("ubtn로그인", "로그인");

                        viewstate2=viewstate;
                        eventvalidaton2=eventvalidaton;
                        viewstategggenerator2=viewstategggenerator;

                    } else if (i == 3) {
                        String isError = document.select(".errBox").attr("style");
                        if (isError.equals("color:Red;")) { //에러가 있다는 것?
                            String errorMessage = document.select("#uvs로그인").text();
                            Message message = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("message", errorMessage);
                            message.setData(bundle);
                            handler.sendMessage(message);
                            handler.post(new Runnable() {
                                @Override
                                public void run() { // 화면에 변경하는 작업을 구현
                                    Charles.cancelProgressDialog();
                                }
                            });
                            System.out.println("로그인 오류");
                            break;
                        } else if (document.select("#ubtn메뉴_진단").attr("value").equals("자가진단")){
                            viewstate2=viewstate;
                            eventvalidaton2=eventvalidaton;
                            viewstategggenerator2=viewstategggenerator;
                            PageData.put("ubtn메뉴_진단", "자가진단");
                            System.out.println("시험시간표 버튼");
                        }
                        else {
                            loginPass = true;
                            System.out.println("loginPass true"); //이미 여기서 확인 페이지 넘어왔음
                        }
                    }
                    if (i == 4 || (i == 3 && loginPass == true)) {
                        if(i == 4) {
                            document = Jsoup.connect(urlString)
                                    .data(FormData).data(PageData).postDataCharset("UTF-8").post();
                            System.out.println("i == 4");
                        }
                        else {
                            System.out.println("i == 3");
                        }
                        System.out.println(document.text());
                        String isTodayFirst = document.select(".pointEtc").text();
                        System.out.println(isTodayFirst);
                        if (isTodayFirst.equals("거짓 확인시 「감염병의 예방 및 관리에 관한 법률」에 의해 처벌 받을 수 있습니다. ※ 1개이상 \"예\" 선택시 등교 불가 ※")) {
                            //check 로 이동
                            Intent intent = new Intent(LoginActivity.this, CheckHealthActivity.class);
                            intent.putExtra("__VIEWSTATE", viewstate2);
                            intent.putExtra("__EVENTVALIDATION", eventvalidaton2);
                            intent.putExtra("__VIEWSTATEGENERATOR", viewstategggenerator2);
                            if (loginPass == true) {
                                System.out.println("loginPass");
                                intent.putExtra("FROM", "login1");
                                intent.putExtra("utxtID", id);
                                intent.putExtra("utxtPassword", pw);
                            }
                            else {
                                intent.putExtra("FROM", "login");
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void     run() { // 화면에 변경하는 작업을 구현
                                    Charles.cancelProgressDialog();
                                }
                            });
                            startActivity(intent);
                            finish();
                            break;
                        } else {

                            viewstate = document.select("#__VIEWSTATE").attr("value");
                            eventvalidaton = document.select("#__EVENTVALIDATION").attr("value");
                            viewstategggenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");

                            String imageUrl = document.select("#uimg사진").attr("src");
                            imageUrl = imageUrl.substring(imageUrl.indexOf(',') + 1);

                            editor.putString("URL", imageUrl);
                            editor.putString("URLDATE", Charles.getTime("yyyy.MM.dd"));
                            editor.putString("URLVALUE", document.text());

                            //재진단용 데이터 - 로그인 페이지
                            editor.putString("__VIEWSTATE", viewstate);
                            editor.putString("__EVENTVALIDATION", eventvalidaton);
                            editor.putString("__VIEWSTATEGENERATOR", viewstategggenerator);
                            //

                            String isPass = document.select("#udiv_result").attr("class");
                            editor.putString("isPass", isPass);
                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this, QRActivity.class);
                            intent.putExtra("QR", imageUrl);
                            intent.putExtra("data", document.text());
                            intent.putExtra("isPass", isPass);
                            handler.post(new Runnable() {
                                @Override
                                public void run() { // 화면에 변경하는 작업을 구현
                                    Charles.cancelProgressDialog();
                                }
                            });
                            startActivity(intent);
                        }
                        finish();
                    }
                }
            } catch (IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() { // 화면에 변경하는 작업을 구현
                        Charles.cancelProgressDialog();
                        String message;
                        if (e.getMessage().equals("Chain validation failed")) {
                            message = "네트워크 인증 정보가 올바르지 않습니다.";
                        } else {
                            message = "네트워크에 연결되어 있지 않습니다.";
                        }

                        Charles.showAlertDialog("네트워크 오류", message,LoginActivity.this);
                        imm.hideSoftInputFromWindow(ID.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(PW.getWindowToken(), 0);
                    }
                });
            }
        }
    }


    class ValueHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle.getString("message").equals("check")) {
                setContentView(R.layout.activity_checkhealth);
            }
            errorTextView("* " + bundle.getString("message"));
        }
    }
}
