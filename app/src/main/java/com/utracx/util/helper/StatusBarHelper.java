package com.utracx.util.helper;

import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.utracx.R;
import com.utracx.view.activity.LoginActivity;

import org.jetbrains.annotations.NotNull;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public final class StatusBarHelper {
    private StatusBarHelper() {
    }

    public static void setupStatusBarWithToolbar(@NotNull AppCompatActivity activity) {
        Window window = activity.getWindow();

        if (activity instanceof LoginActivity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ResourcesCompat.getColor(activity.getResources(), R.color.colorPrimary, activity.getTheme()));
        }
    }
}
