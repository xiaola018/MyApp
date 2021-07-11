package com.yxx.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.yxx.app.R;

public class MenuConnectView extends LinearLayout {

    private ProgressBar connect_progressbar;

    public MenuConnectView(Context context) {
        this(context, null);
    }

    public MenuConnectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MenuConnectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_menu_home_connect_status,this);
        initView();
    }

    private void initView(){
        connect_progressbar  = findViewById(R.id.connect_progressbar);
    }

    public void showProgressBar(boolean show){
        connect_progressbar.setVisibility(show ? VISIBLE : GONE);
    }
}
