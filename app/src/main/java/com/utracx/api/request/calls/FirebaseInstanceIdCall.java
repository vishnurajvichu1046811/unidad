package com.utracx.api.request.calls;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.firebase.FirebaseRequestBody;
import com.utracx.api.model.rest.firebase.FirebaseResponseData;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseInstanceIdCall implements Callback<FirebaseResponseData> {
    public static final String BUNDLE_KEY_USER_EMAIL = "key_user_email";
    public static final String BUNDLE_KEY_USER_PASSWORD = "key_user_password";
    public static final String BUNDLE_KEY_FIREBASE_ID = "key_firebase_id";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "FirebaseInstanceIdCall";

    private final FirebaseRequestBody requestBody;

    public FirebaseInstanceIdCall(@NonNull FirebaseRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @SuppressLint("NewApi")
    @Override
    public void onResponse(@NotNull Call<FirebaseResponseData> call, @NotNull Response<FirebaseResponseData> response) {
        Bundle dataBundle = new Bundle();

        if (response.body() != null && response.body().getCode() != null
                && response.body().getCode() == 200) {

            Log.d(TAG, "onResponse Firebase ID register/unregister completed");

            dataBundle.putString(BUNDLE_KEY_USER_EMAIL, requestBody.getUsername());
            dataBundle.putString(BUNDLE_KEY_USER_PASSWORD, requestBody.getPassword());
            dataBundle.putString(BUNDLE_KEY_FIREBASE_ID, requestBody.getFirebaseID());

        }

        try {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
        } catch (Exception e) {
            Log.e(TAG, "onResponse: unable to read server response", e);
        }

    }

    @Override
    public void onFailure(@NotNull Call<FirebaseResponseData> call, @NotNull Throwable t) {
        Log.e(TAG, "onFailure: ", t);
    }
}
