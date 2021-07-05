package com.yxx.app.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.yxx.app.R;
import com.yxx.app.util.TimeUtil;

import java.util.Calendar;

/**
 * Author: yangxl
 * Date: 2021/7/3 17:13
 * Description:     生成
 */
public class GenerateActivity extends AppCompatActivity implements View.OnClickListener {

    //<editor-fold desc="UI">
    private Toolbar toolbar;
    private EditText editText_num;//每日张数
    private EditText editText_generate_num;//生成张数
    private EditText editText_time_float;//时间浮动
    private EditText editText_price_float;//金额浮动
    private TextView tv_start_date;//起始日期
    private Button btn_sure;//确定
    private LinearLayout listLayout;//生成配置列表
    //</editor-fold>

    private int num = 2;//每日张数
    private int generate_num = 100;//生成张数
    private int time_float = 20;
    private int price_float = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        initView();
        initData();
    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        editText_num = findViewById(R.id.editText_num);
        editText_generate_num = findViewById(R.id.editText_generate_num);
        editText_time_float = findViewById(R.id.editText_time_float);
        editText_price_float = findViewById(R.id.editText_price_float);
        tv_start_date = findViewById(R.id.tv_start_date);
        btn_sure = findViewById(R.id.btn_sure);
        listLayout = findViewById(R.id.listLayout);

        toolbar.setNavigationOnClickListener(view -> finish());
        tv_start_date.setOnClickListener(this);
        btn_sure.setOnClickListener(this);
    }

    private void initData(){
        editText_num.setText(String.valueOf(num));
        editText_generate_num.setText(String.valueOf(generate_num));
        editText_time_float.setText(String.valueOf(time_float));
        editText_price_float.setText(String.valueOf(price_float));
        tv_start_date.setText(TimeUtil.getNYR());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start_date:
                //选择日期
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_start_date.setText(TimeUtil.nyrFormat(i,i1 + 1, i2));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }
}
