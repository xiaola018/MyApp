package com.yxx.app.dialog;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yxx.app.BluetoothManager;
import com.yxx.app.R;
import com.yxx.app.bean.DeviceModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: yangxl
 * Date: 2021/7/2 14:37
 * Description: 搜索蓝牙
 */
public class DiscoveryBluetoothDialog extends Dialog {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private Button btn_cancel;
    private TextView tv_no_data;
    private RecyclerView bondedRecyclerView;

    private DeviceListAdapter mAdapter;

    private Set<String> nameSet = new HashSet<>();

    private DiscoveryBluetoothCallback bluetoothCallback;

    public void setBluetoothCallback(DiscoveryBluetoothCallback bluetoothCallback) {
        this.bluetoothCallback = bluetoothCallback;
    }

    public DiscoveryBluetoothDialog(@NonNull Context context) {
        super(context, R.style.dialog_style);
    }

    public DiscoveryBluetoothDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_discovery_bluetooth);

        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.width = ViewGroup.LayoutParams.MATCH_PARENT;
                attr.height = ViewGroup.LayoutParams.MATCH_PARENT;

                window.setAttributes(attr);
            }

            window.setWindowAnimations(R.style.dialog_anim_style);
        }
        initView();
        initAdapter();
    }

    private void initView(){
        bondedRecyclerView = findViewById(R.id.bondedRecyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerView);
        btn_cancel = findViewById(R.id.btn_cancel);
        tv_no_data = findViewById(R.id.tv_no_data);

        btn_cancel.setOnClickListener(view -> dismiss());

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                BluetoothManager.get().cancelDiscovery();
            }
        });
    }

    public void startDiscovery(){
        nameSet.clear();
        show();
        if(mAdapter != null){
            mAdapter.getData().clear();
            mAdapter.notifyDataSetChanged();
        }
        Set<BluetoothDevice> devices = BluetoothManager.get().getBluetoothAdapter().getBondedDevices();
        List<DeviceModel> models = new ArrayList<>();
        for(BluetoothDevice device : devices){
            DeviceModel model = new DeviceModel();
            model.mDevice = device;
            model.deviceName = device.getName();
            model.address = device.getAddress();
            models.add(model);
        }
        setBoundedAdapter(models);
        mProgressBar.setVisibility(View.VISIBLE);
        BluetoothManager.get().startScanBluetooth();
    }

    private void setBoundedAdapter(List<DeviceModel> deviceModelList){
        DeviceListAdapter mAdapter = new DeviceListAdapter(deviceModelList);
        bondedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bondedRecyclerView.setAdapter(mAdapter);
    }

    public void discoveryFinished(){
        if(mProgressBar != null)mProgressBar.setVisibility(View.GONE);
        if(mAdapter == null || mAdapter.getData() == null || mAdapter.getData().size() == 0){
            if(tv_no_data != null)tv_no_data.setVisibility(View.VISIBLE);
        }
    }

    public void addDevice(DeviceModel deviceModel){
        if(mAdapter != null && !TextUtils.isEmpty(deviceModel.deviceName)
                && !TextUtils.isEmpty(deviceModel.address) && nameSet.add(deviceModel.deviceName)){
            mAdapter.getData().add(deviceModel);
            mAdapter.notifyItemRangeChanged(mAdapter.getItemCount(), mAdapter.getData().size());
        }
    }

    private void initAdapter(){
        mAdapter = new DeviceListAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<DeviceModel> data;

        public DeviceListAdapter(List<DeviceModel> data) {
            this.data = data;
        }

        public List<DeviceModel> getData() {
            return data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_bluetootn_discovery, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder)holder).bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView tv_device_name;
            TextView tv_device_addr;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_device_name = itemView.findViewById(R.id.tv_device_name);
                tv_device_addr = itemView.findViewById(R.id.tv_device_addr);
            }

            public void bind(DeviceModel deviceInfo){
                tv_device_name.setText(deviceInfo.deviceName);
                tv_device_addr.setText(deviceInfo.address);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    //    BluetoothManager.get().makePair(deviceInfo.address);
                    //    BluetoothManager.get().connectGatt(getContext(), deviceInfo, null);
                        dismiss();
                        bluetoothCallback.connectDevice(deviceInfo);
                    }
                });
            }
        }
    }

    public interface DiscoveryBluetoothCallback{
        void cancelDiscovery();
        void connectDevice(DeviceModel deviceModel);
    }
}
