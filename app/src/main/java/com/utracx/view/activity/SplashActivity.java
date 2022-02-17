package com.utracx.view.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.utracx.R;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.viewmodel.DashBoardViewModel;

import java.util.concurrent.Executors;

import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;

public class SplashActivity extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String TAG = "SPLASH_ACTIVITY";

    private DashBoardViewModel activityViewModel;
    private View mContentView;
    private Runnable mHidePart2Runnable;
    private Runnable mHideRunnable;
    private Handler mHideHandler;
    private boolean mVisible = true;

    @Override
    protected void onStart() {
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
         WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        startAnimation();

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        // delayedHide();
    }
    private void initData() {
        if (null == activityViewModel) {
            activityViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        }
    }
    private void initElementsIfRequired() {
        if (null == activityViewModel) {
            activityViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
        }

        if (null == mContentView) {
            mContentView = findViewById(R.id.fullscreen_content);

            // Set up the user interaction to manually show or hide the system UI.
            mContentView.setOnClickListener(this::toggle);
        }

        if (null == mHidePart2Runnable) {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

            mHidePart2Runnable = () ->
                    mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        if (null == mHideHandler) {
            mHideHandler = new Handler();
        }
        if (null == mHideRunnable) {
            mHideRunnable = this::hide;
        }
    }

    private void startAnimation() {

        Animation animationStart = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.splash_logo_scale);
        findViewById(R.id.outer_logo_image_view).startAnimation(animationStart);


        animationStart.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation scaleAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.splash_random_illuminations);
                findViewById(R.id.outer_logo_image_view).startAnimation(scaleAnimation);


                scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        checkLoginState();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void toggle(View toggleButton) {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }

    private void checkLoginState() {
        String userEmail = getUserEmail(this);
        if (!userEmail.isEmpty()) {

            Executors.newSingleThreadExecutor().execute(
                    () -> {
                        UserDataEntity userData = activityViewModel.fetUserDataByEmail(userEmail);
                        if (userData != null) {
                            String usernameString = userData.getUsername();
                            String passwordString = userData.getPassword();

                            if (!usernameString.isEmpty() && !passwordString.isEmpty()) {
                                Log.i(TAG, "checkLoginState: using saved login");
                                loadDashboardActivity();
                                return;
                            }
                        }

                        Log.i(TAG, "checkLoginState: using new login");
                        loadLoginActivity();
                    });
        } else {
            Log.i(TAG, "checkLoginState: using new login");
            loadLoginActivity();
        }
    }

    private void loadDashboardActivity() {

        NavigationHelper.navigateToNewFreshActivity(this, HomeActivity.class, null);
        finish();
    }

    private void loadLoginActivity() {
        NavigationHelper.navigateToNewFreshActivity(this, LoginActivity.class, null);
        finish();
    }

}