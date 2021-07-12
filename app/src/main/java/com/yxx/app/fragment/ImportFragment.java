package com.yxx.app.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yxx.app.R;
import com.yxx.app.activity.GenerateActivity;
import com.yxx.app.activity.MainActivity;
import com.yxx.app.bean.SendInfo;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.MatcherUtil;
import com.yxx.app.util.TimeUtil;

import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/3 14:32
 * Description: 导入
 */
public class ImportFragment extends Fragment implements View.OnClickListener {

    private TextView tv_clear;
    private TextView tv_generate;
    private EditText editText;
    private Button btn_import;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import, null);

        initView(view);

        return view;
    }

    private void initView(View view) {
        tv_clear = view.findViewById(R.id.tv_clear);
        tv_generate = view.findViewById(R.id.tv_generate);
        editText = view.findViewById(R.id.editText);
        btn_import = view.findViewById(R.id.btn_import);

        tv_clear.setOnClickListener(this);
        tv_generate.setOnClickListener(this);
        btn_import.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clear:
                editText.setText("");
                break;
            case R.id.tv_generate:
                startActivityForResult(new Intent(getActivity(), GenerateActivity.class), 1001);
                break;
            case R.id.btn_import:
                importData();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (data != null) {
                List<String> strList = data.getStringArrayListExtra("strList");
                if (strList != null && strList.size() > 0) {
                    editText.setText("");
                    for (String ss : strList) {
                        editText.append(ss + "\n");
                    }
                }
            }

        }
    }

    private void importData() {

        try {
            List<SendInfo> dateStrList = null;
            dateStrList = MatcherUtil.getFormat(editText.getText().toString());
            MainActivity activity = (MainActivity) getActivity();
            List<SendInfo> finalDateStrList = dateStrList;
            new AlertDialog.Builder(getActivity())
                    .setTitle("清空列表")
                    .setMessage("是否清空现有列表？")
                    .setPositiveButton("否", (dialogInterface, i) -> {
                        if (activity != null) {
                            activity.importDataToList(finalDateStrList, false);
                        }
                    }).setNeutralButton("是", (dialogInterface, i) -> {
                if (activity != null) {
                    activity.importDataToList(finalDateStrList, true);
                }
            }).create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
