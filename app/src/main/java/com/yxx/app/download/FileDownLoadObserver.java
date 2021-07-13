package com.yxx.app.download;

import io.reactivex.observers.DefaultObserver;


/**
 * @author Yang xl
 * @date 2020/4/15.
 * description：    下载回调
 */
public abstract class FileDownLoadObserver<T> extends DefaultObserver<T> {


    @Override
    public void onNext(T t) {
        onDownLoadSuccess(t);
    }

    @Override
    public void onError(Throwable throwable) {
        onDownLoadFail(throwable);
    }

    @Override
    public void onComplete() {

    }


    //下载成功的回调
    public abstract void onDownLoadSuccess(T t);

    //下载失败回调
    public abstract void onDownLoadFail(Throwable throwable);

}
