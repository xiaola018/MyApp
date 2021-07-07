package com.yxx.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.yxx.app.MyApplication;

/**
 * Author: yangxl
 * Date: 2021/7/7 15:05
 * Description:
 */
public class SPUtil {

    public static String FILE_NAME = "Share_My_ZS";
    private static SharedPreferences sp;
    private static SharedPreferences.Editor eEditor;

    static{
        sp = MyApplication.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        eEditor = sp.edit();
    }

    public static void putString(String key, String value) {
        eEditor.putString(key, value).apply();
    }

    public static String getString(String key) {
        return sp.getString(key, "");
    }


    public static final String CACHE_DATA_LIST = "cache_data_list";
}
