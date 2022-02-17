package com.utracx.api.request;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utracx.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

final class APIRetrofitClient {

    private static final String BASE_URL = "http://65.2.18.68:8080/utracx/";
//    private static final String BASE_URL = "http://web.utracx.com:8080/utracx/";

    private static final String TAG = "MercydaRetrofitClient";

    private APIRetrofitClient() {
        // ignored, for preventing object creation
    }

    private static final Gson PARSER = new GsonBuilder().setLenient().create();
    private static final HttpLoggingInterceptor HTTP_LOGGER = new HttpLoggingInterceptor();
    private static final Dispatcher dispatcher = new Dispatcher();

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(PARSER))
                    .build();
            return retrofit;
        }
        return retrofit.newBuilder().client(getOkHttpClient()).build();
    }

    @NotNull
    private static OkHttpClient getOkHttpClient() {
        Log.d(TAG, "getOkHttpClient: Created new OkHttp Client");

        if (BuildConfig.DEBUG) {
            HTTP_LOGGER.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            HTTP_LOGGER.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        dispatcher.setMaxRequests(5);
        dispatcher.setMaxRequestsPerHost(5);

        return new OkHttpClient.Builder()
                .callTimeout(1000, TimeUnit.SECONDS)
                .connectTimeout(1000, TimeUnit.SECONDS)
                .readTimeout(1000, TimeUnit.SECONDS)
                .writeTimeout(1000, TimeUnit.SECONDS)
                .addInterceptor(HTTP_LOGGER)
                .dispatcher(dispatcher)
                .build();
    }

    public static void cancelAllWebCalls() {
        dispatcher.cancelAll();
    }
}
