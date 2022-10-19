package com.tignioj.countdowninfo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.tignioj.countdowninfo.databinding.FragmentFirstBinding;
import com.tignioj.entity.CountDownDay;
import com.tignioj.util.MyDateUtils;
import com.tignioj.viewmodel.MyViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingFragment extends Fragment {
    private FragmentFirstBinding binding;
    MyViewModel myViewModel;


    public SettingFragment() {
        // Required empty public constructor
    }


    Date newDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_setting, container, false);
        EditText editTextFinalDay = inflate.findViewById(R.id.et_finalday);
        editTextFinalDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        LiveData<CountDownDay> countDownDay = myViewModel.getCountDownDay();
        CountDownDay value = countDownDay.getValue();
        newDate  = value.getDate();
        if (newDate != null) {
            String format = MyDateUtils.format(value.getDate());
            editTextFinalDay.setText(format);
        } else {
            editTextFinalDay.setText("加载日期失败");
        }

        EditText editTextEvent = inflate.findViewById(R.id.et_event);
        editTextEvent.setText(value.getEvent());

        EditText editTextNote = inflate.findViewById(R.id.et_note);
        editTextNote.setText(value.getNote());

        Button btnOk = inflate.findViewById(R.id.btn_info_ok);
        btnOk.setOnClickListener(v -> {
            CountDownDay cd = new CountDownDay();
            cd.setEvent(editTextEvent.getText().toString());
            cd.setNote(editTextNote.getText().toString());
            cd.setDate(newDate);
            myViewModel.updateCountDownDay(cd);
            Navigation.findNavController(v).navigateUp();
        });
        Button btnNo = inflate.findViewById(R.id.btn_info_cancel);
        btnNo.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return inflate;
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment(SettingFragment.this);
        newFragment.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        SettingFragment fragment;
        Calendar c;
        Date time;
        SimpleDateFormat sdf = MyDateUtils.myFormat;
        MyViewModel myViewModel;

        public DatePickerFragment(SettingFragment fragment) {
            this.fragment = fragment;
            myViewModel = new ViewModelProvider(fragment).get(MyViewModel.class);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LiveData<CountDownDay> countDownDay = myViewModel.getCountDownDay();
            CountDownDay value = countDownDay.getValue();
            time = value.getDate();

            sdf.format(time == null ? new Date() : time);
            c = sdf.getCalendar();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            return new DatePickerDialog(requireContext(), this, year, month, day);
        }


        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fragment.newDate = c.getTime();
        }
    }


}