package com.yxx.app.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yxx.app.BluetoothManager;
import com.yxx.app.BluetoothManagerBle;
import com.yxx.app.R;
import com.yxx.app.activity.MainActivity;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.util.TimeUtil;
import com.yxx.app.util.ToastUtil;

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
    private EditText editText;
    //</editor-fold>

    private TimePickerDialog timePickerDialog;

    private boolean isAtuoDownTime = true;

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
        editText = view.findViewById(R.id.editText);

        tv_date_input.setOnClickListener(this);
        tv_up_time_input.setOnClickListener(this);
        tv_down_time_input.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_add.setOnClickListener(this);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAtuoDownTime = !isChecked;
                if(isChecked){
                    tv_down_time_input.setText(TimeUtil.getCurrHM());
                }else{
                    tv_down_time_input.setText("自动生成");
                }
                tv_down_time_input.setEnabled(isChecked);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_add.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void sendBtnEnable(boolean enable){
        if(btn_send != null)btn_send.setEnabled(enable);
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
            case R.id.tv_down_time_input:
                //选择下时
                new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tv_down_time_input.setText(TimeUtil.hmFormat(hourOfDay,minute));
                    }
                }, TimeUtil.getCurrHour(), TimeUtil.getCurrMinute(), true).show();
                break;
            case R.id.btn_add:
                //添加到列表
                SendInfo sendInfo = getSendInfo();
                MainActivity activity = (MainActivity) getActivity();
                if(activity != null){
                    activity.importData(sendInfo);
                }
                break;
            case R.id.btn_send:
                if(editText.getText().length() == 0){
                    ToastUtil.show("清输入正确的数据");
                    return;
                }
                BluetoothManager.get().sendData(getSendInfo());
                break;
        }
    }

    private SendInfo getSendInfo(){
        String[] dateArray = tv_date_input.getText().toString().split("-");
        String[] upTimeArray = tv_up_time_input.getText().toString().split(":");

        SendInfo sendInfo = new SendInfo();
        sendInfo.year = dateArray[0];
        sendInfo.month = dateArray[1];
        sendInfo.day = dateArray[2];

        sendInfo.u_hours = upTimeArray[0];
        sendInfo.u_minute = upTimeArray[1];

        if(!isAtuoDownTime){
            String[] downTimeArray = tv_down_time_input.getText().toString().split(":");
            sendInfo.d_hours = downTimeArray[0];
            sendInfo.d_minute = downTimeArray[1];
        }else{
            sendInfo.autoDownTime();
        }
        sendInfo.price = editText.getText().toString();
        return sendInfo;
    }
    //</editor-fold>
}
