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
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    private static BluetoothManager mBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private int readCode;//当前接收哪一步的数据
    private TemplateScheme mTemplateScheme;//下载协议
    private OnBluetoothListener onBluetoothListener;

    private ExecutorService mThreadService;
    public boolean isRead = true;

    public long startTime;

    public static BluetoothManager get() {
        if (mBluetoothManager == null) mBluetoothManager = new BluetoothManager();
        return mBluetoothManager;
    }

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mThreadService = Executors.newFixedThreadPool(1);
        startReceiver();
    }

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener) {
        this.onBluetoothListener = onBluetoothListener;
    }

    //<editor-fold desc="蓝牙状态">
    public void openBluetooth() {
        if (mBluetoothAdapter != null) {
            // 蓝牙已打开
            if (isOpen()) {
                LogUtil.d("蓝牙已打开");
                //    startScanBluetooth();
            } else {
                LogUtil.d("提示用户打开蓝牙");
                mBluetoothAdapter.enable();
            }
        } else {
            Toast.makeText(MyApplication.getInstance(), "此设备不支持蓝牙", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isSupport() {
        return mBluetoothAdapter != null;
    }

    public boolean isOpen() {
        return isSupport() && mBluetoothAdapter.isEnabled();
    }

    public boolean isConnect() {
        return mSocket != null && mSocket.isConnected();
    }

    public void setReadCode(int readCode) {
        this.readCode = readCode;
    }

    public void startScanBluetooth() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            cancelDiscovery();
        }
        // 开始搜索
        mBluetoothAdapter.startDiscovery();
        LogUtil.d("正在搜索设备。。");
    }

    public void cancelDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * 蓝牙配对
     */
    public void makePair(String address) {
    }
    //</editor-fold>

    //<editor-fold desc="状态广播">
    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        MyApplication.getInstance().registerReceiver(receiver, filter);
    }


    /**
     * 蓝牙状态广播
     */
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case 10:
                        LogUtil.d("蓝牙关闭状态");
                        onBluetoothListener.closed();
                        disconnect();
                        break;
                    case 11:
                        LogUtil.d("蓝牙正在打开");
                        break;
                    case 12:
                        LogUtil.d("蓝牙打开");
                        onBluetoothListener.open();
                        break;
                    case 13:
                        LogUtil.d("蓝牙正在关闭");
                        break;
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //蓝牙rssi参数，代表蓝牙强度
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                //蓝牙设备名称
                String name = device.getName();
                //蓝牙设备连接状态
                int status = device.getBondState();

                DeviceModel deviceModel = new DeviceModel();
                deviceModel.deviceName = name;
                deviceModel.address = device.getAddress();
                deviceModel.mDevice = device;
                onBluetoothListener.onDeviceAdd(deviceModel);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtil.d("开始搜索");
                onBluetoothListener.discoveryStarted();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtil.d("蓝牙设备搜索完成");
                onBluetoothListener.discoveryFinished();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:// 正在配对
                        //    LogUtil.d("正在配对蓝牙");
                        //    mMakePariListener.whilePari(device);
                        break;
                    case BluetoothDevice.BOND_BONDED:// 配对结束
                        //    LogUtil.d("配对成功");
                        //    mMakePariListener.pairingSuccess(device);
                        break;
                    case BluetoothDevice.BOND_NONE:// 取消配对/未配对
                        //    mMakePariListener.cancelPari(device);
                        //   LogUtil.d("配对失败");
                    default:
                        break;
                }
            }
        }
    };
    //</editor-fold>

    //<editor-fold desc="连接蓝牙">
    public void connect(DeviceModel deviceModel) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BluetoothDevice device = deviceModel.mDevice;
                try {
                    mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    if (mSocket != null) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    mSocket.connect();
                                    onBluetoothListener.onConnectSuccess();
                                    readThread();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    try {
                                        mSocket.close();
                                        mSocket = null;
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                    onBluetoothListener.onConnectError();
                                }
                            }
                        }.start();
                    } else {
                        onBluetoothListener.onConnectError();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //连接失败
                    onBluetoothListener.onConnectError();
                }
            }
        };
        mThreadService.execute(runnable);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        isRead = false;
        try {
            if (mSocket != null) mSocket.close();
            if (mOutputStream != null) mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //</editor-fold>

    //<editor-fold desc="发送数据到模块">

    /**
     * 不开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendByte(byte[] buff, boolean addHead) {
        byte[] sendData = buff;
        if (addHead) {
            try {
                byte[] dataCommByte = new byte[8 + sendData.length];
                dataCommByte[0] = 0x46;
                dataCommByte[1] = (byte) 0xb9;
                dataCommByte[2] = (byte) 0x6a;
                dataCommByte[3] = 0x00;
                int len = sendData.length + 6;//长度
                dataCommByte[4] = ByteUtil.int2Byte(len);
                int sum = len + new BigInteger("6a", 16).intValue();
                for (int i = 0; i < sendData.length; i++) {
                    sum += new BigInteger(Hex.bytesToHex(new byte[]{sendData[i]}), 16).intValue();
                    dataCommByte[i + 5] = sendData[i];
                }
                byte[] hibyte = ByteUtil.int2BytesHib(sum);
                dataCommByte[dataCommByte.length - 3] = hibyte[0];
                dataCommByte[dataCommByte.length - 2] = hibyte[1];
                dataCommByte[dataCommByte.length - 1] = 0x16;//结尾


                sendData = dataCommByte;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("添加数据包失败");
            }
        }
        LogUtil.d("准备发送数据，总长度: " + sendData.length);
        try {
            if (mOutputStream == null) {
                mOutputStream = mSocket.getOutputStream();
            }
            mOutputStream.write(sendData);
            LogUtil.d("数据发送成功");
            //数据发送成功了
            onBluetoothListener.onSendSuccess(readCode);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d("数据发送失败");
            onBluetoothListener.onSendFaile(readCode, "");
        }

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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendByte(buff, addHead);
            }
        };
        mThreadService.execute(runnable);
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
        if (!isConnect()) {
            Toast.makeText(MyApplication.getInstance(), "蓝牙未连接", Toast.LENGTH_LONG).show();
            return;
        }
        this.readCode = BluetoothManager.CODE_PRINT;
        //获取梳理好的字节数据，准备组装发送
        byte[] sendByteArray = ByteUtil.combData(infoList);
        sendThread(sendByteArray);
    }
    //</editor-fold>

    //<editor-fold desc="从模板读取数据">
    public void readThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mInputStream = mSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] rxbuffer = new byte[128];
                while (isRead) {
                    try {
                        do {
                            int len = mInputStream.read(rxbuffer);
                            if (len != -1) {
                                long s = System.currentTimeMillis();
                                byte[] dataBuffer = new byte[len];
                                if (dataBuffer.length >= 0)
                                    System.arraycopy(rxbuffer, 0, dataBuffer, 0, dataBuffer.length);
                                LogUtil.d(String.format("收到数据，readCode : %s , Hex = %s", readCode, Hex.bytesToHex(dataBuffer)));
                                if (templateCallbackNotNull()) {
                                    mTemplateScheme.read(dataBuffer, readCode);
                                    break;
                                }
                            }
                        } while (mInputStream.available() != 0);
                    } catch (Exception e) {

                    }
                }
            }
        }.start();
    }
    //</editor-fold>

    /**
     * 开启模板数据下载协议
     */
    public void setTemplateScheme(TemplateScheme templateScheme) {
        mTemplateScheme = templateScheme;
    }

    private boolean templateCallbackNotNull() {
        return mTemplateScheme != null && mTemplateScheme.getDownCallback() != null;
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
