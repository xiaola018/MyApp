package com.yxx.app.util;

/**
 * Author: yangxl
 * Date: 2021/7/13 19:41
 * Description:
 */
public class ByteUtil {
    public static void abc(){
        int2Byte(0);
        int2Bytes(0);
    }

    public static void int2Byte(int num){
        byte b = (byte)( (num << 24) >> 24 );
        LogUtil.d("  ===  222 ==" + b);
    }

    public static void int2Bytes(int num)  {
        byte[] mybyte = new byte[2];
        mybyte[1] =(byte)( (num << 16) >> 24 );
        mybyte[0] =(byte)( (num << 24) >> 24 );
        for(byte b : mybyte){
            LogUtil.d("== bb ==" + b);
        }
    }

    private void WriteInt(int n) {
        byte[] buf = new byte[4];
        buf[3] =(byte)( n >> 24 );
        buf[2] =(byte)( (n << 8) >> 24 );
        buf[1] =(byte)( (n << 16) >> 24 );
        buf[0] =(byte)( (n << 24) >> 24 );
    }
}
