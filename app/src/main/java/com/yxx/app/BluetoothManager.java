package com.yxx.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.yxx.app.util.LogUtil;

/**
 * Author: yangxl
 * Date: 2021/7/3 8:57
 * Description:
 */
public class BluetoothManager {

    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private OnBluetoothListener onBluetoothListener;

    public static BluetoothManager get(){
        if(mBluetoothManager == null)mBluetoothManager = new BluetoothManager();
        return mBluetoothManager;
    }

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startReceiver();
    }

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener) {
        this.onBluetoothListener = onBluetoothListener;
    }

    public void openBluetooth(){
        if(mBluetoothAdapter != null){
            // 蓝牙已打开
            if (isOpen()){
                LogUtil.d("蓝牙已打开");
            //    startScanBluetooth();
            }else{
                LogUtil.d("提示用户打开蓝牙");
                mBluetoothAdapter.enable();
            }
        }else{
            Toast.makeText(MyApplication.getInstance(),"此设备不支持蓝牙",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isSupport(){
        return mBluetoothAdapter != null;
    }

    public boolean isOpen(){
        return isSupport() && mBluetoothAdapter.isEnabled();
    }

    private void startReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        MyApplication.getInstance().registerReceiver(receiver, filter);
    }

    public void startScanBluetooth() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            cancelDiscovery();
        }
        // 开始搜索
        mBluetoothAdapter.startDiscovery();
        LogUtil.d("正在搜索设备》。。");
    }

    public void cancelDiscovery(){
        mBluetoothAdapter.cancelDiscovery();
    }

    /**
     *  蓝牙状态广播
     */
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d("有回调=== action -=== " + action);
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                LogUtil.d("state = " + state);
                switch (state){
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
                        //    startActivity(new Intent(MainActivity.this, SeachBluetoothActivity.class));
                       // startScanBluetooth();
                        break;
                    case 13:
                        LogUtil.d("蓝牙正在关闭");
                        break;
                }
            }else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //蓝牙rssi参数，代表蓝牙强度
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                //蓝牙设备名称
                String name = device.getName();
                //蓝牙设备连接状态
                int status = device.getBondState();

                LogUtil.d("device name: "+device.getName()+" address: "+device.getAddress());
                onBluetoothListener.onDevice(device.getName(), device.getAddress());
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtil.d("开始搜索");
                onBluetoothListener.discoveryStarted();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                LogUtil.d("蓝牙设备搜索完成");
                onBluetoothListener.discoveryFinished();
            }
        }
    };

    public interface OnBluetoothListener{
        void open();
        void closed();
        void discoveryStarted();
        void discoveryFinished();
        void onDevice(String name, String address);
    }
}
