package com.yxx.app;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

/**
 * Author: yangxl
 * Date: 2021/7/22 16:59
 * Description:     蓝牙连接发送服务
 */
public class BluetoothService extends Service {

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

    private Handler mTimeHandler = new Handler();

    private boolean isConnect;
    private boolean isRead = true;

    private int readCode;//当前接收哪一步的数据

    private TemplateScheme mTemplateScheme;//下载协议

    private MyBinder mBinder = new MyBinder();

    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private MyHandler mHandler = new MyHandler();

    private BluetoothManager.OnBluetoothListener onBluetoothListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //注册蓝牙连接广播
        startReceiver();
    }

    public class MyBinder extends Binder {

        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void setOnBluetoothListener(BluetoothManager.OnBluetoothListener onBluetoothListener) {
        this.onBluetoothListener = onBluetoothListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d("service onBind");
        return mBinder;
    }

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

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
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

    //<editor-fold desc="连接蓝牙">
    public void connect(DeviceModel deviceModel) {
        BluetoothDevice device = deviceModel.mDevice;
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    if (mSocket != null) {
                        try {
                            mSocket.connect();
                            onBluetoothListener.onConnectSuccess();
                            mOutputStream = mSocket.getOutputStream();
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
                    } else {
                        onBluetoothListener.onConnectError();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //连接失败
                    onBluetoothListener.onConnectError();
                }
            }
        }.start();

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

    /**
     * 开启模板数据下载协议
     */
    public void setTemplateScheme(TemplateScheme templateScheme) {
        mTemplateScheme = templateScheme;
    }

    private boolean templateCallbackNotNull() {
        return mTemplateScheme != null && mTemplateScheme.getDownCallback() != null;
    }

    //<editor-fold desc="发送数据到模块">

    /**
     * 不开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendByte(byte[] buff, boolean addHead) {
        if (mSocket == null) {
            onBluetoothListener.onSendFaile(-1, "");
            return;
        }
        try {
            byte[] sendData = buff;
            if (addHead) {
                try {
                    byte[] dataCommByte = new byte[8 + sendData.length];
                    mOutputStream.write((byte) 0x46);
                    mOutputStream.write((byte) 0xb9);
                    mOutputStream.write((byte) 0x6a);
                    mOutputStream.write((byte) 0x00);
                    mOutputStream.flush();
                    int len = sendData.length + 6;//长度
                    dataCommByte[4] = ByteUtil.int2Byte(len);
                    mOutputStream.write(dataCommByte[4]);
                    int sum = len + new BigInteger("6a", 16).intValue();
                    for (byte sendDatum : sendData) {
                        sum += (sendDatum & 0xFF);
                        mOutputStream.write(sendDatum);
                    }
                    byte[] hibyte = ByteUtil.int2BytesHib(sum);
                    mOutputStream.write(hibyte[0]);
                    mOutputStream.write(hibyte[1]);
                    mOutputStream.write((byte) 0x16);
                    sendData = dataCommByte;
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.d("添加数据包失败");
                }
            } else {
                mOutputStream.write(sendData);
            }
            mOutputStream.flush();
            LogUtil.d("数据发送成功");
            //数据发送成功了
            onBluetoothListener.onSendSuccess(readCode);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d("数据发送失败");
            onBluetoothListener.onSendFaile(readCode, "");
        }

    }

    public void wirteTempData(byte[] buff) {
        mHandler.removeMessages(0);
        if (mSocket == null) {
            onBluetoothListener.onSendFaile(-1, "");
            return;
        }
        try {
            mOutputStream.write((byte) 0x46);
            mOutputStream.write((byte) 0xb9);
            mOutputStream.write((byte) 0x6a);
            mOutputStream.write((byte) 0x00);
            mOutputStream.flush();
            int len = buff.length + 6;//长度
            mOutputStream.write(ByteUtil.int2Byte(len));
            int sum = len + new BigInteger("6a", 16).intValue();
            for (byte sendDatum : buff) {
                sum += (sendDatum & 0xFF);
                mOutputStream.write(sendDatum);
                mOutputStream.flush();
            }
            byte[] hibyte = ByteUtil.int2BytesHib(sum);
            mOutputStream.write(hibyte[0]);
            mOutputStream.write(hibyte[1]);
            mOutputStream.write((byte) 0x16);
            mOutputStream.flush();
            LogUtil.d("数据发送成功");
            //数据发送成功了
            onBluetoothListener.onSendSuccess(readCode);
            mHandler.sendEmptyMessageDelayed(0, 50);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d("数据发送失败");
            onBluetoothListener.onSendFaile(readCode, "");
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                wirteTempData(new byte[]{});
            }
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
/*        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendByte(buff, addHead);
            }
        };
        mThreadService.execute(runnable);*/
        sendByte(buff, addHead);
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
        this.readCode = CODE_PRINT;
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
                        int len;
                        while ((len = mInputStream.read(rxbuffer)) != -1) {
                            byte[] dataBuffer = new byte[len];
                            if (dataBuffer.length > 0) {
                                System.arraycopy(rxbuffer, 0, dataBuffer, 0, dataBuffer.length);
                                LogUtil.d(String.format("收到数据，readCode : %s , Hex = %s", readCode, Hex.bytesToHex(dataBuffer)));
                                if (templateCallbackNotNull()) {
                                    mTemplateScheme.read(dataBuffer, readCode);
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }
    //</editor-fold>
}
