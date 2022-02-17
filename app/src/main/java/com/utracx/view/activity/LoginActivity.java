package com.utracx.view.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.barteksc.pdfviewer.util.Util;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.utracx.R;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.LoginRequestCall;
import com.utracx.api.request.interfaces.LoginCallback;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.KeyBoardHelper;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.util.helper.SharedPreferencesHelper;
import com.utracx.util.helper.SnackBarHelper;
import com.utracx.util.helper.StatusBarHelper;
import com.utracx.viewmodel.DashBoardViewModel;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_RESPONSE_DATA;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_EMAIL;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_ID;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_MOB;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_PASSWORD;
import static com.utracx.database.AppDatabase.databaseWriteExecutor;
import static com.utracx.util.ConstantVariables.APP_UPDATE_REQUEST_CODE;
import static com.utracx.util.helper.NavigationHelper.navigateToNewActivity;
import static com.utracx.util.helper.NavigationHelper.navigateToNewFreshActivity;
import static com.utracx.util.helper.NavigationHelper.startWebViewActivity;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;

public class LoginActivity extends AppCompatActivity implements LoginCallback {

    private static final String TAG = "LOGIN_ACTIVITY";

    EditText textEditTextUserName, textEditTextPassword;
    ProgressBar pgsBar;
    Button buttonLogin;
    boolean doubleBackToExitPressedOnce = false;
    private final CountDownTimer timer = new CountDownTimer(2000L, 2000L) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            doubleBackToExitPressedOnce = false;
        }
    };

    private DashBoardViewModel activityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.setupStatusBarWithToolbar(this);
        setContentView(R.layout.activity_login);
        updateAppIfRequired();
        initUI();
        initViewModel();

    }

    private void updateAppIfRequired() {
        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = AppUpdateManagerFactory.create(this).getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(this::onUpdateCheckSuccess);
        appUpdateInfoTask.addOnFailureListener(this::onUpdateCheckFailed);
    }

    // Checks whether the platform allows the specified type of update,
    // and current version staleness.
    private void onUpdateCheckSuccess(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            // Request the update.
            try {
                AppUpdateManagerFactory.create(this).startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.IMMEDIATE,
                        // The current activity making the update request.
                        this,
                        // Include a request code to later monitor this update request.
                        APP_UPDATE_REQUEST_CODE
                );
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "updateAppIfRequired: unable to update app to latest version", e);
                checkLoginState();
            }
        } else {
            checkLoginState();
        }
    }

    private void onUpdateCheckFailed(Exception e) {
        Log.e(TAG, "onUpdateCheckFailed: ", e);
        checkLoginState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkLoginState();
            } else {
                Log.e(TAG, "onActivityResult:  Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
                updateAppIfRequired();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void initViewModel() {
        activityViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);
    }

    private void initUI() {

        pgsBar = findViewById(R.id.pBar);
        textEditTextUserName = findViewById(R.id.textEditTextUserName);
        textEditTextPassword = findViewById(R.id.textEditTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this::performUserInputValidation);
        textEditTextPassword.setOnEditorActionListener(this::onKeyboardOkPressed);
    }

    private boolean onKeyboardOkPressed(TextView textView, int actionId, KeyEvent keyEvent) {
        if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
            performUserInputValidation(textView);
            return true;
        }
        return false;
    }

    private void checkLoginState() {
        toggleViewVisibility(true);
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
                        toggleViewVisibility(false);
                    });
        } else {
            Log.i(TAG, "checkLoginState: using new login");
            toggleViewVisibility(false);
        }
    }

    private void toggleViewVisibility(boolean isLoading) {
        runOnUiThread(
                () -> {
                    textEditTextUserName.setVisibility(isLoading ? INVISIBLE : VISIBLE);
                    textEditTextPassword.setVisibility(isLoading ? INVISIBLE : VISIBLE);
                    buttonLogin.setVisibility(isLoading ? INVISIBLE : VISIBLE);
                    pgsBar.setVisibility(isLoading ? VISIBLE : INVISIBLE);
                }
        );
    }

    private void loadDashboardActivity() {
        postToCrashlytics();
        NavigationHelper.clearNavigateToActivity(LoginActivity.this, HomeActivity.class, null);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        SnackBarHelper.showLongMessage(this, "Please click BACK again to exit");
        doubleBackToExitPressedOnce = true;
        timer.start();
    }

    private void handlerCallback() {
        doubleBackToExitPressedOnce = false;
    }

    private void performUserInputValidation(View view) {
        textEditTextUserName.setError(null);
        textEditTextPassword.setError(null);

        String username = null;
        if (textEditTextUserName.getText() != null) {
            username = textEditTextUserName.getText().toString().trim();
        }

        String password = null;
        if (textEditTextPassword.getText() != null) {
            password = textEditTextPassword.getText().toString();
        }

        if (username == null || username.trim().isEmpty()) {
            textEditTextUserName.setError("Enter a Valid Username");
            textEditTextUserName.requestFocus();
            textEditTextUserName.setFocusableInTouchMode(true);
            textEditTextUserName.setFocusable(true);
            buttonLogin.setClickable(true);
        }

        if (password == null || password.isEmpty()) {
            textEditTextPassword.setError("MINIMUM 6 CHARACTERS REQUIRED");
            textEditTextPassword.requestFocus();
            textEditTextPassword.setFocusableInTouchMode(true);
            textEditTextPassword.setFocusable(true);
            buttonLogin.setClickable(true);
        }

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            KeyBoardHelper.hideKeyboard(this, view);
            pgsBar.setVisibility(VISIBLE);

            makeLoginWebCall(username, password);
        }
    }
    private void makeLoginWebCall(@NonNull String username, @NonNull String password) {
        ApiUtils.getInstance().getSOService().sendLoginData(username, password).enqueue(
                new LoginRequestCall(username, password, this)
        );
    }

    @Override
    public void onLoginCompleted(Bundle dataBundle) {
        if (dataBundle != null && dataBundle.containsKey(BUNDLE_KEY_USER_EMAIL)
                && dataBundle.containsKey(BUNDLE_KEY_USER_EMAIL)
                && dataBundle.containsKey(BUNDLE_KEY_USER_PASSWORD)
                && dataBundle.containsKey(BUNDLE_KEY_USER_ID)
                && dataBundle.containsKey(BUNDLE_KEY_USER_MOB)) {

            String usernameEmail = dataBundle.getString(BUNDLE_KEY_USER_EMAIL, null);

            if (usernameEmail != null && !usernameEmail.isEmpty()) {
                SharedPreferencesHelper.saveUserEmail(this, dataBundle.getString(BUNDLE_KEY_USER_EMAIL, ""));
            }

            String userMobile= dataBundle.getString(BUNDLE_KEY_USER_MOB, null);
            if (userMobile != null && !userMobile.isEmpty()) {
                SharedPreferencesHelper.saveUserMobile(this, dataBundle.getString(BUNDLE_KEY_USER_MOB, ""));
            }

            String password = dataBundle.getString(BUNDLE_KEY_USER_PASSWORD, null);
            String userID = dataBundle.getString(BUNDLE_KEY_USER_ID, null);
            if (usernameEmail != null && !usernameEmail.isEmpty() && password != null && !password.isEmpty()
                    && userID != null && !userID.isEmpty()) {
                activityViewModel.insertUserData(new UserDataEntity(usernameEmail, password, userID));

            }

            loadDashboardActivity();
        } else {
            Log.e(TAG, "onLoginCompleted: failed");
        }
    }

    private void postToCrashlytics() {
        databaseWriteExecutor.execute(() -> {
            UserDataEntity data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                    SharedPreferencesHelper.getUserEmail(this)
            );
            if (data != null) {
                FirebaseCrashlytics.getInstance().setUserId(data.getUserID());
                FirebaseCrashlytics.getInstance().log("User email - " + data.getUsername());
                FirebaseCrashlytics.getInstance().log("User ID - " + data.getUserID());
            }
        });
    }

    @Override
    public void onLoginFailed(Bundle data) {
        if (data != null && data.containsKey(BUNDLE_KEY_RESPONSE_DATA)) {
            String responseString = data.getString(BUNDLE_KEY_RESPONSE_DATA, null);
            if (responseString != null) {
                Log.e(TAG, "onLoginFailed: API Response String - " + responseString);
            }
        }
        SnackBarHelper.showIndeterminateMessage(
                LoginActivity.this,
                "Please verify entered Username/Password",
                "OK",
                (view) -> Log.i(TAG, "onSnackBarClicked: Dismissed")
        );
        toggleViewVisibility(false);
    }

    @Override
    public void onLoginError(@Nullable Bundle data) {
        SnackBarHelper.showIndeterminateMessage(
                LoginActivity.this,
                "Login Failed. Unable to reach the server. (Code: EC8)",
                "Retry",
                this::performUserInputValidation
        );
        toggleViewVisibility(false);
    }

    public void onClickOpenWebsite(View view) {
        startWebViewActivity(this, Uri.parse("https://unidadindia.com"));
    }

    public void onForgotPasswordClick(View view) {
        new AlertDialog.Builder(view.getContext(), R.style.AlertDialog)
                .setTitle(R.string.forgot_password_title)
                .setMessage(R.string.forgot_password_message)
                .setPositiveButton(R.string.ok, (null))
                .create()
                .show();
    }
}
