package com.yxx.app.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lljjcoder.Interface.OnCustomCityPickerItemClickListener;
import com.lljjcoder.bean.CustomCityData;
import com.lljjcoder.citywheel.CityConfig;
import com.lljjcoder.citywheel.CustomConfig;
import com.lljjcoder.style.citycustome.CustomCityPicker;
import com.lljjcoder.style.citypickerview.CityPickerView;
import com.lljjcoder.style.citypickerview.widget.wheel.OnWheelChangedListener;
import com.lljjcoder.style.citypickerview.widget.wheel.WheelView;
import com.lljjcoder.style.citypickerview.widget.wheel.adapters.ArrayWheelAdapter;
import com.yxx.app.MyApplication;
import com.yxx.app.R;
import com.yxx.app.bean.ProInfo;
import com.yxx.app.dialog.NeverMenuPopup;
import com.yxx.app.util.JsonUtils;
import com.yxx.app.util.LogUtil;
import com.yxx.app.view.MenuConnectView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
public class ReplaceCityActivity extends AppCompatActivity {

    /**
     * 自定义数据源-省份数据
     */
    private List<CustomCityData> mProvinceListData = new ArrayList<>();

    private WheelView proWheelView;
    private WheelView cityWheelView;
    private Toolbar toolbar;
    private Button btn_replace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace_city);

        initView();

        readJsonText();
    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        proWheelView = findViewById(R.id.proWheelView);
        cityWheelView = findViewById(R.id.cityWheelView);
        proWheelView = findViewById(R.id.proWheelView);
        cityWheelView = findViewById(R.id.cityWheelView);
        btn_replace = findViewById(R.id.btn_replace);


        proWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                updateCities();
            }
        });

        btn_replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomCityData proData = mProvinceListData.get(proWheelView.getCurrentItem());
                LogUtil.d(" 省份 : " + proData.getName());
                LogUtil.d(" 城市 : " + proData.getList().get(cityWheelView.getCurrentItem()));
            }
        });
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
                    List<ProInfo> proInfoList = JsonUtils.fromJsonArray(stringBuffer.toString(), ProInfo.class);

                    for(ProInfo proInfo : proInfoList){
                        CustomCityData pro = new CustomCityData(proInfo.getId(), proInfo.getName());
                        List<CustomCityData> cityList = new ArrayList<>();
                        for(ProInfo cityInfo : proInfo.getCities()){
                            CustomCityData city = new CustomCityData(cityInfo.getId(), cityInfo.getName());
                            cityList.add(city);
                        }
                        pro.setList(cityList);
                        mProvinceListData.add(pro);
                    }

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

    private void  updatePro(){
        ArrayWheelAdapter arrayWheelAdapter = new ArrayWheelAdapter<CustomCityData>(this, mProvinceListData);
        //自定义item
/*        if (config.getCustomItemLayout() != CityConfig.NONE && config.getCustomItemTextViewId() != CityConfig.NONE) {
            arrayWheelAdapter.setItemResource(config.getCustomItemLayout());
            arrayWheelAdapter.setItemTextResource(config.getCustomItemTextViewId());
        } else {
            arrayWheelAdapter.setItemResource(R.layout.default_item_city);
            arrayWheelAdapter.setItemTextResource(R.id.default_item_city_name_tv);
        }*/
        proWheelView.setViewAdapter(arrayWheelAdapter);
        proWheelView.setCurrentItem(0);
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


        //设置最初的默认城市
/*        int cityDefault = -1;
        if (!TextUtils.isEmpty(config.getDefaultCityName()) && pCityList.size() > 0) {
            for (int i = 0; i < pCityList.size(); i++) {
                if (pCityList.get(i).getName().startsWith(config.getDefaultCityName())) {
                    cityDefault = i;
                    break;
                }
            }
        }*/


        ArrayWheelAdapter cityWheel = new ArrayWheelAdapter<CustomCityData>(this, pCityList);
        //自定义item
/*        if (config.getCustomItemLayout() != CityConfig.NONE && config.getCustomItemTextViewId() != CityConfig.NONE) {
            cityWheel.setItemResource(config.getCustomItemLayout());
            cityWheel.setItemTextResource(config.getCustomItemTextViewId());
        } else {
            cityWheel.setItemResource(R.layout.default_item_city);
            cityWheel.setItemTextResource(R.id.default_item_city_name_tv);
        }*/

        cityWheelView.setCyclic(false);
        cityWheelView.setViewAdapter(cityWheel);
        cityWheelView.setCurrentItem(0);
        cityWheelView.setVisibleItems(5);
/*        if (-1 != cityDefault) {
            mViewCity.setCurrentItem(cityDefault);
        } else {
            mViewCity.setCurrentItem(0);
        }*/


        cityWheelView.setViewAdapter(cityWheel);

    }
}
