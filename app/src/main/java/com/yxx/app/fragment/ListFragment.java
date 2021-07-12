package com.yxx.app.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.yxx.app.BluetoothManager;
import com.yxx.app.R;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.util.Hex;
import com.yxx.app.util.JsonUtils;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.SPUtil;
import com.yxx.app.util.TimeUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/3 14:32
 * Description: 列表
 */
public class ListFragment extends Fragment implements View.OnClickListener {

    private TextView tv_num_and_price;
    private RecyclerView mRecyclerView;
    private Button btn_all_send;
    private TextView tv_edit;
    private TextView tv_copy;

    private SendDataAdapter mAdapter;

    private int currentStatus;//当前状态， 0（复制，编辑）， 1 （完成，清空）

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, null);

        initView(view);

        initAdapter();
        return view;
    }

    private void initView(View view) {
        tv_num_and_price = view.findViewById(R.id.tv_num_and_price);
        btn_all_send = view.findViewById(R.id.btn_all_send);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        tv_edit = view.findViewById(R.id.tv_edit);
        tv_copy = view.findViewById(R.id.tv_copy);

        tv_copy.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        btn_all_send.setOnClickListener(this);
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        String jsonStr = SPUtil.getString(SPUtil.CACHE_DATA_LIST);
        List<SendInfo> infoList = null;
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                infoList = JsonUtils.fromJsonArray(jsonStr, SendInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (infoList == null) {
            showEditTextView(false);
            infoList = new ArrayList<>();
        }else{
            showEditTextView(true);
        }
        mAdapter = new SendDataAdapter(infoList);
        mRecyclerView.setAdapter(mAdapter);
        setNumPrice();
    }

    public void addData(List<SendInfo> data, boolean isClear) {
        if (isClear) {
            clearCache();
        }
        if(data != null && data.size() > 0 && tv_edit.getVisibility() == View.INVISIBLE){
            showEditTextView(true);
        }
        mAdapter.add(data);
        //保存数据
        String jsonStr = JsonUtils.toJson(mAdapter.getData());
        SPUtil.putString(SPUtil.CACHE_DATA_LIST, jsonStr);
        setNumPrice();
    }

    private void setNumPrice(){
        tv_num_and_price.setText(getNumAndPrice());
    }

    /**
     * 计算总张数和总金额
     */
    private String getNumAndPrice() {
        int allPrice = 0;
        for(SendInfo info : mAdapter.getData()){
            allPrice += Integer.parseInt(info.price);
        }
        return String.format("%s 张    %s 元", mAdapter.getItemCount(), allPrice);
    }


    private void setCurrentStatus(){
        if(currentStatus == 0){
            currentStatus = 1;
            tv_copy.setText("完 成");
            tv_edit.setText("清 空");
        }else{
            currentStatus = 0;
            tv_copy.setText("复 制");
            tv_edit.setText("编 辑");
        }
    }

    private void copyData(){
        try{
            StringBuffer buffer = new StringBuffer();
            for(SendInfo info : mAdapter.getData()){
                buffer.append(String.format("%s-%s-%s %s:%s%s %s",info.year,info.month,info.day,
                        info.u_hours,info.u_minute, info.hasDownTime() ?
                                "-" + TimeUtil.hmFormat(info.d_hours, info.d_minute) : "",info.price));
                buffer.append("\n");
            }

            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", buffer.toString());
            cm.setPrimaryClip(mClipData);
            Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_copy:
                if(currentStatus == 0){
                    //复制
                    copyData();
                }else{
                    //完成
                    setCurrentStatus();
                    mAdapter.notifyDataSetChanged();
                }

                break;
            case R.id.tv_edit:
                if(currentStatus == 1){
                    showEditTextView(false);
                    clearCache();
                    setNumPrice();
                }
                setCurrentStatus();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_all_send:
                BluetoothManager.get().sendData(mAdapter.getData());
                break;
        }
    }

    //清空列表缓存数据
    private void clearCache(){
        mAdapter.clear();
        SPUtil.putString(SPUtil.CACHE_DATA_LIST,"");
    }

    private void showEditTextView(boolean show){
        if(show){
            btn_all_send.setEnabled(true);
            tv_copy.setVisibility(View.VISIBLE);
            tv_edit.setVisibility(View.VISIBLE);
        }else{
            btn_all_send.setEnabled(false);
            tv_copy.setVisibility(View.INVISIBLE);
            tv_edit.setVisibility(View.INVISIBLE);
        }
    }

    public class SendDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<SendInfo> data;

        public SendDataAdapter(List<SendInfo> data) {
            this.data = data;
        }

        public List<SendInfo> getData() {
            return data;
        }

        public void setData(List<SendInfo> data) {
            this.data = data;
        }

        public void clear() {
            data.clear();
            mAdapter.notifyDataSetChanged();
        }

        public void add(List<SendInfo> data) {
            if (data != null) {
                this.data.addAll(data);
                notifyItemRangeChanged(getItemCount(), data.size());
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getActivity()).inflate(
                    R.layout.list_send_data, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder) holder).bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_nyr;
            TextView tv_time;
            TextView tv_price;
            ImageView iv_send;
            ImageView iv_remove;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_nyr = itemView.findViewById(R.id.tv_nyr);
                tv_time = itemView.findViewById(R.id.tv_time);
                tv_price = itemView.findViewById(R.id.tv_price);
                iv_send = itemView.findViewById(R.id.iv_send);
                iv_remove = itemView.findViewById(R.id.iv_remove);
            }

            public void bind(SendInfo sendInfo) {
                tv_nyr.setText(TimeUtil.nyrFormat(sendInfo.year, sendInfo.month, sendInfo.day));
                tv_time.setText(String.format("%s%s",TimeUtil.hmFormat(sendInfo.u_hours, sendInfo.u_minute),
                        sendInfo.hasDownTime() ? "-" + TimeUtil.hmFormat(sendInfo.d_hours, sendInfo.d_minute) : ""));
                tv_price.setText(sendInfo.price);

                if(currentStatus == 0){
                    iv_send.setVisibility(View.VISIBLE);
                    iv_remove.setVisibility(View.GONE);
                }else{
                    iv_send.setVisibility(View.GONE);
                    iv_remove.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
