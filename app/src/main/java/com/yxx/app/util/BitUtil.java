package com.yxx.app.util;

import java.util.Arrays;

/**
 * Author: yangxl
 * Date: 2021/7/12 14:42
 * Description:
 */
public class BitUtil {

    /**
     *  获取状态寄存器所需的值
     * @return
     */
    public static String getStatusBit(){
        String[] array = new String[]{
                "0",
                "0",
                "0",
                "0",
                "0",
                "0",
                "0",
                SPUtil.isNowPrint() + ""
        };
        return Arrays.toString(array);
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

