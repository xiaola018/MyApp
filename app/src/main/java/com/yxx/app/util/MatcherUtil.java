package com.yxx.app.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: yangxl
 * Date: 2021/7/5 17:06
 * Description:
 */
public class MatcherUtil {

    static String matcherStr1 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?(\\d{1,2}:\\d{1,2})?(\\d+|\\s+\\d+)?";
    static String matcherStr2 = "\\d{1,2}:\\d{1,2}(\\s+\\d+)";
    static String matcherStr3 = "\\d{4}[年/-]\\d{1,2}[月/-]\\d{1,2}日?(\\s+)?(\\d{1,2}:\\d{1,2})?";
    static String matcherStr4 = "\\d{1,2}:\\d{1,2}";

    public static List<String> getDateFormatStr(String str) {
        List<String> stringList = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return stringList;
        }
        String[] array = str.split("\n");
        for (String s : array) {
            Pattern r = Pattern.compile(matcherStr1);
            Matcher m = r.matcher(s);
            if (m.find()) {
                stringList.add(m.group());
            } else {
                Pattern r2 = Pattern.compile(matcherStr2);
                Matcher m2 = r2.matcher(s);
                if (m2.find()) {
                    stringList.add(m2.group());
                }
            }
        }

        for (String ss : stringList) {
            LogUtil.d("  == ss == " + ss);
        }
        return stringList;
    }

    public static String getFormatStr(String str) {
        String ss = "";
        Pattern r = Pattern.compile(matcherStr3);
        Matcher m = r.matcher(str);
        if (m.find()) {
            ss = m.group();
        } else {
            Pattern r2 = Pattern.compile(matcherStr4);
            Matcher m2 = r2.matcher(str);
            if (m2.find()) {
                ss = m2.group();
            }
        }
        return ss;
    }
}
