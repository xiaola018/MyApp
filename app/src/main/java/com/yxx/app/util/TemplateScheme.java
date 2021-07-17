package com.yxx.app.util;

import android.content.res.AssetManager;

import com.yxx.app.BluetoothManager;
import com.yxx.app.MyApplication;

import java.io.BufferedInputStream;
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

    private byte argBuffer;//回传的变量，用于接收握手成功后的参数值

    private InputStream inputStream;//模板文件数据流
    private int fileLength;//文件总长度
    private int readLength;//从文件读取了多少长度

    public static void start(String fileName, OnTemplateDownCallback downCallback) {
        TemplateScheme mTemplateScheme = new TemplateScheme(fileName, downCallback);
        BluetoothManager.get().templateDownload(mTemplateScheme);
        //下发开始下载模板
        mTemplateScheme.sendStartDownloadCmd();
    }

    private String fileName;
    private OnTemplateDownCallback downCallback;

    public TemplateScheme(String fileName, OnTemplateDownCallback downCallback) {
        this.fileName = fileName;
        this.downCallback = downCallback;
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
        byte[] bytes = new byte[]{2};
        BluetoothManager.get().setReadCode(BluetoothManager.CODE_START_DOWNLOAD);
        BluetoothManager.get().sendThread(bytes);
    }

    /**
     * 发送握手指令
     */
    public void sendHandShakeCmd() {
        byte[] bytes = new byte[0x7f];
        BluetoothManager.get().setReadCode(BluetoothManager.CODE_HANDSHAKE);
        BluetoothManager.get().sendThread(bytes);
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
        BluetoothManager.get().setReadCode(BluetoothManager.CODE_SET_BAUD);
        BluetoothManager.get().sendThread(txBuffer);
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
        BluetoothManager.get().setReadCode(BluetoothManager.CODE_READY_DOWNLOAD);
        BluetoothManager.get().sendThread(txBuffer);
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
        BluetoothManager.get().setReadCode(BluetoothManager.CODE_ERASE);
        BluetoothManager.get().sendThread(txBuffer);
    }

    public void sendTemplateData(String fileName, OnTemplateDownCallback callback) {
        callback.onTemplateDownStart();
        MyThread myThread = new MyThread(fileName, callback);
        myThread.start();
    }

    private class MyThread extends Thread {

        private String fileName;
        private OnTemplateDownCallback callback;

        public MyThread(String fileName, OnTemplateDownCallback callback) {
            this.fileName = fileName;
            this.callback = callback;
        }

        @Override
        public void run() {
            byte[] txBuffer = new byte[5];
            txBuffer[0] = 0x22;
            txBuffer[3] = 0x5a;
            txBuffer[4] = (byte) 0xa5;
            AssetManager manager = MyApplication.getInstance().getResources().getAssets();
            try {
                if (inputStream == null) {
                    LogUtil.d("开始读取文件");
                    inputStream = manager.open(fileName);
                    //总长度
                    fileLength = inputStream.available();
                }

                byte[] tempbytes = new byte[128];
                int len;
                int progress = 0;
                BufferedInputStream in = new BufferedInputStream(inputStream);
                while ((len = in.read(tempbytes)) != -1) {
/*                    progress += len;
                    callback.onTemplateDownProgress((progress * 100 / length));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
            } catch (IOException e) {
                e.printStackTrace();
                callback.onTemplateDownFail(0, "");
            } finally {
                callback.onTemplateDownFinish(1);
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
                //下发握手指令
                sendHandShakeCmd();
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
                    //设置波特率成功,擦除芯片
                } else {
                    //设置失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_SET_BAUD, "");
                }
                break;
            case BluetoothManager.CODE_READY_DOWNLOAD://准备下载回传
                if (bytes.length >= 1 && bytes[0] == 0x05) {
                    //成功
                } else {
                    //失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_READY_DOWNLOAD, "");
                }
                break;
            case BluetoothManager.CODE_ERASE://擦除回传
                if (bytes.length >= 1 && bytes[0] == 0x03) {
                    //擦除成功
                } else {
                    //擦除失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_ERASE, "");
                }
                break;
            case BluetoothManager.CODE_TEMPLATE_DATA://下发模板数据回传
                if(bytes.length >= 2 && bytes[0] == 0x02 && "T".equals(new String(new byte[bytes[1]]))){
                    //模板数据下发成功，下发下一段数据

                }else{
                    //模板数据下发失败
                }
                break;
        }
    }

    public interface OnTemplateDownCallback {
        void onTemplateDownStart();

        void onTemplateDownProgress(int progress);

        void onTemplateDownFinish(int code);

        void onTemplateDownFail(int code, String msg);
    }
}
