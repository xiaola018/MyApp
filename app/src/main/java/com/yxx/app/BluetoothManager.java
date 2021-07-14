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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    /**
     * 蓝牙状态广播
     */
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d("有回调=== action -=== " + action);
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
                LogUtil.d("onCharacteristicWrite");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //所有app发送给模块数据成功的回调都在这里
                    LogUtil.d("所有app发送给模块数据成功的回调都在这里");
                    //    sendHandler(BleBluetoothManage.SERVICE_SEND_DATA_NUMBER, String.valueOf(characteristic.getValue().length));
                    sendDataSign = true;//等到发送数据回调成功才可以继续发送
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                LogUtil.d("onDescriptorWrite");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //mBluetoothGatt.writeDescriptor(descriptor);
                    //来到这里，才算真正的建立连接
                    LogUtil.d("设置监听成功,可以发送数据了...");
                    isConnect = true;
                    onBluetoothListener.onConnectSuccess();
/*                    mDeiceModule.setUUID(null,descriptor.getUuid().toString(),null);
                    log("服务中连接成功，给与的返回名称是->"+gatt.getDevice().getName());
                    log("服务中连接成功，给与的返回地址是->"+gatt.getDevice().getAddress());
                    sendHandler(BleBluetoothManage.SERVICE_CONNECT_SUCCEED,null);*/
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
                //模块发送的所有数据都会回调到这里
                //蓝牙发送给app的回调
                LogUtil.d("接收到数据回调");
                try {
                    LogUtil.d("数据是：" + new String(characteristic.getValue(), 0, characteristic.getValue().length, "GB2312"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //    sendHandler( characteristic.getValue().clone());//再转码后发送给handler更新UI
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                LogUtil.d("onMtuChanged :  " + mtu);
                if(gatt != null && status == BluetoothGatt.GATT_SUCCESS){
                    separateLength = mtu;
                }
            }
        });
    }

    private void requestMtu(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(mBluetoothGatt != null){
                mBluetoothGatt.requestMtu(512);
            }
        }
    }

    //发送线程 --> app发送给模块数据
    private void sendThread(byte[] buff) {
        LogUtil.d("进入发送方法");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LogUtil.d(" ==== s总数据长度 === " + buff.length);
                List<byte[]> sendDataArray = ByteUtil.getSendDataByte(buff, separateLength);

/*                    byte[] bb = new byte[]{-27,7,7,7,7,-27,9,-12,44,67,45,44,9,7,-22,-5,55,5,9,12,56
                        ,66,77,88,99,12,1,14,-27,7};
                     mNeedCharacteristic.setValue(bb);
                     mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);//蓝牙发送数据，一次顶多20字节*/
                int number = 0;
                for (byte[] sendData : sendDataArray) {
                    LogUtil.d(" ==== sendData  length === " + sendData.length);
                    try {
                    //    if (ModuleParameters.getLevel() != 0) {//设置发送间隔等级，从0到10，
                    //        Thread.sleep(1);//最高,多延时100ms
                    //    }
                        LogUtil.d("每次延时 ： " + (5 + 10 * ModuleParameters.getState()));
                        Thread.sleep(5 + 10 * ModuleParameters.getState());//每次发包前，延时一会，更容易成功
                    //    Thread.sleep(100);
                        mNeedCharacteristic.setValue(sendData);
                        sendDataSign = !mBluetoothGatt.writeCharacteristic(mNeedCharacteristic);//蓝牙发送数据，一次顶多20字节
                        if (sendDataSign) {
                            Thread.sleep(1000 + 500 * ModuleParameters.getState());
                            LogUtil.d("发送失败....");
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
        };
        mThreadService.execute(runnable);
        LogUtil.d("进入线程池发送方法");
    }

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
/*        if(!isConnect || !isOpen()){
            Toast.makeText(MyApplication.getInstance(), "蓝牙未连接",Toast.LENGTH_LONG).show();
            return;
        }*/

        //获取梳理好的字节数据，准备组装发送
        byte[] sendByteArray = ByteUtil.combData(infoList);

        sendThread(sendByteArray);
    }


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
    }
}
