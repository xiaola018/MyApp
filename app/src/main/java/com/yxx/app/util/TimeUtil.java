package com.yxx.app.util;

import java.util.Calendar;

/**
 * Author: yangxl
 * Date: 2021/7/3 16:41
 * Description:
 */
public class TimeUtil {

    public static String getNYR(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        if(month.length() == 1){
            month = "0" + month;
        }
        if(day.length() == 1){
            day = "0" + day;
        }
        return year + "-" + month + "-" + day;
    }

    public static int getCurrHour(){
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrMinute(){
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static String getCurrHM(){
        String h = String.valueOf(getCurrHour());
        String m = String.valueOf(getCurrMinute());
        if(h.length() == 1){
            h = "0" + h;
        }
        if(m.length() == 1){
            m = "0" + m;
        }
        return h + ":" + m;
    }

    public static String nyrFormat(int year, int month, int day){
        String m = String.valueOf(month);
        String d = String.valueOf(day);
        if(m.length() == 1){
            m = "0" + m;
        }
        if(d.length() == 1){
            d = "0" + d;
        }
        return year + "-" + m + "-" + d;
    }

    public static String hmFormat(int hourOfDay, int minute){
        String h = String.valueOf(hourOfDay);
        String m = String.valueOf(minute);
        if(h.length() == 1){
            h = "0" + h;
        }
        if(m.length() == 1){
            m = "0" + m;
        }
        return h + ":" + m;
    }
}
