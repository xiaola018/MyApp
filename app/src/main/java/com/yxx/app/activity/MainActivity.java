package com.yxx.app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yxx.app.BluetoothManager;
import com.yxx.app.BluetoothManagerBle;
import com.yxx.app.MyApplication;
import com.yxx.app.R;
import com.yxx.app.UpdateManager;
import com.yxx.app.bean.DeviceModel;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.dialog.DiscoveryBluetoothDialog;
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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

    private long backTime;

    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);
        findView();

        initView();
        initViewPager();

    //    BluetoothManager.get().setOnBluetoothListener(this);
        BluetoothManager.get().bindService(this, this);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        int a = new BigInteger("ffcc", 16).intValue();
        int b = new BigInteger("fe98", 16).intValue();
        int c = new BigInteger("1c200", 16).intValue();
        int d = new BigInteger("07e5", 16).intValue();
        int e = new BigInteger("4b00", 16).intValue();
        LogUtil.d(" == 19200, int to hex == " + Hex.decToHex(19200));
        LogUtil.d(" == ffcc, hex to int  == " + a);
        LogUtil.d(" == fe98, hex to int == " + b);
        LogUtil.d(" == 1c200, hex to int == " + c);
        LogUtil.d(" == 07e5, hex to int == " + d);
        LogUtil.d(" == 4b00, hex to int == " + e);

        byte[] bs = new byte[]{0x1c, (byte) 0x200};
        LogUtil.d(" == bs === " + Arrays.toString(bs));

        String bsHex = Hex.bytesToHex(bs);
        LogUtil.d("=== bsHex === "  + bsHex);

        byte i1 = (byte) (19200 & 0xFF);//??????
        byte i2 = (byte) (19200 >>> 8);//??????
        LogUtil.d(" ==== big === " + i2);
        LogUtil.d(" ==== little === " + i1);
        String aHex = Hex.bytesToHex(new byte[]{i2,i1});
        LogUtil.d("====== iiiii ==== " + aHex);
        LogUtil.d(" ===== aHex to int == " + new BigInteger(aHex, 16).intValue());
        LogUtil.d(" ==== i1 === " + Arrays.toString(ByteUtil.int2Bytes(2021)));
        byte b1 = (byte) 0x4b;
        byte b2 = (byte) 0x00;
        LogUtil.d("=== gao === " + b1);
        LogUtil.d("=== gao === " + b2);

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

        toolbar.inflateMenu(R.menu.menu_home);
        toolbar.setOnMenuItemClickListener(this);
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_connect);
        menuConnectView = (MenuConnectView) menuItem.getActionView();
        menuConnectView.setOnClickListener(v -> checkPermis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        BluetoothManager.get().disconnect(this);
        super.onDestroy();

    }

    private void initViewPager() {

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(inputFragment = new InputFragment());
        fragmentList.add(importFragment = new ImportFragment());
        fragmentList.add(listFragment = new ListFragment());

        mViewPager.setOffscreenPageLimit(fragmentList.size());
        mViewPager.setAdapter(new BaseFragmentStateAdapter(getSupportFragmentManager(),
                getLifecycle(), fragmentList));

        //TabLayout???Viewpager2????????????
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("??? ???");
                    break;
                case 1:
                    tab.setText("??? ???");
                    break;
                case 2:
                    tab.setText("??? ???");
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
                                alertdialogbuilder.setMessage("??????????????????,??????\"??????\"-\"??????\"?????????????????????");
                                alertdialogbuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                });
                                alertdialogbuilder.setNeutralButton("??????", null);
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
            LogUtil.d("????????????");
            BluetoothManager.get().openBluetooth();
        }else{
            LogUtil.d("???????????????");
            discoveryDialog.startDiscovery();
        }
    }

    @Override
    public void cancelDiscovery() {

    }

    @Override
    public void connectDevice(DeviceModel deviceModel){
        menuConnectView.showProgressBar(true);
        BluetoothManager.get().connect(deviceModel);
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
            Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
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

        int x = metrics.widthPixels - 500;
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
        mHandler.sendEmptyMessage(1008);
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
        mHandler.sendEmptyMessage(1005);
    }

    @Override
    public void onConnectError() {
        LogUtil.d("onConnectError");
        mHandler.sendEmptyMessage(1009);
    }

    @Override
    public void onStateDisconnected() {
        LogUtil.d("onStateDisconnected");
        mHandler.sendEmptyMessage(1008);
    }

    @Override
    public void onSendSuccess(int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code == BluetoothManagerBle.CODE_PRINT){
                    ToastUtil.show("????????????");
                }
            }
        });
    }

    @Override
    public void onSendFaile(int code, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(code == BluetoothManagerBle.CODE_PRINT){
                    ToastUtil.show("????????????");
                }
            }
        });
    }

    private void connectState(){
        toolbar.setTitle("?????????");
        menuConnectView.showProgressBar(false);
        inputFragment.sendBtnEnable(true);
        toolbar.setNavigationIcon(R.mipmap.ic_b_c);
    }

    private void unconnectState(String title){
        toolbar.setTitle(title);
        menuConnectView.showProgressBar(false);
        inputFragment.sendBtnEnable(false);
        toolbar.setNavigationIcon(R.mipmap.ic_b_n);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backTime < 2000L) {
            super.onBackPressed();
            System.exit(0);
        } else {
            ToastUtil.show("????????????????????????");
            backTime = System.currentTimeMillis();
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(weakReference.get() != null){
                MainActivity activity = weakReference.get();
                switch (msg.what){
                    case 1005:
                        activity.connectState();
                        break;
                    case 1008:
                        activity.unconnectState("?????????");
                        break;
                    case 1009:
                        ToastUtil.show("?????????????????????????????????????????????");
                        activity.unconnectState("?????????");
                        break;
                }
            }
        }
    }
}