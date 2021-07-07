package com.yxx.app.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.yxx.app.R;
import com.yxx.app.bean.SendInfo;
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
public class ListFragment extends Fragment {

    private TextView tv_num_and_price;
    private RecyclerView mRecyclerView;
    private Button btn_all_send;

    private SendDataAdapter mAdapter;

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
            infoList = new ArrayList<>();
        }
        mAdapter = new SendDataAdapter(infoList);
        mRecyclerView.setAdapter(mAdapter);
        setNumPrice();
    }

    public void addData(List<SendInfo> data, boolean isClear) {
        if (isClear) {
            mAdapter.clear();
            SPUtil.putString(SPUtil.CACHE_DATA_LIST,"");
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
                tv_time.setText(TimeUtil.hmFormat(sendInfo.hours, sendInfo.minute));
                tv_price.setText(sendInfo.price);
            }
        }
    }
}
