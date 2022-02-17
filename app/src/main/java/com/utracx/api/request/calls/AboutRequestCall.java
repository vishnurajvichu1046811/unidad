package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.about.AboutResponseData;
import com.utracx.api.request.interfaces.AboutCallback;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutRequestCall implements Callback<AboutResponseData> {
    public static final String BUNDLE_KEY_ABOUT_DATA = "key_about_data";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "AboutRequestCall";

    private final AboutCallback callback;

    public AboutRequestCall(@NonNull AboutCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(@NotNull Call<AboutResponseData> call, Response<AboutResponseData> response) {
        Bundle dataBundle = new Bundle();
        if (response.body() != null && response.code() == 200) {
            dataBundle.putParcelable(BUNDLE_KEY_ABOUT_DATA, response.body());
            callback.onAboutDataFetched(dataBundle);
        } else {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            callback.onAboutDataFetchFailed(dataBundle);
        }
    }

    @Override
    public void onFailure(@NotNull Call<AboutResponseData> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }
        callback.onAboutDataFetchError(null);
    }
}
