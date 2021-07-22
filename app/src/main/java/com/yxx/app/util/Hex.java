package com.yxx.app.util;

import com.google.common.primitives.Bytes;
import com.yxx.app.bean.SendInfo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/7 9:27
 * Description:
 */
public class Hex {

    //数字转16进制， 低位在前
    public static String decToHex(int dec) {
        String hex = "";
        while (dec != 0) {
            String h = Integer.toString(dec & 0xff, 16);
            if ((h.length() & 0x01) == 1)
                h = '0' + h;
            hex = hex + h;
            dec = dec >> 8;
        }
        return hex.toUpperCase();
    }

    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * 16进制字符串转成字节数组
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static List<Byte> listToHexStr(List<SendInfo> infoList) {
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < infoList.size(); i++) {
            SendInfo info = infoList.get(i);
            info.autoDownTime();
            byte[] yearBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.year)));
            byte[] monthBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.month)));
            byte[] dayBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.day)));
            byte[] u_hoursBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.u_hours)));
            byte[] u_minuteBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.u_minute)));
            byte[] d_hoursBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.d_hours)));
            byte[] d_minuteBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.d_minute)));
            byte[] priceBytes = Hex.hexToByteArray(decToHex(Integer.parseInt(info.price)));

            byteList.addAll(Bytes.asList(yearBytes));
            byteList.addAll(Bytes.asList(monthBytes));
            byteList.addAll(Bytes.asList(dayBytes));
            if (u_hoursBytes.length == 0) {
                byteList.add((byte) 0x00);
            } else {
                byteList.addAll(Bytes.asList(u_hoursBytes));
            }
            if (u_minuteBytes.length == 0) {
                byteList.add((byte) 0x00);
            } else {
                byteList.addAll(Bytes.asList(u_minuteBytes));
            }
            if (d_hoursBytes.length == 0) {
                byteList.add((byte) 0x00);
            } else {
                byteList.addAll(Bytes.asList(d_hoursBytes));
            }
            if (d_minuteBytes.length == 0) {
                byteList.add((byte) 0x00);
            } else {
                byteList.addAll(Bytes.asList(d_minuteBytes));
            }
            byteList.addAll(Bytes.asList(priceBytes));

            if (priceBytes.length == 1) {
                byteList.add((byte) 0x00);
            }

        }
        LogUtil.d("==== byteList === " + byteList.size());
        return byteList;
    }
}