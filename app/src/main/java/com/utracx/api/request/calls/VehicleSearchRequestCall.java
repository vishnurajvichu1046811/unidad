package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.rest.search.SearchResponse;
import com.utracx.api.request.interfaces.VehicleResponseCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleSearchRequestCall implements Callback<SearchResponse> {

    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "VehicleRequestCall";
    private final VehicleResponseCallback vehicleResponseCallback;

    public VehicleSearchRequestCall(@NonNull VehicleResponseCallback vehicleResponseCallback) {
        this.vehicleResponseCallback = vehicleResponseCallback;
    }

    @Override
    public void onResponse(@NotNull Call<SearchResponse> call, Response<SearchResponse> response) {

        Bundle dataBundle = new Bundle();
        if (response.body() != null && response.body().getCode() != null
                && response.body().getCode() == 200) {
            dataBundle.putParcelableArrayList(BUNDLE_KEY_RESPONSE_DATA, new ArrayList<>(response.body().getData()));
            vehicleResponseCallback.onVehicleResponseReceived(dataBundle);
        } else {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            vehicleResponseCallback.onVehicleResponseFailed(dataBundle);
        }
    }

    @Override
    public void onFailure(@NotNull Call<SearchResponse> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }
        vehicleResponseCallback.onVehicleResponseFailed(null);
    }
}
