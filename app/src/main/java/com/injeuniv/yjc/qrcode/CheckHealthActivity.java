package com.injeuniv.yjc.qrcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CheckHealthActivity extends AppCompatActivity {
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private ScrollView scrollView;
    private RadioButton radioes[][] = new RadioButton[4][2];
    private RadioGroup groupes[] = new RadioGroup[4];
    private LinearLayout linearLayout;
    private TextView error;
    private String viewstate, viewstategggenerator, eventvalidaton, urlString,id,pw;
    private Map<String, String> FormData, PageData;
    private Document document;
    private CrawlingWeb thread;
    private Thread t;
    private CheckBox checkBox;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private ValueHandler handler;
    private Button checked;
    private Boolean recheck,examtable;

    @Override
    public void onBackPressed() {
        if (!recheck) {
            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                if (toast != null) toast.cancel();
                toast = Toast.makeText(this, "??? ??? ??? ????????? ?????? ???????????????.", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
                ActivityCompat.finishAffinity(CheckHealthActivity.this);
                if (toast != null) toast.cancel();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(CheckHealthActivity.this);
            builder.setTitle("?????????");
            builder.setMessage("???????????? ?????????????????????????");
            builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("?????????", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkhealth);

        FormData = new HashMap<>();
        PageData = new HashMap<>();
        document = null;
        thread = new CrawlingWeb();
        handler = new ValueHandler();

        examtable = false;

        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sPreferences.edit();


        radioes[0][0] = findViewById(R.id.radioButton1_1);
        radioes[0][1] = findViewById(R.id.radioButton1_2);
        radioes[1][0] = findViewById(R.id.radioButton2_1);
        radioes[1][1] = findViewById(R.id.radioButton2_2);
        radioes[2][0] = findViewById(R.id.radioButton3_1);
        radioes[2][1] = findViewById(R.id.radioButton3_2);
        radioes[3][0] = findViewById(R.id.radioButton4_1);
        radioes[3][1] = findViewById(R.id.radioButton4_2);


        groupes[0] = findViewById(R.id.radioGroup1);
        groupes[1] = findViewById(R.id.radioGroup2);
        groupes[2] = findViewById(R.id.radioGroup3);
        groupes[3] = findViewById(R.id.radioGroup4);


        scrollView = findViewById(R.id.scroll);

        for (int i = 0; i < radioes.length; i++) {
            radioes[i][0].setOnClickListener(radioButtonClick);
            radioes[i][1].setOnClickListener(radioButtonClick);
        }

        linearLayout = findViewById(R.id.health_error);
        error = findViewById(R.id.health_text5);

        urlString = "https://ijis.inje.ac.kr/covid19/main.aspx";

        Intent secondIntent = getIntent();
        viewstate = secondIntent.getStringExtra("__VIEWSTATE");
        viewstategggenerator = secondIntent.getStringExtra("__VIEWSTATEGENERATOR");
        eventvalidaton = secondIntent.getStringExtra("__EVENTVALIDATION");
        String state = secondIntent.getStringExtra("FROM");
        recheck = false;
        id = "";
        pw = "";
        if (state.equals("recheck")) {
            recheck = true;
        }

        if (state.equals("login1")) {
            examtable = true;
            id = secondIntent.getStringExtra("utxtID");
            pw = secondIntent.getStringExtra("utxtPassword");
        }

        TextView textView = findViewById(R.id.health_text1);
        textView.setText(Charles.getTime("yyyy??? MM??? dd???") + "\n?????? ????????? ????????? ?????? ?????????.");

        checkBox = findViewById(R.id.health_check);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check = sPreferences.getString("check", null);
                if (check != null || !checkBox.isChecked()) {
                    radioButtonAllCheck();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckHealthActivity.this);
                    builder.setTitle("????????????");
                    builder.setMessage("?????? ????????? ??????????????? ?????? ??? ????????? ?????? ???????????? ?????? ?????? ?????? ??? ?????????, ?????? ?????? ????????? ???????????? ????????????.");
                    builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            radioButtonAllCheck();
                        }
                    });
                    builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setChecked(false);
                        }
                    });
                    builder.setNeutralButton("???????????? ?????? ???????????? ??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putString("check", "no");
                            editor.commit();
                            radioButtonAllCheck();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        });

        checked = findViewById(R.id.health_button);
        checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRadioButton() == "") {
                    t = new Thread(thread);
                    t.start();
                } else {
                    errorTextView(checkRadioButton());
                }
            }
        });

        if(sPreferences.getBoolean("autocheck",false) == true && !recheck) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CheckHealthActivity.this);
            builder.setTitle("?????? ?????? ??????");
            builder.setMessage("?????? ????????? ??????????????? ?????? ??? ????????? ?????? ???????????? ?????? ?????? ?????? ??? ?????????, ?????? ?????? ????????? ???????????? ????????????.");
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < radioes.length; i++) {
                        groupes[i].clearCheck();
                        radioes[i][1].setChecked(true);
                    }
                    checked.callOnClick();
                }
            });
            builder.setNegativeButton("??????",null);

            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    View.OnClickListener radioButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkBox.setChecked(false);
        }
    };

    void radioButtonAllCheck() {
        System.out.println("AllChecked");
        for (int i = 0; i <         radioes.length; i++) {
            groupes[i].clearCheck();
            radioes[i][1].setChecked(checkBox.isChecked());
        }
    }

    public String checkRadioButton() {
        String message = "";
        for (int i = 0; i < radioes.length; i++) {
            if (radioes[i][0].isChecked() == radioes[i][1].isChecked()) {
                switch (i) {
                    case 0:
                        message += "* 1. ????????? ?????????19 ??????????????? ??????????\n" +
                                "(?????? ????????????) ??????(37.5?????????, ??????, ????????????, ??????, ?????????, ?????????, ?????? ??? ????????????)";
                        break;
                    case 1:
                        message += "* 2. ????????? ???????????? ??? ?????? ?????????????????? ?????? ??? 10???(???????????? ??????????????? 14???) ????????????????";
                        break;
                    case 2:
                        message += "* 3.  ????????? ??????/????????????/?????????19 ?????? ??? ?????? ???????????????????";
                        break;
                    case 3:
                        message += "* 4. ???????????? ??????/????????????/?????????19 ?????? ??? ?????? ???????????????????";
                        break;
                }
                message += "???(???) ??????????????? ?????????.\n";
            }
        }
        return message.trim();
    }

    void errorTextView(String errorMessage) {
        linearLayout.setVisibility(View.VISIBLE);
        error.setText(errorMessage);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    class CrawlingWeb implements Runnable {
        @Override
        public void run() {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() { // ????????? ???????????? ????????? ??????
                        Charles.showProgressDialog(CheckHealthActivity.this,0);
                    }
                });
                System.out.println(id + " + " + pw);
                FormData.clear();
                FormData.put("__EVENTTARGET", "");
                FormData.put("__EVENTARGUMENT", "");
                FormData.put("__VIEWSTATE", viewstate);
                FormData.put("__EVENTVALIDATION", eventvalidaton);
                FormData.put("__VIEWSTATEGENERATOR", viewstategggenerator);
                if (recheck)
                    PageData.put("ubtn?????????", "?????????");
                else if (examtable)
                {
                    PageData.put("utxtID", id);
                    PageData.put("utxtPassword", pw);
                    PageData.put("ubtn?????????", "?????????");
                }
                else
                    PageData.put("ubtn??????_??????", "????????????");

                document = Jsoup.connect(urlString)
                        .data(FormData).data(PageData).postDataCharset("UTF-8").post();

                System.out.println("0"+ document.text());

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
                PageData.put("urbt???", returnState(radioes[0][0]));
                PageData.put("urbt????????????", returnState(radioes[1][0]));
                PageData.put("urbt????????????", returnState(radioes[2][0]));
                PageData.put("urbt?????????", returnState(radioes[3][0]));
                PageData.put("ubtn??????", "??????");

                document = Jsoup.connect(urlString)
                        .data(FormData).data(PageData).postDataCharset("UTF-8").post();

                System.out.println(document.text());

                String resultDay = document.select("#udiv_result").text(); //2021.06.07

                if (resultDay.equals(Charles.getTime("yyyy.MM.dd"))) { //????????????~
                    String imageUrl = document.select("#uimg??????").attr("src");
                    imageUrl = imageUrl.substring(imageUrl.indexOf(',') + 1);

                    viewstate = document.select("#__VIEWSTATE").attr("value");
                    eventvalidaton = document.select("#__EVENTVALIDATION").attr("value");
                    viewstategggenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");

                    editor.putString("URL", imageUrl);
                    editor.putString("URLDATE", Charles.getTime("yyyy.MM.dd"));
                    editor.putString("URLVALUE", document.text());
                    editor.putString("__VIEWSTATE", viewstate);
                    editor.putString("__EVENTVALIDATION", eventvalidaton);
                    editor.putString("__VIEWSTATEGENERATOR", viewstategggenerator);
                    String isPass = document.select("#udiv_result").attr("class");
                    editor.putString("isPass", isPass);
                    editor.commit();
                    Intent intent = new Intent(CheckHealthActivity.this, QRActivity.class);
                    intent.putExtra("QR", imageUrl);
                    intent.putExtra("data", document.text());
                    intent.putExtra("isPass", isPass);
                    handler.post(new Runnable() {
                        @Override
                        public void run() { // ????????? ???????????? ????????? ??????
                            Charles.cancelProgressDialog();
                        }
                    });
                    startActivity(intent);
                    finish();
                } else { //??????
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "error");
                    Message message = new Message();
                    message.setData(bundle);
                    handler.sendMessage(message);
                    handler.post(new Runnable() {
                        @Override
                        public void run() { // ????????? ???????????? ????????? ??????
                            Charles.cancelProgressDialog();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ValueHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle.getString("message").equals("error")) {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(CheckHealthActivity.this, "??? ??? ?????? ????????? ??????????????????. ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT);
                toast.show();
                Charles.cancelProgressDialog();
                Intent intent = new Intent(CheckHealthActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public String returnState(RadioButton c) {
        return c.isChecked() ? "True" : "False";
    }
}