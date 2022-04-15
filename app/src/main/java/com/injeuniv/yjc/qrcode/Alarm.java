package com.injeuniv.yjc.qrcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Charles.showShortcutNotiByTime(context);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
