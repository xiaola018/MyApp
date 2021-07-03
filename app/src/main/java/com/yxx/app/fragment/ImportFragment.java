package com.yxx.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yxx.app.R;

/**
 * Author: yangxl
 * Date: 2021/7/3 14:32
 * Description: 导入
 */
public class ImportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import, null);

        initView(view);

        return view;
    }

    private void initView(View view){

    }
}
