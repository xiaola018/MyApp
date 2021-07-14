package com.yxx.app.util;

import com.google.common.primitives.Bytes;
import com.yxx.app.bean.SendInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/13 19:41
 * Description:
 */
public class ByteUtil {
    //数据包特征首字符
    private final static int FEATURES_START_CHAR = 160;
    //数据包特征尾字符
    private final static int FEATURES_END_CHAR = 10;
    //命令类型
    private final static int CMD_TYPE = 26;

    private static int DATA_DEFAULT_LENGTH = 20;//每个包默认长度

    public static byte int2Byte(int num){
        return (byte)( (num << 24) >> 24 );
    }

    //int 转 byte[] , 低位在前
    public static byte[] int2Bytes(int num)  {
        byte[] mybyte = new byte[2];
        mybyte[1] =(byte)( (num << 16) >> 24 );
        mybyte[0] =(byte)( (num << 24) >> 24 );
        return mybyte;
    }

    //int 转 byte[] , 高位在前
    public static byte[] int2BytesHib(int num)  {
        byte[] mybyte = new byte[2];
        mybyte[0] =(byte)( (num << 16) >> 24 );
        mybyte[1] =(byte)( (num << 24) >> 24 );
        return mybyte;
    }

    private void int4Bytes(int n) {
        byte[] buf = new byte[4];
        buf[3] =(byte)( n >> 24 );
        buf[2] =(byte)( (n << 8) >> 24 );
        buf[1] =(byte)( (n << 16) >> 24 );
        buf[0] =(byte)( (n << 24) >> 24 );
    }

    public static byte[] combData(List<SendInfo> infoList) {
        List<Byte> byteList = new ArrayList<>();
        //添加命令类型
        byteList.add(ByteUtil.int2Byte(CMD_TYPE));

        //添加票总张数
        byteList.add(ByteUtil.int2Byte(infoList.size()));

        //添加状态寄存器
        byteList.add(BitUtil.bitToByte(BitUtil.getStatusBit()));

        //添加需要打印的票数据
        for(SendInfo info : infoList){
            try{
                //年
                byteList.addAll(Bytes.asList(ByteUtil.int2Bytes(Integer.parseInt(info.year))));
                //月
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.month)));
                //日
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.day)));
                //上时- 时
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.u_hours)));
                //上时- 分
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.u_minute)));
                //下时- 时
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.d_hours)));
                //下时- 分
                byteList.add(ByteUtil.int2Byte(Integer.parseInt(info.d_minute)));
                //金额
                byteList.addAll(Bytes.asList(ByteUtil.int2Bytes(Integer.parseInt(info.price))));
            }catch (Exception e){

            }
        }
        LogUtil.d("组装完成需要打印的票数据， size = " + byteList.size());

        //添加数据包长度。//类型+总张数+寄存器+票数据 = 总长度
        byteList.addAll(0,Bytes.asList(ByteUtil.int2Bytes(byteList.size())));

        //添加校验码.//类型+总张数+寄存器+票数据 (字节相加)
        byte signByte = getSign(byteList);
        byteList.add(0, signByte);

        //添加特征首字符
        byteList.add(0, ByteUtil.int2Byte(FEATURES_START_CHAR));
        byteList.add(0, ByteUtil.int2Byte(FEATURES_START_CHAR));

        //添加特征尾字符
        byteList.add(ByteUtil.int2Byte(FEATURES_END_CHAR));
        byteList.add(ByteUtil.int2Byte(FEATURES_END_CHAR));

        return Bytes.toArray(byteList);
    }

    private static byte getSign(List<Byte> byteList) {
        int byteNum = 0;
        for (Byte b : byteList) {
            byteNum += b;
        }
        return ByteUtil.int2Byte(byteNum);
    }

    //<editor-fold desc="数据分包">
    /**
     *  将需要发送的字节数据分包为List byte数组
     * @param buff     发送的字节数组
     * @param separateLength    分包的长度
     * @return
     */
    public static List<byte[]> getSendDataByte(byte[] buff, int separateLength) {
        List<byte[]> listSendData = new ArrayList<>();
        if(separateLength < DATA_DEFAULT_LENGTH)separateLength = DATA_DEFAULT_LENGTH;
        int[] sendDataLength = dataSeparate(buff.length, separateLength);
        for (int i = 0; i < sendDataLength[0]; i++) {
            byte[] dataFor20 = new byte[separateLength];
            System.arraycopy(buff, i * separateLength, dataFor20, 0, separateLength);
            listSendData.add(dataFor20);
        }
        if (sendDataLength[1] > 0) {
            byte[] lastData = new byte[sendDataLength[1]];
            System.arraycopy(buff, sendDataLength[0] * separateLength, lastData, 0, sendDataLength[1]);
            listSendData.add(lastData);
        }
        return listSendData;
    }

    /**
     * 将数据分包处理
     * @param dataLength    数据总长度
     * @param separateLength    分包长度（每次发送多少字节）
     * @return
     */
    private static int[] dataSeparate(int dataLength, int separateLength) {
        int[] lens = new int[2];
        lens[0] = dataLength / separateLength;
        lens[1] = dataLength % separateLength;
        return lens;
    }
    //</editor-fold>
}
