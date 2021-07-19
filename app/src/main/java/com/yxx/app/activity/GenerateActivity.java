package com.yxx.app.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.yxx.app.R;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
    private int generate_num = 10;//生成张数
    private int time_float = 20;
    private int price_float = 100;

    private static final int NUM_MIN = 1;
    private static final int NUM_MAX = 5;
    private static final int GENERATE_NUM_MIN = 1;
    private static final int GENERATE_NUM_MAX = 50;

    private CharSequence wordNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        initView();
        initData();
        btn_sure.setEnabled(true);
    }

    private void initView() {
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

        initTextWatcher();

    }

    private void initData() {
        editText_num.setText(String.valueOf(num));
        editText_generate_num.setText(String.valueOf(generate_num));
        editText_time_float.setText(String.valueOf(time_float));
        editText_price_float.setText(String.valueOf(price_float));
        tv_start_date.setText(TimeUtil.getNYR());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_start_date:
                //选择日期
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_start_date.setText(TimeUtil.nyrFormat(i, i1 + 1, i2));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.btn_sure:
                generateData();
                break;
        }
    }

    private void initTextWatcher() {
        editText_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                wordNum = charSequence;
                if (charSequence != null && !TextUtils.isEmpty(charSequence)) {
                    if (Integer.parseInt(charSequence.toString()) < NUM_MIN) {
                        editText_num.setText(String.valueOf(NUM_MIN));
                        editText_num.setSelection(editText_num.length());
                    } else if (Integer.parseInt(charSequence.toString()) > NUM_MAX) {
                        editText_num.setText(String.valueOf(NUM_MAX));
                        editText_num.setSelection(editText_num.length());
                    }
                    num = Integer.parseInt(editText_num.getText().toString());
                    addListView();
                } else {
                    removeListView();
                }
                btnEnable();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (wordNum.length() == 0) {
                    //    removeListView();
                }
            }
        });

        editText_generate_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && !TextUtils.isEmpty(charSequence)) {
                    if (Integer.parseInt(charSequence.toString()) < GENERATE_NUM_MIN) {
                        editText_generate_num.setText(String.valueOf(GENERATE_NUM_MIN));
                        editText_generate_num.setSelection(editText_generate_num.length());
                    } else if (Integer.parseInt(charSequence.toString()) > GENERATE_NUM_MAX) {
                        editText_generate_num.setText(String.valueOf(GENERATE_NUM_MAX));
                        editText_generate_num.setSelection(editText_generate_num.length());
                    }
                }
                btnEnable();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void generateData() {
        ArrayList<String> strList = new ArrayList<>();
        generate_num = Integer.parseInt(editText_generate_num.getText().toString());
        time_float = Integer.parseInt(editText_time_float.getText().toString());
        price_float = Integer.parseInt(editText_price_float.getText().toString());

        try {
            String dateStr = tv_start_date.getText().toString();
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            //    calendar.setTime(sdfDate.parse(dateStr));
            Date date = sdfDate.parse(dateStr);
            long dateTime = date.getTime();
            int dateNum = (int) Math.ceil((double) generate_num / num);
            int currentNum = 0;
            LogUtil.d(" dateNum == " + dateNum);
            //总天数
            for (int i = 0; i < dateNum; i++) {
                int page;
                if (generate_num <= num) {
                    page = generate_num;
                } else {
                    if (generate_num - currentNum > num) {
                        page = num;
                        currentNum += num;
                    } else {
                        page = generate_num - currentNum;
                    }
                }
                //每天的张数
                for (int j = 0; j < page; j++) {
                    calendar.setTime(new Date(dateTime));
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    //sdfDate.format(new Date(dateTime));
                    LogUtil.d(String.format("%s-%s-%s", year, month, day));
                    LogUtil.d(" === 生成了 ==== " + "第" + (i + 1) + "天 : " + "第" + (j + 1) + "张");

                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                    String timeStr = ((TextView)listLayout.getChildAt(j).findViewById(R.id.tv_list_time)).getText().toString();
                    Date date2 = sdf2.parse(timeStr);
                    if(date2 != null){
                        long beginTime = date2.getTime();
                        long maxTime = beginTime + price_float * 60 * 1000;
                        long rtn = beginTime + (long)(Math.random()*(maxTime - beginTime));
                        calendar.setTime(new Date(rtn));

                        int price = Integer.parseInt(((EditText)listLayout.getChildAt(j).
                                findViewById(R.id.list_editText_price)).getText().toString());
                        int maxPrice = price + price_float;
                        int ranPrice = new Random().nextInt(maxPrice)%(maxPrice-price+1) + price;

                        strList.add(TimeUtil.nyrFormat(year, month, day) + " " + TimeUtil.hmFormat(calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE)) + " " + ranPrice);
                    }

                }
                dateTime = dateTime + 24 * 60 * 60 * 1000;
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra("strList", strList);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void addListView() {
        listLayout.removeAllViews();
        for (int i = 0; i < num; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.list_generate_recyclerview, null);
            TextView tv_list_num = view.findViewById(R.id.tv_list_num);
            TextView tv_list_time = view.findViewById(R.id.tv_list_time);
            EditText editText = view.findViewById(R.id.list_editText_price);

            tv_list_num.setText(String.format("    第%s张：", getCstr(i + 1)));
            tv_list_time.setOnClickListener(view12 -> new TimePickerDialog(GenerateActivity.this, (view1, hourOfDay, minute) ->
                    tv_list_time.setText(TimeUtil.hmFormat(hourOfDay, minute)), TimeUtil.getCurrHour(), TimeUtil.getCurrMinute(), true).show());
            if (i == 0) tv_list_time.setText(String.format("%s:%s", "06", "00"));
            if (i == 1) tv_list_time.setText(String.format("%s:%s", "10", "00"));
            if (i == 2) tv_list_time.setText(String.format("%s:%s", "14", "30"));
            if (i == 3) tv_list_time.setText(String.format("%s:%s", "18", "00"));
            if (i == 4) tv_list_time.setText(String.format("%s:%s", "20", "30"));
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    btnEnable();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            listLayout.addView(view);
        }
    }

    private void removeListView() {
        listLayout.removeAllViews();
    }

    private String getCstr(int i) {
        switch (i) {
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
        }
        return "" + i;
    }

    private void btnEnable() {
        boolean flag = editText_num.length() > 0 && editText_generate_num.length() > 0
                && editText_time_float.length() > 0 && editText_price_float.length() > 0;
        boolean flag2 = true;
        for (int i = 0; i < listLayout.getChildCount(); i++) {
            View view = listLayout.getChildAt(i);
            EditText editText = view.findViewById(R.id.list_editText_price);
            if (editText.length() == 0) {
                flag2 = false;
                break;
            }
        }
        btn_sure.setEnabled(flag && flag2);
    }
}
