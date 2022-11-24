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
import android.widget.RadioGroup;
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
import com.tignioj.countdowninfo.databinding.FragmentSecondBinding;
import com.tignioj.entity.AppSetting;
import com.tignioj.entity.MyGeo;
import com.tignioj.entity.MyGeoBean;
import com.tignioj.util.MyWeatherUtil;
import com.tignioj.viewmodel.MyViewModel;

import java.util.List;
import java.util.Objects;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    AppSetting nAppSetting;

    final String TAG = "SecondFragment";


    // seekbar参数
    int step = 1;
    int max = 250;
    int prograssMin = 0;
    int progressMax = (max - prograssMin) / step;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

    private void loadAppSetting(AppSetting appSetting) {
        copyAppSetting(appSetting);

        binding.checkBoxWeather.setChecked(appSetting.isShowWeather());
        binding.checkBoxShowSecond.setChecked(appSetting.isShowSecond());
        binding.etProvince.setText(appSetting.getCityName());
        binding.checkBoxNgihtMode.setChecked(appSetting.isNightMode());
        binding.tvCurrentCity.setText(appSetting.getCityName() != null ? "当前已选择:" + appSetting.getCityName() : "");
        binding.checkBoxScreenAlwaysOn.setChecked(appSetting.isScreenAlwaysOn());
        binding.editTextTextAPIKey.setText(appSetting.getHeWeatherAPIKey());


        binding.seekBarClockTextSize.setMax(progressMax);
//        binding.seekBarNoteTextSize.setMax(maxval);
//        binding.seekBarDateTextSize.setMax(maxval);
//        binding.seekBarWeatherTextSize.setMax(maxval);
//        binding.seekBarEventTextSize.setMax(maxval);
//        binding.seekBarCountDayTextSize.setMax(maxval);

        binding.seekBarClockTextSize.setProgress(appSetting.clockTextSize);
//        binding.seekBarCountDayTextSize.setProgress(appSetting.countDownTextSize);
//        binding.seekBarDateTextSize.setProgress(appSetting.dateTextSize);
//        binding.seekBarWeatherTextSize.setProgress(appSetting.weatherTextSize);
//        binding.seekBarEventTextSize.setProgress(appSetting.eventTextSize);
//        binding.seekBarNoteTextSize.setProgress(appSetting.noteTextSize);

        if (!nAppSetting.isShowWeather() || Objects.equals(null, nAppSetting.getHeWeatherAPIKey()) || Objects.equals("", nAppSetting.getHeWeatherAPIKey())) {
            enableWeather(false);
        } else {
            enableWeather(true);
        }

    }

    public void enableWeather(boolean b) {
        nAppSetting.setShowWeather(b);
        binding.etProvince.setEnabled(b);
        binding.btnGetArea.setEnabled(b);
        binding.spinnerArea.setEnabled(b);
        if (b) {
            binding.tvCurrentCity.setText(nAppSetting.getCityName() != null ? "当前已选择:" + nAppSetting.getCityName() : "");
        } else {
            binding.tvCurrentCity.setText("已关闭天气功能");
        }
    }

    MyViewModel myViewModel;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // 从MyViewModel拿出来的数据直接修改，实际上修改的是全局的数据
        AppSetting appSettingVM = myViewModel.getAppSetting().getValue();
        nAppSetting = new AppSetting();
        if (appSettingVM == null) appSettingVM = myViewModel.getDefaultAppSetting();
        // 加载数据===========================================
        loadAppSetting(appSettingVM);


        // 监听==============================================
        binding.checkBoxShowSecond.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setShowSecond(isChecked));
        binding.checkBoxWeather.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enableWeather(isChecked);
        });
        binding.checkBoxNgihtMode.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setNightMode(isChecked));
        binding.checkBoxScreenAlwaysOn.setOnCheckedChangeListener((buttonView, isChecked) -> nAppSetting.setScreenAlwaysOn(isChecked));
        binding.btnResetAppSetting.setOnClickListener(v -> {
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
                if (!"".equals(s.toString().trim())) {
                    nAppSetting.setCityName(s.toString());
                }
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
                if (!"".equals(s.toString().trim())) {
                    nAppSetting.setHeWeatherAPIKey(s.toString());
                }
            }
        });

//        final String geoUrl = "http://192.168.2.180:8090/geo";
        // 天气==========================
        binding.btnGetArea.setOnClickListener(v -> {
            String pv = binding.etProvince.getText().toString().trim();
            binding.tvCurrentCity.setText("正在请求省份中...");
            String key = binding.editTextTextAPIKey.getText().toString().trim();
            if (pv.length() == 0 || key.length() == 0) {
                Log.d(TAG, "省份或者APIKey 没输入");
                binding.tvCurrentCity.setText("省份或者APIKey没填入");
                Toast.makeText(requireActivity(), "省份或者APIKey没输入", Toast.LENGTH_SHORT).show();
                return;
            }
            String geoUrl = MyWeatherUtil.getGeoURL(requireActivity().getApplication(), pv, key);
            RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
            Log.d(TAG, "url=" + geoUrl);
            requestQueue.add(new StringRequest(Request.Method.GET, geoUrl, (Response.Listener<String>) response -> {
                Gson gson = new Gson();
                MyGeoBean geoBean = gson.fromJson(response, MyGeoBean.class);
                if ("200".equals(geoBean.code)) {
                    if (geoBean.location != null && geoBean.location.size() != 0) {
                        List<MyGeo> myGeos = geoBean.location;
                        String[] items = new String[myGeos.size()];
                        for (int i = 0; i < myGeos.size(); i++) {
                            MyGeo myGeo = geoBean.location.get(i);
                            items[i] = myGeo.getName();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
                        binding.spinnerArea.setAdapter(adapter);
                        binding.spinnerArea.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                                        binding.tvCurrentCity.setText(items[position]);
                                        MyGeo geo = myGeos.get(position);
                                        nAppSetting.setCityName(geo.getName());
                                        nAppSetting.setCityCode(geo.getId());
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                }
                        );
                    } else {
                        binding.tvCurrentCity.setText("没有该地区的信息");
                    }
                } else {
                    binding.tvCurrentCity.setText("请求失败！服务器返回：" + geoBean.code);
                }

            }, (Response.ErrorListener) error -> {
                binding.tvCurrentCity.setText("网络异常");
                Log.d(TAG, "网络异常，无法请求地区");
            }));
            requestQueue.start();
        });


        // 字体===========================
        class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
            public final int radioID;

            public MySeekBarListener(int radioId) {
                this.radioID = radioId;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress * step + prograssMin;
                String txt = "预览:" + val;
//                binding.textViewPreview.setText(txt);
                binding.textViewPreview.setTextSize(val);
                binding.textViewFontSize.setText(String.valueOf(val));
                if (radioID==R.id.radioClock)  nAppSetting.clockTextSize=val;
                else if (radioID==R.id.radioCountDown) nAppSetting.countDownTextSize=val;
                else if (radioID==R.id.radioEvent) nAppSetting.eventTextSize=val;
                else if (radioID==R.id.radioNote) nAppSetting.noteTextSize=val;
                else if (radioID==R.id.radioDate) nAppSetting.dateTextSize=val;
                else if (radioID==R.id.radioWeather) nAppSetting.weatherTextSize=val;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }
        binding.radioFontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int textSize=10;
                binding.seekBarClockTextSize.setOnSeekBarChangeListener(new MySeekBarListener(checkedId));
                if (checkedId==R.id.radioClock)  textSize=nAppSetting.clockTextSize;
                else if (checkedId==R.id.radioCountDown) textSize=nAppSetting.countDownTextSize;
                else if (checkedId==R.id.radioEvent) textSize=nAppSetting.eventTextSize;
                else if (checkedId==R.id.radioNote) textSize=nAppSetting.noteTextSize;
                else if (checkedId==R.id.radioDate) textSize=nAppSetting.dateTextSize;
                else if (checkedId==R.id.radioWeather) textSize=nAppSetting.weatherTextSize;
                binding.seekBarClockTextSize.setProgress(textSize);
            }
        });
        binding.buttonFontSizeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = binding.seekBarClockTextSize.getProgress()+1;
                if (progress<= progressMax) binding.seekBarClockTextSize.setProgress(progress);
            }
        });
        binding.buttonFontSizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = binding.seekBarClockTextSize.getProgress()-1;
                if (progress>= prograssMin) binding.seekBarClockTextSize.setProgress(progress);
            }
        });

        // 确认=========================================
        AppSetting finalAppSettingVM = appSettingVM;
        binding.buttonAppSettingOk.setOnClickListener(view12 -> {
            myViewModel.updateAppSetting(nAppSetting);
            // 检测地区有没修改，地区修改了并且开启了天气功能，则更新天气
            if (!Objects.equals(nAppSetting.getCityCode(), finalAppSettingVM.getCityCode())) {
                if (nAppSetting.isShowWeather()) {
                    myViewModel.updateWeathers();
                }
            }
//                NavHostFragment.findNavController(SecondFragment.this) .navigate(R.id.action_SecondFragment_to_FirstFragment);
            Navigation.findNavController(view12).navigateUp();
        });


    }

    private void copyAppSetting(AppSetting as) {
        nAppSetting.dateTextSize = as.dateTextSize;
        nAppSetting.weatherTextSize = as.weatherTextSize;
        nAppSetting.clockTextSize = as.clockTextSize;
        nAppSetting.countDownTextSize = as.countDownTextSize;
        nAppSetting.eventTextSize = as.eventTextSize;
        nAppSetting.noteTextSize = as.noteTextSize;

        nAppSetting.setShowSecond(as.isShowSecond());
        nAppSetting.setShowWeather(as.isShowWeather());
        nAppSetting.setNightMode(as.isNightMode());
        nAppSetting.setScreenAlwaysOn(as.isScreenAlwaysOn());

        nAppSetting.setCityCode(as.getCityCode());
        nAppSetting.setCityName(as.getCityName());
        nAppSetting.setHeWeatherAPIKey(as.getHeWeatherAPIKey());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}