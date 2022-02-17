package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface VehicleListCallback {
    void onVehicleListFetched(Bundle data);

    void onVehicleListFetchFailed(Bundle data);

    void onVehicleListFetchError(@Nullable Bundle data);


}
