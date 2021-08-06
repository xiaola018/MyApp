package com.yxx.app.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lljjcoder.bean.CustomCityData;
import com.lljjcoder.style.citypickerview.widget.wheel.WheelView;
import com.lljjcoder.style.citypickerview.widget.wheel.adapters.ArrayWheelAdapter;
import com.yxx.app.BluetoothManager;
import com.yxx.app.BluetoothManagerBle;
import com.yxx.app.R;
import com.yxx.app.bean.ProInfo;
import com.yxx.app.util.JsonUtils;
import com.yxx.app.util.SPUtil;
import com.yxx.app.util.TemplateScheme;
import com.yxx.app.util.ToastUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Author: yangxl
 * Date: 2021/7/12 16:49
 * Description:     更换城市
 */
public class ReplaceCityActivity extends AppCompatActivity implements
        View.OnClickListener, TemplateScheme.OnTemplateDownCallback {

    /**
     * 自定义数据源-省份数据
     */
    private final List<CustomCityData> mProvinceListData = new ArrayList<>();

    private List<ProInfo> proInfoList;

    private WheelView proWheelView;
    private WheelView cityWheelView;
    private Toolbar toolbar;
    private Button btn_replace;

    private MyHandler mHandler = new MyHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace_city);

        initView();

        readJsonText();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        proWheelView = findViewById(R.id.proWheelView);
        cityWheelView = findViewById(R.id.cityWheelView);
        proWheelView = findViewById(R.id.proWheelView);
        cityWheelView = findViewById(R.id.cityWheelView);
        btn_replace = findViewById(R.id.btn_replace);

        toolbar.setNavigationOnClickListener(view -> finish());

        proWheelView.addChangingListener((wheel, oldValue, newValue) -> updateCities());

        btn_replace.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(!BluetoothManager.get().isConnect()){
            ToastUtil.show("蓝牙未连接");
            return;
        }
        CustomCityData proData = mProvinceListData.get(proWheelView.getCurrentItem());
        String proName = proData.getName();
        String cityName = proData.getList().get(cityWheelView.getCurrentItem()).getName();
        SPUtil.setCheckedProvince(proName);
        SPUtil.setCheckedCity(cityName);

        try {
            String fileName = proInfoList.get(proWheelView.getCurrentItem()).getCities().get(cityWheelView.getCurrentItem()).getFile();
            if(!TextUtils.isEmpty(fileName)){
                onTemplateDownStart();
                TemplateScheme templateScheme = new TemplateScheme(fileName,this);
                templateScheme.sendStartDownloadCmd();
            }else{
                ToastUtil.show("模板文件不存在");
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.show("模板文件错误");
        }
    }

    @Override
    public void onTemplateDownStart() {
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onTemplateDownProgress(int progress) {
        Message message = new Message();
        message.arg1 = progress;
        message.what = 1;
        mHandler.sendMessage(message);
    }

    @Override
    public void onSendSuccess(int code) {

    }

    @Override
    public void onTemplateDownFinish(int code) {
        Message msg = new Message();
        msg.arg1 = code;
        msg.what = 2;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onTemplateDownFail(int code, String msg) {
        if(code != BluetoothManagerBle.CODE_PRINT){
            Message message = new Message();
            message.arg1 = code;
            message.obj = msg;
            message.what = 3;
            mHandler.sendMessage(message);
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@androidx.annotation.NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    btn_replace.setText("0%");
                    btn_replace.setEnabled(false);
                    break;
                case 2:
                    btn_replace.setText("更 换");
                    btn_replace.setEnabled(true);
                    ToastUtil.show("更换成功");
                    break;
                case 1:
                    int progress = msg.arg1;
                    btn_replace.setText(String.format("%s%s", progress, "%"));
                    break;
                case 3:
                    int code = msg.arg1;
                    String text = msg.obj != null ? msg.obj.toString() : "";
                    btn_replace.setText("更 换");
                    btn_replace.setEnabled(true);
                    ToastUtil.show(String.format("%s，错误码：%s", !TextUtils.isEmpty(text) ? text : "数据发送错误", code));
                    btn_replace.setText("更 换");
                    btn_replace.setEnabled(true);
                    break;
            }
        }
    }

    /**
     * 读取省份城市文件
     */
    private void readJsonText() {
        Observable.create(new ObservableOnSubscribe<List<CustomCityData>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<CustomCityData>> emitter) throws Exception {
                try {
                    InputStreamReader inputReader = new InputStreamReader(ReplaceCityActivity.this.getResources().getAssets().open("pro_city_json.txt"));
                    BufferedReader bufReader = new BufferedReader(inputReader);
                    StringBuilder stringBuffer = new StringBuilder();
                    String str = "";
                    while ((str = bufReader.readLine()) != null) {
                        stringBuffer.append(str);
                    }
                    proInfoList = JsonUtils.fromJsonArray(stringBuffer.toString(), ProInfo.class);

                    for (ProInfo proInfo : proInfoList) {
                        CustomCityData pro = new CustomCityData(proInfo.getId(), proInfo.getName());
                        List<CustomCityData> cityList = new ArrayList<>();
                        for (ProInfo cityInfo : proInfo.getCities()) {
                            CustomCityData city = new CustomCityData(cityInfo.getId(), cityInfo.getName());
                            cityList.add(city);
                        }
                        pro.setList(cityList);
                        mProvinceListData.add(pro);
                    }

                    Comparator comparator = Collator.getInstance(Locale.CHINA);
                    Collections.sort(mProvinceListData, (p1, p2) -> {
                        return comparator.compare(p1.getName(), p2.getName());
                    });

                    emitter.onNext(mProvinceListData);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CustomCityData>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<CustomCityData> data) {
                        updatePro();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updatePro() {
        ArrayWheelAdapter arrayWheelAdapter = new ArrayWheelAdapter<CustomCityData>(this, mProvinceListData);
        //自定义item
/*        if (config.getCustomItemLayout() != CityConfig.NONE && config.getCustomItemTextViewId() != CityConfig.NONE) {
            arrayWheelAdapter.setItemResource(config.getCustomItemLayout());
            arrayWheelAdapter.setItemTextResource(config.getCustomItemTextViewId());
        } else {
            arrayWheelAdapter.setItemResource(R.layout.default_item_city);
            arrayWheelAdapter.setItemTextResource(R.id.default_item_city_name_tv);
        }*/
        arrayWheelAdapter.setTextColor(Color.parseColor("#010101"));
        arrayWheelAdapter.setTextSize(25);
    //    arrayWheelAdapter.setItemResource(R.layout.item_custome_city);
    //    arrayWheelAdapter.setItemTextResource(R.id.item_custome_city_name_tv);
   //     R.layout.default_item_city
        proWheelView.setViewAdapter(arrayWheelAdapter);
        //获取上一次选中的省份
        String proName = SPUtil.getCheckedProvince();
        if (!TextUtils.isEmpty(proName)) {
            for (int i = 0; i < mProvinceListData.size(); i++) {
                if(proName.equals(mProvinceListData.get(i).getName())){
                    proWheelView.setCurrentItem(i);
                    break;
                }
            }
        } else {
            proWheelView.setCurrentItem(0);
        }
        proWheelView.setVisibleItems(5);
        proWheelView.setCyclic(false);

        updateCities();
    }

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        //省份滚轮滑动的当前位置
        int pCurrent = proWheelView.getCurrentItem();

        //省份选中的名称
        List<CustomCityData> proArra = mProvinceListData;
        CustomCityData mProvinceBean = proArra.get(pCurrent);

        List<CustomCityData> pCityList = mProvinceBean.getList();
        if (pCityList == null) return;


        ArrayWheelAdapter cityWheel = new ArrayWheelAdapter<CustomCityData>(this, pCityList);
        //自定义item
        cityWheel.setTextColor(Color.parseColor("#010101"));
        cityWheel.setTextSize(25);
        //cityWheel.setItemResource(R.layout.item_custome_city);
        //cityWheel.setItemTextResource(R.id.item_custome_city_name_tv);
        cityWheelView.setCyclic(false);
        cityWheelView.setViewAdapter(cityWheel);
        cityWheelView.setVisibleItems(5);

        //获取上一次选中的城市
        String cityName = SPUtil.getCheckedProvince();
        if (!TextUtils.isEmpty(cityName)) {
            for (int i = 0; i < pCityList.size(); i++) {
                if(cityName.equals(pCityList.get(i).getName())){
                    cityWheelView.setCurrentItem(i);
                    break;
                }
            }
        } else {
            cityWheelView.setCurrentItem(0);
        }

        cityWheelView.setViewAdapter(cityWheel);

    }
}
