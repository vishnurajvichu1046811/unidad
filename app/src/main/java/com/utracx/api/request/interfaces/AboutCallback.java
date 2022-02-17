package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface AboutCallback {
    void onAboutDataFetched(Bundle data);

    void onAboutDataFetchFailed(Bundle data);

    void onAboutDataFetchError(@Nullable Bundle data);
}
