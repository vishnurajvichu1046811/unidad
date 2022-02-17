package com.utracx.api.request.interfaces;

import android.os.Bundle;

public interface VehicleResponseCallback {
    void onVehicleResponseReceived(Bundle data);

    void onVehicleResponseFailed(Bundle data);
}
