package com.yxx.app.util;

import android.widget.Toast;

import com.yxx.app.MyApplication;

/**
 * Author: yangxl
 * Date: 2021/7/15 14:23
 * Description:
 */
public class ToastUtil {

    public static void show(String text){
        Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
    }
}
