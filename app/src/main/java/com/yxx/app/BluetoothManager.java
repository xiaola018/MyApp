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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
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

    //下发擦除
    public static final int CODE_ERASE = 1005;

    //下发模板数据
    public static final int CODE_TEMPLATE_DATA = 1006;

    //下发准备下载
    public static final int CODE_READY_DOWNLOAD = 1007;

    //蓝牙的特征值，发送
    private final static String SERVICE_EIGENVALUE_SEND = "0000ffe1-0000-1000-8000-00805f9b34fb";
    //蓝牙的特征值，接收
    private final static String SERVICE_EIGENVALUE_READ = "00002902-0000-1000-8000-00805f9b34fb";

    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private OnBluetoothListener onBluetoothListener;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNeedCharacteristic;

    private DeviceModel deviceModel;

    private Handler mTimeHandler = new Handler();

    private boolean sendDataSign = true;//发送的标志位

    private ExecutorService mThreadService;

    private boolean isConnect;

    private int separateLength;//分包的长度
    private int sendCount;//数据需要发送几次
    private int currentSendCount;//当前发送第几次数据

    private int readCode;//当前接收哪一步的数据

    private TemplateScheme mTemplateScheme;//下载协议


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
        return isOpen() && isConnect;
    }

    public void setReadCode(int readCode) {
        this.readCode = readCode;
    }

    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        MyApplication.getInstance().registerReceiver(receiver, filter);
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
        LogUtil.d("取消蓝牙搜索");
    }

    /**
     * 蓝牙配对
     */
    public void makePair(String address) {
        LogUtil.d("开始配对......");
        if (isOpen()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            device.createBond();
        } else {
            Toast.makeText(MyApplication.getInstance(), "蓝牙未打开", Toast.LENGTH_LONG).show();
        }
    }

    //<editor-fold desc="状态广播">
    /**
     * 蓝牙状态广播
     */
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                LogUtil.d("state = " + state);
                switch (state) {
                    case 10:
                        LogUtil.d("蓝牙关闭状态");
                        onBluetoothListener.closed();
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

                LogUtil.d("device name: " + device.getName() + " address: " + device.getAddress());

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
                        LogUtil.d("正在配对蓝牙");
                        //    mMakePariListener.whilePari(device);
                        break;
                    case BluetoothDevice.BOND_BONDED:// 配对结束
                        LogUtil.d("配对成功");
                        //    mMakePariListener.pairingSuccess(device);
                        break;
                    case BluetoothDevice.BOND_NONE:// 取消配对/未配对
                        //    mMakePariListener.cancelPari(device);
                        LogUtil.d("配对失败");
                    default:
                        break;
                }
            }
        }
    };
    //</editor-fold>

    //<editor-fold desc="蓝牙连接">
    public void connectGatt(Context context, DeviceModel deviceModel) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectGattThread(context, deviceModel);
            }
        };
        mThreadService.execute(runnable);
    }

    private void connectGattThread(Context context, DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
        mBluetoothGatt = deviceModel.mDevice.connectGatt(context, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                LogUtil.d(" connect == " + newState);
                if (newState == 133) {
                    LogUtil.d("出现133问题，需要扫描重连");
                    isConnect = false;
                    onBluetoothListener.onConnectError();
                }
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        LogUtil.d("connect = STATE_CONNECTED");
                        if (mBluetoothGatt == null) {
                            isConnect = false;
                            onBluetoothListener.onConnectError();
                        }
                        requestMtu();
                        //发现蓝牙服务
                        mTimeHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothGatt.discoverServices();//扫描服务
                            }
                        }, 1000);//坑：设置延迟时间过短，很可能发现不了服务
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        LogUtil.d("蓝牙断开连接");
                        isConnect = false;
                        onBluetoothListener.onStateDisconnected();
                        break;
                }
                super.onConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //所有app发送给模块数据成功的回调都在这里
                    LogUtil.d("发送模块数据成功, readCode : " + readCode);
                    //    sendHandler(BleBluetoothManage.SERVICE_SEND_DATA_NUMBER, String.valueOf(characteristic.getValue().length));
                    sendDataSign = true;//等到发送数据回调成功才可以继续发送
                    if (sendCount == currentSendCount) {
                        LogUtil.d("数据已全部发送完毕");
                        //数据发送成功了
                        onBluetoothListener.onSendSuccess(readCode);
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //mBluetoothGatt.writeDescriptor(descriptor);
                    //来到这里，才算真正的建立连接
                    LogUtil.d("设置监听成功,可以发送数据了...");
                    isConnect = true;
                    onBluetoothListener.onConnectSuccess();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                List<BluetoothGattService> servicesLists = mBluetoothGatt.getServices();//获取模块的所有服务
                LogUtil.d("扫描到服务的个数:" + servicesLists.size());
                int i = 0;

                for (final BluetoothGattService servicesList : servicesLists) {
                    ++i;
                    LogUtil.d("-----------打印服务----------");
                    LogUtil.d(i + "号服务的uuid: " + servicesList.getUuid().toString());
                    List<BluetoothGattCharacteristic> gattCharacteristics = servicesList
                            .getCharacteristics();//获取单个服务下的所有特征
                    int j = 0;
                    LogUtil.d("----------打印特征-----------");
                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        ++j;
                        if (gattCharacteristic.getUuid().toString().equals(SERVICE_EIGENVALUE_SEND)) {//汇承蓝牙的UUID
                            LogUtil.d(i + "号服务的第" + j + "个特征" + gattCharacteristic.getUuid().toString());
                            //    mDeiceModule.setUUID(servicesList.getUuid().toString(),null,gattCharacteristic.getUuid().toString());//存下特征
                            mNeedCharacteristic = gattCharacteristic;
                            LogUtil.d("发送特征：" + mNeedCharacteristic.getUuid().toString());
                            mBluetoothGatt.setCharacteristicNotification(
                                    mNeedCharacteristic, true);
                            mTimeHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothGattService linkLossService = gatt.getService(servicesList.getUuid());
                                    BluetoothGattDescriptor clientConfig = mNeedCharacteristic
                                            .getDescriptor(UUID.fromString(SERVICE_EIGENVALUE_READ));//这个收取数据的UUID
                                    if (clientConfig != null) {
                                        //BluetoothGatt.getService(service)
                                        clientConfig
                                                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        mBluetoothGatt.writeDescriptor(clientConfig);//必须是设置这个才能监听模块数据
                                    } else {
                                        LogUtil.d("备用方法测试");
                                        //    setNotification(mBluetoothGatt,linkLossService.getCharacteristic(UUID.fromString(SERVICE_EIGENVALUE_READ)),true);
                                    }
                                }
                            }, 200);
                        } else {
                            LogUtil.d(i + "号服务的第" + j + "个特征" + gattCharacteristic.getUuid().toString());
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                //蓝牙发送给app的回调

                try {
                    //    LogUtil.d("数据是：" + new String(characteristic.getValue(), 0, characteristic.getValue().length, "GB2312"));
                    LogUtil.d("接收到数据回调 readCode = " + readCode);
                    LogUtil.d("接收到数据回调 str = " + Arrays.toString(characteristic.getValue()));
                    LogUtil.d("接收到数据回调 Hex = " + Hex.bytesToHex(characteristic.getValue()));
                    if (mTemplateScheme != null) {
                        mTemplateScheme.read(characteristic.getValue(), readCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                LogUtil.d("onMtuChanged :  " + mtu);
                if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                    separateLength = mtu;
                }
            }
        });
    }

    private void requestMtu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.requestMtu(512);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="发送数据到模块">

    /**
     * 不开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendByte(byte[] buff) {
        LogUtil.d("进入发送方法, 数据总长度 == " + buff.length);
        //根据长度获取分包后的数据
        List<byte[]> sendDataArray = ByteUtil.getSendDataByte(buff, separateLength);
        int number = 0;
        sendCount = 0;
        currentSendCount = 0;
        sendCount = sendDataArray.size();
        for (byte[] sendData : sendDataArray) {
            currentSendCount++;
            try {
            //    Thread.sleep(5 + 10 * ModuleParameters.getState());//每次发包前，延时一会，更容易成功
                Thread.sleep(10);
                mNeedCharacteristic.setValue(sendData);
                sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);//蓝牙发送数据
                if (sendDataSign) {
                    Thread.sleep(1000 + 500 * ModuleParameters.getState());
                    LogUtil.d("发送失败....");
                    onBluetoothListener.onSendFaile(readCode, "");
                    if (templateCallbackIsNull())
                        mTemplateScheme.getDownCallback().onTemplateDownFail(readCode, "");
                    sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);
                    if (sendDataSign) {
                        LogUtil.d("无法发送数据");
                        return;
                    }
                }
                while (!sendDataSign) {
                    Thread.sleep(10 + 10 * ModuleParameters.getState());
                    number++;
                    if (number == 40) {
                        mNeedCharacteristic.setValue(new byte[0]);//额外发送会导致发包重复，所以发一个空包去提醒
                        sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);
                        LogUtil.d("额外发送一次," + sendDataSign);
                    }
                    if (number == 80) {
                        mNeedCharacteristic.setValue(new byte[0]);//额外发送会导致发包重复，所以发一个空包去提醒
                        sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);
                        LogUtil.d("再次额外发送一次," + sendDataSign);
                    }

                    if (number == 180) {
                        mNeedCharacteristic.setValue(new byte[0]);//额外发送会导致发包重复，所以发一个空包去提醒
                        sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);
                        LogUtil.d("第三次额外发送一次," + sendDataSign);
                    }

                    if (number == 300) {
                        sendDataSign = true;
                        onBluetoothListener.onSendFaile(readCode, "");
                        if (templateCallbackIsNull())
                            mTemplateScheme.getDownCallback().onTemplateDownFail(readCode, "");
                        LogUtil.d("发送失败,关闭线程");
                        return;
                    }
                }
                number = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启新线程发送
     *
     * @param buff 发送到模板的数据
     */
    public void sendThread(byte[] buff) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendByte(buff);
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
        if (!isConnect || !isOpen()) {
            Toast.makeText(MyApplication.getInstance(), "蓝牙未连接", Toast.LENGTH_LONG).show();
            return;
        }
        this.readCode = CODE_PRINT;
        //获取梳理好的字节数据，准备组装发送
        byte[] sendByteArray = ByteUtil.combData(infoList);
        sendThread(sendByteArray);
    }
    //</editor-fold>

    /**
     * 开启模板数据下载协议
     */
    public void setTemplateScheme(TemplateScheme templateScheme) {
        mTemplateScheme = templateScheme;
    }

    private boolean templateCallbackIsNull() {
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
