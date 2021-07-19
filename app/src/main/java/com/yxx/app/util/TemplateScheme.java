package com.yxx.app.util;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import com.yxx.app.BluetoothManager;
import com.yxx.app.MyApplication;

import org.w3c.dom.ls.LSOutput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/14 11:13
 * Description:
 */
public class TemplateScheme {

    //波特率
    public static final int BAUD = 115200;

    private byte argBuffer;//回传的变量，用于接收握手成功后的参数值

    private int readLength = 1;//从文件读取了多少长度

    private BluetoothManager bluetoothManager;

    private String fileName;
    private OnTemplateDownCallback downCallback;
    private int sendCount;

    private Handler timeHandler = new Handler(Looper.getMainLooper());

    public TemplateScheme(String fileName, OnTemplateDownCallback downCallback) {
        this.fileName = fileName;
        this.downCallback = downCallback;
        bluetoothManager = BluetoothManager.get();
        bluetoothManager.setTemplateScheme(this);
    }

    public void setDownCallback(OnTemplateDownCallback downCallback) {
        this.downCallback = downCallback;
    }

    public OnTemplateDownCallback getDownCallback() {
        return downCallback;
    }

    /**
     * 发送下载模板开始指令
     */
    public void sendStartDownloadCmd() {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xA0;
        bytes[1] = (byte) 0xA0;
        bytes[2] = (byte) 0xFC;
        bytes[3] = 0x01;
        bytes[4] = 0x00;
        bytes[5] = (byte) 0xfb;
        bytes[6] = 0x0a;
        bytes[7] = 0x0a;
        bluetoothManager.setReadCode(BluetoothManager.CODE_START_DOWNLOAD);
        bluetoothManager.sendThread(bytes);
    }

    /**
     * 发送握手指令
     */
    public void sendHandShakeCmd() {
        byte[] bytes = new byte[]{0x7f};
        LogUtil.d("发送握手指令：" + bytes.length);
        bluetoothManager.setReadCode(BluetoothManager.CODE_HANDSHAKE);
        bluetoothManager.sendThread(bytes);
    }

    /**
     * 发送设置参数指令
     */
    public void sendSetBaudCmd() {
        byte[] baudBytes = ByteUtil.int2BytesHib(BAUD);
        byte[] txBuffer = new byte[8];
        txBuffer[0] = 0x01;
        txBuffer[1] = argBuffer;
        txBuffer[2] = 0x40;
        txBuffer[3] = baudBytes[0];
        txBuffer[4] = baudBytes[1];
        txBuffer[5] = 0x00;
        txBuffer[6] = 0x00;
        txBuffer[7] = (byte) 0x97;
        bluetoothManager.setReadCode(BluetoothManager.CODE_SET_BAUD);
        bluetoothManager.sendThread(txBuffer);
    }

    /**
     * 发送准备下载指令
     */
    public void sendReadyDownloadCmd() {
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x05;
        txBuffer[1] = 0x00;
        txBuffer[2] = 0x00;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        bluetoothManager.setReadCode(BluetoothManager.CODE_READY_DOWNLOAD);
        bluetoothManager.sendThread(txBuffer);
    }

    /**
     * 发送擦除芯片指令
     */
    public void sendEraseCmd() {
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x03;
        txBuffer[1] = 0x00;
        txBuffer[2] = 0x00;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        bluetoothManager.setReadCode(BluetoothManager.CODE_ERASE);
        bluetoothManager.sendThread(txBuffer);
    }

    public void sendTemplateData() {
        MyThread myThread = new MyThread();
        myThread.start();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {
            byte[] txBuffer = new byte[5];
            txBuffer[0] = (byte) (readLength == 1 ? 0x22 : 0x02);
            txBuffer[3] = 0x5a;
            txBuffer[4] = (byte) 0xa5;
            AssetManager manager = MyApplication.getInstance().getResources().getAssets();
            InputStream inputStream = null;//模板文件数据流
            BufferedInputStream bufferedInputStream = null;//缓冲区
            int fileLength;//文件总长度
            try {
                inputStream = manager.open(fileName);
                //总长度
                fileLength = inputStream.available();
                LogUtil.d("模板文件总长度：" + fileLength);
                inputStream.skip(readLength - 1);

                byte[] tempbytes = new byte[128];
                int len;
                bufferedInputStream = new BufferedInputStream(inputStream);
                if ((len = bufferedInputStream.read(tempbytes)) != -1) {
                    sendCount++;
                    LogUtil.d(String.format("发送模板数据第%s次",sendCount));
                    readLength += len;
                    downCallback.onTemplateDownProgress((readLength * 100 / fileLength));
                    byte[] bytesHib = ByteUtil.int2BytesHib(readLength);
                    txBuffer[1] = bytesHib[0];
                    txBuffer[2] = bytesHib[1];
                    byte[] sendBytes = new byte[txBuffer.length + tempbytes.length];
                    System.arraycopy(txBuffer, 0, sendBytes, 0, txBuffer.length);
                    System.arraycopy(tempbytes, 0, sendBytes, txBuffer.length, tempbytes.length);

                    //    LogUtil.d("读取的数据 : " + Arrays.toString(tempbytes));
                    //    sendTemplateData();

                    bluetoothManager.setReadCode(BluetoothManager.CODE_TEMPLATE_DATA);
                    bluetoothManager.sendThread(sendBytes);
                } else {
                    //已经读取完了
                    LogUtil.d("已经读取完啦");
                    downCallback.onTemplateDownFinish(BluetoothManager.CODE_TEMPLATE_DATA);
                    bluetoothManager.setTemplateScheme(null);
                }

            } catch (IOException e) {
                e.printStackTrace();
                downCallback.onTemplateDownFail(BluetoothManager.CODE_TEMPLATE_DATA, "");
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (bufferedInputStream != null) bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取从模块回传过来的数据
     *
     * @param bytes 数据
     * @param code  当前需要读取哪个指令下的数据
     */
    public void read(byte[] bytes, int code) {
        if (bytes == null) {
            downCallback.onTemplateDownFail(code, "");
            return;
        }
        switch (code) {
            case BluetoothManager.CODE_START_DOWNLOAD://下发模板指令回传
                timeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //下发握手指令
                        sendHandShakeCmd();
                    }
                },150);

                break;
            case BluetoothManager.CODE_HANDSHAKE://握手回传
                if (bytes.length >= 5) {
                    argBuffer = bytes[4];
                    if (bytes[0] == 0x50) {
                        //握手成功,设置波特率
                        sendSetBaudCmd();
                    } else {
                        //握手失败
                        downCallback.onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "");
                    }
                } else {
                    //握手失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "");
                }
                break;
            case BluetoothManager.CODE_SET_BAUD://设置波特率回传
                if (bytes.length >= 1 && bytes[0] == 0x01) {
                    //设置波特率成功,准备下载
                    sendReadyDownloadCmd();
                } else {
                    //设置失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_SET_BAUD, "");
                }
                break;
            case BluetoothManager.CODE_READY_DOWNLOAD://准备下载回传
                if (bytes.length >= 1 && bytes[0] == 0x05) {
                    //准备下载回调成功。 擦除芯片指令
                    sendEraseCmd();
                } else {
                    //失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_READY_DOWNLOAD, "");
                }
                break;
            case BluetoothManager.CODE_ERASE://擦除回传
                if (bytes.length >= 1 && bytes[0] == 0x03) {
                    //擦除成功
                    sendTemplateData();
                } else {
                    //擦除失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_ERASE, "");
                }
                break;
            case BluetoothManager.CODE_TEMPLATE_DATA://下发模板数据回传
                if (bytes.length >= 2 && bytes[0] == 0x02 && "T".equals(new String(new byte[bytes[1]]))) {
                    //模板数据下发成功，下发下一段数据
                    sendTemplateData();
                } else {
                    //模板数据下发失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_TEMPLATE_DATA, "");
                }
                sendTemplateData();
                break;
        }
    }

    public interface OnTemplateDownCallback {
        void onTemplateDownStart();

        void onTemplateDownProgress(int progress);

        void onSendSuccess(int code);

        void onTemplateDownFinish(int code);

        void onTemplateDownFail(int code, String msg);
    }
}
