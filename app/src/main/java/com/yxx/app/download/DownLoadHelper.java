package com.yxx.app.download;

import android.annotation.SuppressLint;
import android.os.FileUtils;
import android.text.TextUtils;

import com.yxx.app.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * @author Yang xl
 * @date 2020/4/15.
 * description：    下载
 */
public class DownLoadHelper {
    // 超时15s
    private static final int DEFAULT_TIMEOUT = 15;
    // 网络工具retrofit
    private Retrofit retrofit;
    // 下载进度、完成、失败等的回调事件
    private List<DownloadListener> mDownloadListeners;

    private static DownLoadHelper loadHelper;
    private List<Disposable> compositeDisposable = new ArrayList<>();

    public static DownLoadHelper getInstance() {
        synchronized (Object.class) {
            if (loadHelper == null) {
                loadHelper = new DownLoadHelper();
            }
        }
        return loadHelper;
    }

    public void addDownLoadListener(DownloadListener listener) {
        if (null == mDownloadListeners) {
            mDownloadListeners = new ArrayList<>();
        }
        if (!mDownloadListeners.contains(listener)) {
            mDownloadListeners.add(listener);
        }
    }

    public void removeDownLoadListener(DownloadListener listener) {
        if (null != mDownloadListeners && mDownloadListeners.size() > 0) {
            mDownloadListeners.remove(listener);
        }
        if(compositeDisposable.size() > 0){
            try{
                Disposable disposable = compositeDisposable.get(compositeDisposable.size() - 1);
                if( null != disposable)disposable.dispose();
            }catch (Exception e){}
        }
    }


    private void initClient(final DownModel downModel) {
        //创建客户端
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {

                        Request request = chain.request();
                        if (downModel.getDownSize() != 0 && downModel.getTotalSize() != 0) {
                            request = request.newBuilder()
                                    .addHeader("RANGE", "bytes=" + downModel.getDownSize() + "-" + downModel.getTotalSize()).build();
                        }
                        Response response = chain.proceed(request);
                        return response.newBuilder().body(new DownloadResponseBody(response.body(),
                                new DownloadResponseBody.ProgressListener() {
                                    @Override
                                    public void onProgress(long totalSize, long downSize) {

                                        long currentDown = downSize + downModel.getTotalSize() - totalSize;
                                        downModel.setDownSize(currentDown);
                                        if (downModel.getTotalSize() == 0) {
                                            downModel.setTotalSize(totalSize);
                                        }

                                        if (null != mDownloadListeners && mDownloadListeners.size() > 0) {
                                            for (DownloadListener listener : mDownloadListeners) {
                                                listener.onProgress(downModel.getTag(), (int) (currentDown * 100 / totalSize));
                                            }
                                        }

                                    }
                                })).build();
                    }
                })
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://gkimg.cdn.midasjoy.com/")
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

    }

    /**
     * @param url      文件网络地址
     * @param destDir  目标目录
     * @param fileName 文件名
     */
    public void downLoadFile(String url, String destDir, String fileName) {
        if (TextUtils.isEmpty(url)
                || TextUtils.isEmpty(destDir) || TextUtils.isEmpty(fileName)) {
            return;
        }
        DownModel downModel = new DownModel();
        downModel.setUrl(url);
        downModel.setName(fileName);
        downModel.setDestDir(destDir);
        //保存
//        downModel.save();

        downLoadFile(downModel);
    }


    /**
     * @param downModel 下载实体类
     */
    @SuppressLint("CheckResult")
    public void downLoadFile(final DownModel downModel) {

        initClient(downModel);

        if (null != mDownloadListeners && mDownloadListeners.size() > 0) {
            for (DownloadListener listener : mDownloadListeners) {
                listener.onStartDownload(downModel.getUrl());
            }
        }


        Disposable disposable =
            retrofit.create(DownLoadApi.class)
                .downloadFile(downModel.getUrl())

                .map(new Function<ResponseBody, File>() {
                    @Override
                    public File apply(ResponseBody responseBody) throws Exception {
                        if (downModel.getDownSize() > 0) {
                            //断点下载
                            return FileUtil.saveFile(responseBody.byteStream(), downModel.getDownSize(), downModel.getDestDir(), downModel.getName());
                        }
                        return FileUtil.saveFile(responseBody.byteStream(), downModel.getDestDir(), downModel.getName());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new FileObserver() {
                    @Override
                    public void onSuccess(File file) {
                        downModel.setState(DownModel.DOWN_FINISH);
                        downModel.setFileSize(FileUtil.getFormatSize(file.length()));
                        //保存
//                        downModel.save();

                        if (null != mDownloadListeners && mDownloadListeners.size() > 0) {
                            for (DownloadListener listener : mDownloadListeners) {
                                listener.onFinishDownload(downModel.getUrl(), file);
                            }
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        downModel.setState(DownModel.DOWN_FAIL);
                        //保存
//                        downModel.save();

                        if (null != mDownloadListeners && mDownloadListeners.size() > 0) {
                            for (DownloadListener listener : mDownloadListeners) {
                                listener.onFail(downModel.getUrl(), msg);
                            }
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }


}
