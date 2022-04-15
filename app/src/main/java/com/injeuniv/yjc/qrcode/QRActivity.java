package com.injeuniv.yjc.qrcode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QRActivity extends AppCompatActivity {
    private long backKeyPressedTime = 0;
    private SharedPreferences sPreferences;
    private SharedPreferences.Editor editor;
    private Toast toast;
    private SwipeRefreshLayout layout;
    private String ImageURL, isPass;
    private String Data[];
    private refreshHandler handler;
    private CrawlingWeb thread;
    private TextView tx1, tx2, tx3, tx6, date, state, update;
    private ImageView ImageView, ImageView2, ImageView3;
    private byte[] bytePlainOrg;
    private TimerHandler2 tHandler;
    private Button bWebView;
    private Thread t, t2;
    private LinearLayout normal, big;
    private Bitmap bm;
    private boolean isFirst,loginPass;
    private Map<String, String> FormData, PageData;
    private String urlString;
    private String viewstate, eventvalidaton, viewstategggenerator;
    private String viewstate2, eventvalidaton2, viewstategggenerator2;
    private String getVersion;
    private UpdateHandler handler2;

    @Override
    protected void onResume() {
        String nowTime = Charles.getTime("yyyy.MM.dd");
        String updateTime = sPreferences.getString("URLDATE","");
        if (!nowTime.equals((updateTime))) {
            layout.setRefreshing(true);
            refresh();
        }
        super.onResume();
    }

    public String getVersionInfo(Context context) {
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    class UpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String text = bundle.getString("message_u");
            String[] data = text.split(" ");

            getVersion = data[2];

            editor.putString("newVersion",getVersion);
            editor.putString("nowVersion",getVersionInfo(QRActivity.this));
            editor.putString("newLink",data[4]);
            editor.commit();

            if(!getVersion.equals(getVersionInfo(QRActivity.this))) {
                update.setVisibility(View.VISIBLE);
            } else {
                update.setVisibility(View.GONE);
            }
        }
    }

    class UpdateInfo implements Runnable {
        @Override
        public void run() {
            try {
                String urlString = "https://github.com/yjc1434/InjeUniv_Covid19_QRCODE/blob/main/README.md";
                Document document = Jsoup.connect(urlString).get();
                String text = document.select(".readme").text();
                Message message = handler2.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("message_u", text);
                message.setData(bundle);
                handler2.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class CrawlingWeb implements Runnable {
        @Override
        public void run() {
            try {
                loginPass = false;
                Document document;
                Bundle bundle = new Bundle();

                //System.out.println("Thread Run!");
                String id, pw;

                id = sPreferences.getString("ID", null);
                pw = sPreferences.getString("Password", null);

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
                    } else if (i == 3) {
                        String isError = document.select(".errBox").attr("style");
                        if (isError.equals("color:Red;")) { //에러가 있다는 것?
                            Message message = handler.obtainMessage();
                            String errorMessage = document.select("#uvs로그인").text();
                            bundle.putString("message", errorMessage);
                            message.setData(bundle);
                            handler.sendMessage(message);
                            break;
                        } else if (document.select("#ubtn메뉴_진단").attr("value").equals("자가진단")){
                            viewstate2=viewstate;
                            eventvalidaton2=eventvalidaton;
                            viewstategggenerator2=viewstategggenerator;
                            PageData.put("ubtn메뉴_진단", "자가진단");
                        }
                        else {
                            loginPass = true;
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
                        String isTodayFirst = document.select(".pointEtc").text();

                        if (isTodayFirst.equals("거짓 확인시 「감염병의 예방 및 관리에 관한 법률」에 의해 처벌 받을 수 있습니다. ※ 1개이상 증상시 등교 불가 ※")) {
                            bundle.putString("message", "healthcheck");
                            break;
                        } else {
                            ImageURL = document.select("#uimg사진").attr("src");
                            ImageURL = ImageURL.substring(ImageURL.indexOf(',') + 1);

                            viewstate = document.select("#__VIEWSTATE").attr("value");
                            eventvalidaton = document.select("#__EVENTVALIDATION").attr("value");
                            viewstategggenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");

                            editor.putString("URL", ImageURL);
                            editor.putString("URLDATE", Charles.getTime("yyyy.MM.dd"));
                            editor.putString("URLVALUE", document.text());

                            editor.putString("__VIEWSTATE", viewstate);
                            editor.putString("__EVENTVALIDATION", eventvalidaton);
                            editor.putString("__VIEWSTATEGENERATOR", viewstategggenerator);

                            isPass = document.select("#udiv_result").attr("class");
                            editor.putString("isPass", isPass);
                            editor.commit();

                            Data = document.text().split(" ");

                            bundle.putString("message", "refresh");
                        }
                    }
                }
                Message message = handler.obtainMessage();
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (IOException ex) {
                handler.post(new Runnable() {
                    @Override
                    public void run() { // 화면에 변경하는 작업을 구현
                        layout.setRefreshing(false);
                        t2.interrupt();
                        Charles.showAlertDialog("네트워크 오류", "네트워크에 연결되어 있지 않습니다.", QRActivity.this);
                    }
                });
            }
        }
    }

    class refreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                if (message.equals("refresh")) {
                    setQRImage();
                    setDayCircle();
                    layout.setRefreshing(false);
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(QRActivity.this, "새로고침되었습니다.", Toast.LENGTH_SHORT);
                    t2.interrupt();
                    toast.show();
                } else if (message.equals("healthcheck")) {
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(QRActivity.this, "QR코드의 기한이 만료되었습니다. 자가진단을 재실행합니다.", Toast.LENGTH_SHORT);
                    Intent intent = new Intent(QRActivity.this, CheckHealthActivity.class);
                    intent.putExtra("__VIEWSTATE", viewstate2);
                    intent.putExtra("__EVENTVALIDATION", eventvalidaton2);
                    intent.putExtra("__VIEWSTATEGENERATOR", viewstategggenerator2);

                    if (loginPass == true) {
                        System.out.println("loginPass");
                        intent.putExtra("FROM", "login1");
                        String id,pw;
                        id = sPreferences.getString("ID","");
                        pw = sPreferences.getString("Password","");
                        intent.putExtra("utxtID", id);
                        intent.putExtra("utxtPassword", pw);
                    }
                    else {
                        intent.putExtra("FROM", "login");
                    }

                    layout.setRefreshing(false);
                    t2.interrupt();
                    toast.show();
                    startActivity(intent);
                } else { //error
                    if (toast != null) toast.cancel();
                    toast = Toast.makeText(QRActivity.this, "ID 또는 비밀번호의 정보가 변경되었습니다. 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT);
                    layout.setRefreshing(false);
                    t2.interrupt();
                    toast.show();
                    editor.putBoolean("Auto",false);
                    editor.commit();
                    Intent intent = new Intent(QRActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
            catch (Exception ex) {
                Charles.showAlertDialog("Exception",ex.getMessage(),QRActivity.this);
            }
        }
    }

    public void refresh() {
        t = new Thread(thread);
        t2 = new Thread(new Timer());

        t.start();
        t2.start();
    }

    public void setQRImage() {
        bytePlainOrg = Base64.decode(ImageURL, 0);
        //byte[] 데이터  stream 데이터로 변환 후 bitmapFactory로 이미지 생성
        ByteArrayInputStream inStream = new ByteArrayInputStream(bytePlainOrg);
        bm = BitmapFactory.decodeStream(inStream);
        ImageView.setImageBitmap(bm);
    }


    public void setDayCircle() {
        state.setTextColor(Color.WHITE);
        date.setTextColor(Color.WHITE);
        if (isPass.equals("resultCircle tCircle cBG_Monday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.mon);
        } else if (isPass.equals("resultCircle tCircle cBG_Tuesday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.tue);
        } else if (isPass.equals("resultCircle tCircle cBG_Wednesday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.wed);
        } else if (isPass.equals("resultCircle tCircle cBG_Thursday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.tur);
        } else if (isPass.equals("resultCircle tCircle cBG_Friday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.fri);
        } else if (isPass.equals("resultCircle tCircle cBG_Saturday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.sat);
        } else if (isPass.equals("resultCircle tCircle cBG_Sunday")) {
            state.setText("등교가능");
            ImageView2.setImageResource(R.drawable.sun);
        } else {
            state.setText("등교불가");
            state.setText("등교불가");
            ImageView2.setImageResource(R.drawable.no);
            state.setTextColor(Color.RED);
            date.setTextColor(Color.RED);
        }
        int index = 0;
        for (int i = 0; i < Data.length;i++) {
            if (Data[i].equals("소속")) {
                index = i;
            }
        }
        if (index == 0) {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(QRActivity.this, "오류가 발생했습니다. 계속될 경우 웹브라우저를 이용하여 로그인해주세요.", Toast.LENGTH_SHORT);
            toast.show();

            editor.remove("URL");
            editor.remove("URLDATE");
            editor.remove("URLVALUE");
            editor.remove("__VIEWSTATE");
            editor.remove("__EVENTVALIDATION");
            editor.remove("__VIEWSTATEGENERATOR");
            editor.remove("Auto");
            editor.commit();

            Intent intent = new Intent(QRActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            date.setText(Data[index - 1]);
            tx1.setText(Data[index+1] + " " + Data[index+2]);
            tx2.setText(Data[index+4]);
            tx3.setText(Data[index+6]);

            tx1.setSingleLine();
            tx1.setEllipsize(TextUtils.TruncateAt.END);

            editor.putString("group", (String) tx1.getText());
            editor.putString("name", (String) tx3.getText());
            editor.commit();

            UpdateInfo thread = new UpdateInfo();
            Thread t = new Thread(thread);
            t.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_result);

            Intent secondIntent = getIntent();

            sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            editor = sPreferences.edit();

            ImageURL = secondIntent.getStringExtra("QR");
            Data = secondIntent.getStringExtra("data").split(" ");
            isPass = secondIntent.getStringExtra("isPass");

            FormData = new HashMap<String, String>();
            PageData = new HashMap<String, String>();

            layout = findViewById(R.id.swipe_layout);
            ImageView = findViewById(R.id.qrView);
            ImageView2 = findViewById(R.id.circleView);

            tx1 = findViewById(R.id.result_1);
            tx2 = findViewById(R.id.result_2);
            tx3 = findViewById(R.id.result_3);
            tx6 = findViewById(R.id.result_title);

            date = findViewById(R.id.newDate);
            state = findViewById(R.id.newState);

            thread = new CrawlingWeb();
            handler = new refreshHandler();
            tHandler = new TimerHandler2();
            handler2 = new UpdateHandler();

            normal = findViewById(R.id.normalLayout);
            big = findViewById(R.id.bigLayout);

            urlString = "https://ijis.inje.ac.kr/covid19/main.aspx";

            isFirst = false;

            setQRImage();
            setDayCircle();

            layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            });

            ImageView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    try {
                        normal.setVisibility(View.GONE);
                        big.setVisibility(View.VISIBLE);

                        if (!isFirst) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(normal.getWidth(), normal.getHeight());
                            big.setLayoutParams(params);
                            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            ImageView3 = findViewById(R.id.bigQR);
                            ImageView3.setImageBitmap(bm);
                            ImageView3.setLayoutParams(params);

                            isFirst = true;
                            ImageView3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    normal.setVisibility(View.VISIBLE);
                                    big.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(QRActivity.this);
                        builder.setTitle("Exception");
                        builder.setMessage(ex.getMessage());
                        builder.setPositiveButton("확인", null);
                        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            });

            Button retry = findViewById(R.id.result_retry);
            Button exit = findViewById(R.id.result_exit);

            bWebView = findViewById(R.id.result_qr);

            bWebView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toast != null) toast.cancel();
                    Intent intent = new Intent(QRActivity.this, WebViewActivity.class);
                    startActivity(intent);
                }
            });


            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(QRActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });

            update = findViewById(R.id.result_update);

            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(QRActivity.this, CheckHealthActivity.class);
                    viewstate = sPreferences.getString("__VIEWSTATE", null);
                    viewstategggenerator = sPreferences.getString("__VIEWSTATEGENERATOR", null);
                    eventvalidaton = sPreferences.getString("__EVENTVALIDATION", null);
                    intent.putExtra("__VIEWSTATE", viewstate);
                    intent.putExtra("__EVENTVALIDATION", eventvalidaton);
                    intent.putExtra("__VIEWSTATEGENERATOR", viewstategggenerator);
                    intent.putExtra("FROM", "recheck");
                    startActivity(intent);
                }
            });
            if (sPreferences.getBoolean("notion", false) == true) {
                Charles.setShortcutNoti(this);
            }
        } catch (Exception ex) {
            Charles.showAlertDialog("Exception", ex.getMessage(), this);
        }

    }


    @Override
    public void onBackPressed() {
        if (normal.getVisibility() == View.GONE) {
            ImageView3.callOnClick();
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
                ActivityCompat.finishAffinity(QRActivity.this);
                toast.cancel();
            }
        }
    }

    class Timer implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(R.integer.TimeOut);
            } catch (InterruptedException e) {
                System.out.println("스레드 중단됨");
            }
            if (layout.isRefreshing()) {
                System.out.println("열려있다!");
                Message message = tHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("message4", "open");
                message.setData(bundle);
                tHandler.sendMessage(message);
            }
        }
    }

    class TimerHandler2 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle.getString("message4").equals("open")) {
                layout.setRefreshing(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(QRActivity.this);
                builder.setTitle("연결 실패");
                builder.setMessage("연결 시간이 초과되었습니다.");
                builder.setPositiveButton("확인", null);
                builder.setNegativeButton("재시도", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        layout.setRefreshing(true);
                        refresh();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }
}
