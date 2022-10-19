package com.tignioj.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.tignioj.broadcast.MyClockReceiver;

public class MyClockService extends Service {
    public MyClockService() {
    }


    private MyClockReceiver receiver;

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);

        receiver = new MyClockReceiver(getApplication());
        registerReceiver(receiver, intentFilter);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
    }
}