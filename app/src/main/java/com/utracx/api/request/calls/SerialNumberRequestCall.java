package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.serial_number.SerialNumberDataResponse;
import com.utracx.api.request.interfaces.SerialNumberCallback;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SerialNumberRequestCall implements Callback<SerialNumberDataResponse> {
    public static final String BUNDLE_KEY_SERIAL_NUMBER = "key_serial_number";
    public static final String BUNDLE_KEY_REG_NUMBER = "key_reg_number";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "SerialNumberRequestCall";
    private final SerialNumberCallback serialNumberCallback;
    private final String vehicleRegistration;

    public SerialNumberRequestCall(@NonNull String vehicleRegistration, @NonNull SerialNumberCallback serialNumberCallback) {
        this.serialNumberCallback = serialNumberCallback;
        this.vehicleRegistration = vehicleRegistration;
    }

    @Override
    public void onResponse(@NotNull Call<SerialNumberDataResponse> call, Response<SerialNumberDataResponse> response) {

        Bundle dataBundle = new Bundle();
        if (response.body() != null && response.body().getCode() != null
                && response.body().getCode() == 200
                && response.body().getData().getSerialNo() != null) {

            dataBundle.putString(BUNDLE_KEY_SERIAL_NUMBER, response.body().getData().getSerialNo());
            dataBundle.putString(BUNDLE_KEY_REG_NUMBER, vehicleRegistration);
            serialNumberCallback.onSerialNumberFetched(dataBundle);
        } else {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            serialNumberCallback.onSerialNumberFetchFailed(dataBundle);
        }
    }

    @Override
    public void onFailure(@NotNull Call<SerialNumberDataResponse> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }
        serialNumberCallback.onSerialNumberFetchError(null);
    }
}
