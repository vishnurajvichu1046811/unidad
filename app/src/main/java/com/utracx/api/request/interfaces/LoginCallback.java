package com.utracx.api.request.interfaces;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface LoginCallback {
    void onLoginCompleted(Bundle data);

    void onLoginFailed(Bundle data);

    void onLoginError(@Nullable Bundle data);
}
