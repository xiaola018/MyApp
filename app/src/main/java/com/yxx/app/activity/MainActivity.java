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
import android.content.res.AssetManager;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

        BluetoothManager.get().setOnBluetoothListener(this);
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

/*        new Thread(){
            @Override
            public void run() {
                super.run();
                sendByteData();
            }
        }.start();*/

    }

    InputStream inputStream = null;//模板文件数据流
    BufferedInputStream bufferedInputStream = null;//缓冲区
    int fileLength;//文件总长度
    int readLength,sendCount;
    byte[] fileBytes = null;
    private void sendByteData(){
        byte[] txBuffer = new byte[5];
        txBuffer[0] = (byte) (readLength == 1 ? 0x22 : 0x02);
        txBuffer[3] = 0x5a;
        txBuffer[4] = (byte) 0xa5;



        try {
            if(inputStream == null){
                AssetManager manager = MyApplication.getInstance().getResources().getAssets();
                inputStream = manager.open("bin/JYG_TEST_DATA.bin");
                //总长度
                fileLength = inputStream.available();
                fileBytes = new byte[fileLength];
                LogUtil.d("数据总长度 ：" + fileLength);

            }

            LogUtil.d("readLength = " + readLength);
            inputStream.skip(readLength);
            bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] tempbytes = new byte[128];
            int len;

            if ((len = bufferedInputStream.read(tempbytes)) != -1) {
                sendCount++;
                LogUtil.d(String.format("发送模板数据第%s次", sendCount));

                byte[] bytesHib = ByteUtil.int2BytesHib(readLength);
                txBuffer[1] = bytesHib[0];
                txBuffer[2] = bytesHib[1];
                readLength += len;
                byte[] sendBytes = new byte[txBuffer.length + len];
                System.arraycopy(txBuffer, 0, sendBytes, 0, txBuffer.length);
                System.arraycopy(tempbytes, 0, sendBytes, txBuffer.length, len);

                sendByteData();
            } else {
                //已经读取完了
                LogUtil.d("已经读取完啦");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
/*            try {
                if (inputStream != null) inputStream.close();
                if (bufferedInputStream != null) bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
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
        BluetoothManager.get().disconnect();
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
        unconnectState("已断开");
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectState();
            }
        });
    }

    @Override
    public void onConnectError() {
        LogUtil.d("onConnectError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unconnectState("未连接");
            }
        });
    }

    @Override
    public void onStateDisconnected() {
        LogUtil.d("onStateDisconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unconnectState("已断开");
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
                    ToastUtil.show("发送失败");
                }
            }
        });
    }

    private void connectState(){
        toolbar.setTitle("已连接");
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
}