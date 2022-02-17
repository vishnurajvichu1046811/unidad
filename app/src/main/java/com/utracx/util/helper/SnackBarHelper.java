package com.utracx.util.helper;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

public final class SnackBarHelper {
    private SnackBarHelper() {
    }

    public static void showShortMessage(@NonNull Activity activity, String message) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, null, null, LENGTH_SHORT));
    }

    public static void showLongMessage(@NonNull Activity activity, String message) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, null, null, LENGTH_LONG));
    }

    public static void showShortMessage(@NonNull Activity activity, String message,
                                        @Nullable String actionLabel, @Nullable View.OnClickListener actionClickListener) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, actionLabel, actionClickListener, LENGTH_SHORT));
    }

    public static void showLongMessage(@NonNull Activity activity, String message,
                                       @Nullable String actionLabel, @Nullable View.OnClickListener actionClickListener) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, actionLabel, actionClickListener, LENGTH_LONG));
    }

    public static void showIndeterminateMessage(@NonNull Activity activity, String message) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, null, null, LENGTH_INDEFINITE));
    }

    public static void showIndeterminateMessage(@NonNull Activity activity, String message,
                                                @Nullable String actionLabel, @Nullable View.OnClickListener actionClickListener) {
        activity.runOnUiThread(() -> showSnackBar(activity, message, actionLabel, actionClickListener, LENGTH_INDEFINITE));
    }

    private static void showSnackBar(Activity activity, String message, String actionLabel, View.OnClickListener actionClickListener, int showDuration) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, showDuration);
        if (actionLabel != null) {
            snackbar.setAction(actionLabel, actionClickListener);
        }
        snackbar.show();
    }

}
