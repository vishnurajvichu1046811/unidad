package com.utracx.view.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Super Class for timed refresh in an Activity
 * How to use :
 * 1. For an activity to be refreshed periodically, say every 5 seconds, extend this class
 * 2. implement the HandlerTimerCallback interface in the above Activity
 * 3. In the onResume() method of the Activity (said in #1),
 * before the call of super.onResume(), call updateCallBack() method.
 */

@SuppressLint("Registered") //This is a Base activity for handling the Screen auto-refresh
public class BaseRefreshActivity extends AppCompatActivity implements Runnable {
    private static final int REFRESH_INTERVAL = 15000;
    private static final String TAG = "BaseRefreshActivity";
    private Handler uiHandler;
    private HandlerTimerCallback handlerTimerCallback;
    private boolean isPeriodicRefreshRequired = true;

    @Override
    public void run() {
        if (uiHandler != null && handlerTimerCallback != null) {
            Log.i(TAG, "run: Auto-refreshing data");
            handlerTimerCallback.onUpdate();
            uiHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (uiHandler == null && isPeriodicRefreshRequired) {
            uiHandler = new Handler();
        }
    }

    @Override
    protected void onPause() {
        removeHandlerCallbacks();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        removeHandlerCallbacks();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        removeHandlerCallbacks();
        super.onBackPressed();
    }

    protected void updateCallbackChild(@Nullable HandlerTimerCallback handlerTimerCallback) {
        this.handlerTimerCallback = handlerTimerCallback;
    }

    private void startHandler() {
        if (uiHandler != null && isPeriodicRefreshRequired) {
            uiHandler.post(this);
        }
    }

    private void removeHandlerCallbacks() {
        Log.d(TAG, "removeHandlerCallbacks");
        try {
            if (uiHandler != null) {
                uiHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAutoRefresh(boolean isPeriodicRefreshRequired) {
        this.isPeriodicRefreshRequired = isPeriodicRefreshRequired;
        if (isPeriodicRefreshRequired) {
            startHandler();
        }
    }

    public interface HandlerTimerCallback {
        void onUpdate();
    }
}
