package com.utracx.api.request.calls;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.utracx.api.model.rest.vehicle_count.VehicleCountData;
import com.utracx.api.model.rest.vehicle_count.VehicleCountResponseData;
import com.utracx.api.request.interfaces.VehicleCountCallback;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleCountRequestCall implements Callback<VehicleCountResponseData> {
    public static final String BUNDLE_KEY_MOVING_COUNT = "key_moving_count";
    public static final String BUNDLE_KEY_IDLE_COUNT = "key_idle_count";
    public static final String BUNDLE_KEY_SLEEP_COUNT = "key_sleep_count";
    public static final String BUNDLE_KEY_INACTIVE_COUNT = "key_inactive_count";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";

    private static final String TAG = "VehicleCountRequestCall";
    private VehicleCountCallback vehicleCountCallback;

    public VehicleCountRequestCall(VehicleCountCallback vehicleCountCallback) {
        this.vehicleCountCallback = vehicleCountCallback;
    }

    @SuppressLint("NewApi")
    @Override
    public void onResponse(Call<VehicleCountResponseData> call, Response<VehicleCountResponseData> response) {

        if (response != null && response.body() != null && response.body().getCode() != null
                && response.body().getCode() == 200 && response.body().getData() != null) {
            VehicleCountData tempVehicleCountResponseData = response.body().getData();

            Bundle dataBundle = new Bundle();
            dataBundle.putInt(BUNDLE_KEY_MOVING_COUNT, tempVehicleCountResponseData.getMovingCount());
            dataBundle.putInt(BUNDLE_KEY_IDLE_COUNT, tempVehicleCountResponseData.getHaltCount());
            dataBundle.putInt(BUNDLE_KEY_SLEEP_COUNT, tempVehicleCountResponseData.getSleepCount());
            dataBundle.putInt(BUNDLE_KEY_INACTIVE_COUNT, tempVehicleCountResponseData.getInactiveCount());

            if (vehicleCountCallback != null) {
                vehicleCountCallback.onVehicleCountFetched(dataBundle);
            }
        } else {
            Bundle dataBundle = new Bundle();
            if (response != null && response.raw() != null) {
                dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            }
            if (vehicleCountCallback != null) {
                vehicleCountCallback.onVehicleCountFetchFailed(dataBundle);
            }
        }
    }

    @Override
    public void onFailure(@NotNull Call<VehicleCountResponseData> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }

        if (vehicleCountCallback != null) {
            vehicleCountCallback.onVehicleCountFetchError(null);
        }
        Log.e(TAG, "onFailure: ", t);
    }


    private String getUserEmail() {
        return "fci@gmail.com";
    }

}