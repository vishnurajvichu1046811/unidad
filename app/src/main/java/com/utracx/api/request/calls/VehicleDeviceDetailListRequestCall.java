package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.device_data.VehicleDeviceResponseData;
import com.utracx.api.request.interfaces.VehicleDeviceDetailListCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleDeviceDetailListRequestCall implements Callback<VehicleDeviceResponseData> {
    public static final String BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST = "key_vehicle_device_detail_list";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "VehicleDetailListCall";
    private VehicleDeviceDetailListCallback vehicleDeviceDetailListCallback;

    public VehicleDeviceDetailListRequestCall(VehicleDeviceDetailListCallback vehicleDeviceDetailListCallback) {
        this.vehicleDeviceDetailListCallback = vehicleDeviceDetailListCallback;
    }

    @Override
    public void onResponse(@NotNull Call<VehicleDeviceResponseData> call, @NotNull Response<VehicleDeviceResponseData> response) {
        Bundle dataBundle = new Bundle();

        if (response.body() != null && (response.code() == 200
                || (response.body().getCode() != null && response.body().getCode() == 200))) {

            ArrayList<DeviceData> data = response.body().getData();
            if (data != null) {
                Log.d(TAG, "onResponse: received packet count : " + data.size());
                dataBundle.putParcelableArrayList(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST, data);
            }

            if (vehicleDeviceDetailListCallback != null && !dataBundle.isEmpty()) {
                vehicleDeviceDetailListCallback.onVehicleDeviceDetailsListFetched(dataBundle);
            }
            Log.d(TAG, "Fetched new Device data List");
        } else {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            if (vehicleDeviceDetailListCallback != null  && !dataBundle.isEmpty()) {
                vehicleDeviceDetailListCallback.onVehicleDeviceDetailsListFetchFailed(dataBundle);
            }
            Log.e(TAG, "API FAILED invalid response from the API");
        }
    }

    @Override
    public void onFailure(@NotNull Call<VehicleDeviceResponseData> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }

        if (vehicleDeviceDetailListCallback != null) {
            vehicleDeviceDetailListCallback.onVehicleDeviceDetailsListFetchError(null);
        }
        Log.e(TAG, "onFailure: ", t);
    }
}
