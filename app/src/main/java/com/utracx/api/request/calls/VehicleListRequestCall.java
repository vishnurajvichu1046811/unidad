package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import com.utracx.api.model.rest.vehicle_list.VehicleListResponseData;
import com.utracx.api.request.interfaces.VehicleListCallback;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleListRequestCall implements Callback<VehicleListResponseData> {
    public static final String BUNDLE_KEY_VEHICLE_LIST = "key_vehicle_list";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "VehicleListRequestCall";
    private VehicleListCallback vehicleListCallback;

    public VehicleListRequestCall(VehicleListCallback vehicleListCallback) {
        this.vehicleListCallback = vehicleListCallback;
    }

    @Override
    public void onResponse(Call<VehicleListResponseData> call, Response<VehicleListResponseData> response) {
        if (response != null && response.body() != null && response.body().getCode() != null
                && response.body().getCode().equals(200.0) && response.body().getData() != null
                && response.body().getData().size() > 0) {
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelableArrayList(BUNDLE_KEY_VEHICLE_LIST, new ArrayList<>(response.body().getData()));

            if (vehicleListCallback != null) {
                vehicleListCallback.onVehicleListFetched(dataBundle);
            }
            Log.d(TAG, "Fetched Vehicle List");
        } else {
            Bundle dataBundle = new Bundle();
            if (response != null && response.raw() != null) {
                dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            }
            if (vehicleListCallback != null) {
                vehicleListCallback.onVehicleListFetchFailed(dataBundle);
            }
            Log.i(TAG, "API FAILED invalid response from the API");
        }
    }

    @Override
    public void onFailure(Call<VehicleListResponseData> call, Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }

        if (vehicleListCallback != null) {
            vehicleListCallback.onVehicleListFetchError(null);
        }
        Log.e(TAG, "onFailure: ", t);
    }
}
