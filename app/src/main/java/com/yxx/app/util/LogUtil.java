package com.yxx.app.util;

import android.util.Log;

import com.yxx.app.BuildConfig;

/**
 * Author: yangxl
 * Date: 2021/7/2 15:17
 * Description:
 */
public class LogUtil {

    public static final String TAG = "com.yxx.app.log";

    public static void d(String msg){
        if(BuildConfig.DEBUG)Log.d(TAG, msg);
    }
}
