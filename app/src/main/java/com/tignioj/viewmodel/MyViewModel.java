package com.tignioj.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.tignioj.config.MyConfig;
import com.tignioj.entity.AppSetting;
import com.tignioj.entity.CountDownDay;
import com.tignioj.entity.Weather3DayBean;
import com.tignioj.entity.WeatherDay;
import com.tignioj.util.MyDateUtils;
import com.tignioj.util.MyWeatherUtil;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MyViewModel extends AndroidViewModel {
    private static final String TAG = "MyViewModel";
    private static MutableLiveData<List<WeatherDay>> weathers; // 天气数据
    private static MutableLiveData<String> lastUpdate; // 上次更新天气时间
    private static MutableLiveData<CountDownDay> countdownDay; // 倒数日
    private static MutableLiveData<AppSetting> appSetting; // 应用设置

    public MyViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getLastUpdate() {
        if (lastUpdate == null) lastUpdate = new MutableLiveData<>();
        return lastUpdate;
    }

    public void updateLastUpdate(String update) {
        lastUpdate.postValue(update);
    }

    public LiveData<List<WeatherDay>> getWeathers() {
        if (weathers == null) {
            weathers = new MutableLiveData<>();
            loadWeathers();
        }
        return weathers;
    }

    public LiveData<CountDownDay> getCountDownDay() {
        if (countdownDay == null) {
            countdownDay = new MutableLiveData<>();
            CountDownDay countDownDay = getDownDayFromSHDB();
            countdownDay.postValue(countDownDay);
        }
        return countdownDay;
    }

    private CountDownDay getDefaultCountDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int i = c.get(Calendar.YEAR);
        Log.d(TAG, c.getTime().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date nYear;
        try {
            nYear = sdf.parse(String.valueOf(i + 1));
        } catch (ParseException e) {
            e.printStackTrace();
            nYear = new Date();
        }

        return new CountDownDay(nYear, "距离新的一年还剩下", "说点什么？");
    }

    @NonNull
    private CountDownDay getDownDayFromSHDB() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        CountDownDay cd = getDefaultCountDay();
        String string = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_TARGET_DATE, MyDateUtils.format(cd.getDate()));
        String eventText = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_EVENT_TEXT, cd.getEvent());
        String eventNotes = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_NOTES, cd.getNote());
        return new CountDownDay(MyDateUtils.parse(string), eventText, eventNotes);
    }

    private void saveCountDownDayToSHDB(CountDownDay countDownDay) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_NOTES, countDownDay.getNote());
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_EVENT_TEXT, countDownDay.getEvent());
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_COUNTDOWN_TARGET_DATE, MyDateUtils.myFormat.format(countDownDay.getDate()));
        edit.apply();
    }

    private void saveAppSettingToSHDB(AppSetting as) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY__APP_SETTING_SHOW_SECOND, as.isShowSecond());
        edit.putBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_ENABLE_WEATHER, as.isShowWeather());
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_CITY_CODE, as.getCityCode());
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_CITY_NAME, as.getCityName());
        edit.putBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_SCREEN_ALWAYS_ON, as.isScreenAlwaysOn());
        edit.putBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_IS_NIGHT_MODE, as.isNightMode());
        edit.putString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_HE_WEATHER_APIKEY, as.getHeWeatherAPIKey());

        edit.putInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_COUNTDOWN, as.countDownTextSize);
        edit.putInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_CLOCK, as.clockTextSize);
        edit.putInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_DATE, as.dateTextSize);
        edit.putInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_EVENT, as.eventTextSize);
        edit.putInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_NOTE, as.noteTextSize);

        edit.apply();
    }

    private AppSetting getAppSettingFromSHDB() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        AppSetting defaultAppSetting = getDefaultAppSetting();
        // 判断是否第一次启动，如果是第一次启动，则直接将默认设置保存到本地
        if (!mPrefs.contains(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_DATE)) {
            AppSetting dfas = getDefaultAppSetting();
            saveAppSettingToSHDB(dfas);
            return dfas;
        } else {


            boolean showSecond = mPrefs.getBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY__APP_SETTING_SHOW_SECOND, defaultAppSetting.isShowSecond());
            boolean showWeather = mPrefs.getBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_ENABLE_WEATHER, defaultAppSetting.isShowWeather());
            String cityCode = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_CITY_CODE, defaultAppSetting.getCityCode());
            String cityName = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_CITY_NAME, defaultAppSetting.getCityName());
            String heWeatherAPIKey = mPrefs.getString(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_HE_WEATHER_APIKEY, defaultAppSetting.getHeWeatherAPIKey());

            boolean screenAlawaysOn = mPrefs.getBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_SCREEN_ALWAYS_ON, defaultAppSetting.isScreenAlwaysOn());
            boolean nightMode = mPrefs.getBoolean(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_IS_NIGHT_MODE, defaultAppSetting.isNightMode());

            // 字体
            int clockTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_CLOCK, defaultAppSetting.clockTextSize);
            int dateTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_DATE, defaultAppSetting.dateTextSize);
            int weatherTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_WEATHER, defaultAppSetting.weatherTextSize);
            int noteTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_NOTE, defaultAppSetting.noteTextSize);
            int eventTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_EVENT, defaultAppSetting.eventTextSize);
            int countDownTextSize = mPrefs.getInt(MyConfig.PERSONAL_SHP_CONFIG_KEY_APP_SETTING_TEXTSIZE_COUNTDOWN, defaultAppSetting.countDownTextSize);

            AppSetting as = new AppSetting(showWeather, showSecond, screenAlawaysOn, nightMode, cityName, cityCode,
                    clockTextSize, dateTextSize, weatherTextSize, countDownTextSize, eventTextSize, noteTextSize, heWeatherAPIKey);
            return as;
        }
    }


    public LiveData<AppSetting> getAppSetting() {
        if (appSetting == null) {
            appSetting = new MutableLiveData<>();
            AppSetting as = getAppSettingFromSHDB();
            if (as.getHeWeatherAPIKey() == null) as.setShowWeather(false);
            appSetting.setValue(as);
        }
        return appSetting;
    }

    public void updateAppSetting(AppSetting as) {
        saveAppSettingToSHDB(as);
        appSetting.setValue(as);
    }

    public void updateCountDownDay(CountDownDay countDownDay) {
        saveCountDownDayToSHDB(countDownDay);
        countdownDay.postValue(countDownDay);
    }


    // 数据没有变动，但是需要重新postValue以便告知UI更新
    public void updateCountDownDay() {
        CountDownDay downDayFromSHDB = getDownDayFromSHDB();
        countdownDay.postValue(downDayFromSHDB);
    }


    public void updateWeathers() {
        loadWeathers();
    }


    private void loadWeathers() {

        AppSetting value = getAppSetting().getValue();
        if (value == null) {
            return;
        }
        if (!value.isShowWeather()) {
            Log.d(TAG, "用户关闭了天气功能，故不发起请求");
            return;
        }

//        String url = "http://192.168.2.180:8090/v7/weather/3d";
        if (value.getHeWeatherAPIKey() == null) {
            Log.d(TAG, "尚未配置APIKEY, 无法加载天气信息");
            return;
        }


        String place = value.getCityName();
        String url = MyWeatherUtil.getWeatherURL(getApplication());
        Log.d(TAG, "正在请求" + place + "地区的天气预报");
        StringBuilder sb = new StringBuilder();
        ArrayList<WeatherDay> userArrayList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        StringRequest stringRequestForWeather = new StringRequest(Request.Method.GET, url,
                (Response.Listener<String>) response -> {
                    Gson gson = new Gson();
                    Weather3DayBean weather3DayBean = gson.fromJson(response, Weather3DayBean.class);
                    if (!"200".equals(weather3DayBean.code)) {
                        Log.d(TAG, "请求数据解析失败:" + response);
                        return;
                    }
                    updateLastUpdate(weather3DayBean.updateTime);
                    for (Object obj : weather3DayBean.daily) {
                        LinkedTreeMap object = (LinkedTreeMap) obj;
                        String fxDate = (String) object.get("fxDate");
                        String textDay = (String) object.get("textDay");
                        String textNight = (String) object.get("textNight");
                        String tempMin = (String) object.get("tempMin");
                        String tempMax = (String) object.get("tempMax");
                        String precip = (String) object.get("precip");
                        String humidity = (String) object.get("humidity");
                        sb.append(fxDate.substring(5, fxDate.length())).append(" ")
                                .append(textDay).append("~").append(textNight)
                                .append(",").append(tempMin).append("~").append(tempMax).append("℃")
                                .append(",雨").append(precip).append("ml")
                                .append(",湿").append(humidity).append("%")
                                .append("\n");
                        userArrayList.add(new WeatherDay(sb.toString()));
                    }
                    if (userArrayList.size() != 0) {
                        Log.d(TAG, "成功更新了" + userArrayList.size() + "条天气信息");
                        weathers.postValue(userArrayList);
                    }
                }, (Response.ErrorListener) error -> {
            Log.d(TAG, "网络原因导致请求天气失败:" + error.toString());
            Toast.makeText(getApplication(), "网络原因导致请求天气失败:" + error.toString(), Toast.LENGTH_SHORT).show();
        });


        requestQueue.add(stringRequestForWeather);
        requestQueue.start();

    }


    public AppSetting getDefaultAppSetting() {
//        return new AppSetting(true, true, false, false, "北京", "101010100", 60, 24,12,196, 40, 24, null);
        return new AppSetting(false, true, false, false, null, null, 60, 24, 12, 196, 40, 24, null);
    }
}