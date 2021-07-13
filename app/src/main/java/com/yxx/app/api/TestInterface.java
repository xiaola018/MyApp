package com.yxx.app.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Author: yangxl
 * Date: 2021/7/13 16:48
 * Description:
 */
public interface TestInterface {
    @GET("cityjson")
    Observable<String> getCall();
}
