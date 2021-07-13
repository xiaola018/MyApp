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
import com.yxx.app.bean.DeviceModel;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.dialog.DiscoveryBluetoothDialog;
import com.yxx.app.dialog.NeverMenuPopup;
import com.yxx.app.fragment.BaseFragmentStateAdapter;
import com.yxx.app.fragment.ImportFragment;
import com.yxx.app.fragment.InputFragment;
import com.yxx.app.fragment.ListFragment;
import com.yxx.app.util.Hex;
import com.yxx.app.util.LogUtil;
import com.yxx.app.view.MenuConnectView;
import com.yxx.widget.TabLayout;
import com.yxx.widget.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener ,
        Toolbar.OnMenuItemClickListener,
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

        initListener();
    }

    private void findView() {
        toolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        ll_custom = findViewById(R.id.ll_custom);

/*        String hex = "3B00";
        byte[] bs = Hex.hexToByteArray(hex);
        for(byte b : bs){
            LogUtil.d(" == b == " + b);
        }

        String hexss = Hex.bytesToHex(bs);
        LogUtil.d("hexss = " + hexss);
        LogUtil.d(" num == " + Integer.parseInt("E057", 16));
        LogUtil.d(" num == " + Integer.toHexString(Integer.parseInt("E057", 16)));*/


    //    String numHex = Hex.decToHex(1);
   //     LogUtil.d("== numHex== " + numHex);
  //      byte[] numBytes = Hex.hexToByteArray(numHex);
    ///    for(byte b : numBytes){
    //        LogUtil.d( " == bbb=== " + Hex.bytesToHex(numBytes));
   //     }
    }

    private void initView() {
        discoveryDialog = new DiscoveryBluetoothDialog(this);
        discoveryDialog.setBluetoothCallback(this);
        setSupportActionBar(toolbar);
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

    private void initListener(){
        BluetoothManager.get().setOnBluetoothListener(new BluetoothManager.OnBluetoothListener() {
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
                    }
                });
            }
        });
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

            }
        });
    }
}