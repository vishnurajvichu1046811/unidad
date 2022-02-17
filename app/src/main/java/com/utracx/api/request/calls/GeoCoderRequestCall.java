package com.utracx.api.request.calls;

import static com.utracx.api.request.calls.NominatimRequestCall.BUNDLE_KEY_RESPONSE_DATA;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.utracx.api.request.interfaces.GeocodeListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoCoderRequestCall {
    public static final String BUNDLE_KEY_GEO_CODER_DATA = "geo_coder";
    private static final String TAG = "GeoCoderRequestCall";
    private final GeocodeListener geocodeListener;
    private final Context context;

    public GeoCoderRequestCall(Context context, @NonNull GeocodeListener geocodeListener) {
        this.geocodeListener = geocodeListener;
        this.context = context;
    }

    public void getReverseAddress(Double lat, Double lng) {
        Bundle dataBundle = new Bundle();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    lat,
                    lng, 10);
            Log.i(TAG, "getReverseAddress: ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            if (addresses.size() > 0 &&
                    addresses.get(0) != null &&
                    addresses.get(0).getAddressLine(0) != null) {
                dataBundle.putString(BUNDLE_KEY_GEO_CODER_DATA, addresses.get(0).
                        getAddressLine(0));
                geocodeListener.onReverseGeocodeSuccess(dataBundle);
            }else {
                dataBundle.putString(BUNDLE_KEY_RESPONSE_DATA, "Not available");
                geocodeListener.onReverseGeocodeFailed(dataBundle);
            }
        }  else {
            geocodeListener.onReverseGeocodeError(dataBundle);
        }
    }
}
