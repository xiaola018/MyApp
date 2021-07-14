package com.yxx.app.util;

import android.content.res.AssetManager;

import com.yxx.app.BluetoothManager;
import com.yxx.app.MyApplication;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: yangxl
 * Date: 2021/7/14 11:13
 * Description:
 */
public class TemplateScheme {

    //波特率
    public static final int BAUD = 115200;

    //下发模板指令标识
    public static final int send_start_download_cmd = 1;

    //下发握手指令
    public static final int send_handshake_cmd = 2;

    //下发设置波特率指令
    public static final int send_set_baud_cmd = 3;

    //下发擦除指令
    public static final int send_erase_cmd = 4;

    //下发模板数据
    public static final int send_template_data_cmd = 5;

    //握手成功指令
    public static final byte HANDSHAKE_RXBUFFER = 0x50;

    //设置参数成功指令
    public static final byte SETBAUD_RXBUFFER =  0x01;

    //擦除模板成功指令
    public static final byte ERASE_RXBUFFER = 0x03;

    public void getStartDownloadCmd(){
        byte[] bytes = new byte[]{2};
        BluetoothManager.get().sendThread(bytes);
    }

    public static byte getHandShakeCmd(){
        return 0x7f;
    }

    public static byte[] getSetBaudCmd(byte arg){
        byte[] baudBytes = ByteUtil.int2BytesHib(BAUD);
        byte[] txBuffer = new byte[8];
        txBuffer[0] = 0x01;
        txBuffer[1] = arg;
        txBuffer[2] = 0x40;
        txBuffer[3] = baudBytes[0];
        txBuffer[4] = baudBytes[1];
        txBuffer[5] = 0x00;
        txBuffer[6] = 0x00;
        txBuffer[7] = (byte) 0x97;
        return txBuffer;
    }

    public static byte[] getEraseCmd(){
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x03;
        txBuffer[1] = 0x00;
        txBuffer[2] = 0x00;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        return txBuffer;
    }

    public void sendTemplateData(String fileName){
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x22;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        AssetManager manager = MyApplication.getInstance().getResources().getAssets();
        try {
            InputStream inputStream = manager.open(fileName);
            //总长度
            int length = inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
