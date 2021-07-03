package com.yxx.app.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.yxx.app.R;
import com.yxx.app.util.TimeUtil;

import java.sql.Time;
import java.util.Calendar;

/**
 * Author: yangxl
 * Date: 2021/7/3 14:31
 * Description: 输入
 */
public class InputFragment extends Fragment implements View.OnClickListener {

    //<editor-fold desc="UI控件">
    private TextView tv_date_input;//日期
    private TextView tv_up_time_input;//上时
    private TextView tv_down_time_input;//下时
    private Switch mSwitch;//下时开关
    private Button btn_send;//发送
    private Button btn_add;//添加
    //</editor-fold>

    private TimePickerDialog timePickerDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, null);

        initView(view);

        initData();
        return view;
    }

    private void initView(View view){
        mSwitch = view.findViewById(R.id.swicth);
        tv_date_input = view.findViewById(R.id.tv_date_input);
        tv_up_time_input = view.findViewById(R.id.tv_up_time_input);
        tv_down_time_input = view.findViewById(R.id.tv_down_time_input);
        btn_send = view.findViewById(R.id.btn_send);
        btn_add = view.findViewById(R.id.btn_add);

        tv_date_input.setOnClickListener(this);
        tv_up_time_input.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_add.setOnClickListener(this);


    }

    private void initData(){
        tv_date_input.setText(TimeUtil.getNYR());
        tv_up_time_input.setText(TimeUtil.getCurrHM());
    }

    //<editor-fold desc="点击事件">
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_date_input:
                //选择日期
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_date_input.setText(TimeUtil.nyrFormat(i,i1 + 1, i2));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.tv_up_time_input:
                //选择上时
                new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tv_up_time_input.setText(TimeUtil.hmFormat(hourOfDay,minute));
                    }
                }, TimeUtil.getCurrHour(), TimeUtil.getCurrMinute(), true).show();
                break;
        }
    }
    //</editor-fold>
}
