package com.tignioj.util;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.tignioj.countdowninfo.R;
import com.tignioj.entity.AppSetting;
import com.tignioj.entity.WeatherDay;
import com.tignioj.viewmodel.MyViewModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class MyWeatherUtil {
    public static void request3DayWeathers(String place) {
        String url = "http://192.168.2.180:8090/v7/weather/3d";
        Log.d("network", "正在请求" + place + "地区的天气预报");
    }

    public static String getAPIKey(Application application) {
        MyViewModel myViewModel = new MyViewModel(application);
                LiveData<AppSetting> asd = myViewModel.getAppSetting();
        AppSetting appSetting = asd.getValue();
        if (appSetting != null && appSetting.getHeWeatherAPIKey() != null) {
            return appSetting.getHeWeatherAPIKey();
        }
        return null;
    }

    public static String getGeoURL(Application application, String cityName) throws Exception {
        String encode = Uri.encode(cityName);
        if (getAPIKey(application) == null) {
            throw new Exception("未配置和风APIKey");
        }
        String url = application.getString(R.string.geoapi) + "/v2/city/lookup?location=" + encode + "&key=" + getAPIKey(application);
        return url;
    }

    public static String getWeatherURL(Application application) {
        MyViewModel myViewModel = new MyViewModel(application);
        LiveData<AppSetting> asd = myViewModel.getAppSetting();
        AppSetting appSetting = asd.getValue();
        if (appSetting!=null && appSetting.getHeWeatherAPIKey()!=null) {
            return application.getString(R.string.heweatherapi) + "/v7/weather/3d?key=" +
                    appSetting.getHeWeatherAPIKey() + "&location=" + appSetting.getCityCode();
        }
        return null;
    }
}
