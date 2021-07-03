package com.yxx.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yxx.app.R;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, null);

        initView(view);

        return view;
    }

    private void initView(View view){
        mSwitch = view.findViewById(R.id.swicth);
        tv_date_input = view.findViewById(R.id.tv_date_input);
        tv_up_time_input = view.findViewById(R.id.tv_up_time_input);
        tv_down_time_input = view.findViewById(R.id.tv_down_time_input);
        btn_send = view.findViewById(R.id.btn_send);
        btn_add = view.findViewById(R.id.btn_add);

        btn_send.setOnClickListener(this);
        btn_add.setOnClickListener(this);
    }

    //<editor-fold desc="点击事件">
    @Override
    public void onClick(View view) {

    }
    //</editor-fold>
}
