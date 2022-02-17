package com.utracx.util.helper;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import static android.widget.Toast.LENGTH_LONG;

public final class ToastHelper {
    private ToastHelper() {
    }

    public static void showToastMessage(@NonNull Activity activity, String message) {
        activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
    }

    public static void showLongToastMessage(@NonNull Activity activity, String message) {
        activity.runOnUiThread(() -> Toast.makeText(activity, message, LENGTH_LONG).show());
    }
}
