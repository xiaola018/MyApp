package com.yxx.app.util;

import android.text.TextUtils;

import com.yxx.app.bean.SendInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/3 16:41
 * Description:
 */
public class TimeUtil {

    public static String getNYR() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        return year + "-" + month + "-" + day;
    }

    public static int getCurrHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static String getCurrHM() {
        String h = String.valueOf(getCurrHour());
        String m = String.valueOf(getCurrMinute());
        if (h.length() == 1) {
            h = "0" + h;
        }
        if (m.length() == 1) {
            m = "0" + m;
        }
        return h + ":" + m;
    }

    public static String nyrFormat(int year, int month, int day) {
        String m = String.valueOf(month);
        String d = String.valueOf(day);
        if (m.length() == 1) {
            m = "0" + m;
        }
        if (d.length() == 1) {
            d = "0" + d;
        }
        return year + "-" + m + "-" + d;
    }

    public static String nyrFormat(String y, String m, String d) {
        if (m.length() == 1) {
            m = "0" + m;
        }
        if (d.length() == 1) {
            d = "0" + d;
        }
        return y + "-" + m + "-" + d;
    }

    public static String hmFormat(int hourOfDay, int minute) {
        String h = String.valueOf(hourOfDay);
        String m = String.valueOf(minute);
        if (h.length() == 1) {
            h = "0" + h;
        }
        if (m.length() == 1) {
            m = "0" + m;
        }
        return h + ":" + m;
    }

    public static String hmFormat(String h, String m) {
        if (h.length() == 1) {
            h = "0" + h;
        }
        if (m.length() == 1) {
            m = "0" + m;
        }
        return h + ":" + m;
    }

    private static String lengthFormat(int arg) {
        String str = String.valueOf(arg);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    public static List<SendInfo> getInfos(List<String> stringList) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        List<SendInfo> infoList = new ArrayList<>();
        for (String str : stringList) {
            try {
                SendInfo sendInfo = new SendInfo();
                String dateStr = MatcherUtil.getFormatStr(str);
                String pstr = str.replace(dateStr, "").replace(" ", "");

                Date date = null;
                if (str.contains("年")) {
                    if (str.contains(":")) {
                        date = sdf1.parse(dateStr.trim());
                    } else {
                        date = sdf1.parse(dateStr.trim() + " " + getCurrHM());
                    }
                } else if (str.contains("-")) {
                    if (str.contains(":")) {
                        date = sdf2.parse(dateStr.trim());
                    } else {
                        date = sdf2.parse(dateStr.trim() + " " + getCurrHM());
                    }
                } else if (str.contains("/")) {
                    if (str.contains(":")) {
                        date = sdf3.parse(dateStr.trim());
                    } else {
                        date = sdf3.parse(dateStr.trim() + " " + getCurrHM());
                    }
                } else {
                    date = sdf2.parse(getNYR() + " " + str.trim());
                }
                Calendar c = Calendar.getInstance();
                if (date != null) {
                    c.setTime(date);
                    sendInfo.year = String.valueOf(c.get(Calendar.YEAR));
                    sendInfo.month = lengthFormat(c.get(Calendar.MONTH) + 1);
                    sendInfo.day = lengthFormat(c.get(Calendar.DAY_OF_MONTH));
                    sendInfo.hours = lengthFormat(c.get(Calendar.HOUR_OF_DAY));
                    sendInfo.minute = lengthFormat(c.get(Calendar.MINUTE));
                }

                sendInfo.price = pstr;
                if (!TextUtils.isEmpty(pstr)) {
                    infoList.add(sendInfo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infoList;
    }
}
