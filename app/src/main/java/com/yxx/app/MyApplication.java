package com.yxx.app;

import android.app.Application;

/**
 * Author: yangxl
 * Date: 2021/7/3 11:23
 * Description:
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
