package com.yxx.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.yxx.app.bean.DeviceModel;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.util.ByteUtil;
import com.yxx.app.util.Hex;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.TemplateScheme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: yangxl
 * Date: 2021/7/3 8:57
 * Description:
 */
public class BluetoothManager {

    private static BluetoothManager mBluetoothManager;

    private BluetoothService mService;

    public static BluetoothManager get() {
        if (mBluetoothManager == null) mBluetoothManager = new BluetoothManager();
        return mBluetoothManager;
    }

    public BluetoothManager() {
    }

    public void bindService(Context context, OnBluetoothListener listener){
        Intent intent = new Intent(context, BluetoothService.class);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BluetoothService.MyBinder binder = (BluetoothService.MyBinder) iBinder;
                mService = binder.getService();
                mService.setOnBluetoothListener(listener);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener) {
        mService.setOnBluetoothListener(onBluetoothListener);
    }

    //<editor-fold desc="蓝牙状态">
    public void openBluetooth() {
        mService.openBluetooth();
    }

    public boolean isSupport() {
        return mService.isSupport();
    }

    public boolean isOpen() {
        return mService.isOpen();
    }

    public boolean isConnect() {
        return mService.isConnect();
    }

    public void setReadCode(int readCode) {
        mService.setReadCode(readCode);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mService.getBluetoothAdapter();
    }

    public void startScanBluetooth() {
        mService.startScanBluetooth();
    }

    public void cancelDiscovery() {
        mService.cancelDiscovery();
    }

    /**
     * 蓝牙配对
     */
    public void makePair(String address) {
    }
    //</editor-fold>


    //<editor-fold desc="连接蓝牙">
    public void connect(DeviceModel deviceModel) {
        mService.connect(deviceModel);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mService.disconnect();

    }
    //</editor-fold>

    //<editor-fold desc="发送数据到模块">

    /**
     * 不开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendByte(byte[] buff, boolean addHead) {
        mService.sendByte(buff,addHead);
    }

    /**
     * 开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendThread(byte[] buff) {
        sendThread(buff, false);
    }

    /**
     * 开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendThread(byte[] buff, boolean addHead) {
        mService.sendThread(buff,addHead);
    }


    /**
     * 发送单条打印数据
     *
     * @param sendInfo
     */
    public void sendData(SendInfo sendInfo) {
        List<SendInfo> list = new ArrayList<>();
        list.add(sendInfo);
        sendData(list);
    }

    /**
     * 发送数据， 外部调用
     *
     * @param infoList 操作页面下封装好的数据
     */
    public void sendData(List<SendInfo> infoList) {
        mService.sendData(infoList);
    }
    //</editor-fold>

    /**
     * 开启模板数据下载协议
     */
    public void setTemplateScheme(TemplateScheme templateScheme) {
        mService.setTemplateScheme(templateScheme);
    }

    //<editor-fold desc="监听回调">
    public interface OnBluetoothListener {
        void open();

        void closed();

        void discoveryStarted();

        void discoveryFinished();

        void onDeviceAdd(DeviceModel deviceModel);

        void whilePari(BluetoothDevice device);//正在配对

        void pairingSuccess(BluetoothDevice device);//配对结束

        void cancelPari(BluetoothDevice device);//取消配对，未配对

        void onConnectSuccess();

        void onConnectError();

        void onStateDisconnected();

        void onSendSuccess(int code);

        void onSendFaile(int code, String msg);
    }
    //</editor-fold>
}
