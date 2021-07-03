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

import androidx.core.app.ActivityCompat;

import com.yxx.app.util.LogUtil;

/**
 * Author: yangxl
 * Date: 2021/7/3 8:57
 * Description:
 */
public class BluetoothManager {

    private static BluetoothManager mBluetoothManager;

    public static BluetoothManager get(){
        if(mBluetoothManager == null)mBluetoothManager = new BluetoothManager();
        return mBluetoothManager;
    }

    private BluetoothAdapter mBluetoothAdapter;

/*    public void isBluetoothEnable() {
        startReceiver();
        //获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null){
            // 蓝牙已打开
            if (mBluetoothAdapter.isEnabled()){
                LogUtil.d("设备蓝牙已打开");
                startScanBluetooth();
            }else{//未打开则开启，此处可以通过弹框提示来提示用户开启
                LogUtil.d("提示用户打开蓝牙");
                mBluetoothAdapter.enable();
            }
        }else{
            //设备不支持蓝牙
        }
    }*/

/*    private void startReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);

    }*/

    private void startScanBluetooth() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 开始搜索
        mBluetoothAdapter.startDiscovery();
        LogUtil.d("正在搜索设备》。。");
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
                        break;
                    case 11:
                        LogUtil.d("蓝牙正在打开");
                        break;
                    case 12:
                        LogUtil.d("蓝牙打开");
                        //    startActivity(new Intent(MainActivity.this, SeachBluetoothActivity.class));
                        startScanBluetooth();
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
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtil.d("开始搜索");
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                LogUtil.d("蓝牙设备搜索完成");
            }
        }
    };
}
