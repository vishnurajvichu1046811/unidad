package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface GeocodeListener {
    void onReverseGeocodeSuccess(Bundle data);

    void onReverseGeocodeFailed(Bundle data);

    void onReverseGeocodeError(@Nullable Bundle data);
}
