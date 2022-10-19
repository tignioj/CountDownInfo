package com.tignioj.countdowninfo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.tignioj.config.MyConfig;
import com.tignioj.countdowninfo.databinding.FragmentSecondBinding;
import com.tignioj.entity.AppSetting;
import com.tignioj.entity.MyGeo;
import com.tignioj.entity.MyGeoBean;
import com.tignioj.util.MyWeatherUtil;
import com.tignioj.viewmodel.MyViewModel;

import java.util.List;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    AppSetting nAppSetting;

    final String TAG = "SecondFragment";


    // seekbar参数
    int step = 1;
    int max = 250;
    int min = 0;
    int maxval = (max - min) / step;




    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

     private void loadAppSetting(AppSetting appSetting) {
         binding.checkBoxWeather.setChecked(appSetting.isShowWeather());
         binding.checkBoxShowSecond.setChecked(appSetting.isShowSecond());
         binding.etProvince.setText(appSetting.getCityName());
         binding.checkBoxNgihtMode.setChecked(appSetting.isNightMode());
         binding.tvCurrentCity.setText("当前已选择:" + appSetting.getCityCode() + ":" + appSetting.getCityName());
         binding.checkBoxScreenAlwaysOn.setChecked(appSetting.isScreenAlwaysOn());
         binding.editTextTextAPIKey.setText(appSetting.getHeWeatherAPIKey());


         binding.seekBarClockTextSize.setMax(maxval);
         binding.seekBarNoteTextSize.setMax(maxval);
         binding.seekBarDateTextSize.setMax(maxval);
         binding.seekBarWeatherTextSize.setMax(maxval);
         binding.seekBarEventTextSize.setMax(maxval);
         binding.seekBarCountDayTextSize.setMax(maxval);

         binding.seekBarClockTextSize.setProgress(appSetting.clockTextSize);
         binding.seekBarCountDayTextSize.setProgress(appSetting.countDownTextSize);
         binding.seekBarDateTextSize.setProgress(appSetting.dateTextSize);
         binding.seekBarWeatherTextSize.setProgress(appSetting.weatherTextSize);
         binding.seekBarEventTextSize.setProgress(appSetting.eventTextSize);
         binding.seekBarNoteTextSize.setProgress(appSetting.noteTextSize);

     }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyViewModel myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        
        // 从MyViewModel拿出来的数据直接修改，实际上修改的是全局的数据
        AppSetting globalAppSetting = myViewModel.getAppSetting().getValue();


        // 加载数据===========================================
        if (globalAppSetting != null) {
            loadAppSetting(globalAppSetting);
            nAppSetting = new AppSetting();
            nAppSetting.dateTextSize = globalAppSetting.dateTextSize;
            nAppSetting.weatherTextSize = globalAppSetting.dateTextSize;
            nAppSetting.clockTextSize = globalAppSetting.clockTextSize;
            nAppSetting.countDownTextSize = globalAppSetting.countDownTextSize;
            nAppSetting.eventTextSize = globalAppSetting.eventTextSize;
            nAppSetting.noteTextSize = globalAppSetting.noteTextSize;

            nAppSetting.setShowSecond(globalAppSetting.isShowSecond());
            nAppSetting.setShowWeather(globalAppSetting.isShowWeather());
            nAppSetting.setNightMode(globalAppSetting.isNightMode());
            nAppSetting.setScreenAlwaysOn(globalAppSetting.isScreenAlwaysOn());

            nAppSetting.setCityCode(globalAppSetting.getCityCode());
            nAppSetting.setCityName(globalAppSetting.getCityName());
            nAppSetting.setHeWeatherAPIKey(globalAppSetting.getHeWeatherAPIKey());
        } else {
            nAppSetting = myViewModel.getDefaultAppSetting();
        }

        // 监听==============================================
        binding.checkBoxShowSecond.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setShowSecond(isChecked));
        binding.checkBoxWeather.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setShowWeather(isChecked));
        binding.checkBoxNgihtMode.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setNightMode(isChecked));
        binding.checkBoxScreenAlwaysOn.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setScreenAlwaysOn(isChecked));
        binding.btnResetAppSetting.setOnClickListener(v-> {
            loadAppSetting(myViewModel.getDefaultAppSetting());
        });
        binding.etProvince.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                nAppSetting.setCityName(s.toString());
            }
        });
        binding.editTextTextAPIKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                nAppSetting.setHeWeatherAPIKey(s.toString());
            }
        });

//        final String geoUrl = "http://192.168.2.180:8090/geo";
        // 天气==========================
        binding.btnGetArea.setOnClickListener(v -> {
            String geoUrl = null;
            try {
                geoUrl = MyWeatherUtil.getGeoURL(requireActivity().getApplication(), binding.etProvince.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireActivity(), "尚未配置APIKey，无法请求", Toast.LENGTH_SHORT).show();
                return;
            }
            RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
            Log.d(TAG, "url=" + geoUrl);
            requestQueue.add(new StringRequest(Request.Method.GET, geoUrl, (Response.Listener<String>) response -> {
                Gson gson = new Gson();
                String[] items = new String[]{"没有数据，请更换省份关键词"};
                MyGeoBean geoBean = gson.fromJson(response, MyGeoBean.class);
                List<MyGeo> myGeos = null;
                if ("200".equals(geoBean.code)) {
                    if (geoBean != null && geoBean.location != null) {
                        myGeos = geoBean.location;
                        items = new String[myGeos.size()];
                        for (int i = 0; i < myGeos.size(); i++) {
                            MyGeo myGeo = geoBean.location.get(i);
                            items[i] = myGeo.getAdm1() + myGeo.getName();
                        }
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
                binding.spinnerArea.setAdapter(adapter);
                String[] finalItems = items;
                List<MyGeo> finalMyGeos = myGeos;
                binding.spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                        binding.tvCurrentCity.setText("当前已选择:" + finalItems[position]);
                        if (finalMyGeos == null) return;
                        MyGeo geo = finalMyGeos.get(position);
                        nAppSetting.setCityName(geo.getName());
                        nAppSetting.setCityCode(geo.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }, (Response.ErrorListener) error -> {

            }));
            requestQueue.start();
        });


        // 字体===========================
        class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
            public String name;
            public MySeekBarListener(String name) {
                this.name = name;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress*step + min;
                String txt = "预览:" + val;
                binding.textViewPreview.setText(txt);
                binding.textViewPreview.setTextSize(val);
                switch (name) {
                    case "时钟": nAppSetting.clockTextSize = val; break;
                    case "倒计时": nAppSetting.countDownTextSize = val; break;
                    case "事件": nAppSetting.eventTextSize = val; break;
                    case "日期": nAppSetting.dateTextSize= val; break;
                    case "天气": nAppSetting.weatherTextSize= val; break;
                    case "备注": nAppSetting.noteTextSize = val; break;
                    default:break;
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        }
        binding.seekBarClockTextSize.setOnSeekBarChangeListener(new MySeekBarListener("时钟"));
        binding.seekBarCountDayTextSize.setOnSeekBarChangeListener(new MySeekBarListener("倒计时"));
        binding.seekBarDateTextSize.setOnSeekBarChangeListener(new MySeekBarListener("日期"));
        binding.seekBarWeatherTextSize.setOnSeekBarChangeListener(new MySeekBarListener("天气"));
        binding.seekBarEventTextSize.setOnSeekBarChangeListener(new MySeekBarListener("事件"));
        binding.seekBarNoteTextSize.setOnSeekBarChangeListener(new MySeekBarListener("备注"));



        // 确认=========================================
        binding.buttonAppSettingOk.setOnClickListener(view12 -> {
            myViewModel.updateAPpSetting(nAppSetting);
            if (nAppSetting.isShowWeather()) {
                myViewModel.updateWeathers();
            }
//                NavHostFragment.findNavController(SecondFragment.this) .navigate(R.id.action_SecondFragment_to_FirstFragment);
            Navigation.findNavController(view12).navigateUp();
        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}