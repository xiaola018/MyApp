package com.yxx.app.bean;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author: yangxl
 * Date: 2021/7/6 14:15
 * Description:
 */
public class SendInfo {
    public String year = "";
    public String month = "";
    public String day = "";
    public String u_hours = "";
    public String u_minute = "";
    public String d_hours = "";
    public String d_minute = "";
    public String price = "";

    public int type;

    public boolean hasDownTime(){
        return !TextUtils.isEmpty(d_hours) && !TextUtils.isEmpty(d_minute);
    }

    public void autoDownTime(){
        if(!hasDownTime()){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),
                    Integer.parseInt(u_hours),Integer.parseInt(u_minute));
            long beginTime = calendar.getTime().getTime();
            long maxTime = beginTime + 4 * 60 * 60 * 1000;
            long rtn = beginTime + (long)(Math.random()*(maxTime - beginTime));
            calendar.setTime(new Date(rtn));
            this.d_hours = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            this.d_minute = String.valueOf(calendar.get(Calendar.MINUTE));
        }
    }

    @Override
    public String toString() {
        return "SendInfo{" +
                "year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", day='" + day + '\'' +
                ", u_hours='" + u_hours + '\'' +
                ", u_minute='" + u_minute + '\'' +
                ", d_hours='" + d_hours + '\'' +
                ", d_minute='" + d_minute + '\'' +
                ", price='" + price + '\'' +
                ", type=" + type +
                '}';
    }
}
