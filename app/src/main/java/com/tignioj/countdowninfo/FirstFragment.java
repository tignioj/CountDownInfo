package com.tignioj.countdowninfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.tignioj.countdowninfo.databinding.FragmentFirstBinding;
import com.tignioj.entity.AppSetting;
import com.tignioj.entity.CountDownDay;
import com.tignioj.entity.WeatherDay;
import com.tignioj.util.MyDateUtils;
import com.tignioj.viewmodel.MyViewModel;

import java.util.Date;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        this.inflater = inflater;
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        MyViewModel myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        LiveData<List<WeatherDay>> weathers = myViewModel.getWeathers();
        weathers.observe(getViewLifecycleOwner(), weatherDays -> {
            for (WeatherDay u : weatherDays) {
                binding.tvWeatherdata.setText(u.toString());
            }
        });


        LiveData<CountDownDay> countDownDay = myViewModel.getCountDownDay();
        countDownDay.observe(getViewLifecycleOwner(), countDownDay1 -> {
            binding.tvCountdownDay.setText(MyDateUtils.getDiffDay( new Date(), countDownDay1.getDate()));
            binding.tvSaying.setText(countDownDay1.getNote());
            binding.tvEvent.setText(countDownDay1.getEvent());
        });


        LiveData<String> lastUpdate = myViewModel.getLastUpdate();
        lastUpdate.observe(getViewLifecycleOwner(), str-> {
            AppSetting value = myViewModel.getAppSetting().getValue();
            if (value!=null) {
                binding.tvLastUpdate.setText(str);
            }
        });

        LiveData<AppSetting> appSetting = myViewModel.getAppSetting();
        appSetting.observe(getViewLifecycleOwner(), as -> {
            if (!as.isShowWeather()) {
                weathers.removeObservers(getViewLifecycleOwner());
                binding.tvWeatherdata.setVisibility(View.GONE);
                binding.tvCityName.setVisibility(View.GONE);
                binding.tvLastUpdate.setVisibility(View.GONE);
            } else {
                binding.tvCityName.setText(as.getCityName());
                weathers.observe(getViewLifecycleOwner(), weatherDays -> {
                    for (WeatherDay u : weatherDays) {
                        binding.tvWeatherdata.setText(u.toString());
                    }
                });
                binding.tvCityName.setVisibility(View.VISIBLE);
                binding.tvLastUpdate.setVisibility(View.VISIBLE);
                binding.tvWeatherdata.setVisibility(View.VISIBLE);
            }

            if(as.isShowSecond()) {
                binding.tvUpdateTime.setFormat12Hour(getString(R.string.text_clock_12h_format));
                binding.tvUpdateTime.setFormat24Hour(getString(R.string.text_clock_24h_format));
            } else {
                binding.tvUpdateTime.setFormat12Hour(getString(R.string.text_clock_12h_format_no_second));
                binding.tvUpdateTime.setFormat24Hour(getString(R.string.text_clock_24h_format_no_second));
            }

            // 调整字体
            binding.tvUpdateTime.setTextSize(as.clockTextSize);
            binding.tvEvent.setTextSize(as.eventTextSize);
            binding.tvSaying.setTextSize(as.noteTextSize);
            binding.tvCountdownDay.setTextSize(as.countDownTextSize);

            if (as.isScreenAlwaysOn()) {
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            if (as.isNightMode()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}