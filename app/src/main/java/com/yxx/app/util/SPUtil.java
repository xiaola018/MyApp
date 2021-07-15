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

    public static int getInt(String key){
        return sp.getInt(key, 0);
    }

    public static void putInt(String key, int value){
        eEditor.putInt(key, value).apply();
    }


    public static final String CACHE_DATA_LIST = "cache_data_list";
    public static final String CACHE_IS_NOW_PRINT = "cache_is_now_print";
    public static final String CACHE_CHECKED_PROVINCE = "cache_checked_province";
    public static final String CACHE_CHECKED_CITY = "cache_checked_city";

    /**
     *  是否立即打印
     */
    public static int isNowPrint(){
        return getInt(CACHE_IS_NOW_PRINT);
    }

    public static void setCheckedProvince(String proName){
        putString(CACHE_CHECKED_PROVINCE, proName);
    }

    public static void setCheckedCity(String cityName){
        putString(CACHE_CHECKED_CITY, cityName);
    }

    public static String getCheckedProvince(){
        return getString(CACHE_CHECKED_PROVINCE);
    }

    public static String getCheckedCity(){
        return getString(CACHE_CHECKED_CITY);
    }
}
