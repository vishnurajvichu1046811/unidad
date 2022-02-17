package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface VehicleCountCallback {
    void onVehicleCountFetched(Bundle data);

    void onVehicleCountFetchFailed(Bundle data);

    void onVehicleCountFetchError(@Nullable Bundle data);
}
