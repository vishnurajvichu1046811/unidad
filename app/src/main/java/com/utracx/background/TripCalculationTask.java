package com.utracx.background;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.request.interfaces.VehicleDeviceDetailListCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.api.request.calls.VehicleDeviceDetailListRequestCall.BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST;

public final class TripCalculationTask implements VehicleDeviceDetailListCallback, Runnable {
    private static final String TAG = "TripCalculationTask";
    private List<DeviceData> deviceDetails;
    private TripCalculationTaskCallback callback;
    private ArrayList<DeviceData> filteredDeviceDataList = new ArrayList<>();

    public TripCalculationTask(TripCalculationTaskCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onVehicleDeviceDetailsListFetched(Bundle dataBundle) {
        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST)) {
            List<DeviceData> deviceDetails = dataBundle.getParcelableArrayList(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST);
            if (deviceDetails != null && !deviceDetails.isEmpty()) {
                Log.d(TAG, "onVehicleDeviceDetailsListFetched: new Device data list obtained ");
                this.deviceDetails = deviceDetails;
                run();
                return;
            }
        }

        //no data obtained
        if (callback != null) {
            callback.onDeviceDataCleanupFailed(null);
        }
    }

    @Override
    public void onVehicleDeviceDetailsListFetchFailed(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onVehicleDeviceDetailsListFetchFailed: API Response String - " + responseString);
            }
        }
        if (callback != null) {
            callback.onDeviceDataCleanupFailed(data);
        }
        Log.e(TAG, "onVehicleDeviceDetailsListFetchFailed: ");
    }

    @Override
    public void onVehicleDeviceDetailsListFetchError(@Nullable Bundle data) {
        if (callback != null) {
            callback.onDeviceDataCleanupFailed(data);
        }
        Log.e(TAG, "onVehicleDeviceDetailsListFetchError: ");
    }

    @Override
    public void run() {
        Iterator<DeviceData> itr = deviceDetails.iterator();
        DeviceData tempDeviceData;
        filteredDeviceDataList.clear();

        while (itr.hasNext()) {
            tempDeviceData = itr.next();
            if (tempDeviceData != null && tempDeviceData.getD() != null
                    && tempDeviceData.getD().getGnssFix() != null
                    && tempDeviceData.getD().getGnssFix() == 1
                    && tempDeviceData.getD().getSpeed() != null
                    && tempDeviceData.getD().getSourceDate() != null
                    && tempDeviceData.getD().getVehicleMode() != null
                    && tempDeviceData.getD().getLatitude() != null
                    && tempDeviceData.getD().getLongitude() != null
                    && hasValidGPS(tempDeviceData)) {
                filteredDeviceDataList.add(tempDeviceData);
            }
        }

        if (callback != null) {
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelableArrayList(BUNDLE_KEY_VEHICLE_DEVICE_DETAIL_LIST, filteredDeviceDataList);
            callback.onDeviceDataCleanupComplete(dataBundle);
        }
        Log.d(TAG, "run: completed data cleanup");
    }

    private boolean hasValidGPS(@NonNull DeviceData newDeviceData) {
        return newDeviceData.getD().getLatitude() != 0.0 && newDeviceData.getD().getLongitude() != 0.0;
    }

    public interface TripCalculationTaskCallback {
        void onDeviceDataCleanupComplete(Bundle dataBundle);

        void onDeviceDataCleanupFailed(Bundle dataBundle);
    }
}
