package com.yxx.app.download;

import java.io.File;


/**
 * @author Yang xl
 * @date 2020/4/15.
 * description：    下载回调
 */
public interface DownloadListener {
    /**
     * 开始下载
     *
     * @param tag 标识
     */
    void onStartDownload(String tag);

    /**
     * 进度
     *
     * @param tag      标识
     * @param progress 进度 0-100
     */
    void onProgress(String tag, int progress);

    /**
     * 下载成功
     *
     * @param tag  标识
     * @param file 下载的文件
     */
    void onFinishDownload(String tag, File file);

    /**
     * 下载失败
     *
     * @param tag 标识
     * @param msg  异常
     */
    void onFail(String tag, String msg);

}
