package com.tignioj.broadcast;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.tignioj.entity.AppSetting;
import com.tignioj.viewmodel.MyViewModel;

import java.util.Date;

public class MyClockReceiver extends BroadcastReceiver {

    Application application;
    public final String CLOCK_TAG = "MyClockReceive";

    public MyClockReceiver(Application application) {
        this.application = application;
    }
    public MyClockReceiver() {}
    public static int minutes = 60; // 更新频率,单位分钟（因为ACTION_TIME_TICK一分钟一次广播)

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(CLOCK_TAG, action);
        if (action.equals(Intent.ACTION_TIME_TICK)) { // 检测分钟变化
            minutes--;
            Log.d(CLOCK_TAG, String.valueOf(new Date()) + "是否更新:" + (minutes<=0) +
                    ",还剩下" + minutes + "分钟后更新");
            if((minutes)<=0) {{
                MyViewModel myViewModel = new MyViewModel((Application) context.getApplicationContext());
                AppSetting appSetting = myViewModel.getAppSetting().getValue();
                if (appSetting!=null && appSetting.isShowWeather()){
                    myViewModel.updateWeathers();
                    Log.d(CLOCK_TAG, "更新天气完成");
                }
                minutes = 60;
            }}
        }

        if (action.equals(Intent.ACTION_TIME_CHANGED) || //
                action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                action.equals(Intent.ACTION_DATE_CHANGED)
        ) {
            Log.d(CLOCK_TAG,"检测到时间改变操作:" + action);
            doWorkSon(context);
        }
        if (action.equals(Intent.ACTION_DATE_CHANGED)) {
            doWorkSon(context);
        }
    }

    private void doWorkSon(Context context) {
        MyViewModel myViewModel = new MyViewModel((Application) context.getApplicationContext());
        myViewModel.updateCountDownDay();
        AppSetting value = myViewModel.getAppSetting().getValue();
        if (value!=null)  { myViewModel.updateWeathers(); }
    }
}