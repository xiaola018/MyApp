package com.yxx.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.yxx.app.api.Api;
import com.yxx.app.api.AppInterface;
import com.yxx.app.bean.VersionInfo;
import com.yxx.app.dialog.ApkDownloadDialog;
import com.yxx.app.dialog.LoadingDialog;
import com.yxx.app.util.JsonUtils;
import com.yxx.app.util.LogUtil;

import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Author: yangxl
 * Date: 2021/7/13 15:22
 * Description:
 */
public class UpdateManager {

    public static void check(Activity context, boolean isAuto) {
        LoadingDialog loadingDialog = null;
        if(!isAuto){
            loadingDialog = new LoadingDialog(context);
            loadingDialog.show();
        }
        LoadingDialog finalProgressDialog = loadingDialog;
        Api.get().create(AppInterface.class).updateLog()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull String s) {
                        LogUtil.d("请求成功 : " + s);
                        VersionInfo versionInfo = JsonUtils.fromJson(s, VersionInfo.class);
                        if (isNewVersion(versionInfo.getVersionName())) {
                            showUpdateLogDialog(context, versionInfo);
                        }else{
                            if(!isAuto) Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        LogUtil.d("请求失败 : " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        if(finalProgressDialog != null)
                            finalProgressDialog.dismiss();
                    }
                });
    }

    public static void showUpdateLogDialog(Activity activity, VersionInfo versionInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(String.format("是否更新到%s版本", versionInfo.getVersionName()))
                .setMessage(versionInfo.getUpdateLog());
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ApkDownloadDialog downloadDialog = new ApkDownloadDialog(activity);
                downloadDialog.show();
                downloadDialog.download();
            }
        });
        if(versionInfo.getApkInstall() == 1){
            builder.setCancelable(false);
        }else{
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.create().show();
    }

    /**
     * 版本信息
     *
     * @return
     */
    public static String versionName() {
        PackageManager manager = MyApplication.getInstance().getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    /**
     * 版本号
     *
     * @return
     */
    public static int versionCode() {
        PackageManager manager = MyApplication.getInstance().getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 判断当前版本是不是最新版本
     */
    public static boolean isNewVersion(int code) {
        return code > versionCode();
    }

    /**
     * 判断当前版本是不是最新版本
     *
     * @param version 格式 V2.5
     */
    public static boolean isNewVersion(String version) {
        if (TextUtils.isEmpty(version)) return true;
        return !(version.compareToIgnoreCase(String.format(Locale.getDefault(), "%s", versionName())) <= 0);
    }
}
