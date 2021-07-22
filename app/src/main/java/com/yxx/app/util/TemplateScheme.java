package com.yxx.app.util;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.yxx.app.BluetoothManager;
import com.yxx.app.MyApplication;

import org.w3c.dom.ls.LSOutput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
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

    private int readLength = 0;//从文件读取了多少长度

    private BluetoothManager bluetoothManager;

    private String fileName;
    private OnTemplateDownCallback downCallback;
    private int sendCount;

    private int sendHandShakeCmdMaxCount = 30;//发送握手指令最大次数
    public int sendHandShakeCmdCurrentCount = 1;//当前发送第几次握手指令

    private boolean isHandShake;

    public MyHander mHander;

    public TemplateScheme(String fileName, OnTemplateDownCallback downCallback) {
        this.fileName = fileName;
        this.downCallback = downCallback;
        bluetoothManager = BluetoothManager.get();
        bluetoothManager.setTemplateScheme(this);
        mHander = new MyHander(this);
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
        LogUtil.d("下发开始下载指令");
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
        if(!isHandShake){
            byte[] bytes = new byte[]{0x7f};
            bluetoothManager.setReadCode(BluetoothManager.CODE_HANDSHAKE);
            bluetoothManager.sendByte(bytes, false);
        }

    }

    public void sendHandShakeHandler() {
        mHander.sendEmptyMessage(0);
    }

    /**
     * 发送设置参数指令
     */
    public void sendSetBaudCmd() {
        LogUtil.d("下发设置参数指令");
        byte[] baudBytes = ByteUtil.int2BytesHib(BAUD);
        byte[] txBuffer = new byte[8];
        txBuffer[0] = 0x01;
        txBuffer[1] = argBuffer;
        txBuffer[2] = 0x40;
        txBuffer[3] = (byte)0xff;
        txBuffer[4] = (byte)0xcc;
        txBuffer[5] = 0x00;
        txBuffer[6] = 0x00;
        txBuffer[7] = (byte) 0x97;
        bluetoothManager.setReadCode(BluetoothManager.CODE_SET_BAUD);
        bluetoothManager.sendThread(txBuffer, true);
    }

    /**
     * 发送准备下载指令
     */
    public void sendReadyDownloadCmd() {
        LogUtil.d("下发准备下载指令");
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x05;
        txBuffer[1] = 0x00;
        txBuffer[2] = 0x00;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        bluetoothManager.setReadCode(BluetoothManager.CODE_READY_DOWNLOAD);
        bluetoothManager.sendThread(txBuffer, true);
    }

    /**
     * 发送擦除芯片指令
     */
    public void sendEraseCmd() {
        LogUtil.d("下发擦除芯片指令");
        byte[] txBuffer = new byte[5];
        txBuffer[0] = 0x03;
        txBuffer[1] = 0x00;
        txBuffer[2] = 0x00;
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;
        bluetoothManager.setReadCode(BluetoothManager.CODE_ERASE);
        bluetoothManager.sendThread(txBuffer, true);
    }

    public void sendTemplateData() {
        bluetoothManager.startTime = System.currentTimeMillis();
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
                inputStream.skip(readLength);

                byte[] tempbytes = new byte[128];
                int len;
                bufferedInputStream = new BufferedInputStream(inputStream);
                if ((len = bufferedInputStream.read(tempbytes)) != -1) {
                    sendCount++;
                    LogUtil.d(String.format("发送模板数据第%s次", sendCount + ",耗时：" + (System.currentTimeMillis() - bluetoothManager.startTime)));

                    downCallback.onTemplateDownProgress((readLength * 100 / fileLength));
                    byte[] bytesHib = ByteUtil.int2BytesHib(readLength);
                    txBuffer[1] = bytesHib[0];
                    txBuffer[2] = bytesHib[1];
                    readLength += len;
                    byte[] sendBytes = new byte[txBuffer.length + tempbytes.length];
                    System.arraycopy(txBuffer, 0, sendBytes, 0, txBuffer.length);
                    System.arraycopy(tempbytes, 0, sendBytes, txBuffer.length, tempbytes.length);

                    bluetoothManager.setReadCode(BluetoothManager.CODE_TEMPLATE_DATA);
                    bluetoothManager.sendByte(sendBytes, true);
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
                byte[] callbytes = new byte[]{(byte) 0xa0, (byte) 0x02, (byte) 0x01};
                if (bytes.length >= 6 && callbytes[0] == bytes[0] && callbytes[0] == bytes[1]
                        && callbytes[1] == bytes[2] && callbytes[2] == bytes[5]) {
                    //收到开始下载回传指令
                    mHander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    //下发握手指令
                                    mHander.sendEmptyMessage(0);
                                }
                            }.start();
                        }
                    }, 100);
                }

                break;
            case BluetoothManager.CODE_HANDSHAKE://握手回传
                mHander.removeMessages(0);
                isHandShake = true;
                if (bytes.length >= 5) {
                    try {
                        byte[] commbytes = checkDataComm(bytes);
                        if(commbytes.length >= 5 && commbytes[0] == 0x50){
                            LogUtil.d("握手成功");
                            argBuffer = commbytes[4];
                            sendSetBaudCmd();
                        }else{
                            downCallback.onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "");
                            LogUtil.d("握手失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //握手失败
                        downCallback.onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "");
                    }
                } else {
                    //握手失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "");
                }
                break;
            case BluetoothManager.CODE_SET_BAUD://设置波特率回传
                byte[] rxbuffer = checkDataComm(bytes);
                if (rxbuffer.length >= 1 && rxbuffer[0] == 0x01) {
                    //设置波特率成功,准备下载
                    sendReadyDownloadCmd();
                } else {
                    //设置失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_SET_BAUD, "");
                }
                break;
            case BluetoothManager.CODE_READY_DOWNLOAD://准备下载回传
                byte[] readyCommByte = checkDataComm(bytes);
                if (readyCommByte.length >= 1 && readyCommByte[0] == 0x05) {
                    //准备下载回调成功。 擦除芯片指令
                    sendEraseCmd();
                } else {
                    //失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_READY_DOWNLOAD, "");
                }
                break;
            case BluetoothManager.CODE_ERASE://擦除回传
                byte[] eraseCommBytes = checkDataComm(bytes);
                if (eraseCommBytes.length >= 1 && eraseCommBytes[0] == 0x03) {
                    //擦除成功
                    sendTemplateData();
                } else {
                    //擦除失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_ERASE, "");
                }
                break;
            case BluetoothManager.CODE_TEMPLATE_DATA://下发模板数据回传
                byte[] tempDataCommBytes = checkDataComm(bytes);
                if (tempDataCommBytes.length >= 2 && tempDataCommBytes[0] == 0x02 && "T".equals(Hex.hexStr2Str(Hex.bytesToHex(new byte[]{tempDataCommBytes[1]})))) {
                    //模板数据下发成功，下发下一段数据
                    sendTemplateData();
                } else {
                    //模板数据下发失败
                    downCallback.onTemplateDownFail(BluetoothManager.CODE_TEMPLATE_DATA, "");
                }
                break;
        }
    }

    /**
     *  校验数据包
     *  -- 校验包头
     *  -- 检查验证码
     * @param readBytes
     * @return
     */
    private byte[] checkDataComm(byte[] readBytes) {
        try{
            for (int i = 0; i < readBytes.length; i++) {
                if (readBytes[i] == (byte) 0xb9 && readBytes[i + 1] == (byte) 0x68 && readBytes[i + 2] == (byte) 0x00) {
                    int len = new BigInteger(Hex.bytesToHex(new byte[]{readBytes[i + 3]}), 16).intValue() - 6;//包长度
                //    LogUtil.d("接收到数据包长度 ： " + len);
                    int offSet = i + 4;//开始偏移的起始位置
                    byte verifyH = readBytes[readBytes.length - 3];
                    byte verifyL = readBytes[readBytes.length - 2];
                    int recvSum = len + 6 + new BigInteger("68",16).intValue();
                    byte[] rxbuffer = new byte[readBytes.length - offSet - 3];
                    System.arraycopy(readBytes, offSet, rxbuffer, 0, rxbuffer.length);
                    for (int j = 0; j < rxbuffer.length; j++) {
                        String hex = Hex.bytesToHex(new byte[]{rxbuffer[j]});
                        BigInteger bigInteger = new BigInteger(hex,16);
                        recvSum += bigInteger.intValue();
                    }
                //    LogUtil.d("校验码数值 ：" + recvSum);
                    byte[] hibyte = ByteUtil.int2BytesHib(recvSum);
                //    LogUtil.d("校验码Hex ：" +Hex.bytesToHex(hibyte));
                    if(hibyte[0] == verifyH && hibyte[1] == verifyL){
                        return rxbuffer;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static class MyHander extends Handler {
        private final WeakReference<TemplateScheme> weakReference;

        public MyHander(TemplateScheme templateScheme) {
            weakReference = new WeakReference<>(templateScheme);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            TemplateScheme templateScheme = weakReference.get();
            if (templateScheme == null) return;
            switch (msg.what) {
                case 0:
                    if (templateScheme.sendHandShakeCmdCurrentCount <= templateScheme.sendHandShakeCmdMaxCount) {
                        //    LogUtil.d(String.format("下发第%s次握手指令", templateScheme.sendHandShakeCmdCurrentCount));
                        //下发握手指令
                        templateScheme.sendHandShakeCmd();
                        templateScheme.sendHandShakeCmdCurrentCount++;
                        sendEmptyMessageDelayed(0, 10);
                    } else {
                        templateScheme.getDownCallback().onTemplateDownFail(BluetoothManager.CODE_HANDSHAKE, "连接超时");
                    }

                    break;
            }
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