package com.utracx.background;

import static com.utracx.api.request.calls.GeoCoderRequestCall.BUNDLE_KEY_GEO_CODER_DATA;
import static com.utracx.api.request.calls.NominatimRequestCall.BUNDLE_KEY_NOMINATIM_DATA;
import static com.utracx.api.request.calls.SerialNumberRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.util.helper.MapHelper.getAddressString;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.utracx.api.model.osm.nominatim.NominatimData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.GeoCoderRequestCall;
import com.utracx.api.request.calls.NominatimRequestCall;
import com.utracx.api.request.interfaces.GeocodeListener;
import com.utracx.database.AppDatabase;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.NominatimEntitiy;

import java.util.concurrent.Executors;

public class AddressTask implements GeocodeListener, Runnable {
    private static final String TAG = "VehicleLocationUpdater";

    private final GeoAddressUpdateCallback callback;
    private final AddressCallback addressCallback;
    private final LastUpdatedData deviceData;
    private final Context context;
    private final Marker marker;
    private final NominatimRequestCall requestCall = new NominatimRequestCall(this);
    private final Boolean isGeoCoder;
    private double lat = 0.0;
    private double lng = 0.0;

    public AddressTask(boolean isGeoCoder, @NonNull LastUpdatedData deviceData, @NonNull Context context,
                       @NonNull GeoAddressUpdateCallback callback, AddressCallback addressCallback) {
        this.isGeoCoder = isGeoCoder;
        this.deviceData = deviceData;
        this.context = context.getApplicationContext();
        this.callback = callback;
        this.addressCallback = addressCallback;
        this.marker = null;
    }

    @Override
    public synchronized void run() {

        // For packet Data
        if (deviceData != null && deviceData.getGnssFix() != null && deviceData.getGnssFix() != 0
                && deviceData.getLatitude() != null && deviceData.getLongitude() != null) {
            try {
                lat = deviceData.getLatitude();
                lng = deviceData.getLongitude();
            } catch (NumberFormatException e) {
                Log.e(TAG, "OpenStreetMapsLocationUpdater: unable to parse", e);
            }
        }

        // For Map InfoWindow data
        if (marker != null && marker.getId() != null && marker.getPosition() != null) {
            try {
                LatLng markerPosition = marker.getPosition();
                lat = markerPosition.latitude;
                lng = markerPosition.longitude;
            } catch (NumberFormatException e) {
                Log.e(TAG, "OpenStreetMapsLocationUpdater: unable to get marker data", e);
            }
        }

        if (lat != 0.0 && lng != 0.0) {
            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        if (isGeoCoder) {
                            GeoCoderRequestCall geoCoderRequestCall = new GeoCoderRequestCall(context, this);
                            geoCoderRequestCall.getReverseAddress(lat, lng);
                        } else {
                            AppDatabase db = AppDatabase.getDatabase(context);
                            NominatimEntitiy data = db.nominatimDao().getNominatimData(lat, lng);
                            if (data != null && data.getResolvedAddress() != null && !data.getResolvedAddress().isEmpty()) {
                                AppDatabaseRepository.getInstance(context.getApplicationContext()).updateResolvedAddress(
                                        lat,
                                        lng,
                                        data.getResolvedAddress()
                                );
                                if (addressCallback != null) {
                                    addressCallback.onLocationResolvedSuccess(data.getResolvedAddress(), lat, lng);
                                }
                            } else {
                                ApiUtils.getInstance().getOpenStreetMapService().getAddressFromLatLon(lat, lng).enqueue(requestCall);
                            }
                        }
                    }
            );
        }
    }


    @Override
    public void onReverseGeocodeSuccess(Bundle dataBundle) {
        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_NOMINATIM_DATA)
                && dataBundle.getParcelable(BUNDLE_KEY_NOMINATIM_DATA) != null) {

            NominatimData nominatimData = dataBundle.getParcelable(BUNDLE_KEY_NOMINATIM_DATA);
            if (nominatimData != null) {
                String address = getAddressString(nominatimData, lat, lng);

                if (address != null && !address.isEmpty()) {
                    if (callback != null) {
                        callback.updateAddressInformation(address, lat, lng);
                    }
                    if (addressCallback != null) {
                        addressCallback.onLocationResolvedSuccess(address, lat, lng);
                    }
                }
            }
        } else if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_GEO_CODER_DATA)) {
            String address = dataBundle.getString(BUNDLE_KEY_GEO_CODER_DATA);
            if (callback != null) {
                callback.updateAddressInformation(address, lat, lng);
            }
            if (addressCallback != null) {
                addressCallback.onLocationResolvedSuccess(address, lat, lng);
            }
        }
    }

    @Override
    public void onReverseGeocodeFailed(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(BUNDLE_KEY_RESPONSE_DATA, null);
            if (addressCallback != null)
                addressCallback.onLocationResolvedFailed(lat, lng);

            if (responseString != null) {
                Log.e(TAG, "onReverseGeocodeFailed: API Response String - " + responseString);
            }
        }
    }

    @Override
    public void onReverseGeocodeError(@Nullable Bundle data) {
        Log.e(TAG, "onReverseGeocodeError: API Response failed ");
        if (addressCallback != null)
            addressCallback.onLocationResolvedFailed(lat, lng);
    }

    public interface AddressCallback {
        void onLocationResolvedSuccess(String resolvedAddress, double lat, double lng);

        void onLocationResolvedFailed(double lat, double lng);
    }

    public interface GeoAddressUpdateCallback {
        void updateAddressInformation(@NonNull String resolvedAddress, double lat, double lng);
    }

}
