package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface SerialNumberCallback {
    void onSerialNumberFetched(Bundle data);

    void onSerialNumberFetchFailed(Bundle data);

    void onSerialNumberFetchError(@Nullable Bundle data);
}
