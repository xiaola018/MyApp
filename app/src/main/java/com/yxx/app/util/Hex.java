package com.yxx.app.util;

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

    public static List<String> listToHexStr(List<SendInfo> infoList) {
        List<String> hexList = new ArrayList<>();
        for (SendInfo info : infoList) {
            hexList.add(decToHex(Integer.parseInt(info.year)));
            hexList.add(decToHex(Integer.parseInt(info.month)));
            hexList.add(decToHex(Integer.parseInt(info.day)));
            hexList.add(decToHex(Integer.parseInt(info.u_hours)));
            hexList.add(decToHex(Integer.parseInt(info.u_minute)));
            hexList.add(decToHex(Integer.parseInt(info.d_hours)));
            hexList.add(decToHex(Integer.parseInt(info.d_minute)));
            hexList.add(decToHex(Integer.parseInt(info.price)));
        }
        LogUtil.d("== list hex str = " + hexList.toString());

        BitSet bitSet = new BitSet();

        return hexList;
    }

    /**
     * Bit转Byte
     */
    public static byte bitToByte(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {//4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }

    /**
     * Byte转Bit
     */
    public static String byteToBit(byte b) {
        return "" +(byte)((b >> 7) & 0x1) +
                (byte)((b >> 6) & 0x1) +
                (byte)((b >> 5) & 0x1) +
                (byte)((b >> 4) & 0x1) +
                (byte)((b >> 3) & 0x1) +
                (byte)((b >> 2) & 0x1) +
                (byte)((b >> 1) & 0x1) +
                (byte)((b >> 0) & 0x1);
    }
}