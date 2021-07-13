package com.yxx.app.bean;

import java.io.Serializable;

/**
 * Author: yangxl
 * Date: 2021/7/13 17:03
 * Description:
 */
public class VersionInfo implements Serializable {

    /**
     * versionCode : 0 ,
     * versionName : 1.0.2（主要已这个字段做版本判断）
     * updateLog : 这次更新的说明
     * apkInstall : 1 (是否强制安装，1是 ， 0 否)
     * downloadUrl : 一个下载的url
     */

    private int versionCode;
    private String versionName;
    private String updateLog;
    private int apkInstall;
    private String downloadUrl;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public int getApkInstall() {
        return apkInstall;
    }

    public void setApkInstall(int apkInstall) {
        this.apkInstall = apkInstall;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
