package com.utracx.api.request.calls;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.model.osm.nominatim.NominatimData;
import com.utracx.api.request.interfaces.GeocodeListener;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NominatimRequestCall implements Callback<NominatimData> {
    public static final String BUNDLE_KEY_NOMINATIM_DATA = "key_nominatim";
    public static final String BUNDLE_KEY_MARKER_ID = "key_marker_id";
    public static final String BUNDLE_KEY_RESPONSE_DATA = "key_response_data";
    private static final String TAG = "NominatimRequestCall";
    private final GeocodeListener nominatimCallback;
    private String markerID = null;

    public NominatimRequestCall(@NonNull GeocodeListener nominatimCallback) {
        this.nominatimCallback = nominatimCallback;
    }

    public NominatimRequestCall(@NonNull GeocodeListener nominatimCallback, String markerID) {
        this.nominatimCallback = nominatimCallback;
        this.markerID = markerID;
    }

    @Override
    public void onResponse(@NotNull Call<NominatimData> call,
                           @NotNull Response<NominatimData> response) {

        Bundle dataBundle = new Bundle();

        if (markerID != null) {
            dataBundle.putString(BUNDLE_KEY_MARKER_ID, markerID);
        }

        if (response.code() == 200 && response.body() != null) {
            dataBundle.putParcelable(BUNDLE_KEY_NOMINATIM_DATA, response.body());
            nominatimCallback.onReverseGeocodeSuccess(dataBundle);
        } else {
            dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, response.raw().toString());
            nominatimCallback.onReverseGeocodeFailed(dataBundle);
        }
    }

    @Override
    public void onFailure(Call<NominatimData> call, @NotNull Throwable t) {
        if (call.isCanceled()) {
            //cancelled by user action on navigation event
            //callback came after cancelled by user action on navigation event
            Log.d(TAG, "onFailure: " + t.getMessage());
            return;
        }
        nominatimCallback.onReverseGeocodeError(null);
        Log.e(TAG, "onFailure: ", t);
    }
}
