package com.yxx.app.util;


import com.yxx.app.MyApplication;

/**
 * @author Mr.Yang
 * @date 2019/8/22 0022
 */
public class DpUtils {

    public static int dp2px(float dpValue) {
        float density = MyApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int px2dp(float pxValue) {
        float density = MyApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    public static int sp2px(float spValue) {
        float density = MyApplication.getInstance().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * density + 0.5f);
    }
}
