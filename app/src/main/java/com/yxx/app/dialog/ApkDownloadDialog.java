package com.yxx.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.yxx.app.R;
import com.yxx.app.activity.ReplaceCityActivity;
import com.yxx.app.download.DownLoadHelper;
import com.yxx.app.download.DownloadListener;
import com.yxx.app.util.LogUtil;

import java.io.File;

/**
 * Author: yangxl
 * Date: 2021/7/13 15:47
 * Description:
 */
public class ApkDownloadDialog extends Dialog {

    private Activity context;

    private TextView tvProgressNum;
    private ProgressBar mProgressBar;

    private boolean isFinish;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ApkDownloadDialog(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    public ApkDownloadDialog(@NonNull Activity context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_apk_download);
        tvProgressNum = findViewById(R.id.tvProgressNum);
        mProgressBar = findViewById(R.id.mProgressBar);

        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (d.getWidth() * 0.8);
        dialogWindow.setAttributes(p);
        this.setCancelable(false);
    }

    public void setProgress(int progress){
        tvProgressNum.setText(String.format("%s%s",progress,"%"));
        mProgressBar.setProgress(progress);
    }

    public void download(String url){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        DownLoadHelper.getInstance().addDownLoadListener(new MyDownloadListener());
        DownLoadHelper.getInstance().downLoadFile(url, directory.getAbsolutePath(), "update.apk");
    }

    private class MyDownloadListener implements DownloadListener{

        @Override
        public void onStartDownload(String tag) {
            LogUtil.d("????????????");
        }

        @Override
        public void onProgress(String tag, int progress) {
            LogUtil.d("???????????? " + progress);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setProgress(progress);
                }
            });

        }

        @Override
        public void onFinishDownload(String tag, File file) {
            LogUtil.d("????????????");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(!isFinish){
                        installAPK(context, file);
                        isFinish = false;
                    }
                }
            });

        }

        @Override
        public void onFail(String tag, String msg) {
            LogUtil.d("??????????????? " + msg);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    Toast.makeText(context,"????????????",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void installAPK(Context activity, File apkFile) {
        if(activity != null && isShowing())dismiss();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, "com.yxx.app.fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }
}
