package com.utracx.api.request;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utracx.BuildConfig;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class OpenStreetMapRetrofitClient {
    static final Gson PARSER = new GsonBuilder().setLenient().create();
    static final HttpLoggingInterceptor HTTP_LOGGER = new HttpLoggingInterceptor();
    private static final long CACHE_SIZE = 5242880L;
    private static final int CACHE_VALIDITY = 86400;
    private static final String TAG = "OSM_CLIENT";
    private static final String OPEN_STREET_MAP_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final long OPEN_STREET_MAP_DELAY = 1000;
    private static final Interceptor GZIP_INTERCEPTOR = chain -> {
        Log.i(TAG, "GzipRequestInterceptor　chain.request().toString():" + chain.request().toString());
        Request request = chain.request()
                .newBuilder()
                .header("User-Agent", "android " + new Random().nextInt(2000000) + "version " + android.os.Build.VERSION.RELEASE)
                .header("Content-Encoding", "gzip")
                .build();
        Log.i(TAG, "GzipRequestInterceptor　request.toString():" + request.toString());
        return chain.proceed(request);
    };

    static final OkHttpClient.Builder OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(1000, TimeUnit.SECONDS)
            .connectTimeout(1000, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(1000, TimeUnit.SECONDS)
            .addInterceptor(GZIP_INTERCEPTOR)
            .addInterceptor(HTTP_LOGGER);

    private static final Interceptor CACHE_INTERCEPTOR = chain -> chain.proceed(chain.request()).newBuilder()
            .header("Cache-Control", "public, max-age=" + CACHE_VALIDITY)
            .build();
    private static final Interceptor interceptor = chain -> {
        try {
            Thread.sleep(OPEN_STREET_MAP_DELAY);
        } catch (InterruptedException e) {
            Log.e(TAG, "intercept: ", e);
        }
        return chain.proceed(chain.request());
    };

    static OkHttpClient osmClient = null;
    private static Dispatcher dispatcher = null;
    private static Retrofit osmRetrofitClient = null;

    static Retrofit getOpenStreetMapClient() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1);
            dispatcher.setMaxRequestsPerHost(1);
        }

        if (BuildConfig.DEBUG) {
            HTTP_LOGGER.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            HTTP_LOGGER.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        if (osmClient == null) {
            osmClient = OK_HTTP_CLIENT
                    .addNetworkInterceptor(interceptor)
                    .addNetworkInterceptor(CACHE_INTERCEPTOR)
                    .dispatcher(dispatcher)
                    .cache(new Cache(Environment.getDownloadCacheDirectory(), CACHE_SIZE))
                    .build();
        }

        if (osmRetrofitClient == null) {
            osmRetrofitClient = new Retrofit.Builder()
                    .baseUrl(OPEN_STREET_MAP_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(PARSER))
                    .client(osmClient)
                    .build();
        }

        return osmRetrofitClient;
    }
}
