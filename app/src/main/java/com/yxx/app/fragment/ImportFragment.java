package com.yxx.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yxx.app.R;
import com.yxx.app.activity.GenerateActivity;

/**
 * Author: yangxl
 * Date: 2021/7/3 14:32
 * Description: 导入
 */
public class ImportFragment extends Fragment implements View.OnClickListener {

    private TextView tv_generate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import, null);

        initView(view);

        return view;
    }

    private void initView(View view){
        tv_generate = view.findViewById(R.id.tv_generate);
        tv_generate.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_generate:
                startActivityForResult(new Intent(getActivity(), GenerateActivity.class), 1001);
                break;
        }
    }
}
