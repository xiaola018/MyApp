package com.yxx.app.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yxx.app.BluetoothManager;
import com.yxx.app.R;
import com.yxx.app.bean.DeviceInfo;

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

    private DeviceListAdapter mAdapter;

    private Set<String> nameSet = new HashSet<>();

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
        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerView);
        btn_cancel = findViewById(R.id.btn_cancel);
        tv_no_data = findViewById(R.id.tv_no_data);

        btn_cancel.setOnClickListener(view -> BluetoothManager.get().cancelDiscovery());
    }

    public void startDiscovery(){
        nameSet.clear();
        show();
        if(mAdapter != null){
            mAdapter.getData().clear();
            mAdapter.notifyDataSetChanged();
        }
        mProgressBar.setVisibility(View.VISIBLE);
        BluetoothManager.get().startScanBluetooth();
    }

    public void discoveryFinished(){
        mProgressBar.setVisibility(View.GONE);
        if(mAdapter.getData() == null || mAdapter.getData().size() == 0){
            tv_no_data.setVisibility(View.VISIBLE);
        }
    }

    public void addDevice(String name, String address){
        if(mAdapter != null && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(address) && nameSet.add(name)){
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.deviceName = name;
            deviceInfo.address = address;
            mAdapter.getData().add(deviceInfo);
            mAdapter.notifyItemRangeChanged(mAdapter.getItemCount(), mAdapter.getData().size());
        }
    }

    private void initAdapter(){
        mAdapter = new DeviceListAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<DeviceInfo> data;

        public DeviceListAdapter(List<DeviceInfo> data) {
            this.data = data;
        }

        public List<DeviceInfo> getData() {
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

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_device_name = itemView.findViewById(R.id.tv_device_name);
            }

            public void bind(DeviceInfo deviceInfo){
                tv_device_name.setText(deviceInfo.deviceName);
            }
        }
    }
}
