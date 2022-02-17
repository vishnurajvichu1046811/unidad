package com.utracx.util.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import com.utracx.R;
import com.utracx.view.activity.LoginActivity;

public class NavigationHelper {
    public static final String KEY_NAVIGATION_DATA_BUNDLE = "key_nav_data_bundle";
    private static CustomTabsIntent customTabsIntent;

    public static void navigateToNewActivity(@NonNull Activity sourceActivity, @NonNull Class<?> targetActivityClass, @Nullable Bundle extraData) {
        Intent navigationIntent = new Intent(sourceActivity, targetActivityClass);
        if (extraData != null) {
            navigationIntent.putExtra(KEY_NAVIGATION_DATA_BUNDLE, extraData);
        }
        sourceActivity.startActivity(navigationIntent);
    }

    public static void navigateToNewFreshActivity(@NonNull Activity sourceActivity, @NonNull Class<?> targetActivityClass, @Nullable Bundle extraData) {
        Intent navigationIntent = new Intent(sourceActivity, targetActivityClass);
        if (extraData != null) {
            navigationIntent.putExtra(KEY_NAVIGATION_DATA_BUNDLE, extraData);
        }
        navigationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sourceActivity.startActivity(navigationIntent);
    }

    public static void clearNavigateToLoginActivity(@NonNull Activity sourceActivity) {
        Intent navigationIntent = new Intent(sourceActivity, LoginActivity.class);
        navigationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sourceActivity.startActivity(navigationIntent);
        sourceActivity.finish();
    }

    public static void clearNavigateToActivity(@NonNull Activity sourceActivity, @NonNull Class<?> targetActivityClass, @Nullable Bundle extraData) {
        Intent navigationIntent = new Intent(sourceActivity, targetActivityClass);
        navigationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sourceActivity.startActivity(navigationIntent);
        sourceActivity.finish();
    }

    public static void startWebViewActivity(@NonNull Context context, @NonNull Uri webURL) {
        if (customTabsIntent == null) {
            customTabsIntent = new CustomTabsIntent.Builder()
                    .setStartAnimations(context.getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left)
                    .setShowTitle(true)
                    .enableUrlBarHiding()
                    .setExitAnimations(context.getApplicationContext(), R.anim.slide_in_left, R.anim.slide_out_right)
                    .setToolbarColor(Color.parseColor("#FFE52B"))
                    .build();
        }

        customTabsIntent.launchUrl(context, webURL);
    }
}
