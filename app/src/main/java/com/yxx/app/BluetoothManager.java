package com.yxx.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.common.io.ByteSource;
import com.google.common.primitives.Bytes;
import com.yxx.app.bean.DeviceModel;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.util.BitUtil;
import com.yxx.app.util.ByteUtil;
import com.yxx.app.util.Hex;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.ModuleParameters;
import com.yxx.app.util.TemplateScheme;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    //下发票据打印
    public static final int CODE_PRINT = 1001;

    //下发开始下载模板
    public static final int CODE_START_DOWNLOAD = 1002;

    //下发握手
    public static final int CODE_HANDSHAKE = 1003;

    //下发设置波特率
    public static final int CODE_SET_BAUD = 1004;

    //下发准备下载
    public static final int CODE_READY_DOWNLOAD = 1005;

    //下发擦除
    public static final int CODE_ERASE = 1006;

    //下发模板数据
    public static final int CODE_TEMPLATE_DATA = 1007;

    private static BluetoothManager mBluetoothManager;

    private BluetoothService mService;
    private OnBluetoothListener bluetoothListener;

    public long startTime;

    public static BluetoothManager get() {
        if (mBluetoothManager == null) mBluetoothManager = new BluetoothManager();
        return mBluetoothManager;
    }

    public void bindService(Context context, OnBluetoothListener bluetoothListener){
        LogUtil.d("开始绑定服务");
        this.bluetoothListener = bluetoothListener;
        context.bindService(new Intent(context, BluetoothService.class),
                conn, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtil.d("服务绑定成功");
            BluetoothService.MyBinder binder = (BluetoothService.MyBinder) iBinder;
            mService = binder.getService();
            setOnBluetoothListener(bluetoothListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public BluetoothManager() {
    }

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener) {
        mService.setOnBluetoothListener(onBluetoothListener);
    }

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
        mService.makePair(address);
    }

    public void connectGatt(Context context, DeviceModel deviceModel) {
        mService.connectGatt(context, deviceModel);
    }

    public void disconnect(){
        mService.disconnect();
        MyApplication.getInstance().unbindService(conn);
    }

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
        mService.sendThread(buff, addHead);
    }


    /**
     * 发送单条打印数据
     *
     * @param sendInfo
     */
    public void sendData(SendInfo sendInfo) {
        mService.sendData(sendInfo);
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
