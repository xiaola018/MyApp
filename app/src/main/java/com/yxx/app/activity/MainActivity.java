package com.yxx.app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.common.primitives.Bytes;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yxx.app.BluetoothManager;
import com.yxx.app.MyApplication;
import com.yxx.app.R;
import com.yxx.app.UpdateManager;
import com.yxx.app.api.Api;
import com.yxx.app.api.TestInterface;
import com.yxx.app.bean.DeviceModel;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.dialog.DiscoveryBluetoothDialog;
import com.yxx.app.dialog.LoadingDialog;
import com.yxx.app.dialog.NeverMenuPopup;
import com.yxx.app.fragment.BaseFragmentStateAdapter;
import com.yxx.app.fragment.ImportFragment;
import com.yxx.app.fragment.InputFragment;
import com.yxx.app.fragment.ListFragment;
import com.yxx.app.util.ByteUtil;
import com.yxx.app.util.Hex;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.ToastUtil;
import com.yxx.app.view.MenuConnectView;
import com.yxx.widget.TabLayout;
import com.yxx.widget.TabLayoutMediator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener ,
        Toolbar.OnMenuItemClickListener,
        BluetoothManager.OnBluetoothListener,
        DiscoveryBluetoothDialog.DiscoveryBluetoothCallback{

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;

    private Toolbar toolbar;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private LinearLayout ll_custom;

    private MenuConnectView menuConnectView;
    private NeverMenuPopup menuPopup;

    private InputFragment inputFragment;
    private ImportFragment importFragment;
    private ListFragment listFragment;

    private DiscoveryBluetoothDialog discoveryDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();

        initView();
        initViewPager();

        BluetoothManager.get().bindService(this, this);
        //MainActivity
        //BluetoothManager
        //BluetoothService
        //清单文件
    }

    private void findView() {
        toolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        ll_custom = findViewById(R.id.ll_custom);
    }

    private void initView() {
        discoveryDialog = new DiscoveryBluetoothDialog(this);
        discoveryDialog.setBluetoothCallback(this);
        setSupportActionBar(toolbar);
    //    abc();
    }

    private void abc(){
        byte[] bytes = new byte[]{
                0x46,(byte)0xb9,0x68,0x00,0x38,0x50,0x01,0x51,0x06,0x20,
                (byte) 0x93,0x03,0x01,(byte)0xff,(byte)0xff, (byte)0xbf,
                (byte)0xaf,(byte)0xff,0x00,(byte)0xce,(byte)0xf7,(byte)0xb0,
                0x73,0x55,0x00,(byte)0xf7,0x64,0x0c,(byte)0x8a,0x16, (byte)0x91,
                (byte)0xba,0x0e,0x1e,0x1f,(byte)0xff,0x01,0x00,0x00,(byte)0xfe,
                0x04,(byte)0xa8,0x21,0x04,0x09, (byte)0x80,(byte)0xff,0x60,
                0x14,0x3c,0x65,(byte)0x81,(byte)0x9b,(byte)0xc6,(byte)0xff,0x15,(byte)0x91,0x16
        };
        LogUtil.d("进入方法");

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte) 0xb9 && bytes[i + 1] == (byte) 0x68 && bytes[i + 2] == (byte) 0x00) {
                int len = bytes[i + 3];//包长度
                int offSet = i + 4;//开始偏移的起始位置
                byte verifyH = bytes[bytes.length - 3];
                byte verifyL = bytes[bytes.length - 2];
                int recvSum = len + 0x68;
                byte[] rxbuffer = new byte[bytes.length - offSet - 3];
                System.arraycopy(bytes, offSet, rxbuffer, 0, rxbuffer.length);
                for (int j = 0; j < rxbuffer.length; j++) {
                    String hex = Hex.bytesToHex(new byte[]{rxbuffer[j]});
                    BigInteger bigInteger = new BigInteger(hex,16);
                    recvSum += bigInteger.intValue();
                }
                byte[] hibyte = ByteUtil.int2BytesHib(recvSum);
                if(hibyte[0] == verifyH && hibyte[1] == verifyL){
                    if(rxbuffer[0] == 0x50){
                        //握手成功,设置波特率
                        LogUtil.d("握手成功");
                    }else{
                        //握手失败
                    }
                }else{
                    //握手失败
                    LogUtil.d("握手失败");
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.menu_home);
        toolbar.setOnMenuItemClickListener(this);
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_connect);
        menuConnectView = (MenuConnectView) menuItem.getActionView();
        menuConnectView.setOnClickListener(v -> checkPermis());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothManager.get().disconnect();
    }

    private void initViewPager() {

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(inputFragment = new InputFragment());
        fragmentList.add(importFragment = new ImportFragment());
        fragmentList.add(listFragment = new ListFragment());

        mViewPager.setOffscreenPageLimit(fragmentList.size());
        mViewPager.setAdapter(new BaseFragmentStateAdapter(getSupportFragmentManager(),
                getLifecycle(), fragmentList));

        //TabLayout和Viewpager2进行关联
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("输 入");
                    break;
                case 1:
                    tab.setText("导 入");
                    break;
                case 2:
                    tab.setText("列 表");
                    break;
            }
        }).attach();
    }

    @Override
    public void onClick(View view) {

    }

    private void checkPermis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            if(aBoolean){
                                openBluetooth();
                            }else{
                                AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MainActivity.this);
                                alertdialogbuilder.setMessage("缺少必要权限,请在\"设置\"-\"权限\"中打开所需权限");
                                alertdialogbuilder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                });
                                alertdialogbuilder.setNeutralButton("取消", null);
                                final AlertDialog alertdialog1 = alertdialogbuilder.create();
                                alertdialog1.show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        openBluetooth();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        showPopup();
        return false;
    }

    private void openBluetooth(){
        if(!BluetoothManager.get().isOpen()){
            LogUtil.d("打开蓝牙");
            BluetoothManager.get().openBluetooth();
        }else{
            LogUtil.d("蓝牙已打开");
            discoveryDialog.startDiscovery();
        }
    }

    @Override
    public void cancelDiscovery() {

    }

    @Override
    public void connectDevice(DeviceModel deviceModel){
        menuConnectView.showProgressBar(true);
        BluetoothManager.get().connectGatt(this, deviceModel);
    }

    public void importData(SendInfo sendInfo){
        if(listFragment != null){
            List<SendInfo> data = new ArrayList<>();
            data.add(sendInfo);
            importDataToList(data, false);
        }
    }

    public void importDataToList(List<SendInfo> data, boolean isClear){
        if(listFragment != null){
            listFragment.addData(data,isClear);
            setCurrentPager(2);
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCurrentPager(int index){
        mViewPager.setCurrentItem(index);
    }


    private void showPopup(){
        WindowManager wm = (WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        if(menuPopup == null){
            menuPopup = new NeverMenuPopup(this);
        }

        int x = metrics.widthPixels - 300;
        menuPopup.showAsDropDown(toolbar, x, 0, Gravity.NO_GRAVITY);

        menuPopup.setOnPopupClickCallbck(new NeverMenuPopup.OnPopupClickCallbck() {
            @Override
            public void onDiscoveryBluetooth() {
                openBluetooth();
            }

            @Override
            public void onReplaceCity() {
                startActivity(new Intent(MainActivity.this, ReplaceCityActivity.class));
            }

            @Override
            public void checkUpdate() {
                UpdateManager.check(MainActivity.this,false);
            }
        });
    }


    @Override
    public void open() {
        LogUtil.d("open");
        openBluetooth();
    }

    @Override
    public void closed() {
        LogUtil.d("closed");
    }

    @Override
    public void discoveryStarted() {
        LogUtil.d("discoveryStarted");
    }

    @Override
    public void discoveryFinished() {
        LogUtil.d("discoveryFinished");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                discoveryDialog.discoveryFinished();
            }
        });

    }

    @Override
    public void onDeviceAdd(DeviceModel deviceModel) {
        LogUtil.d("onDeviceAdd");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                discoveryDialog.addDevice(deviceModel);
            }
        });

    }


    @Override
    public void whilePari(BluetoothDevice device) {

    }

    @Override
    public void pairingSuccess(BluetoothDevice device) {

    }

    @Override
    public void cancelPari(BluetoothDevice device) {

    }

    @Override
    public void onConnectSuccess() {
        LogUtil.d("onConnectSuccess");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("已连接");
                menuConnectView.showProgressBar(false);
                inputFragment.sendBtnEnable(true);
                toolbar.setNavigationIcon(R.mipmap.ic_b_c);
            }
        });
    }

    @Override
    public void onConnectError() {
        LogUtil.d("onConnectError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("未连接");
                menuConnectView.showProgressBar(false);
                inputFragment.sendBtnEnable(false);
                toolbar.setNavigationIcon(R.mipmap.ic_b_n);
            }
        });
    }

    @Override
    public void onStateDisconnected() {
        LogUtil.d("onStateDisconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("已断开");
                menuConnectView.showProgressBar(false);
                inputFragment.sendBtnEnable(false);
                toolbar.setNavigationIcon(R.mipmap.ic_b_n);
            }
        });
    }

    @Override
    public void onSendSuccess(int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code == BluetoothManager.CODE_PRINT){
                    ToastUtil.show("发送成功");
                }
            }
        });
    }

    @Override
    public void onSendFaile(int code, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code == BluetoothManager.CODE_PRINT){
                    ToastUtil.show("发送成功");
                }
            }
        });
    }
}