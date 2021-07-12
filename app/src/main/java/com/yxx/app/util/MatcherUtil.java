package com.yxx.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.yxx.app.bean.SendInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: yangxl
 * Date: 2021/7/5 17:06
 * Description:
 */
public class MatcherUtil {
    static String matcherStr1 = "\\d{1,2}:\\d{1,2}-\\d{1,2}:\\d{1,2}";
    static String matcherStr2 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?(\\d{1,2}:\\d{1,2}-\\d{1,2}:\\d{1,2})?(\\d+|\\s+\\d+)?";
    static String matcherStr3 = "\\d{1,2}:\\d{1,2}-\\d{1,2}:\\d{1,2}(\\s+\\d+)?";
    static String matcherStr4 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?(\\d{1,2}:\\d{1,2})?(\\d+|\\s+\\d+)?";
    static String matcherStr5 = "\\d{1,2}:\\d{1,2}(\\s+\\d+)";

    static String matcherStr6 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?(\\d{1,2}:\\d{1,2})?";
    static String matcherStr7 = "\\d{1,2}:\\d{1,2}";

    static String matcherStr8 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?";

    public static List<SendInfo> getFormat(String text) throws Exception{
        List<SendInfo> infoList = new ArrayList<>();
        if (TextUtils.isEmpty(text)) {
             return infoList;
        }
        String[] array = text.split("\n");
        for(String str : array){
            //先验证是不是10:33-14:00 格式
            Pattern r1 = Pattern.compile(matcherStr1);
            Matcher m1 = r1.matcher(str);
            if (m1.find()) {
                //验证是否带年月日
                Pattern r2 = Pattern.compile(matcherStr2);
                Matcher m2 = r2.matcher(str);
                if (m2.find()) {

                    //取出年月日
                    Pattern r8 = Pattern.compile(matcherStr8);
                    Matcher m8 = r8.matcher(m2.group());
                    if(m8.find()){
                        SendInfo sendInfo = new SendInfo();
                        try{
                            TimeUtil.setInfoNYR(sendInfo, m8.group());
                            String timeStr = m2.group().replace(m8.group(), "");
                            String[] mArray = timeStr.split(" ");
                            TimeUtil.setInfoHM(sendInfo, mArray[0]);
                            if(mArray.length > 1 && !TextUtils.isEmpty(mArray[1])){
                                sendInfo.price = mArray[1];
                                infoList.add(sendInfo);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }else{
                    //不带年月日， 直接取出10:33-14:00 格式的
                    Pattern r3 = Pattern.compile(matcherStr3);
                    Matcher m3 = r3.matcher(str);
                    if(m3.find()){
                        SendInfo sendInfo = new SendInfo();
                        try{
                            TimeUtil.setInfoNYR(sendInfo, null);
                            String[] m3Array = m3.group().split(" ");
                            TimeUtil.setInfoHM(sendInfo, m3Array[0]);
                            if(m3Array.length > 1 && !TextUtils.isEmpty(m3Array[1])){
                                sendInfo.price = m3Array[1];
                                infoList.add(sendInfo);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                //不是10:33-14:00格式， 验证是否10:33 555 格式
                Pattern r4 = Pattern.compile(matcherStr4);
                Matcher m4 = r4.matcher(str);
                if (m4.find()) {
                    //带年月日
                    Pattern r8 = Pattern.compile(matcherStr8);
                    Matcher m8 = r8.matcher(m4.group());
                    if(m8.find()){
                        SendInfo sendInfo = new SendInfo();
                        try{
                            TimeUtil.setInfoNYR(sendInfo, m8.group());
                            String timeStr = m4.group().replace(m8.group(), "");
                            String[] mArray = timeStr.split(" ");
                            TimeUtil.setInfoHM(sendInfo, mArray[0]);
                            if(mArray.length > 1 && !TextUtils.isEmpty(mArray[1])){
                                sendInfo.price = mArray[1];
                                infoList.add(sendInfo);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                } else {
                    //不带年月日
                    Pattern r5 = Pattern.compile(matcherStr5);
                    Matcher m5 = r5.matcher(str);
                    if (m5.find()) {
                        SendInfo sendInfo = new SendInfo();
                        try{
                            TimeUtil.setInfoNYR(sendInfo, null);
                            String[] mArray = m5.group().split(" ");
                            TimeUtil.setInfoHM(sendInfo, mArray[0]);
                            if(mArray.length > 1 && !TextUtils.isEmpty(mArray[1])){
                                sendInfo.price = mArray[1];
                                infoList.add(sendInfo);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return infoList;
    }


    public static List<String> getDateFormatStr1(String str){
        List<String> stringList = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return stringList;
        }

        String[] array = str.split("\n");
        for (String s : array) {
            LogUtil.d("分割的数据 : " + s);
            Pattern r = Pattern.compile(matcherStr1);
            Matcher m = r.matcher(s);
            if (m.find()) {
                LogUtil.d("m1 : == " + m.group());
                //stringList.add(m.group());
                Pattern r2 = Pattern.compile(matcherStr2);
                Matcher m2 = r2.matcher(s);
                if (m2.find()) {
                    stringList.add(m2.group());
                }
            } else{
                LogUtil.d("没有找到m1");
                Pattern r3 = Pattern.compile(matcherStr3);
                Matcher m3 = r3.matcher(s);
                if (m3.find()) {
                    stringList.add(m3.group());
                } else {
                    Pattern r4 = Pattern.compile(matcherStr4);
                    Matcher m4 = r4.matcher(s);
                    if (m4.find()) {
                        stringList.add(m4.group());
                    }
                }
            }
        }
        return stringList;
    }

/*    public static List<String> getDateFormatStr2(String str) {
        List<String> stringList = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return stringList;
        }
        String[] array = str.split("\n");
        for (String s : array) {
            Pattern r = Pattern.compile(matcherStr3);
            Matcher m = r.matcher(s);
            if (m.find()) {
                stringList.add(m.group());
            } else {
                Pattern r2 = Pattern.compile(matcherStr4);
                Matcher m2 = r2.matcher(s);
                if (m2.find()) {
                    stringList.add(m2.group());
                }
            }
        }
        return stringList;
    }*/

    public static String getFormatStr(String str) {
        String ss = "";
        Pattern r = Pattern.compile(matcherStr5);
        Matcher m = r.matcher(str);
        if (m.find()) {
            ss = m.group();
        } else {
            Pattern r2 = Pattern.compile(matcherStr6);
            Matcher m2 = r2.matcher(str);
            if (m2.find()) {
                ss = m2.group();
            }
        }
        return ss;
    }
}
