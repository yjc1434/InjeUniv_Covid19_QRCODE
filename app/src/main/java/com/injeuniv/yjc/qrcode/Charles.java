package com.injeuniv.yjc.qrcode;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Charles {
    private static SharedPreferences sPreferences;
    private static String CHANNEL_ID = "QRquick";
    private static String CHANEL_NAME = "QR바로가기";
    private static ProgressDialog dialog;
    private static Thread thread;
    private static LoadingThread loadingThreaed;
    private static LoadingHandler loadingHandler;
    private static NotificationManager manager;
    private static Context activity;
    private static AlertDialog.Builder builder;


    public static void showProgressDialog(Context context, int mode) {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(mode == 1) {
            dialog.setMessage("웹으로 이동 중...");
        }
        else {
            dialog.setMessage("로딩 중...");
        }
        dialog.setCancelable(false);

        loadingThreaed = new LoadingThread();
        loadingHandler = new LoadingHandler();

        thread = new Thread(loadingThreaed);

        activity = context;

        thread.start();
        dialog.show();
    }

    public static void cancelProgressDialog() {
        if(dialog.isShowing()) {
            dialog.cancel();
            thread.interrupt();
        }
    }

    private static class LoadingThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(R.integer.TimeOut);
            } catch (InterruptedException e) {
            }
            if (dialog.isShowing()) {
                Message message = loadingHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("loadingHandler", "timeout");
                message.setData(bundle);
                loadingHandler.sendMessage(message);
            }
        }
    }

    private static class LoadingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String message = bundle.getString("loadingHandler");
            if (message.equals("timeout")) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                    builder = new AlertDialog.Builder(activity);
                    builder.setTitle("연결 실패");
                    builder.setMessage("연결 시간이 초과되었습니다.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Activity context = (Activity)activity;
                            context.finish();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }
    }

    public static void showAlertDialog(String Title, String Message, Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(Title);
        builder.setMessage(Message);
        builder.setPositiveButton("확인", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getTime(String pattern) {
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat(pattern);
        String getTime = simpleDate.format(mDate);
        return getTime;
    }

    public static void setShortcutNoti(Context context) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nbuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
            nbuilder = new NotificationCompat.Builder(context, CHANNEL_ID); //하위 버전일 경우
        } else {
            nbuilder = new NotificationCompat.Builder(context);
        }
        Intent noti = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, noti, PendingIntent.FLAG_UPDATE_CURRENT);
        nbuilder.setTicker("앱 바로가기")
                .setSmallIcon(R.drawable.noti)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentTitle("자가진단 앱 바로가기")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        if (sPreferences.getBoolean("touch_kill",false) == true) {
            nbuilder.setAutoCancel(true);
        }
        else {
            nbuilder.setAutoCancel(false);
        }

        if (sPreferences.getString("noti_style", "0").equals("2")) {
            nbuilder.setOngoing(true);
        }

        Notification notification = nbuilder.build();
        manager.notify(1, notification);
    }

    public static String parseTime(String str) {
        String[] date = str.split(" ");
        int ihour,imin;

        //오전12:00 > 0:00 오후12:00 > 12:00
        if(date[0].equals("오전") || date[0].equals("오후")) { //12시간
            ihour = Integer.parseInt(date[1].substring(0,date[1].indexOf('시')));
            imin = Integer.parseInt(date[2].substring(0,date[2].indexOf('분')));

            if (date[0].equals("오후") && ihour >= 1 && ihour <= 11) {
                ihour += 12;
            }
            if (date[0].equals("오전") && ihour == 12) {
                ihour = 0;
            }
        }
        else { //24시간
            ihour = Integer.parseInt(date[0].substring(0,date[0].indexOf('시')));
            imin = Integer.parseInt(date[1].substring(0,date[1].indexOf('분')));
        }
        return ihour + ":"  + imin;
    }

    public static void showShortcutNotiByTime(Context context) throws ParseException {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nbuilder = null;

        String start = sPreferences.getString("time_Start","");
        String end = sPreferences.getString("time_End","");

        start = parseTime(start);
        end = parseTime(end);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        Date startDate = sdf.parse(start);
        Date EndDate = sdf.parse(end);

        Intent noti = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, noti, PendingIntent.FLAG_UPDATE_CURRENT);

        nbuilder.setTicker("앱 바로가기")
                .setSmallIcon(R.drawable.noti)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentTitle("자가진단 앱 바로가기")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true);


        Notification notification = nbuilder.build();
        manager.notify(1, notification);

        Calendar nextNotifyTime = Calendar.getInstance();
        nextNotifyTime.add(Calendar.DATE, 1);
    }

    public static void cancelShortcutNoti(Context context) {
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
    }
}
