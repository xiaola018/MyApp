package com.yxx.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.yxx.app.R;

public class HomeChildMenuView extends LinearLayout {
    public HomeChildMenuView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_menu_print_now,this);
    }

    public HomeChildMenuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_menu_print_now,this);
    }

    public HomeChildMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
