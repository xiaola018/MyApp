package com.yxx.app.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yxx.app.R;
import com.yxx.app.UpdateManager;
import com.yxx.app.util.LogUtil;
import com.yxx.app.util.SPUtil;

/**
 * Author: yangxl
 * Date: 2021/7/13 14:12
 * Description:
 */
public class NeverMenuPopup extends PopupWindow implements View.OnClickListener {

    private View mContentView;
    private TextView list1;
    private TextView list2;
    private LinearLayout list3;
    private RelativeLayout list4;
    private TextView tvVersionName;
    private CheckBox checkbox;

    private OnPopupClickCallbck onPopupClickCallbck;

    public NeverMenuPopup(Context context) {
        super(context);

        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);
        setTouchable(true);

        mContentView = LayoutInflater.from(context).inflate(R.layout.layout_child_menu_custom, null, false);

        setContentView(mContentView);
        setCheckbox();

        list1 = mContentView.findViewById(R.id.list1);
        list2 = mContentView.findViewById(R.id.list2);
        list3 = mContentView.findViewById(R.id.list3);
        list4 = mContentView.findViewById(R.id.list4);
        checkbox = mContentView.findViewById(R.id.checkbox);
        tvVersionName = mContentView.findViewById(R.id.tvVersionName);

        tvVersionName.setText(String.format("v%s", UpdateManager.versionName()));

        list1.setOnClickListener(this);
        list2.setOnClickListener(this);
        list3.setOnClickListener(this);
        list4.setOnClickListener(this);

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SPUtil.putInt(SPUtil.CACHE_IS_NOW_PRINT, b ? 1 : 0);
            }
        });
    }

    public void setOnPopupClickCallbck(OnPopupClickCallbck onPopupClickCallbck) {
        this.onPopupClickCallbck = onPopupClickCallbck;
    }

    private void setCheckbox(){
        if(checkbox != null){
            checkbox.setChecked(SPUtil.isNowPrint() == 1);
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT >= 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (Build.VERSION.SDK_INT >= 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        dismiss();
        switch (view.getId()){
            case R.id.list1:
                onPopupClickCallbck.onDiscoveryBluetooth();
                break;
            case R.id.list2:
                onPopupClickCallbck.onReplaceCity();
                break;
            case R.id.list3:
                SPUtil.putInt(SPUtil.CACHE_IS_NOW_PRINT, SPUtil.isNowPrint() == 0 ? 1 : 0);
                setCheckbox();
                break;
            case R.id.list4:
                onPopupClickCallbck.checkUpdate();
                break;
        }
    }

    public interface OnPopupClickCallbck{
        void onDiscoveryBluetooth();
        void onReplaceCity();
        void checkUpdate();
    }
}
