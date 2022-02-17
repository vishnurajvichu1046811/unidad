package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface VehicleDeviceDetailListCallback {
    void onVehicleDeviceDetailsListFetched(Bundle data);

    void onVehicleDeviceDetailsListFetchFailed(Bundle data);

    void onVehicleDeviceDetailsListFetchError(@Nullable Bundle data);
}
