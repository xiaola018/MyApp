package com.yxx.app.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Author: yangxl
 * Date: 2021/7/13 16:44
 * Description:
 */
public class Api {

    private static Api instance;
    private Retrofit mRetrofit;

    public static Api get() {
        if (instance == null) {
            synchronized (Api.class) {
                if (instance == null) {
                    instance = new Api();
                }
            }
        }
        return instance;
    }

    public Api() {
        init();
    }

    private void init() {
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .interceptors().add(new Interceptor(){
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                    //    requestBuilder.addHeader("Content-Type","application/x-www-form-urlencoded");

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://120.24.160.68/")
                .client(okHttpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public <T> T create(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }
}
