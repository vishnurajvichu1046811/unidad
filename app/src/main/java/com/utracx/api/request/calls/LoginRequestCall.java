package com.utracx.api.request.calls;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.utracx.api.model.rest.login.LoginResponseData;
import com.utracx.api.request.interfaces.LoginCallback;
import com.utracx.util.helper.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRequestCall implements Callback<LoginResponseData> {
    public static final String BUNDLE_KEY_USER_EMAIL = "key_user_email";
    public static final String BUNDLE_KEY_USER_PASSWORD = "key_user_password";
    public static final String BUNDLE_KEY_USER_ID = "key_user_id";

    public static final String BUNDLE_KEY_USER_MOB = "key_user_mob";
    public static final String BUNDLE_KEY_USER_FULLNAME = "key_user_fullname";
    public static final String BUNDLE_KEY_USER_ADDRESS = "key_user_address";
    public static final String BUNDLE_KEY_UPDATE_VEHICLE = "key_update_vehicle";

    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "LoginRequestCall";
    public static final String BUNDLE_KEY_USER_EMAIL_ = "user_mailId";

    private LoginCallback loginCallback;
    private String username;
    private String password;

    public LoginRequestCall(String username, String password, LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
        this.username = username;
        this.password = password;
    }

    @SuppressLint("NewApi")
    @Override
    public void onResponse(Call<LoginResponseData> call, Response<LoginResponseData> response) {

        if (response != null && response.body() != null && response.body().getCode() != null
                && response.body().getCode() == 200 && response.body().getLoginData() != null
                && response.body().getLoginData().getId() != null && response.body().getLoginData().getMobile() != null) {

            Bundle dataBundle = new Bundle();
            dataBundle.putString(BUNDLE_KEY_USER_EMAIL, username);
            dataBundle.putString(BUNDLE_KEY_USER_PASSWORD, password);
            dataBundle.putString(BUNDLE_KEY_USER_ID, response.body().getLoginData().getId());
            dataBundle.putString(BUNDLE_KEY_USER_MOB, response.body().getLoginData().getMobile());
            dataBundle.putString(BUNDLE_KEY_USER_FULLNAME, response.body().getLoginData().getFullname());
            dataBundle.putString(BUNDLE_KEY_USER_ADDRESS, response.body().getLoginData().getAddress());
            dataBundle.putString(BUNDLE_KEY_USER_EMAIL_, response.body().getLoginData().getEmail());

            if (loginCallback != null) {
                loginCallback.onLoginCompleted(dataBundle);
            }

        } else {
            Bundle dataBundle = new Bundle();
            if (response != null && response.raw() != null) {
                dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            }
            if (loginCallback != null) {
                loginCallback.onLoginFailed(dataBundle);
            }
        }
    }

    @Override
    public void onFailure(Call<LoginResponseData> call, Throwable t) {
        if (loginCallback != null) {
            loginCallback.onLoginError(null);
        }
        Log.e(TAG, "onFailure: ", t);
    }
}
